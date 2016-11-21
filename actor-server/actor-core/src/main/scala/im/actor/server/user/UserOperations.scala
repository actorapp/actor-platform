package im.actor.server.user

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.util.FastFuture
import akka.pattern.ask
import akka.util.Timeout
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.users.{ ApiFullUser, ApiSex, ApiUser }
import im.actor.server.auth.DeviceInfo
import im.actor.server.bots.BotCommand
import im.actor.server.db.DbExtension
import im.actor.server.file.Avatar
import im.actor.server.names.GlobalNamesStorageKeyValueStorage
import im.actor.server.persist.UserRepo
import im.actor.server.pubsub.PubSubExtension
import im.actor.server.sequence.{ SeqState, SeqUpdatesExtension }
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
  def nextId(): Future[Int] = FastFuture.successful(IdUtils.nextIntId())

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

  def changeName(userId: Int, authId: Long, name: String): Future[SeqState] =
    (processorRegion.ref ? ChangeName(userId, authId, name)).mapTo[SeqState]

  def changeNickname(userId: Int, authId: Long, nickname: Option[String]): Future[SeqState] =
    (processorRegion.ref ? ChangeNickname(userId, authId, nickname)).mapTo[SeqState]

  def changeAbout(userId: Int, authId: Long, about: Option[String]): Future[SeqState] =
    (processorRegion.ref ? ChangeAbout(userId, authId, about)).mapTo[SeqState]

  def changeTimeZone(userId: Int, authId: Long, timeZone: String): Future[SeqState] =
    (processorRegion.ref ? ChangeTimeZone(userId, authId, timeZone)).mapTo[SeqState]

  def changePreferredLanguages(userId: Int, authId: Long, preferredLanguages: Seq[String]): Future[SeqState] =
    (processorRegion.ref ? ChangePreferredLanguages(userId, authId, preferredLanguages)).mapTo[SeqState]

  def setDeviceInfo(userId: Int, authId: Long, data: DeviceInfo): Future[Unit] =
    for {
      _ ← changeTimeZone(userId, authId, data.timeZone)
      _ ← changePreferredLanguages(userId, authId, data.preferredLanguages)
    } yield ()

  def updateAvatar(userId: Int, authId: Long, avatarOpt: Option[Avatar]): Future[UpdateAvatarAck] =
    (processorRegion.ref ? UpdateAvatar(userId, authId, avatarOpt)).mapTo[UpdateAvatarAck]

  def addContact(userId: Int, authId: Long, contactUserId: Int, localName: Option[String], phone: Option[Long], email: Option[String]): Future[SeqState] =
    (processorRegion.ref ? AddContacts(userId, authId, Seq(ContactToAdd(contactUserId, localName, phone, email)))).mapTo[SeqState]

  def addContact(userId: Int, contactUserId: Int, localName: Option[String], phone: Option[Long], email: Option[String]): Future[Unit] =
    addContact(userId, 0L, contactUserId, localName, phone, email) map (_ ⇒ ())

  def addContacts(userId: Int, authId: Long, contactsToAdd: Seq[ContactToAdd]): Future[SeqState] =
    if (contactsToAdd.nonEmpty)
      (processorRegion.ref ? AddContacts(userId, authId, contactsToAdd)).mapTo[SeqState]
    else
      seqUpdExt.getSeqState(userId, authId)

  def removeContact(userId: Int, authId: Long, contactUserId: Int): Future[SeqState] =
    (processorRegion.ref ? RemoveContact(userId, authId, contactUserId)).mapTo[SeqState]

  def editLocalName(userId: Int, authId: Long, contactUserId: Int, localName: Option[String]): Future[SeqState] =
    (processorRegion.ref ? EditLocalName(userId, authId, contactUserId, localName, supressUpdate = false)).mapTo[SeqState]

  def editLocalNameSilently(userId: Int, contactUserId: Int, localName: Option[String]): Future[Unit] =
    (processorRegion.ref ? EditLocalName(userId, 0L, contactUserId, localName, supressUpdate = true)).mapTo[SeqState] map (_ ⇒ ())

  def addBotCommand(userId: Int, command: BotCommand): Future[AddBotCommandAck] =
    (processorRegion.ref ? AddBotCommand(userId, command)).mapTo[AddBotCommandAck]

  def removeBotCommand(userId: Int, slashCommand: String): Future[RemoveBotCommandAck] =
    (processorRegion.ref ? RemoveBotCommand(userId, slashCommand)).mapTo[RemoveBotCommandAck]

  def addExt(userId: Int, ext: UserExt): Future[Unit] =
    (processorRegion.ref ? AddExt(userId, ext)) map (_ ⇒ ())

  def removeExt(userId: Int, key: String): Future[Unit] =
    (processorRegion.ref ? RemoveExt(userId, key)) map (_ ⇒ ())

}

private[user] sealed trait Queries {

  import UserQueries._

  val viewRegion: UserViewRegion
  implicit val system: ActorSystem
  import system.dispatcher
  val log: LoggingAdapter
  private lazy val globalNamesStorage = new GlobalNamesStorageKeyValueStorage

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
      name ← localNameOpt map FastFuture.successful getOrElse getName(userId)
    } yield name

  def getUser(userId: Int): Future[UserState] =
    (viewRegion.ref ? GetUser(userId)).mapTo[UserState]

  def getContactRecords(userId: Int): Future[(Seq[Long], Seq[String])] =
    (viewRegion.ref ? GetContactRecords(userId)).mapTo[GetContactRecordsResponse] map (r ⇒ (r.phones, r.emails))

  def getContactRecordsSet(userId: Int): Future[(Set[Long], Set[String])] =
    for ((phones, emails) ← getContactRecords(userId)) yield (phones.toSet, emails.toSet)

  def checkAccessHash(userId: Int, clientAuthId: Long, accessHash: Long): Future[Boolean] =
    (viewRegion.ref ? CheckAccessHash(userId, clientAuthId, accessHash)).mapTo[CheckAccessHashResponse] map (_.isCorrect)

  def getAccessHash(userId: Int, clientAuthId: Long): Future[Long] =
    (viewRegion.ref ? GetAccessHash(userId, clientAuthId)).mapTo[GetAccessHashResponse] map (_.accessHash)

  def isAdmin(userId: Int): Future[Boolean] =
    (viewRegion.ref ? IsAdmin(userId)).mapTo[IsAdminResponse].map(_.isAdmin)

  def findUserIds(query: String): Future[Seq[Int]] =
    for {
      byPhoneAndEmail ← DbExtension(system).db.run(UserRepo.findIds(query))
      byNickname ← globalNamesStorage.getUserId(query)
    } yield byPhoneAndEmail ++ byNickname
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
