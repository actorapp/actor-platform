package im.actor.server.api.rpc.service

import im.actor.api.{rpc => api}
import im.actor.server.SqlSpecHelpers
import im.actor.util.testing._

import scala.concurrent._

class AuthServiceSpec extends ActorSpecification with SqlSpecHelpers with ServiceSpecHelpers with HandlerMatchers {
  def is = sequential^s2"""

  AuthService
    SendAuthCode $sendAuthCode

    SignUp       $signUp

    SignIn       $signIn
                 """

  def sendAuthCode = s2"""
    SendAuthCode handler should
      respond ok to valid number ${s.sendAuthCode.e1}
      not fail if number already exists ${s.sendAuthCode.e1}
                       """

  def signUp       = s2"""
    SignUp handler should
      respond ok to a valid request ${s.signUp().e1}
                     """

  def signIn       = s2"""
    SignIn handler should
      respond with PhoneNumberUnoccupied if phone is not registered ${s.signIn().unoccupied}
      respond ok to a valid request ${s.signIn().valid}
                     """

  object s {
    implicit val service = new auth.AuthServiceImpl {
      override implicit val ec: ExecutionContext = system.dispatcher
      override implicit val actorSystem = system

      val db = migrateAndInitDb()
    }

    implicit val ec = system.dispatcher

    object sendAuthCode {
      val authId = createAuthId(service.db)
      val phoneNumber = buildPhone()

      def e1 = {
        service.handleSendAuthCode(authId, None, phoneNumber, 1, "apiKey") must beOkLike {
          case (api.auth.ResponseSendAuthCode(_, false), Vector()) => ok
        }.await
      }
    }

    case class signUp()  {
      val authId = createAuthId(service.db)
      val phoneNumber = buildPhone()
      val smsHash = getSmsHash(authId, phoneNumber)(service)

      def e1 = {
        service.handleSignUp(
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
        ) must beOkLike {
          case (api.auth.ResponseAuth(_, _, _), Vector()) => ok
        }.await
      }
    }

    case class signIn() {
      val authId = createAuthId(service.db)
      val phoneNumber = buildPhone()

      def unoccupied = {
        val smsHash = getSmsHash(authId, phoneNumber)(service)

        service.handleSignIn(
          authId = authId,
          optUserId = None,
          rawPhoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = "0000",
          publicKey = Array(1, 2, 3),
          deviceHash = Array(4, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey"
        ) must beErrorLike {
          case api.Errors.PhoneNumberUnoccupied => ok
        }.await
      }

      def valid = {
        createUser(authId, phoneNumber)

        val smsHash = getSmsHash(authId, phoneNumber)(service)

        service.handleSignIn(
          authId = authId,
          optUserId = None,
          rawPhoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = "0000",
          publicKey = Array(1, 2, 3),
          deviceHash = Array(4, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey"
        ) must beOkLike {
          case (_: api.auth.ResponseAuth, Vector()) => ok
        }.await
      }
    }
  }
}
