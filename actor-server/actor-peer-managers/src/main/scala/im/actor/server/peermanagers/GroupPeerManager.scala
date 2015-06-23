package im.actor.server.peermanagers

import java.time._

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NoStackTrace

import akka.actor._
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, _ }
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.util.{ GroupServiceMessages, HistoryUtils, UserUtils }
import im.actor.server.{ models, persist }

case class GroupPeerManagerRegion(ref: ActorRef)

object GroupPeerManager {

  private case class JoinedUser(userId: Int)
  private case object JoinUserFailure

  private case object UserAlreadyJoined extends Exception with NoStackTrace

  import PeerManager._

  private case class Initialized(joinedUserIds: Set[Int])

  private val idExtractor: ShardRegion.IdExtractor = {
    case Envelope(groupId, payload) ⇒ (groupId.toString, payload)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case Envelope(groupId, _) ⇒ (groupId % 100).toString // TODO: configurable
  }

  private def startRegion(props: Option[Props])(implicit system: ActorSystem): GroupPeerManagerRegion =
    GroupPeerManagerRegion(ClusterSharding(system).start(
      typeName = "GroupPeerManager",
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def startRegion()(
    implicit
    system:              ActorSystem,
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ): GroupPeerManagerRegion =
    startRegion(Some(props))

  def startRegionProxy()(implicit system: ActorSystem): GroupPeerManagerRegion =
    startRegion(None)

  def props(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ): Props =
    Props(classOf[GroupPeerManager], db, seqUpdManagerRegion)

  def sendMessage(groupId: Int, senderUserId: Int, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean = false)(
    implicit
    peerManagerRegion: GroupPeerManagerRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[SeqUpdatesManager.SequenceState] = {
    (peerManagerRegion.ref ? Envelope(groupId, SendMessage(senderUserId, senderAuthId, randomId, date, message, isFat))).mapTo[SeqUpdatesManager.SequenceState]
  }

  def joinGroup(group: models.Group, joiningUserId: Int, joiningUserAuthId: Long, invitingUserId: Int)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupPeerManagerRegion,
    ec:                ExecutionContext
  ): Future[Option[(SeqUpdatesManager.SequenceState, Vector[Int], Long, Long)]] = {
    (peerManagerRegion.ref ? Envelope(group.id, JoinGroup(group, joiningUserId, joiningUserAuthId, invitingUserId)))
      .mapTo[(SeqUpdatesManager.SequenceState, Vector[Int], Long, Long)].map(Some(_)).recover { case UserAlreadyJoined ⇒ None }
  }

  def messageReceived(groupId: Int, receiverUserId: Int, receiverAuthId: Long, date: Long, receivedDate: Long)(implicit peerManagerRegion: GroupPeerManagerRegion): Unit = {
    peerManagerRegion.ref ! Envelope(groupId, MessageReceived(receiverUserId, receiverAuthId, date, receivedDate))
  }

  def messageRead(groupId: Int, readerUserId: Int, readerAuthId: Long, date: Long, readDate: Long)(implicit peerManagerRegion: GroupPeerManagerRegion): Unit = {
    peerManagerRegion.ref ! Envelope(groupId, MessageRead(readerUserId, readerAuthId, date, readDate))
  }
}

class GroupPeerManager(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion
) extends PeerManager with Stash {

  import GroupPeerManager._
  import HistoryUtils._
  import PeerManager._
  import SeqUpdatesManager._
  import UserUtils._

  implicit private[this] val system: ActorSystem = context.system
  implicit private[this] val ec: ExecutionContext = context.dispatcher

  private[this] val groupId = self.path.name.toInt
  private[this] val groupPeer = Peer(PeerType.Group, groupId)

  private[this] var lastReceivedDate: Option[Long] = None
  private[this] var lastReadDate: Option[Long] = None

  initialize() pipeTo self

  def receive = initializing

  def initializing: Receive = {
    case Initialized(joinedUserIds) ⇒
      context.become(initialized(joinedUserIds))
      unstashAll()
    case msg ⇒ stash()
  }

  def initialized(joinedUserIds: Set[Int]): Receive = {
    case SendMessage(senderUserId, senderAuthId, randomId, date, message, isFat) ⇒
      val replyTo = sender()
      sendMessage(senderUserId, senderAuthId, randomId, date, message, isFat) pipeTo replyTo onFailure {
        case e ⇒
          replyTo ! Status.Failure(e)
          log.error(e, "Failed to send message")
      }
    case MessageReceived(receiverUserId, _, date, receivedDate) ⇒
      if (!lastReceivedDate.exists(_ > date)) {
        lastReceivedDate = Some(date)
        val update = UpdateMessageReceived(groupPeer, date, receivedDate)

        // TODO: #perf cache user ids

        db.run(for {
          otherGroupUserIds ← persist.GroupUser.findUserIds(groupId).map(_.filterNot(_ == receiverUserId).toSet)
          otherAuthIds ← persist.AuthId.findIdByUserIds(otherGroupUserIds).map(_.toSet)
          _ ← persistAndPushUpdates(otherAuthIds, update, None)
        } yield {
          db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.group(groupId), new DateTime(date)))
        }) onFailure {
          case e ⇒
            log.error(e, "Failed to mark messages received")
        }
      }
    case MessageRead(readerUserId, readerAuthId, date, readDate) ⇒
      if (!lastReadDate.exists(_ > date)) {
        lastReadDate = Some(date)
        val update = UpdateMessageRead(groupPeer, date, readDate)
        val readerUpdate = UpdateMessageReadByMe(groupPeer, date)

        if (!joinedUserIds.contains(readerUserId)) {
          context.become(initialized(joinedUserIds + readerUserId))

          db.run(for (_ ← persist.GroupUser.setJoined(groupId, readerUserId, LocalDateTime.now(ZoneOffset.UTC))) yield {
            val randomId = ThreadLocalRandom.current().nextLong()
            self ! SendMessage(readerUserId, readerAuthId, randomId, new DateTime, GroupServiceMessages.userJoined)
          })
        }

        db.run(for {
          otherGroupUserIds ← persist.GroupUser.findUserIds(groupId).map(_.filterNot(_ == readerUserId).toSet)
          otherAuthIds ← persist.AuthId.findIdByUserIds(otherGroupUserIds).map(_.toSet)
          _ ← persistAndPushUpdates(otherAuthIds, update, None)
          _ ← broadcastUserUpdate(readerUserId, readerUpdate, None)
        } yield {
          // TODO: report errors
          db.run(markMessagesRead(models.Peer.privat(readerUserId), models.Peer.group(groupId), new DateTime(date)))
        }) onFailure {
          case e ⇒
            log.error(e, "Failed to mark messages read")
        }
      }
    case JoinGroup(group, joiningUserId, joiningUserAuthId, invitingUserId) ⇒
      context become {
        case JoinedUser(user) ⇒
          context become initialized(joinedUserIds + user)
          unstashAll()
        case JoinUserFailure ⇒
          context become initialized(joinedUserIds)
          unstashAll()
        case msg ⇒ stash()
      }

      val replyTo = sender()
      db.run {
        val result = for {
          isMember ← persist.GroupUser.find(group.id, joiningUserId).map(_.isDefined)
          updates ← if (isMember) {
            DBIO.failed(UserAlreadyJoined)
          } else {
            persist.GroupUser.find(group.id).flatMap { groupUsers ⇒
              val userIds = groupUsers.map(_.userId)
              val date = new DateTime
              val dateMillis = date.getMillis
              val randomId = ThreadLocalRandom.current().nextLong()
              for {
                _ ← persist.GroupUser.create(group.id, joiningUserId, invitingUserId, date, Some(LocalDateTime.now(ZoneOffset.UTC)))
                seqstate ← DBIO.from(sendMessage(joiningUserId, joiningUserAuthId, randomId, date, GroupServiceMessages.userJoined, isFat = true))
              } yield (seqstate, userIds :+ invitingUserId, dateMillis, randomId)
            }
          }
        } yield updates
        self ! JoinedUser(joiningUserId)
        result
      } pipeTo replyTo onFailure {
        case e ⇒
          self ! JoinUserFailure
          replyTo ! Status.Failure(e)
      }
  }

  private def sendMessage(senderUserId: Int, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean): Future[SeqUpdatesManager.SequenceState] = {
    val outUpdate = UpdateMessage(
      peer = groupPeer,
      senderUserId = senderUserId,
      date = date.getMillis,
      randomId = randomId,
      message = message
    )
    val clientUpdate = UpdateMessageSent(groupPeer, randomId, date.getMillis)
    db.run {
      for {
        _ ← broadcastGroupMessage(senderUserId, senderAuthId, groupId, outUpdate, isFat)
        seqstate ← persistAndPushUpdate(senderAuthId, clientUpdate, None, isFat)
      } yield {
        db.run(writeHistoryMessage(models.Peer.privat(senderUserId), models.Peer.group(groupPeer.id), date, randomId, message.header, message.toByteArray))
        seqstate
      }
    }
  }

  private def initialize(): Future[Initialized] = {
    db.run(for (groupUsers ← persist.GroupUser.find(groupId)) yield {
      val joinedUserIds = groupUsers.foldLeft(Set.empty[Int]) {
        case (acc, groupUser) ⇒
          groupUser.joinedAt match {
            case Some(_) ⇒ acc + groupUser.userId
            case None    ⇒ acc
          }
      }

      Initialized(joinedUserIds)
    })
  }

  private def broadcastGroupMessage(senderUserId: Int, senderAuthId: Long, groupId: Int, update: UpdateMessage, isFat: Boolean) = {
    val updateHeader = update.header
    val updateData = update.toByteArray
    val (updateUserIds, updateGroupIds) = updateRefs(update)

    for {
      userIds ← persist.GroupUser.findUserIds(groupId)
      clientUser ← getUserUnsafe(senderUserId)
      seqstates ← DBIO.sequence(userIds.view.filterNot(_ == senderUserId) map { userId ⇒
        for {
          pushText ← getPushText(update.message, clientUser, userId)
          seqstates ← broadcastUserUpdate(userId, updateHeader, updateData, updateUserIds, updateGroupIds, Some(pushText), Some(groupPeer), isFat)
        } yield seqstates
      }) map (_.flatten)
      selfseqstates ← notifyUserUpdate(senderUserId, senderAuthId, updateHeader, updateData, updateUserIds, updateGroupIds, None, None, isFat)
    } yield seqstates ++ selfseqstates
  }

}