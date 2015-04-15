package im.actor.server.api.rpc.service

import scala.concurrent.Await
import scala.concurrent.duration._

import scalaz._, std.either._

import im.actor.api.rpc._
import im.actor.api.rpc.auth.{ ResponseAuth, ResponseSendAuthCode }
import im.actor.server.api.rpc.RpcApiService
import im.actor.server.api.rpc.service.auth.PublicKey
import im.actor.server.push.{ WeakUpdatesManager, SeqUpdatesManager }
import im.actor.server.session.Session
import im.actor.server.{ models, persist }

class AuthServiceSpec extends BaseServiceSuite {
  behavior of "AuthService"

  "SendAuthCode handler" should "respond ok to a request valid number" in s.sendAuthCode.e1

  it should "not fail if number already exists" in (s.sendAuthCode.e1)

  "SignUp handler" should "respond ok to a valid request" in (s.signUp().e1)

  "SignIn handler" should "respond with PhoneNumberUnoccupied if phone is not registered" in (s.signIn().unoccupied)

  it should "respond ok to a valid request" in (s.signIn().valid)

  it should "respond ok to a request with the same public key" in (s.signIn().samePublicKey)

  object s {
    val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
    val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
    val rpcApiService = system.actorOf(RpcApiService.props())
    val sessionRegion = Session.startRegion(Some(Session.props(rpcApiService, seqUpdManagerRegion, weakUpdManagerRegion)))

    implicit val ec = system.dispatcher
    implicit val service = new auth.AuthServiceImpl(sessionRegion)

    object sendAuthCode {
      val authId = createAuthId()(service.db)
      val sessionId = createSessionId()
      val phoneNumber = buildPhone()

      implicit val clientData = ClientData(authId, sessionId, None)

      def e1() = {
        whenReady(service.handleSendAuthCode(phoneNumber, 1, "apiKey")) { resp =>
          resp should matchPattern {
            case Ok(ResponseSendAuthCode(_, false)) =>
          }
        }
      }
    }

    case class signUp() {
      val authId = createAuthId()(service.db)
      val sessionId = createSessionId()
      val phoneNumber = buildPhone()
      val smsHash = getSmsHash(authId, phoneNumber)

      implicit val clientData = ClientData(authId, sessionId, None)

      def e1() = {
        val request = service.handleSignUp(
          phoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = "0000",
          name = "Wayne Brain",
          publicKey = Array(1, 2, 3),
          deviceHash = Array(4, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey",
          isSilent = false
        )

        whenReady(request) { resp =>
          resp should matchPattern {
            case Ok(ResponseAuth(_, _, _)) =>
          }
        }
      }
    }

    case class signIn() {
      val authId = createAuthId()(service.db)
      val sessionId = createSessionId()
      val phoneNumber = buildPhone()

      implicit val clientData = ClientData(authId, sessionId, None)

      def unoccupied() = {
        val smsHash = getSmsHash(authId, phoneNumber)

        val request = service.handleSignIn(
          phoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = "0000",
          publicKey = Array(1, 2, 3),
          deviceHash = Array(4, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey"
        )

        whenReady(request) { resp =>
          resp should matchPattern {
            case -\/(service.Errors.PhoneNumberUnoccupied) =>
          }
        }
      }

      def valid() = {
        createUser(authId, phoneNumber)

        val smsHash = getSmsHash(authId, phoneNumber)
        val keyData = Array(1.toByte, 2.toByte, 3.toByte)

        val request = service.handleSignIn(
          phoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = "0000",
          publicKey = keyData,
          deviceHash = Array(4, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey"
        )

        val rsp = whenReady(request) { resp =>
          resp should matchPattern {
            case Ok(rsp: ResponseAuth) =>
          }

          resp.toOption.get
        }

        val pkHash = PublicKey.keyHash(keyData)

        Await.result(service.db.run(persist.AuthId.find(authId).head), 5.seconds) should ===(models.AuthId(authId, Some(rsp.user.id), Some(pkHash)))
        Await.result(service.db.run(persist.UserPublicKey.find(rsp.user.id, pkHash).headOption), 5.seconds) should matchPattern {
          case Some(_: models.UserPublicKey) =>
        }
      }

      def samePublicKey() = {
        createUser(authId, phoneNumber)

        val smsHash1 = getSmsHash(authId, phoneNumber)

        whenReady(service.handleSignIn(
          phoneNumber = phoneNumber,
          smsHash = smsHash1,
          smsCode = "0000",
          publicKey = Array(1, 2, 3),
          deviceHash = Array(4, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey"
        )) { resp =>
          resp should matchPattern {
            case Ok(rsp: ResponseAuth) =>
          }
        }

        val smsHash2 = getSmsHash(authId, phoneNumber)

        whenReady(service.handleSignIn(
          phoneNumber = phoneNumber,
          smsHash = smsHash2,
          smsCode = "0000",
          publicKey = Array(1, 2, 3),
          deviceHash = Array(5, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey"
        )) { resp =>
          resp should matchPattern {
            case Ok(rsp: ResponseAuth) =>
          }
        }
      }
    }

  }

}
