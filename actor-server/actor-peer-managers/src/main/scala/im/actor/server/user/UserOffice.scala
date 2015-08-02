package im.actor.server.user

import im.actor.api.rpc.{ AuthorizedClientData, Update }
import im.actor.api.rpc.users.Sex

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NoStackTrace

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.joda.time.DateTime

import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage }
import im.actor.api.rpc.peers.Peer
import im.actor.server.file.Avatar
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.sequence.{ SeqState, SeqStateDate }

object UserOffice extends Commands with Queries {
  case object InvalidAccessHash extends Exception with NoStackTrace

  case object FailedToFetchInfo

  def persistenceIdFor(userId: Int): String = s"User_${userId}"
}

private[user] sealed trait Commands {
  this: Queries ⇒

  import UserCommands._

  def create(userId: Int, accessSalt: String, name: String, countryCode: String, sex: Sex.Sex, isBot: Boolean)(
    implicit
    userOfficeRegion: UserProcessorRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[CreateAck] = {
    (userOfficeRegion.ref ? Create(userId, accessSalt, name, countryCode, sex, isBot)).mapTo[CreateAck]
  }

  def addPhone(userId: Int, phone: Long)(
    implicit
    userOfficeRegion: UserProcessorRegion,
    timeout:          Timeout,
    ec:               ExecutionContext
  ): Future[AddPhoneAck] = {
    (userOfficeRegion.ref ? AddPhone(userId, phone)).mapTo[AddPhoneAck]
  }

  def addEmail(userId: Int, email: String)(
    implicit
    userOfficeRegion: UserProcessorRegion,
    timeout:          Timeout,
    ec:               ExecutionContext
  ): Future[AddEmailAck] = {
    (userOfficeRegion.ref ? AddEmail(userId, email)).mapTo[AddEmailAck]
  }

  def delete(userId: Int)(
    implicit
    userOfficeRegion: UserProcessorRegion,
    timeout:          Timeout,
    ec:               ExecutionContext
  ): Future[DeleteAck] = {
    (userOfficeRegion.ref ? Delete(userId)).mapTo[DeleteAck]
  }

  def changeCountryCode(userId: Int, countryCode: String)(
    implicit
    userOfficeRegion: UserProcessorRegion,
    timeout:          Timeout,
    ec:               ExecutionContext
  ): Future[Unit] = {
    userOfficeRegion.ref ? ChangeCountryCode(userId, countryCode) map (_ ⇒ ())
  }

  def changeName(userId: Int, name: String)(
    implicit
    userOfficeRegion: UserProcessorRegion,
    timeout:          Timeout,
    ec:               ExecutionContext
  ): Future[ChangeNameAck] = {
    (userOfficeRegion.ref ? ChangeName(userId, name)).mapTo[ChangeNameAck]
  }

  def auth(userId: Int, authId: Long)(
    implicit
    userOfficeRegion: UserProcessorRegion,
    timeout:          Timeout,
    ec:               ExecutionContext
  ): Future[NewAuthAck] = {
    (userOfficeRegion.ref ? NewAuth(userId, authId)).mapTo[NewAuthAck]
  }

  def removeAuth(userId: Int, authId: Long)(
    implicit
    userOfficeRegion: UserProcessorRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[RemoveAuthAck] = (userOfficeRegion.ref ? RemoveAuth(userId, authId)).mapTo[RemoveAuthAck]

  def sendMessage(userId: Int, senderUserId: Int, senderAuthId: Long, accessHash: Long, randomId: Long, message: ApiMessage)(
    implicit
    peerManagerRegion: UserProcessorRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[SeqStateDate] = {
    (peerManagerRegion.ref ? SendMessage(userId, senderUserId, senderAuthId, accessHash, randomId, message)).mapTo[SeqStateDate]
  }

  def deliverMessage(userId: Int, peer: Peer, senderUserId: Int, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean)(
    implicit
    region:  UserProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Unit =
    region.ref ! DeliverMessage(userId, peer, senderUserId, randomId, date, message, isFat)

  def deliverOwnMessage(userId: Int, peer: Peer, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean)(
    implicit
    region:  UserProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[SeqState] =
    (region.ref ? DeliverOwnMessage(userId, peer, senderAuthId, randomId, date, message, isFat)).mapTo[SeqState]

  def messageReceived(userId: Int, receiverUserId: Int, receiverAuthId: Long, date: Long, receivedDate: Long)(implicit peerManagerRegion: UserProcessorRegion): Unit = {
    peerManagerRegion.ref ! MessageReceived(userId, receiverUserId, receiverAuthId, date, receivedDate)
  }

  def messageRead(userId: Int, readerUserId: Int, readerAuthId: Long, date: Long, readDate: Long)(implicit peerManagerRegion: UserProcessorRegion): Unit = {
    peerManagerRegion.ref ! MessageRead(userId, readerUserId, readerAuthId, date, readDate)
  }

  def changeNickname(userId: Int, clientAuthId: Long, nickname: Option[String])(
    implicit
    userOfficeRegion: UserProcessorRegion,
    timeout:          Timeout,
    ec:               ExecutionContext
  ): Future[SeqState] = {
    (userOfficeRegion.ref ? ChangeNickname(userId, clientAuthId, nickname)).mapTo[SeqState]
  }

  def changeAbout(userId: Int, clientAuthId: Long, about: Option[String])(
    implicit
    userOfficeRegion: UserProcessorRegion,
    timeout:          Timeout,
    ec:               ExecutionContext
  ): Future[SeqState] = {
    (userOfficeRegion.ref ? ChangeAbout(userId, clientAuthId, about)).mapTo[SeqState]
  }

  def updateAvatar(userId: Int, clientAuthId: Long, avatarOpt: Option[Avatar])(
    implicit
    region:  UserProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[UpdateAvatarAck] = (region.ref ? UpdateAvatar(userId, clientAuthId, avatarOpt)).mapTo[UpdateAvatarAck]

  def broadcastUserUpdate(
    userId:   Int,
    update:   Update,
    pushText: Option[String],
    isFat:    Boolean        = false
  )(implicit
    userViewRegion: UserViewRegion,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    ec:                  ExecutionContext,
    timeout:             Timeout): Future[Seq[SeqState]] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = SeqUpdatesManager.updateRefs(update)

    val originPeer = SeqUpdatesManager.getOriginPeer(update)

    broadcastUserUpdate(userId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)
  }

  def broadcastUserUpdate(
    userId:         Int,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit
    userViewRegion: UserViewRegion,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    ec:                  ExecutionContext,
    timeout:             Timeout): Future[Seq[SeqState]] = {
    for {
      authIds ← getAuthIds(userId)
      seqstates ← SeqUpdatesManager.persistAndPushUpdatesF(authIds.toSet, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)
    } yield seqstates
  }

  def broadcastUsersUpdate(
    userIds:  Set[Int],
    update:   Update,
    pushText: Option[String],
    isFat:    Boolean        = false
  )(implicit
    userViewRegion: UserViewRegion,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    ec:                  ExecutionContext,
    timeout:             Timeout): Future[Seq[SeqState]] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (refUserIds, refGroupIds) = SeqUpdatesManager.updateRefs(update)

    val originPeer = SeqUpdatesManager.getOriginPeer(update)

    for {
      authIds ← getAuthIds(userIds)
      seqstates ← Future.sequence(
        authIds.map(SeqUpdatesManager.persistAndPushUpdateF(_, header, serializedData, refUserIds, refGroupIds, pushText, originPeer, isFat))
      )
    } yield seqstates
  }

  def broadcastClientAndUsersUpdate(
    userIds:  Set[Int],
    update:   Update,
    pushText: Option[String],
    isFat:    Boolean        = false
  )(implicit
    userViewRegion: UserViewRegion,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    ec:                  ExecutionContext,
    timeout:             Timeout,
    client:              AuthorizedClientData): Future[(SeqState, Seq[SeqState])] =
    broadcastClientAndUsersUpdate(client.userId, client.authId, userIds, update, pushText, isFat)

  def broadcastClientAndUsersUpdate(
    clientUserId: Int,
    clientAuthId: Long,
    userIds:      Set[Int],
    update:       Update,
    pushText:     Option[String],
    isFat:        Boolean
  )(implicit
    userViewRegion: UserViewRegion,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    ec:                  ExecutionContext,
    timeout:             Timeout): Future[(SeqState, Seq[SeqState])] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (refUserIds, refGroupIds) = SeqUpdatesManager.updateRefs(update)

    val originPeer = SeqUpdatesManager.getOriginPeer(update)

    for {
      authIds ← getAuthIds(userIds + clientUserId)
      seqstates ← Future.sequence(
        authIds.view
          .filterNot(_ == clientAuthId)
          .map(SeqUpdatesManager.persistAndPushUpdateF(_, header, serializedData, refUserIds, refGroupIds, pushText, originPeer, isFat))
      )
      seqstate ← SeqUpdatesManager.persistAndPushUpdateF(clientAuthId, header, serializedData, refUserIds, refGroupIds, pushText, originPeer, isFat)
    } yield (seqstate, seqstates)
  }
}

private[user] sealed trait Queries {
  import UserQueries._

  def getAuthIds(userId: Int)(implicit region: UserViewRegion, timeout: Timeout, ec: ExecutionContext): Future[Seq[Long]] = {
    println(s"GetAuthIds${userId} -> ${region.ref.path}")
    (region.ref ? GetAuthIds(userId)).mapTo[GetAuthIdsResponse] map (_.authIds)
  }

  def getAuthIds(userIds: Set[Int])(implicit region: UserViewRegion, timeout: Timeout, ec: ExecutionContext): Future[Seq[Long]] = {
    Future.sequence(userIds map (getAuthIds(_))) map (_.toSeq.flatten)
  }
}
