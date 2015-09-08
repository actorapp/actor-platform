package im.actor.server.sequence

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import com.relayrides.pushy.apns.util.{ ApnsPayloadBuilder, SimpleApnsPushNotification }
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.{ models, persist }

private[sequence] class ApplePusher(pushManager: ApplePushManager, db: Database)(implicit system: ActorSystem) extends VendorPush {
  private implicit val ec: ExecutionContext = system.dispatcher

  def deliverApplePush(creds: models.push.ApplePushCredentials, authId: Long, seq: Int, textOpt: Option[String], originPeerOpt: Option[ApiPeer], unreadCount: Int): Unit = {
    val paramBase = "category.mobile.notification"

    system.log.debug("Delivering apple push, authId: {}, seq: {}, text: {}, originPeer: {}", authId, seq, textOpt, originPeerOpt)

    val builder = new ApnsPayloadBuilder

    val action = (textOpt, originPeerOpt) match {
      case (Some(text), Some(originPeer)) ⇒
        persist.AuthId.findUserId(authId) flatMap {
          case Some(userId) ⇒
            val peerStr = originPeer.`type` match {
              case ApiPeerType.Private ⇒ s"PRIVATE_${originPeer.id}"
              case ApiPeerType.Group   ⇒ s"GROUP_${originPeer.id}"
            }

            system.log.debug(s"Loading params ${paramBase}")

            persist.configs.Parameter.findValue(userId, s"${paramBase}.chat.${peerStr}.enabled") flatMap {
              case Some("false") ⇒
                system.log.debug("Notifications disabled")
                DBIO.successful(builder)
              case _ ⇒
                system.log.debug("Notifications enabled")
                for {
                  soundEnabled ← persist.configs.Parameter.findValue(userId, s"${paramBase}.sound.enabled") map (_.getOrElse("true"))
                  vibrationEnabled ← persist.configs.Parameter.findValue(userId, s"${paramBase}.vibration.enabled") map (_.getOrElse("true"))
                  showText ← getShowText(userId, paramBase)
                } yield {
                  if (soundEnabled == "true") {
                    system.log.debug("Sound enabled")
                    builder.setSoundFileName("iapetus.caf")
                  } else if (vibrationEnabled == "true") {
                    system.log.debug("Sound disabled, vibration enabled")
                    builder.setSoundFileName("silence.caf")
                  }

                  if (showText) {
                    system.log.debug("Text enabled")
                    builder.setAlertBody(text)
                  }
                  builder.setBadgeNumber(unreadCount)

                  builder
                }
            }
          case None ⇒ DBIO.successful(builder) // TODO: fail?
        }
      case (Some(text), None) ⇒
        builder.setAlertBody(text)
        DBIO.successful(builder)
      case _ ⇒ DBIO.successful(builder)
    }

    db.run(action) foreach { b ⇒
      builder.addCustomProperty("seq", seq)
      builder.setContentAvailable(true)

      val payload = builder.buildWithDefaultMaximumLength()

      pushManager.getInstance(creds.apnsKey) foreach { mgr ⇒
        mgr.getQueue.put(new SimpleApnsPushNotification(creds.token, payload))
      }
    }
  }

}