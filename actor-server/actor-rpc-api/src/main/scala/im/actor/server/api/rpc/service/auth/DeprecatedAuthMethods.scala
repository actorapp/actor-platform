package im.actor.server.api.rpc.service.auth

import im.actor.api.rpc.ClientData
import im.actor.api.rpc.auth.{ ResponseAuth, ResponseSendAuthCodeObsolete }
import im.actor.api.rpc.misc.ResponseVoid

import scala.concurrent.Future

trait DeprecatedAuthMethods {
  self: AuthServiceImpl ⇒

  private val deprecationException = new scala.RuntimeException("Deprecated method")

  @deprecated("schema api changes", "2015-06-09")
  override def doHandleSendAuthCodeObsolete(
    rawPhoneNumber: Long,
    appId:          Int,
    apiKey:         String,
    clientData:     ClientData
  ): Future[HandlerResult[ResponseSendAuthCodeObsolete]] = Future.failed(deprecationException)

  @deprecated("schema api changes", "2015-06-09")
  override def doHandleSendAuthCallObsolete(
    phoneNumber: Long,
    smsHash:     String,
    appId:       Int,
    apiKey:      String,
    clientData:  ClientData
  ): Future[HandlerResult[ResponseVoid]] = Future.failed(deprecationException)

  @deprecated("schema api changes", "2015-06-09")
  override def doHandleSignInObsolete(
    rawPhoneNumber: Long,
    smsHash:        String,
    smsCode:        String,
    deviceHash:     Array[Byte],
    deviceTitle:    String,
    appId:          Int,
    appKey:         String,
    clientData:     ClientData
  ): Future[HandlerResult[ResponseAuth]] = Future.failed(deprecationException)

  @deprecated("schema api changes", "2015-06-09")
  override def doHandleSignUpObsolete(
    rawPhoneNumber: Long,
    smsHash:        String,
    smsCode:        String,
    name:           String,
    deviceHash:     Array[Byte],
    deviceTitle:    String,
    appId:          Int,
    appKey:         String,
    isSilent:       Boolean,
    clientData:     ClientData
  ): Future[HandlerResult[ResponseAuth]] = Future.failed(deprecationException)

}
