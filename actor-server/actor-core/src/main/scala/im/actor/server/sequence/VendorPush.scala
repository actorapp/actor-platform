package im.actor.server.sequence

import akka.actor._
import akka.pattern.pipe
import im.actor.concurrent.FutureExt
import im.actor.server.db.DbExtension
import im.actor.server.model.push._
import im.actor.server.model.{ DeviceType, Peer, PeerType }
import im.actor.server.persist.{ AuthIdRepo, AuthSessionRepo }
import im.actor.server.persist.configs.ParameterRepo
import im.actor.server.persist.push.{ ActorPushCredentialsRepo, ApplePushCredentialsRepo, FirebasePushCredentialsKV, GooglePushCredentialsRepo }
import im.actor.server.push.actor.ActorPush
import im.actor.server.push.apple.ApplePushProvider
import im.actor.server.push.google.GooglePushProvider
import im.actor.server.sequence.UserSequenceCommands.ReloadSettings
import im.actor.server.userconfig.SettingsKeys
import slick.dbio.DBIO

import scala.concurrent.Future

private[sequence] trait VendorPushCommand

private final case class PushCredentialsInfo(appId: Int, authId: Long)

private final case class AllNotificationSettings(
  generic:  NotificationSettings              = NotificationSettings(),
  specific: Map[String, NotificationSettings] = Map.empty,
  groups:   GroupNotificationSettings         = GroupNotificationSettings()
)

private final case class GroupNotificationSettings(
  enabled:     Boolean = true,
  onlyMention: Boolean = false
)

private final case class NotificationSettings(
  enabled:      Boolean            = true,
  sound:        Boolean            = true,
  vibration:    Boolean            = true,
  text:         Boolean            = true,
  customSounds: Map[Peer, String]  = Map.empty,
  peers:        Map[Peer, Boolean] = Map.empty
)

private case object FailedToUnregister extends RuntimeException("Failed to unregister push credentials")

private[sequence] object VendorPush {

  private final case class Initialized(creds: Seq[(PushCredentials, PushCredentialsInfo)])

  def props(userId: Int) =
    Props(new VendorPush(userId))
}

private object SettingsControl {
  def props(userId: Int) = Props(new SettingsControl(userId))
}

private final class SettingsControl(userId: Int) extends Actor with ActorLogging with Stash {

  import context.dispatcher

  private val db = DbExtension(context.system).db

  private var notificationSettings = AllNotificationSettings()

  self ! ReloadSettings()

  def receive: Receive = {
    case ReloadSettings() ⇒
      context.become(waitForSettings, discardOld = false)
      load() pipeTo self
  }

  def waitForSettings: Receive = {
    case s: AllNotificationSettings ⇒
      this.notificationSettings = s
      log.debug("Loaded settings: {}", s)
      context.parent ! s
      unstashAll()
      context.unbecome()
    case Status.Failure(e) ⇒
      log.error(e, "Failed to load settings")
      load() pipeTo self
    case msg ⇒ stash()
  }

  private def load(): Future[AllNotificationSettings] =
    db.run(for {
      generic ← loadForDevice(DeviceType.Generic)
      mobile ← loadForDevice(DeviceType.Mobile)
      tablet ← loadForDevice(DeviceType.Tablet)
      desktop ← loadForDevice(DeviceType.Desktop)

      groups ← loadForGroups()
    } yield AllNotificationSettings(
      generic = generic,
      specific = Map(
        DeviceType.Mobile → mobile,
        DeviceType.Tablet → tablet,
        DeviceType.Desktop → desktop
      ),
      groups = groups
    ))

  private def loadForGroups(): DBIO[GroupNotificationSettings] =
    for {
      enabled ← ParameterRepo.findBooleanValue(userId, SettingsKeys.accountGroupEnabled, true)
      onlyMentions ← ParameterRepo.findBooleanValue(userId, SettingsKeys.accountGroupMentionEnabled, false)
    } yield GroupNotificationSettings(enabled, onlyMentions)

  private def loadForDevice(deviceType: String): DBIO[NotificationSettings] =
    for {
      enabled ← ParameterRepo.findBooleanValue(userId, SettingsKeys.enabled(deviceType), true)
      sound ← ParameterRepo.findBooleanValue(userId, SettingsKeys.soundEnabled(deviceType), true)
      vibration ← ParameterRepo.findBooleanValue(userId, SettingsKeys.vibrationEnabled(deviceType), true)
      text ← ParameterRepo.findBooleanValue(userId, SettingsKeys.textEnabled(deviceType), true)
      peers ← ParameterRepo.findPeerNotifications(userId, deviceType)
      customSounds ← ParameterRepo.findPeerRingtone(userId)
    } yield NotificationSettings(enabled, sound, vibration, text, customSounds.toMap, peers.toMap)

}

private[sequence] final class VendorPush(userId: Int) extends Actor with ActorLogging with Stash {

  import VendorPush._
  import context.dispatcher
  import im.actor.server.sequence.UserSequenceCommands._

  protected val db = DbExtension(context.system).db

  private val settingsControl = context.actorOf(SettingsControl.props(userId), "settings")
  private val googlePushProvider = new GooglePushProvider(userId, context.system)

  private val applePushProvider = new ApplePushProvider(userId)(context.system)
  private val actorPushProvider = ActorPush(context.system)

  // TODO: why do we need `PushCredentialsInfo`, we have `authId` anyway!
  private var mapping: Map[PushCredentials, PushCredentialsInfo] = Map.empty
  private var notificationSettings = AllNotificationSettings()

  private val firebaseKv = new FirebasePushCredentialsKV()(context.system)

  init()

  def receive = initializing

  def initializing: Receive = {
    case Initialized(creds) ⇒
      unstashAll()
      context become initialized
      mapping = creds.toMap
    case Status.Failure(e) ⇒
      log.error(e, "Failed to init")
      throw e
    case msg ⇒ stash()
  }

  def initialized = commands orElse internal

  def commands: Receive = {
    case r: RegisterPushCredentials if r.creds.isActor ⇒
      register(r.getActor)
    case r: RegisterPushCredentials if r.creds.isApple ⇒
      register(r.getApple)
    case r: RegisterPushCredentials if r.creds.isGcm ⇒
      register(r.getGcm)
    case r: RegisterPushCredentials if r.creds.isFirebase ⇒
      register(r.getFirebase)
    case u: UnregisterPushCredentials if u.creds.isActor ⇒
      unregister(u.getActor)
    case u: UnregisterPushCredentials if u.creds.isApple ⇒
      unregister(u.getApple)
    case u: UnregisterPushCredentials if u.creds.isGcm ⇒
      unregister(u.getGcm)
    case u: UnregisterPushCredentials if u.creds.isFirebase ⇒
      unregister(u.getFirebase)
    case DeliverPush(authId, seq, rules) ⇒
      deliver(authId, seq, rules.getOrElse(PushRules()))
    case r: ReloadSettings ⇒
      settingsControl forward r
  }

  def internal: Receive = {
    case n: AllNotificationSettings ⇒
      this.notificationSettings = n
    case (c: PushCredentials, info: PushCredentialsInfo) ⇒
      mapping += (c → info)
  }

  private def init(): Unit = {
    log.debug("Initializing")
    (for {
      modelAuthIds ← db.run(AuthIdRepo.findByUserId(userId))
      authIds = (modelAuthIds map (_.id)).toSet
      gcmCreds ← db.run(GooglePushCredentialsRepo.find(authIds))
      firebaseCreds ← firebaseKv.find(authIds)
      appleCreds ← db.run(ApplePushCredentialsRepo.find(authIds))
      actorCreds ← db.run(ActorPushCredentialsRepo.find(authIds))

      gcm ← FutureExt.ftraverse(gcmCreds)(withInfo) map (_.flatten)
      firebase ← FutureExt.ftraverse(firebaseCreds)(withInfo) map (_.flatten)
      apple ← FutureExt.ftraverse(appleCreds.filterNot(_.isVoip))(withInfo) map (_.flatten)
      actor ← FutureExt.ftraverse(actorCreds)(withInfo) map (_.flatten)
    } yield Initialized(apple ++ gcm ++ actor)) pipeTo self
  }

  /**
   * Delivers a push to credentials associated with given `authId` according to push `rules`
   *
   */
  private def deliver(authId: Long, seq: Int, rules: PushRules): Unit = {
    mapping foreach {
      case (creds, info) ⇒
        if (creds.authId == authId) {
          deliver(seq, rules, creds, info)
        }
    }
  }

  /**
   * Delivers to a specific creds according to push rules
   *
   * @param seq
   * @param rules
   * @param creds
   * @param info
   */
  private def deliver(seq: Int, rules: PushRules, creds: PushCredentials, info: PushCredentialsInfo): Unit = {
    val deviceType = DeviceType(info.appId)

    if (rules.excludeAuthIds.contains(info.authId)) {
      log.debug("AuthId is excluded, not pushing")
    } else {
      rules.data match {
        case Some(data) ⇒
          val settings = notificationSettings.specific.getOrElse(deviceType, notificationSettings.generic)

          val isVisible = isNotificationVisible(
            settings,
            notificationSettings.groups,
            data.peer,
            data.isMentioned
          )

          if (isVisible)
            deliverVisible(
              seq = seq,
              creds = creds,
              data = data,
              isTextEnabled = settings.text,
              isSoundEnabled = settings.sound,
              customSound = data.peer flatMap (p ⇒ settings.customSounds.get(p)),
              isVibrationEnabled = settings.vibration
            )
          else
            deliverInvisible(seq, creds)

        case _ ⇒
          log.debug("No text, delivering simple seq")
          deliverInvisible(seq, creds)
      }
    }
  }

  private def isNotificationVisible(
    settings:      NotificationSettings,
    groupSettings: GroupNotificationSettings,
    optPeer:       Option[Peer],
    isMentioned:   Boolean
  ) = {
    (settings.enabled, optPeer) match {
      case (true, Some(peer)) ⇒
        peer.`type` match {
          case PeerType.Group ⇒
            if (groupSettings.enabled) {
              if (groupSettings.onlyMention) {
                if (isMentioned) {
                  log.debug("User is mentioned, notification for group {} will be visible", peer)
                  true
                } else {
                  log.debug("Message without mention, notification for group {} will be visible", peer)
                  false
                }
              } else {
                log.debug("Group notifications are enabled, notification for group {} will be visible", peer)
                true
              }
            } else {
              log.debug("Group notifications are disabled, notification for group {} will be invisible", peer)
              false
            }
          case _ ⇒
            settings.peers.get(peer) match {
              case Some(true) ⇒
                log.debug("Notifications for peer {} are enabled, notification will be visible", peer)
                true
              case Some(false) ⇒
                log.debug("Notifications for peer {} are disabled, notification will be invisible", peer)
                false
              case None ⇒
                log.debug("Notifications for peer {} are not set, notification will be visible", peer)
                true
            }

        }
      case (true, None) ⇒
        log.debug("Notifications are enabled, delivering visible push")
        true
      case (false, _) ⇒
        log.debug("Notifications are disabled, delivering invisible push")
        false
    }
  }

  /**
   * Delivers an invisible push with seq and contentAvailable
   *
   * @param seq
   * @param creds
   */
  private def deliverInvisible(seq: Int, creds: PushCredentials): Unit = {
    creds match {
      case c: GooglePushCredentials ⇒
        googlePushProvider.deliverInvisible(seq, c)
      case c: ApplePushCredentials ⇒
        applePushProvider.deliverInvisible(seq, c)
      case c: ActorPushCredentials ⇒
        actorPushProvider.deliver(seq, c)
    }
  }

  /**
   * Delivers a visible push with seq and (optionally) text, sound, vibration
   *
   * @param seq
   * @param creds
   * @param data
   * @param isTextEnabled
   * @param isSoundEnabled
   * @param isVibrationEnabled
   * @return
   */
  private def deliverVisible(
    seq:                Int,
    creds:              PushCredentials,
    data:               PushData,
    isTextEnabled:      Boolean,
    isSoundEnabled:     Boolean,
    customSound:        Option[String],
    isVibrationEnabled: Boolean
  ) = {
    creds match {
      case c: GooglePushCredentials ⇒
        googlePushProvider.deliverVisible(
          seq = seq,
          creds = c,
          data = data,
          isTextEnabled = isTextEnabled,
          isSoundEnabled = isSoundEnabled,
          isVibrationEnabled = isVibrationEnabled
        )
      case c: ApplePushCredentials ⇒
        applePushProvider.deliverVisible(
          seq = seq,
          creds = c,
          data = data,
          isTextEnabled = isTextEnabled,
          isSoundEnabled = isSoundEnabled,
          customSound = customSound,
          isVibrationEnabled = isVibrationEnabled
        )
      case c: ActorPushCredentials ⇒
        actorPushProvider.deliver(seq, c)
    }
  }

  private def register(creds: PushCredentials): Unit =
    withInfo(creds) map (_.getOrElse(throw new RuntimeException(s"Cannot find appId for $creds"))) pipeTo self

  private def withInfo(c: PushCredentials): Future[Option[(PushCredentials, PushCredentialsInfo)]] =
    db.run(for {
      authSessionOpt ← AuthSessionRepo.findByAuthId(c.authId)
    } yield authSessionOpt map (s ⇒ c → PushCredentialsInfo(s.appId, c.authId)))

  private def remove(creds: PushCredentials): Unit =
    mapping -= creds

  private def unregister(creds: PushCredentials): Unit = {
    val replyTo = sender()
    if (mapping.contains(creds)) {
      remove(creds)
      val removeFu = (creds match {
        case c: GCMPushCredentials      ⇒ db.run(GooglePushCredentialsRepo.deleteByToken(c.regId))
        case c: FirebasePushCredentials ⇒ firebaseKv.deleteByToken(c.regId)
        case c: ApplePushCredentials    ⇒ db.run(ApplePushCredentialsRepo.deleteByToken(c.token.toByteArray))
        case c: ActorPushCredentials    ⇒ db.run(ActorPushCredentialsRepo.deleteByTopic(c.endpoint))
      }) map (_ ⇒ UnregisterPushCredentialsAck()) pipeTo replyTo

      removeFu onFailure {
        case e ⇒
          log.error("Failed to unregister creds: {}", creds)
          replyTo ! Status.Failure(FailedToUnregister)
      }
    } else {
      replyTo ! UnregisterPushCredentialsAck()
    }
  }
}
