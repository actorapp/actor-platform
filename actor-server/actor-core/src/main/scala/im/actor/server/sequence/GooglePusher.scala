package im.actor.server.sequence

import scala.concurrent._

import akka.actor.ActorSystem
import com.google.android.gcm.server.Message
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.peers.ApiPeer
import im.actor.server.{ models, persist }

// FIXME: #perf pinned dispatcher
private[sequence] class GooglePusher(pushManager: GooglePushManager, db: Database)(implicit system: ActorSystem) extends VendorPush {
  implicit val ec: ExecutionContext = system.dispatcher

  def deliverGooglePush(creds: models.push.GooglePushCredentials, authId: Long, seq: Int, textOpt: Option[String], originPeerOpt: Option[ApiPeer]): Unit = {
    pushManager.getInstance(creds.projectId) match {
      case Some(gcmSender) ⇒
        system.log.debug("Delivering google push, authId: {}, seq: {}", authId, seq)

        val builder = (new Message.Builder)
          .collapseKey(authId.toString)
          .addData("seq", seq.toString)

        val messageAction = textOpt match {
          case Some(text) ⇒
            persist.AuthIdRepo.findUserId(authId) flatMap {
              case Some(userId) ⇒
                persist.AuthSessionRepo.findAppIdByAuthId(authId) flatMap {
                  case Some(appId) ⇒
                    val category = models.AuthSession.appCategory(appId)
                    val paramBase = s"category.${category}.notification"

                    (originPeerOpt match {
                      case Some(originPeer) ⇒
                        getChatNotificationEnabled(userId, paramBase, originPeer)
                      case None ⇒ DBIO.successful(true)
                    }) flatMap {
                      case true ⇒
                        for {
                          showText ← getShowText(userId, paramBase)
                        } yield {
                          if (showText) {
                            builder.addData("message", text)
                          }

                          builder.build()
                        }
                      case false ⇒ DBIO.successful(builder.build())
                    }
                  case None ⇒ DBIO.successful(builder.build())
                }
              case None ⇒ DBIO.successful(builder.build())
            }
          case None ⇒ DBIO.successful(builder.build())
        }

        db.run(for {
          message ← messageAction
        } yield {
          system.log.debug("Delivering google push message, authId: {}, message: {}", authId, message.toString)

          val resultFuture = Future { blocking { gcmSender.send(message, creds.regId, 3) } }

          resultFuture.map { result ⇒
            system.log.debug("Google push result messageId: {}, error: {}", result.getMessageId, result.getErrorCodeName)
          }.onFailure {
            case e ⇒ system.log.error(e, "Failed to deliver google push")
          }
        })
      case None ⇒
        system.log.error("Key not found for projectId {}", creds.projectId)
    }
  }
}