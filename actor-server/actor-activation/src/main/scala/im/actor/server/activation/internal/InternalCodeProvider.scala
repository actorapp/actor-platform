package im.actor.server.activation.internal

import akka.actor.ActorSystem
import cats.data.{ Xor, XorT }
import im.actor.api.rpc.messaging.ApiTextMessage
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.concurrent.FutureResult
import im.actor.server.acl.ACLUtils
import im.actor.server.activation.common._
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogExtension
import im.actor.server.model.{ AuthEmailTransaction, AuthPhoneTransaction }
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.persist.auth.AuthTransactionRepo
import im.actor.server.persist.presences.UserPresenceRepo
import im.actor.server.persist.{ AuthCodeRepo, UserEmailRepo, UserPhoneRepo }

import scala.concurrent.Future

private[activation] final class InternalCodeProvider(system: ActorSystem)
  extends ActivationProvider
  with CommonAuthCodes
  with FutureResult[String] {

  private val config = InternalActivationConfig.load.getOrElse(throw new RuntimeException("Failed to load InternalActivationConfig"))
  private val onlineTimeWindow = config.onlineWindow.toMillis

  protected val activationConfig: ActivationConfig = ActivationConfig.load.getOrElse(throw new RuntimeException("Failed to load activation config"))
  protected val db = DbExtension(system).db
  protected implicit val ec = system.dispatcher

  override def send(txHash: String, code: Code): Future[Xor[CodeFailure, Unit]] = {
    val response = (for {
      userId ← findUserId(txHash)
      presence ← fromFutureOption("No presence found for user")(db.run(UserPresenceRepo.find(userId)))
      lastSeen ← fromOption("No last seen date for user presence")(presence.lastSeenAt)
      _ ← fromFuture(
        if (wasOnlineRecently(lastSeen.getMillis))
          sendCode(userId, code.code)
        else Future.successful(())
      )
    } yield ()).fold(
      failure ⇒ {
        system.log.debug("Failed to send message via internal code provider: {}", failure)
        Xor.right[CodeFailure, Unit](())
      },
      success ⇒ Xor.right[CodeFailure, Unit](())
    ) recover { case e: RuntimeException ⇒ Xor.left(SendFailure(e.toString)) }
    for {
      resp ← response
      _ ← createAuthCodeIfNeeded(resp, txHash, code.code)
    } yield resp
  }

  // we just validate code here, don't expire it
  override def validate(txHash: String, code: String): Future[ValidationResponse] = {
    val action = for {
      optCode ← AuthCodeRepo.findByTransactionHash(txHash)
      result = optCode map {
        case s if isExpired(s, activationConfig.expiration.toMillis) ⇒ ExpiredCode
        case s if s.code != code ⇒
          if (s.attempts + 1 >= activationConfig.attempts) ExpiredCode else InvalidCode
        case _ ⇒ Validated
      } getOrElse InvalidHash
    } yield result
    db.run(action)
  }

  override def cleanup(txHash: String): Future[Unit] = deleteAuthCode(txHash)

  private def sendCode(userId: Int, code: String): Future[Unit] = {
    val messageText = config.messageTemplate.replace("$$CODE$$", code)
    val userPeer = ApiPeer(ApiPeerType.Private, userId)
    val message = ApiTextMessage(messageText, Vector.empty, None)
    DialogExtension(system).sendMessage(
      peer = userPeer,
      senderUserId = config.senderUserId,
      senderAuthSid = 0,
      senderAuthId = None,
      randomId = ACLUtils.randomLong(),
      message = message,
      accessHash = None,
      isFat = false
    ) map { _ ⇒ system.log.debug("Successfully sent activation code to user: {}", userId) }
  }

  private def wasOnlineRecently(lastSeenMillis: Long): Boolean =
    (lastSeenMillis + onlineTimeWindow) > System.currentTimeMillis

  private def findUserId(txHash: String): XorT[Future, String, Int] =
    for {
      tx ← fromFutureOption("No auth transaction found")(db.run(AuthTransactionRepo.findChildren(txHash)))
      userId ← fromFutureOption("User does not exist(possibly it is new user)")(db.run(tx match {
        case phone: AuthPhoneTransaction ⇒ UserPhoneRepo.findByPhoneNumber(phone.phoneNumber).headOption map (_.map(_.userId))
        case email: AuthEmailTransaction ⇒ UserEmailRepo.find(email.email) map (_.map(_.userId))
        case _                           ⇒ DBIO.successful(None)
      }))
    } yield userId

}
