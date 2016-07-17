package im.actor.server.user

import java.time.Instant

import akka.actor._
import akka.cluster.sharding.ShardRegion
import akka.pattern.{ ask, pipe }
import akka.persistence.RecoveryCompleted
import akka.util.Timeout
import im.actor.api.rpc.collections._
import im.actor.api.rpc.misc.ApiExtension
import im.actor.serialization.ActorSerializer
import im.actor.server.bots.BotCommand
import im.actor.server.cqrs.TaggedEvent
import im.actor.server.db.DbExtension
import im.actor.server.dialog._
import im.actor.server.model.{ Peer, PeerType }
import im.actor.server.names.GlobalNamesStorageKeyValueStorage
import im.actor.server.office.{ PeerProcessor, StopOffice }
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.social.{ SocialExtension, SocialManagerRegion }
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait UserEvent extends TaggedEvent {
  val ts: Instant

  def tags: Set[String] = Set("user")
}

trait UserCommand {
  val userId: Int
}

trait UserQuery {
  val userId: Int
}

private[user] object UserBuilder {
  def apply(e: UserEvents.Created): UserState =
    UserState(
      id = e.userId,
      accessSalt = e.accessSalt,
      name = e.name,
      countryCode = e.countryCode,
      sex = e.sex,
      phones = Seq.empty[Long],
      emails = Seq.empty[String],
      authIds = Seq.empty[Long],
      isDeleted = false,
      isBot = e.isBot,
      nickname = e.nickname,
      about = None,
      avatar = None,
      createdAt = e.ts.toEpochMilli,
      internalExtensions = e.extensions,
      external = e.external,
      isAdmin = e.isAdmin,
      socialContacts = Seq.empty[SocialContact],
      preferredLanguages = Seq.empty[String],
      timeZone = None,
      botCommands = Seq.empty[BotCommand]
    )
}

object UserProcessor {
  def register(): Unit =
    ActorSerializer.register(
      10001 → classOf[UserCommands.NewAuth],
      10002 → classOf[UserCommands.NewAuthAck],
      10007 → classOf[UserCommands.RemoveAuth],
      10008 → classOf[UserCommands.Create],
      10010 → classOf[UserCommands.Delete],
      10012 → classOf[UserCommands.ChangeName],
      10013 → classOf[UserCommands.CreateAck],
      10014 → classOf[UserCommands.ChangeCountryCode],
      10017 → classOf[UserCommands.RemoveAuthAck],
      10018 → classOf[UserCommands.DeleteAck],
      10019 → classOf[UserCommands.AddPhone],
      10020 → classOf[UserCommands.AddPhoneAck],
      10021 → classOf[UserCommands.AddEmail],
      10022 → classOf[UserCommands.AddEmailAck],
      10023 → classOf[UserCommands.ChangeCountryCodeAck],
      10024 → classOf[UserCommands.ChangeNickname],
      10025 → classOf[UserCommands.ChangeAbout],
      10026 → classOf[UserCommands.UpdateAvatar],
      10027 → classOf[UserCommands.UpdateAvatarAck],
      10028 → classOf[UserCommands.AddContacts],
      10029 → classOf[UserCommands.AddSocialContact],
      10030 → classOf[UserCommands.AddSocialContactAck],
      10031 → classOf[UserCommands.UpdateIsAdmin],
      10032 → classOf[UserCommands.UpdateIsAdminAck],
      10035 → classOf[UserCommands.ChangePreferredLanguages],
      10036 → classOf[UserCommands.ChangeTimeZone],
      10037 → classOf[UserCommands.EditLocalName],
      10038 → classOf[UserCommands.RemoveContact],
      10039 → classOf[UserCommands.AddBotCommand],
      10040 → classOf[UserCommands.RemoveBotCommand],

      11001 → classOf[UserQueries.GetAuthIds],
      11002 → classOf[UserQueries.GetAuthIdsResponse],
      11003 → classOf[UserQueries.GetContactRecords],
      11004 → classOf[UserQueries.GetContactRecordsResponse],
      11005 → classOf[UserQueries.CheckAccessHash],
      11006 → classOf[UserQueries.CheckAccessHashResponse],
      11007 → classOf[UserQueries.GetApiStruct],
      11008 → classOf[UserQueries.GetApiStructResponse],
      11009 → classOf[UserQueries.GetAccessHash],
      11010 → classOf[UserQueries.GetAccessHashResponse],
      11011 → classOf[UserQueries.GetUser],
      11012 → classOf[UserQueries.IsAdmin],
      11013 → classOf[UserQueries.IsAdminResponse],
      11014 → classOf[UserQueries.GetLocalName],
      11015 → classOf[UserQueries.GetLocalNameResponse],
      11016 → classOf[UserQueries.GetName],
      11017 → classOf[UserQueries.GetNameResponse],

      12001 → classOf[UserEvents.AuthAdded],
      12002 → classOf[UserEvents.AuthRemoved],
      12003 → classOf[UserEvents.Created],
      12006 → classOf[UserEvents.Deleted],
      12007 → classOf[UserEvents.NameChanged],
      12008 → classOf[UserEvents.CountryCodeChanged],
      12009 → classOf[UserEvents.PhoneAdded],
      12010 → classOf[UserEvents.EmailAdded],
      12011 → classOf[UserEvents.NicknameChanged],
      12012 → classOf[UserEvents.AboutChanged],
      12013 → classOf[UserEvents.AvatarUpdated],
      12014 → classOf[UserEvents.SocialContactAdded],
      12016 → classOf[UserEvents.IsAdminUpdated],
      12017 → classOf[UserEvents.PreferredLanguagesChanged],
      12018 → classOf[UserEvents.TimeZoneChanged],
      12019 → classOf[UserEvents.LocalNameChanged],
      12020 → classOf[UserEvents.BotCommandAdded],
      12021 → classOf[UserEvents.BotCommandRemoved],
      12022 → classOf[UserEvents.ExtAdded],
      12023 → classOf[UserEvents.ExtRemoved],

      13000 → classOf[UserState],
      13001 → classOf[SocialContact]
    )

  def props: Props =
    Props(classOf[UserProcessor])
}

private[user] final class UserProcessor
  extends PeerProcessor[UserState, UserEvent]
  with UserCommandHandlers
  with UserQueriesHandlers {

  import UserCommands._
  import UserQueries._

  protected implicit val system: ActorSystem = context.system
  protected implicit val ec: ExecutionContext = context.dispatcher

  protected val db: Database = DbExtension(system).db
  protected val userExt = UserExtension(system)
  protected lazy val dialogExt = DialogExtension(system)
  protected val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(system)
  protected implicit val socialRegion: SocialManagerRegion = SocialExtension(system).region
  protected val globalNamesStorage = new GlobalNamesStorageKeyValueStorage

  protected implicit val timeout: Timeout = Timeout(10.seconds)

  protected val userId = self.path.name.toInt
  override protected val notFoundError = UserErrors.UserNotFound(userId)

  override def persistenceId = UserOffice.persistenceIdFor(userId)

  protected val contacts = new UserContacts(userId)

  context.setReceiveTimeout(1.hour)

  override def updatedState(evt: UserEvent, state: UserState): UserState = {
    evt match {
      case UserEvents.AuthAdded(_, authId) ⇒
        val updAuthIds = if (state.authIds contains authId) state.authIds else state.authIds :+ authId
        state.copy(authIds = updAuthIds)
      case UserEvents.AuthRemoved(_, authId) ⇒
        state.copy(authIds = state.authIds filterNot (_ == authId))
      case UserEvents.CountryCodeChanged(_, countryCode) ⇒
        state.copy(countryCode = countryCode)
      case UserEvents.NameChanged(_, name) ⇒
        state.copy(name = name)
      case UserEvents.PhoneAdded(_, phone) ⇒
        state.copy(phones = state.phones :+ phone)
      case UserEvents.EmailAdded(_, email) ⇒
        state.copy(emails = state.emails :+ email)
      case UserEvents.SocialContactAdded(_, contact) ⇒
        state.copy(socialContacts = state.socialContacts :+ contact)
      case UserEvents.Deleted(_) ⇒
        state.copy(isDeleted = true)
      case UserEvents.NicknameChanged(_, nickname) ⇒
        state.copy(nickname = nickname)
      case UserEvents.AboutChanged(_, about) ⇒
        state.copy(about = about)
      case UserEvents.AvatarUpdated(_, avatar) ⇒
        state.copy(avatar = avatar)
      case UserEvents.IsAdminUpdated(_, isAdmin) ⇒
        state.copy(isAdmin = isAdmin)
      case UserEvents.TimeZoneChanged(_, timeZone) ⇒
        state.copy(timeZone = timeZone)
      case UserEvents.PreferredLanguagesChanged(_, preferredLanguages) ⇒
        state.copy(preferredLanguages = preferredLanguages)
      case _: UserEvents.Created        ⇒ state
      case _: UserEvents.DialogsChanged ⇒ state
      case UserEvents.BotCommandAdded(_, command) ⇒
        val updCommands = if (state.botCommands exists (_.slashCommand == command.slashCommand)) state.botCommands else state.botCommands :+ command
        state.copy(botCommands = updCommands)
      case UserEvents.BotCommandRemoved(_, slashCommand) ⇒
        val updCommands =
          if (state.botCommands exists (_.slashCommand == slashCommand))
            state.botCommands filterNot (_.slashCommand == slashCommand)
          else state.botCommands
        state.copy(botCommands = updCommands)
      case UserEvents.ExtAdded(_, ext)   ⇒ state.copy(ext = state.ext :+ ext)
      case UserEvents.ExtRemoved(_, key) ⇒ state.copy(ext = state.ext.filterNot(_.key == key))
    }
  }

  override protected def handleInitCommand: Receive = {
    case Create(_, accessSalt, nickName, name, countryCode, sex, isBot, isAdmin, extensions, external) ⇒
      create(accessSalt, nickName, name, countryCode, sex, isBot, isAdmin.getOrElse(false), extensions, external)
  }

  override protected def handleCommand(state: UserState): Receive = {
    case NewAuth(_, authId)                         ⇒ addAuth(state, authId)
    case RemoveAuth(_, authId)                      ⇒ removeAuth(state, authId)
    case ChangeCountryCode(_, countryCode)          ⇒ changeCountryCode(state, countryCode)
    case ChangeName(_, authId, name)                ⇒ changeName(state, authId, name)
    case Delete(_)                                  ⇒ delete(state)
    case AddPhone(_, phone)                         ⇒ addPhone(state, phone)
    case AddEmail(_, email)                         ⇒ addEmail(state, email)
    case AddSocialContact(_, contact)               ⇒ addSocialContact(state, contact)
    case ChangeNickname(_, authId, nickname)        ⇒ changeNickname(state, authId, nickname)
    case ChangeAbout(_, authId, about)              ⇒ changeAbout(state, authId, about)
    case UpdateAvatar(_, authId, avatarOpt)         ⇒ updateAvatar(state, authId, avatarOpt)
    case AddContacts(_, authId, contactsToAdd)      ⇒ addContacts(state, authId, contactsToAdd)
    case RemoveContact(_, authId, contactUserId)    ⇒ removeContact(state, authId, contactUserId)
    case UpdateIsAdmin(_, isAdmin)                  ⇒ updateIsAdmin(state, isAdmin)

    case ChangeTimeZone(_, authId, timeZone)        ⇒ changeTimeZone(state, authId, timeZone)
    case ChangePreferredLanguages(_, authId, langs) ⇒ changePreferredLanguages(state, authId, langs)

    case AddBotCommand(_, command)                  ⇒ addBotCommand(state, command)
    case RemoveBotCommand(_, slashCommand)          ⇒ removeBotCommand(state, slashCommand)
    case AddExt(_, ext)                             ⇒ addExt(state, ext)
    case RemoveExt(_, key)                          ⇒ removeExt(state, key)
    case cmd: EditLocalName                         ⇒ contacts.ref forward cmd
    case query: GetLocalName                        ⇒ contacts.ref forward query
    case StopOffice                                 ⇒ context stop self
    case ReceiveTimeout                             ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopOffice)
    case e @ DialogRootEnvelope(query, command) ⇒
      val msg = e.getAllFields.values.head

      (dialogRoot(state.internalExtensions) ? msg) pipeTo sender()
    case de: DialogEnvelope ⇒
      val msg = de.getAllFields.values.head

      msg match {
        case dc: DialogCommand if dc.isInstanceOf[DialogCommands.SendMessage] || dc.isInstanceOf[DialogCommands.WriteMessageSelf] ⇒
          dialogRoot(state.internalExtensions) ! msg
          handleDialogCommand(state)(dc)
        case dc: DialogCommand ⇒ handleDialogCommand(state)(dc)
        case dq: DialogQuery   ⇒ handleDialogQuery(state)(dq)
      }
    // messages sent from DialogRoot:
    case dc: DialogCommand ⇒ handleDialogCommand(state)(dc)
    case dq: DialogQuery   ⇒ handleDialogQuery(state)(dq)
  }

  override protected def handleQuery(state: UserState): Receive = {
    case GetAuthIds(_)                                   ⇒ getAuthIds(state)
    case GetApiStruct(_, clientUserId, clientAuthId)     ⇒ getApiStruct(state, clientUserId, clientAuthId)
    case GetApiFullStruct(_, clientUserId, clientAuthId) ⇒ getApiFullStruct(state, clientUserId, clientAuthId)
    case GetContactRecords(_)                            ⇒ getContactRecords(state)
    case CheckAccessHash(_, senderAuthId, accessHash)    ⇒ checkAccessHash(state, senderAuthId, accessHash)
    case GetAccessHash(_, clientAuthId)                  ⇒ getAccessHash(state, clientAuthId)
    case GetUser(_)                                      ⇒ getUser(state)
    case IsAdmin(_)                                      ⇒ isAdmin(state)
    case GetName(_)                                      ⇒ getName(state)
  }

  protected def extToApi(exts: Seq[UserExt]): ApiMapValue = {
    ApiMapValue(
      exts.toVector map { ext ⇒
        if (ext.value.isBoolValue) ApiMapValueItem(ext.key, ApiInt32Value(if (ext.getBoolValue) 1 else 0))
        else ApiMapValueItem(ext.key, ApiStringValue(ext.getStringValue))
      }
    )
  }

  protected[this] var userStateMaybe: Option[UserState] = None

  override def receiveRecover: Receive = {
    case evt: UserEvents.Created ⇒
      userStateMaybe = Some(UserBuilder(evt))
    case evt: UserEvent ⇒
      userStateMaybe = userStateMaybe map (updatedState(evt, _))
    case RecoveryCompleted ⇒
      userStateMaybe match {
        case Some(state) ⇒
          context become working(state)
        case None ⇒
          context become initializing
      }
    case unmatched ⇒
      log.error("Unmatched recovery event {}", unmatched)
  }

  private def handleDialogCommand(state: UserState): PartialFunction[DialogCommand, Unit] = {
    case ddc: DirectDialogCommand ⇒ dialogRef(state, ddc) forward ddc
    case dc: DialogCommand        ⇒ dialogRef(state, dc.getDest) forward dc
  }

  private def handleDialogQuery(state: UserState): PartialFunction[DialogQuery, Unit] = {
    case dq: DialogQuery ⇒ dialogRef(state, dq.getDest) forward dq
  }

  private def dialogRef(state: UserState, dc: DirectDialogCommand): ActorRef = {
    val peer = dc.getDest match {
      case Peer(PeerType.Group, _)   ⇒ dc.getDest
      case Peer(PeerType.Private, _) ⇒ if (dc.getOrigin.id == userId) dc.getDest else dc.getOrigin
    }
    dialogRef(state, peer)
  }

  private def dialogRef(state: UserState, peer: Peer): ActorRef =
    context.child(dialogName(peer)) getOrElse context.actorOf(DialogProcessor.props(userId, peer, state.internalExtensions), dialogName(peer))

  private def dialogRoot(extensions: Seq[ApiExtension]): ActorRef = {
    val name = "DialogRoot"
    context.child(name).getOrElse(context.actorOf(DialogRoot.props(userId, extensions), name))
  }

  private def dialogName(peer: Peer): String = peer.typ match {
    case PeerType.Private ⇒ s"Private_${peer.id}"
    case PeerType.Group   ⇒ s"Group_${peer.id}"
    case other            ⇒ throw new Exception(s"Unknown peer type: $other")
  }
}
