package im.actor.server.user

import akka.actor.{ ActorRef, ActorSystem }
import akka.cluster.pubsub.DistributedPubSub
import akka.event.{ Logging, LoggingAdapter }
import akka.pattern.ask
import akka.util.Timeout
import com.google.protobuf.ByteString
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.{ AuthorizedClientData, Update }
import im.actor.api.rpc.users.{ ApiFullUser, ApiSex, ApiUser }
import im.actor.server.auth.DeviceInfo
import im.actor.server.bots.BotCommand
import im.actor.server.db.DbExtension
import im.actor.server.file.Avatar
import im.actor.server.model.{ Peer, SerializedUpdate, UpdateMapping }
import im.actor.server.persist.UserRepo
import im.actor.server.pubsub.PubSubExtension
import im.actor.server.sequence.{ PushData, PushRules, SeqState, SeqUpdatesExtension }
import im.actor.server.{ model, persist ⇒ p }
import im.actor.types._
import im.actor.util.misc.IdUtils
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

  protected val seqUpdExt: SeqUpdatesExtension

  def create(
    userId:      Int,
    accessSalt:  String,
    nickname:    Option[String],
    name:        String,
    countryCode: String,
    sex:         ApiSex.ApiSex,
    isBot:       Boolean,
    isAdmin:     Boolean           = false,
    extensions:  Seq[ApiExtension] = Seq.empty,
    external:    Option[String]    = None
  ): Future[CreateAck] =
    (processorRegion.ref ? Create(userId, accessSalt, nickname, name, countryCode, sex, isBot, Some(isAdmin), extensions, external)).mapTo[CreateAck]

  def updateIsAdmin(userId: Int, isAdmin: Boolean): Future[UpdateIsAdminAck] =
    (processorRegion.ref ? UpdateIsAdmin(userId, Some(isAdmin))).mapTo[UpdateIsAdminAck]

  // FIXME: check existence and reserve generated ids
  def nextId(): Future[Int] = Future.successful(IdUtils.nextIntId())

  def addPhone(userId: Int, phone: Long): Future[Unit] =
    (processorRegion.ref ? AddPhone(userId, phone)).mapTo[AddPhoneAck] map (_ ⇒ ())

  def addEmail(userId: Int, email: String): Future[Unit] =
    (processorRegion.ref ? AddEmail(userId, email)).mapTo[AddEmailAck] map (_ ⇒ ())

  def addSocialContact(userId: Int, contact: SocialContact): Future[Unit] = {
    (processorRegion.ref ? AddSocialContact(userId, contact)).mapTo[AddSocialContactAck] map (_ ⇒ ())
  }

  def delete(userId: Int): Future[Unit] =
    (processorRegion.ref ? Delete(userId)).mapTo[DeleteAck] map (_ ⇒ ())

  def changeCountryCode(userId: Int, countryCode: String): Future[Unit] =
    (processorRegion.ref ? ChangeCountryCode(userId, countryCode)).mapTo[ChangeCountryCodeAck] map (_ ⇒ ())

  def changeName(userId: Int, name: String): Future[SeqState] =
    (processorRegion.ref ? ChangeName(userId, name)).mapTo[SeqState]

  def changeNickname(userId: Int, nickname: Option[String]): Future[SeqState] =
    (processorRegion.ref ? ChangeNickname(userId, nickname)).mapTo[SeqState]

  def changeAbout(userId: Int, about: Option[String]): Future[SeqState] =
    (processorRegion.ref ? ChangeAbout(userId, about)).mapTo[SeqState]

  def changeTimeZone(userId: Int, timeZone: String): Future[SeqState] =
    (processorRegion.ref ? ChangeTimeZone(userId, timeZone)).mapTo[SeqState]

  def setDeviceInfo(userId: Int, data: DeviceInfo): Future[Unit] =
    for {
      _ ← changeTimeZone(userId, data.timeZone)
      _ ← changePreferredLanguages(userId, data.preferredLanguages)
    } yield ()

  def changePreferredLanguages(userId: Int, preferredLanguages: Seq[String]): Future[SeqState] =
    (processorRegion.ref ? ChangePreferredLanguages(userId, preferredLanguages)).mapTo[SeqState]

  def updateAvatar(userId: Int, avatarOpt: Option[Avatar]): Future[UpdateAvatarAck] =
    (processorRegion.ref ? UpdateAvatar(userId, avatarOpt)).mapTo[UpdateAvatarAck]

  def addContact(userId: Int, contactUserId: Int, localName: Option[String], phone: Option[Long], email: Option[String]): Future[SeqState] =
    (processorRegion.ref ? AddContacts(userId, Seq(ContactToAdd(contactUserId, localName, phone, email)))).mapTo[SeqState]

  def addContacts(userId: Int, contactsToAdd: Seq[ContactToAdd]): Future[SeqState] =
    if (contactsToAdd.nonEmpty)
      (processorRegion.ref ? AddContacts(userId, contactsToAdd)).mapTo[SeqState]
    else
      seqUpdExt.getSeqState(userId)

  def removeContact(userId: Int, contactUserId: Int): Future[SeqState] =
    (processorRegion.ref ? RemoveContact(userId, contactUserId)).mapTo[SeqState]

  def editLocalName(userId: Int, contactUserId: Int, localName: Option[String], supressUpdate: Boolean = false): Future[SeqState] =
    (processorRegion.ref ? EditLocalName(userId, contactUserId, localName, supressUpdate)).mapTo[SeqState]

  def addBotCommand(userId: Int, command: BotCommand): Future[AddBotCommandAck] =
    (processorRegion.ref ? AddBotCommand(userId, command)).mapTo[AddBotCommandAck]

  def removeBotCommand(userId: Int, slashCommand: String): Future[RemoveBotCommandAck] =
    (processorRegion.ref ? RemoveBotCommand(userId, slashCommand)).mapTo[RemoveBotCommandAck]

  def addExt(userId: Int, ext: UserExt): Future[Unit] =
    (processorRegion.ref ? AddExt(userId, ext)) map (_ ⇒ ())

  def removeExt(userId: Int, key: String): Future[Unit] =
    (processorRegion.ref ? RemoveExt(userId, key)) map (_ ⇒ ())

  def broadcastUserUpdate(
    userId:     Int,
    update:     Update,
    pushText:   Option[String],
    isFat:      Boolean,
    reduceKey:  Option[String],
    deliveryId: Option[String]
  ): Future[SeqState] = {
    val header = update.header
    val serializedData = update.toByteArray

    val originPeer = seqUpdExt.getOriginPeer(update)

    broadcastUserUpdate(userId, header, serializedData, update._relatedUserIds, update._relatedGroupIds, pushText, originPeer, isFat, reduceKey, deliveryId)
  }

  def broadcastUserUpdate(
    userId:         Int,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Seq[Int],
    groupIds:       Seq[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean,
    reduceKey:      Option[String],
    deliveryId:     Option[String]
  ): Future[SeqState] =
    seqUpdExt.deliverUpdate(
      userId = userId,
      mapping = UpdateMapping(default = Some(SerializedUpdate(header, ByteString.copyFrom(serializedData), userIds = userIds, groupIds = groupIds))),
      pushRules = PushRules(isFat = isFat).withData(PushData().withText(pushText.getOrElse(""))),
      reduceKey = reduceKey,
      deliveryId = deliveryId.getOrElse("")
    )

  def broadcastUsersUpdate(
    userIds:    Set[Int],
    update:     Update,
    pushText:   Option[String],
    isFat:      Boolean,
    deliveryId: Option[String]
  ): Future[Seq[SeqState]] =
    seqUpdExt.broadcastSingleUpdate(
      userIds = userIds,
      update = update,
      pushRules = PushRules(isFat = isFat).withData(PushData().withText(pushText.getOrElse(""))),
      deliveryId = deliveryId.getOrElse("")
    )

  def broadcastClientUpdate(
    update:     Update,
    pushText:   Option[String],
    isFat:      Boolean        = false,
    deliveryId: Option[String] = None
  )(implicit client: AuthorizedClientData): Future[SeqState] = broadcastClientUpdate(client.userId, client.authSid, update, pushText, isFat, deliveryId)

  def broadcastClientUpdate(
    clientUserId:  Int,
    clientAuthSid: Int,
    update:        Update,
    pushText:      Option[String],
    isFat:         Boolean,
    deliveryId:    Option[String]
  ): Future[SeqState] =
    seqUpdExt.deliverSingleUpdate(
      userId = clientUserId,
      update = update,
      pushRules = PushRules(isFat = isFat, excludeAuthSids = Seq(clientAuthSid)).withData(PushData().withText(pushText.getOrElse(""))),
      deliveryId = deliveryId.getOrElse("")
    )

  def broadcastClientAndUsersUpdate(
    userIds:    Set[Int],
    update:     Update,
    pushText:   Option[String],
    isFat:      Boolean        = false,
    deliveryId: Option[String] = None
  )(implicit client: AuthorizedClientData): Future[(SeqState, Seq[SeqState])] =
    broadcastClientAndUsersUpdate(client.userId, client.authSid, userIds, update, pushText, isFat, deliveryId)

  def broadcastClientAndUsersUpdate(
    clientUserId:  Int,
    clientAuthSid: Int,
    userIds:       Set[Int],
    update:        Update,
    pushText:      Option[String],
    isFat:         Boolean,
    deliveryId:    Option[String]
  ): Future[(SeqState, Seq[SeqState])] = {
    val pushRules = PushRules(isFat = isFat).withData(PushData().withText(pushText.getOrElse("")))
    val deliveryIdStr = deliveryId.getOrElse("")

    for {
      seqstates ← seqUpdExt.broadcastSingleUpdate(
        userIds = userIds filterNot (_ == clientUserId),
        update = update,
        pushRules = pushRules,
        deliveryId = deliveryIdStr
      )
      seqstate ← seqUpdExt.deliverSingleUpdate(
        userId = clientUserId,
        update = update,
        pushRules = pushRules,
        deliveryId = deliveryIdStr
      )
    } yield (seqstate, seqstates)
  }
}

private[user] sealed trait Queries {

  import UserQueries._

  val viewRegion: UserViewRegion
  implicit val system: ActorSystem
  import system.dispatcher
  val log: LoggingAdapter

  implicit val timeout: Timeout

  def getAuthIds(userId: Int): Future[Seq[Long]] =
    (viewRegion.ref ? GetAuthIds(userId)).mapTo[GetAuthIdsResponse] map (_.authIds)

  def getAuthIds(userIds: Set[Int]): Future[Seq[Long]] =
    Future.sequence(userIds map getAuthIds) map (_.toSeq.flatten)

  def getAuthIdsMap(userIds: Set[Int]): Future[Map[UserId, Seq[AuthId]]] =
    Future.sequence(userIds map (uid ⇒ getAuthIds(uid) map (uid → _))) map (_.toMap)

  def getApiStruct(userId: Int, clientUserId: Int, clientAuthId: Long): Future[ApiUser] =
    (viewRegion.ref ? GetApiStruct(userId, clientUserId, clientAuthId)).mapTo[GetApiStructResponse] map (_.struct)

  def getApiFullStruct(userId: Int, clientUserId: Int, clientAuthId: Long): Future[ApiFullUser] =
    (viewRegion.ref ? GetApiFullStruct(userId, clientUserId, clientAuthId)).mapTo[GetApiFullStructResponse] map (_.struct)

  def getLocalName(ownerUserId: Int, contactUserId: Int): Future[Option[String]] =
    (viewRegion.ref ? GetLocalName(ownerUserId, contactUserId)).mapTo[GetLocalNameResponse] map (_.localName)

  def getName(userId: Int): Future[String] =
    (viewRegion.ref ? GetName(userId)).mapTo[GetNameResponse] map (_.name)

  def getName(userId: Int, clientUserId: Int): Future[String] =
    for {
      localNameOpt ← getLocalName(clientUserId, userId)
      name ← localNameOpt map Future.successful getOrElse getName(userId)
    } yield name

  def getUser(userId: Int): Future[UserState] =
    (viewRegion.ref ? GetUser(userId)).mapTo[UserState]

  def getContactRecords(userId: Int): Future[(Seq[Long], Seq[String])] =
    (viewRegion.ref ? GetContactRecords(userId)).mapTo[GetContactRecordsResponse] map (r ⇒ (r.phones, r.emails))

  def getContactRecordsSet(userId: Int): Future[(Set[Long], Set[String])] =
    for ((phones, emails) ← getContactRecords(userId)) yield (phones.toSet, emails.toSet)

  def checkAccessHash(userId: Int, senderAuthId: Long, accessHash: Long): Future[Boolean] =
    (viewRegion.ref ? CheckAccessHash(userId, senderAuthId, accessHash)).mapTo[CheckAccessHashResponse] map (_.isCorrect)

  def getAccessHash(userId: Int, clientAuthId: Long): Future[Long] =
    (viewRegion.ref ? GetAccessHash(userId, clientAuthId)).mapTo[GetAccessHashResponse] map (_.accessHash)

  def isAdmin(userId: Int): Future[Boolean] =
    (viewRegion.ref ? IsAdmin(userId)).mapTo[IsAdminResponse].map(_.isAdmin)

  def findUserIds(query: String): Future[Seq[Int]] = DbExtension(system).db.run(UserRepo.findIds(query))
}

private[user] sealed trait AuthCommands {
  self: Queries ⇒

  import UserCommands._
  import akka.cluster.pubsub.DistributedPubSubMediator._

  implicit val system: ActorSystem
  import system.dispatcher
  val processorRegion: UserProcessorRegion

  def authIdTopic(authId: Long): String = s"auth.events.${authId}"

  def auth(userId: Int, authId: Long): Future[NewAuthAck] = {
    (processorRegion.ref ? NewAuth(userId, authId)).mapTo[NewAuthAck]
  }

  def removeAuth(userId: Int, authId: Long): Future[RemoveAuthAck] = (processorRegion.ref ? RemoveAuth(userId, authId)).mapTo[RemoveAuthAck]

  def logoutByAppleToken(token: Array[Byte])(implicit db: Database): Future[Unit] = {
    db.run(p.push.ApplePushCredentialsRepo.findByToken(token)) flatMap { creds ⇒
      Future.sequence(creds map (c ⇒ logout(c.authId))) map (_ ⇒ ())
    }
  }

  def logout(authId: Long)(implicit db: Database): Future[Unit] = {
    db.run(p.AuthSessionRepo.findByAuthId(authId)) flatMap {
      case Some(session) ⇒ logout(session)
      case None          ⇒ throw new Exception("Can't find auth session to logout")
    }
  }

  def logout(session: model.AuthSession)(implicit db: Database): Future[Unit] = {
    log.warning(s"Terminating AuthSession ${session.id} of user ${session.userId} and authId ${session.authId}")
    for {
      _ ← removeAuth(session.userId, session.authId)
      _ ← SeqUpdatesExtension(system).unregisterAllPushCredentials(session.authId)
      _ ← db.run(p.AuthSessionRepo.delete(session.userId, session.id))
    } yield publishAuthIdInvalidated(session.authId)
  }

  private def publishAuthIdInvalidated(authId: Long): Unit =
    PubSubExtension(system).publish(Publish(authIdTopic(authId), AuthEvents.AuthIdInvalidated))
}
