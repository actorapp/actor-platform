package im.actor.server.api.rpc.service

import im.actor.api.{rpc => api}
import im.actor.api.rpc.auth._
import im.actor.api.rpc.misc._
import im.actor.server.SqlSpecHelpers
import im.actor.util.testing._

import scala.concurrent._, duration._

import org.specs2._

class AuthServiceSpec extends ActorSpecification with SqlSpecHelpers with ServiceSpecHelpers {
  def is = sequential^s2"""

  AuthService
    1. SendAuthCode $sendAuthCode

    2. SignUp       $signUp
                 """

  def sendAuthCode = s2"""
    SendAuthCode handler should
      respond ok to valid number ${service.sendAuthCode.e1}
      not fail if number already exists ${service.sendAuthCode.e1}
                       """

  def signUp       = s2"""
    SendAuthCode handler should
      respond ok to a valid request ${service.signUp().e1}
                     """


  object service extends auth.AuthServiceImpl {
    override implicit val ec: ExecutionContext = system.dispatcher
    override implicit val actorSystem = system

    val db = migrateAndInitDb()

    object sendAuthCode {
      val authId = createAuthId(db)
      val phoneNumber = buildPhone()

      def e1 = {
        handleSendAuthCode(authId, None, phoneNumber, 1, "apiKey") must beLike[HandlerResult[ResponseSendAuthCode]] {
          case api.Ok(api.auth.ResponseSendAuthCode(_, false), Vector()) => ok
        }.await
      }
    }

    case class signUp()  {
      val authId = createAuthId(db)
      val phoneNumber = buildPhone()

      val api.auth.ResponseSendAuthCode(smsHash, _) =
        Await.result(handleSendAuthCode(authId, None, phoneNumber, 1, "apiKey"), 1.second).toOption.get._1

      def e1 = {
        handleSignUp(
          authId = authId,
          optUserId = None,
          rawPhoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = "0000",
          name = "Wayne Brain",
          publicKey = Array(1, 2, 3),
          deviceHash = Array(4, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey",
          isSilent = false
        ) must beLike[HandlerResult[ResponseAuth]] {
          case api.Ok(api.auth.ResponseAuth(_, _, _), Vector()) => ok
        }.await
      }
    }
  }
}
