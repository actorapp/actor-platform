package im.actor.server.sequence

import akka.actor._
import akka.pattern.pipe
import im.actor.server.db.DbExtension
import im.actor.server.model.push.{ ActorPushCredentials, ApplePushCredentials, GooglePushCredentials, PushCredentials }
import im.actor.server.model.{ DeviceType, Peer, PeerType }
import im.actor.server.persist.AuthSessionRepo
import im.actor.server.persist.configs.ParameterRepo
import im.actor.server.persist.push.{ ActorPushCredentialsRepo, ApplePushCredentialsRepo, GooglePushCredentialsRepo }
import im.actor.server.push.actor.ActorPush
import im.actor.server.sequence.UserSequenceCommands.ReloadSettings
import im.actor.server.userconfig.SettingsKeys
import slick.dbio.DBIO

import scala.concurrent.Future
import scala.util.control.NoStackTrace

private[sequence] trait VendorPushCommand

private final case class PushCredentialsInfo(appId: Int, authSid: Int)

private final case class AllNotificationSettings(
  generic:  NotificationSettings              = NotificationSettings(),
  specific: Map[String, NotificationSettings] = Map.empty
)

private final case class NotificationSettings(
  enabled:   Boolean            = true,
  sound:     Boolean            = true,
  vibration: Boolean            = true,
  text:      Boolean            = true,
  peers:     Map[Peer, Boolean] = Map.empty
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
      generic ← loadAction(DeviceType.Generic)
      mobile ← loadAction(DeviceType.Mobile)
      tablet ← loadAction(DeviceType.Tablet)
      desktop ← loadAction(DeviceType.Desktop)
    } yield AllNotificationSettings(
      generic = generic,
      specific = Map(
        DeviceType.Mobile → mobile,
        DeviceType.Tablet → tablet,
        DeviceType.Desktop → desktop
      )
    ))

  private def loadAction(deviceType: String): DBIO[NotificationSettings] = {
    for {
      enabled ← ParameterRepo.findBooleanValue(userId, SettingsKeys.enabled(deviceType), true)
      sound ← ParameterRepo.findBooleanValue(userId, SettingsKeys.soundEnabled(deviceType), true)
      vibration ← ParameterRepo.findBooleanValue(userId, SettingsKeys.vibrationEnabled(deviceType), true)
      text ← ParameterRepo.findBooleanValue(userId, SettingsKeys.textEnabled(deviceType), true)
      peers ← ParameterRepo.findPeerNotifications(userId, deviceType)
    } yield NotificationSettings(enabled, sound, vibration, text, peers.toMap)
  }
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

  private var mapping: Map[PushCredentials, PushCredentialsInfo] = Map.empty
  private var notificationSettings = AllNotificationSettings()

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
    case r: RegisterPushCredentials if r.creds.isGoogle ⇒
      register(r.getGoogle)
    case u: UnregisterPushCredentials if u.creds.isActor ⇒
      unregister(u.getActor)
    case u: UnregisterPushCredentials if u.creds.isApple ⇒
      unregister(u.getApple)
    case u: UnregisterPushCredentials if u.creds.isGoogle ⇒
      unregister(u.getGoogle)
    case DeliverPush(seq, rules) ⇒
      deliver(seq, rules.getOrElse(PushRules()))
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
    db.run(for {
      googleCreds ← GooglePushCredentialsRepo.findByUser(userId)
      appleCreds ← ApplePushCredentialsRepo.findByUser(userId)
      actorCreds ← ActorPushCredentialsRepo.findByUser(userId)
      google ← DBIO.sequence(googleCreds map withInfo) map (_.flatten)
      apple ← DBIO.sequence(appleCreds.filterNot(_.isVoip) map withInfo) map (_.flatten)
      actor ← DBIO.sequence(actorCreds map withInfo) map (_.flatten)
    } yield Initialized(apple ++ google ++ actor)) pipeTo self
  }

  /**
   * Delivers a push to all credentials according to push rules
   *
   * @param seq
   * @param rules
   */
  private def deliver(seq: Int, rules: PushRules): Unit = {
    mapping foreach {
      case (creds, info) ⇒ deliver(seq, rules, creds, info)
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

    if (rules.excludeAuthSids.contains(info.authSid)) {
      log.debug("AuthSid is excluded, not pushing")
    } else {
      rules.data match {
        case Some(data) ⇒
          val settings = notificationSettings.specific.getOrElse(deviceType, notificationSettings.generic)

          val isVisible =
            (settings.enabled, data.peer) match {
              case (true, Some(peer)) ⇒
                settings.peers.get(peer) match {
                  case Some(true) ⇒
                    log.debug("Notifications for peer {} are enabled, push will be visible", peer)
                    true
                  case Some(false) ⇒
                    log.debug("Notifications for peer {} are disabled, push will be invisible", peer)
                    false
                  case None ⇒
                    log.debug("Notifications for peer {} are not set, push will be visible", peer)
                    true
                }
              case (true, None) ⇒
                log.debug("Notifications are enabled, delivering visible push")
                true
              case (false, _) ⇒
                log.debug("Notifications are disabled, delivering invisible push")
                false
            }

          if (isVisible) {
            val isSoundEnabled = settings.sound
            val isTextEnabled = settings.text
            val isVibrationEnabled = settings.vibration

            deliverVisible(
              seq = seq,
              creds = creds,
              data = data,
              isTextEnabled = isTextEnabled,
              isSoundEnabled = isSoundEnabled,
              isVibrationEnabled = isVibrationEnabled
            )
          } else deliverInvisible(seq, creds)

        case _ ⇒
          log.debug("No text, delivering simple seq")
          deliverInvisible(seq, creds)
      }
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
  private def deliverVisible(seq: Int, creds: PushCredentials, data: PushData, isTextEnabled: Boolean, isSoundEnabled: Boolean, isVibrationEnabled: Boolean) = {
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
          isVibrationEnabled = isVibrationEnabled
        )
      case c: ActorPushCredentials ⇒
        actorPushProvider.deliver(seq, c)
    }
  }

  private def register(creds: PushCredentials): Unit =
    db.run {
      withInfo(creds) map (_.getOrElse(throw new RuntimeException(s"Cannot find appId for $creds")))
    } pipeTo self

  private def withInfo(c: PushCredentials): DBIO[Option[(PushCredentials, PushCredentialsInfo)]] =
    for {
      authSessionOpt ← AuthSessionRepo.findByAuthId(c.authId)
    } yield authSessionOpt map (s ⇒ c → PushCredentialsInfo(s.appId, s.id))

  private def remove(creds: PushCredentials): Unit =
    mapping -= creds

  private def unregister(creds: PushCredentials): Unit = {
    val replyTo = sender()
    if (mapping.contains(creds)) {
      remove(creds)
      val removeFu = db.run(creds match {
        case c: GooglePushCredentials ⇒ GooglePushCredentialsRepo.deleteByToken(c.regId)
        case c: ApplePushCredentials  ⇒ ApplePushCredentialsRepo.deleteByToken(c.token.toByteArray)
        case c: ActorPushCredentials  ⇒ ActorPushCredentialsRepo.deleteByTopic(c.endpoint)
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