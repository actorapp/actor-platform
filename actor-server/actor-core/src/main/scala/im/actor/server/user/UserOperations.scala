package im.actor.server.user

import akka.actor.{ ActorRef, ActorSystem }
import akka.contrib.pattern.DistributedPubSubExtension
import akka.pattern.ask
import akka.util.Timeout
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.{ AuthorizedClientData, Update }
import im.actor.api.rpc.peers.ApiPeer
import im.actor.api.rpc.users.{ ApiUser, ApiSex }
import im.actor.server.file.Avatar
import im.actor.server.sequence.{ UpdateRefs, SeqUpdatesExtension, SeqState, SeqUpdatesManager }
import im.actor.server.{ models, persist ⇒ p }
import slick.driver.PostgresDriver.api.Database

import scala.concurrent.Future

object AuthEvents {
  case object AuthIdInvalidated
}

trait UserOperations extends Commands with Queries

private[user] sealed trait Commands extends AuthCommands {
  self: Queries ⇒

  import UserCommands._

  implicit val system: ActorSystem
  import system.dispatcher

  val processorRegion: UserProcessorRegion

  implicit val timeout: Timeout

  def create(userId: Int, accessSalt: String, name: String, countryCode: String, sex: ApiSex.ApiSex, isBot: Boolean, extensions: Seq[ApiExtension]): Future[CreateAck] = {
    (processorRegion.ref ? Create(userId, accessSalt, name, countryCode, sex, isBot, extensions)).mapTo[CreateAck]
  }

  def addPhone(userId: Int, phone: Long): Future[Unit] = {
    (processorRegion.ref ? AddPhone(userId, phone)).mapTo[AddPhoneAck] map (_ ⇒ ())
  }

  def addEmail(userId: Int, email: String): Future[Unit] = {
    (processorRegion.ref ? AddEmail(userId, email)).mapTo[AddEmailAck] map (_ ⇒ ())
  }

  def delete(userId: Int): Future[Unit] = {
    (processorRegion.ref ? Delete(userId)).mapTo[DeleteAck] map (_ ⇒ ())
  }

  def changeCountryCode(userId: Int, countryCode: String): Future[Unit] = {
    (processorRegion.ref ? ChangeCountryCode(userId, countryCode)).mapTo[ChangeCountryCodeAck] map (_ ⇒ ())
  }

  def changeName(userId: Int, name: String): Future[SeqState] = {
    (processorRegion.ref ? ChangeName(userId, name)).mapTo[SeqState]
  }

  def changeNickname(userId: Int, clientAuthId: Long, nickname: Option[String]): Future[SeqState] = {
    (processorRegion.ref ? ChangeNickname(userId, clientAuthId, nickname)).mapTo[SeqState]
  }

  def changeAbout(userId: Int, clientAuthId: Long, about: Option[String]): Future[SeqState] = {
    (processorRegion.ref ? ChangeAbout(userId, clientAuthId, about)).mapTo[SeqState]
  }

  def updateAvatar(userId: Int, clientAuthId: Long, avatarOpt: Option[Avatar]): Future[UpdateAvatarAck] =
    (processorRegion.ref ? UpdateAvatar(userId, clientAuthId, avatarOpt)).mapTo[UpdateAvatarAck]

  def broadcastUserUpdate(
    userId:     Int,
    update:     Update,
    pushText:   Option[String],
    isFat:      Boolean,
    deliveryId: Option[String]
  ): Future[Seq[SeqState]] = {
    val header = update.header
    val serializedData = update.toByteArray

    val originPeer = SeqUpdatesManager.getOriginPeer(update)
    val refs = SeqUpdatesManager.updateRefs(update)

    broadcastUserUpdate(userId, header, serializedData, refs, pushText, originPeer, isFat, deliveryId)
  }

  def broadcastUserUpdate(
    userId:         Int,
    header:         Int,
    serializedData: Array[Byte],
    refs:           UpdateRefs,
    pushText:       Option[String],
    originPeer:     Option[ApiPeer],
    isFat:          Boolean,
    deliveryId:     Option[String]
  ): Future[Seq[SeqState]] = {
    for {
      authIds ← getAuthIds(userId)
      seqstates ← SeqUpdatesManager.persistAndPushUpdates(authIds.toSet, header, serializedData, refs, pushText, originPeer, isFat, deliveryId)
    } yield seqstates
  }

  def broadcastUsersUpdate(
    userIds:    Set[Int],
    update:     Update,
    pushText:   Option[String],
    isFat:      Boolean,
    deliveryId: Option[String]
  ): Future[Seq[SeqState]] = {
    val header = update.header
    val serializedData = update.toByteArray

    val originPeer = SeqUpdatesManager.getOriginPeer(update)
    val refs = SeqUpdatesManager.updateRefs(update)

    for {
      authIds ← getAuthIds(userIds)
      seqstates ← Future.sequence(
        authIds.map(SeqUpdatesManager.persistAndPushUpdate(_, header, serializedData, refs, pushText, originPeer, isFat, deliveryId))
      )
    } yield seqstates
  }

  def broadcastClientUpdate(
    update:     Update,
    pushText:   Option[String],
    isFat:      Boolean        = false,
    deliveryId: Option[String] = None
  )(implicit client: AuthorizedClientData): Future[SeqState] = broadcastClientUpdate(client.userId, client.authId, update, pushText, isFat, deliveryId)

  def broadcastClientUpdate(
    clientUserId: Int,
    clientAuthId: Long,
    update:       Update,
    pushText:     Option[String],
    isFat:        Boolean,
    deliveryId:   Option[String]
  ): Future[SeqState] = {
    val header = update.header
    val serializedData = update.toByteArray

    val originPeer = SeqUpdatesManager.getOriginPeer(update)
    val refs = SeqUpdatesManager.updateRefs(update)

    for {
      otherAuthIds ← getAuthIds(clientUserId) map (_.filter(_ != clientAuthId))
      _ ← Future.sequence(
        otherAuthIds map (
          SeqUpdatesManager.persistAndPushUpdate(_, header, serializedData, refs, pushText, originPeer, isFat, deliveryId)
        )
      )

      seqstate ← SeqUpdatesManager.persistAndPushUpdate(clientAuthId, header, serializedData, refs, pushText, originPeer, isFat, deliveryId)
    } yield seqstate
  }

  def broadcastClientAndUsersUpdate(
    userIds:    Set[Int],
    update:     Update,
    pushText:   Option[String],
    isFat:      Boolean        = false,
    deliveryId: Option[String] = None
  )(implicit client: AuthorizedClientData): Future[(SeqState, Seq[SeqState])] =
    broadcastClientAndUsersUpdate(client.userId, client.authId, userIds, update, pushText, isFat, deliveryId)

  def broadcastClientAndUsersUpdate(
    clientUserId: Int,
    clientAuthId: Long,
    userIds:      Set[Int],
    update:       Update,
    pushText:     Option[String],
    isFat:        Boolean,
    deliveryId:   Option[String]
  ): Future[(SeqState, Seq[SeqState])] = {
    val header = update.header
    val serializedData = update.toByteArray

    val originPeer = SeqUpdatesManager.getOriginPeer(update)
    val refs = SeqUpdatesManager.updateRefs(update)

    for {
      authIds ← getAuthIds(userIds + clientUserId)
      seqstates ← Future.sequence(
        authIds.view
          .filterNot(_ == clientAuthId)
          .map(SeqUpdatesManager.persistAndPushUpdate(_, header, serializedData, refs, pushText, originPeer, isFat, deliveryId))
      )
      seqstate ← SeqUpdatesManager.persistAndPushUpdate(clientAuthId, header, serializedData, refs, pushText, originPeer, isFat, deliveryId)
    } yield (seqstate, seqstates)
  }

  def notifyUserUpdate(
    userId:       Int,
    exceptAuthId: Long,
    update:       Update,
    pushText:     Option[String],
    isFat:        Boolean        = false,
    deliveryId:   Option[String] = None
  ): Future[Seq[SeqState]] = {
    val header = update.header
    val serializedData = update.toByteArray

    val originPeer = SeqUpdatesManager.getOriginPeer(update)

    notifyUserUpdate(userId, exceptAuthId, header, serializedData, SeqUpdatesManager.updateRefs(update), pushText, originPeer, isFat, deliveryId)
  }

  def notifyUserUpdate(
    userId:         Int,
    exceptAuthId:   Long,
    header:         Int,
    serializedData: Array[Byte],
    refs:           UpdateRefs,
    pushText:       Option[String],
    originPeer:     Option[ApiPeer],
    isFat:          Boolean,
    deliveryId:     Option[String]
  ) = {
    implicit val s: ActorSystem = system //why it does not compile without this?
    for {
      otherAuthIds ← getAuthIds(userId) map (_.filter(_ != exceptAuthId))
      seqstates ← Future.sequence(otherAuthIds map { authId ⇒
        SeqUpdatesManager.persistAndPushUpdate(authId, header, serializedData, refs, pushText, originPeer, isFat, deliveryId)
      })
    } yield seqstates
  }
}

private[user] sealed trait Queries {

  import UserQueries._

  val viewRegion: UserViewRegion
  implicit val system: ActorSystem
  import system.dispatcher

  implicit val timeout: Timeout

  def getAuthIds(userId: Int): Future[Seq[Long]] = {
    (viewRegion.ref ? GetAuthIds(userId)).mapTo[GetAuthIdsResponse] map (_.authIds)
  }

  def getAuthIds(userIds: Set[Int]): Future[Seq[Long]] = {
    Future.sequence(userIds map getAuthIds) map (_.toSeq.flatten)
  }

  def getApiStruct(userId: Int, clientUserId: Int, clientAuthId: Long): Future[ApiUser] = {
    (viewRegion.ref ? GetApiStruct(userId, clientUserId, clientAuthId)).mapTo[GetApiStructResponse] map (_.struct)
  }

  def getUser(userId: Int): Future[User] = {
    (viewRegion.ref ? GetUser(userId)).mapTo[User]
  }

  def getContactRecords(userId: Int): Future[(Seq[Long], Seq[String])] = {
    (viewRegion.ref ? GetContactRecords(userId)).mapTo[GetContactRecordsResponse] map (r ⇒ (r.phones, r.emails))
  }

  def getContactRecordsSet(userId: Int): Future[(Set[Long], Set[String])] =
    for ((phones, emails) ← getContactRecords(userId)) yield (phones.toSet, emails.toSet)

  def checkAccessHash(userId: Int, senderAuthId: Long, accessHash: Long): Future[Boolean] = {
    (viewRegion.ref ? CheckAccessHash(userId, senderAuthId, accessHash)).mapTo[CheckAccessHashResponse] map (_.isCorrect)
  }

  def getAccessHash(userId: Int, clientAuthId: Long): Future[Long] =
    (viewRegion.ref ? GetAccessHash(userId, clientAuthId)).mapTo[GetAccessHashResponse] map (_.accessHash)
}

private[user] sealed trait AuthCommands {
  self: Queries ⇒

  import UserCommands._
  import akka.contrib.pattern.DistributedPubSubMediator._

  implicit val system: ActorSystem
  import system.dispatcher
  val processorRegion: UserProcessorRegion

  def authIdTopic(authId: Long): String = s"auth.events.${authId}"

  def auth(userId: Int, authId: Long): Future[NewAuthAck] = {
    (processorRegion.ref ? NewAuth(userId, authId)).mapTo[NewAuthAck]
  }

  def removeAuth(userId: Int, authId: Long): Future[RemoveAuthAck] = (processorRegion.ref ? RemoveAuth(userId, authId)).mapTo[RemoveAuthAck]

  def logoutByAppleToken(token: Array[Byte])(implicit db: Database): Future[Unit] = {
    db.run(p.push.ApplePushCredentials.findByToken(token)) flatMap { creds ⇒
      Future.sequence(creds map (c ⇒ logout(c.authId))) map (_ ⇒ ())
    }
  }

  def logout(authId: Long)(implicit db: Database): Future[Unit] = {
    db.run(p.AuthSession.findByAuthId(authId)) flatMap {
      case Some(session) ⇒ logout(session)
      case None          ⇒ throw new Exception("Can't find auth session to logout")
    }
  }

  def logout(session: models.AuthSession)(implicit db: Database): Future[Unit] = {
    system.log.warning(s"Terminating AuthSession ${session.id} of user ${session.userId} and authId ${session.authId}")

    implicit val seqExt = SeqUpdatesExtension(system)
    val mediator = DistributedPubSubExtension(system).mediator

    for {
      _ ← removeAuth(session.userId, session.authId)
      _ ← db.run(p.AuthSession.delete(session.userId, session.id))
      _ = SeqUpdatesManager.deletePushCredentials(session.authId)
    } yield {
      publishAuthIdInvalidated(mediator, session.authId)
    }
  }

  private def publishAuthIdInvalidated(mediator: ActorRef, authId: Long): Unit = {
    mediator ! Publish(authIdTopic(authId), AuthEvents.AuthIdInvalidated)
  }
}
