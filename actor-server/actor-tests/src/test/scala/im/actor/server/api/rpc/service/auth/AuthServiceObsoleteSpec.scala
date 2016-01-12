package im.actor.server.api.rpc.service.auth

import im.actor.api.rpc._
import im.actor.api.rpc.auth.{ ResponseAuth, ResponseSendAuthCodeObsolete }
import im.actor.api.rpc.contacts.UpdateContactRegistered
import im.actor.server._
import im.actor.server.api.rpc.RpcApiService
import im.actor.server.api.rpc.service.auth
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }

import scala.concurrent.Await
import scala.concurrent.duration._
import scalaz._

final class AuthServiceObsoleteSpec extends BaseAppSuite with SeqUpdateMatchers {
  behavior of "Obsolete methods in AuthService"

  "SendAuthCode handler" should "respond ok to a request valid number" in s.sendAuthCode.e1

  it should "not fail if number already exists" in (s.sendAuthCode.e1)

  "SignUp handler" should "respond ok to a valid request" in (s.signUp().e1)

  it should "send ContactRegistered updates" in (s.signUp().e2)

  "SignIn handler" should "respond with PhoneNumberUnoccupied if phone is not registered" in (s.signIn().unoccupied)

  it should "respond ok to a valid request" in (s.signIn().valid)

  it should "logout previous sessions on sign in with the same device hash" in (s.signIn().sameDeviceHash)

  object s {
    implicit val ec = system.dispatcher
    val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
    implicit val sessionRegion = buildSessionRegion()
    implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
    implicit val service = new auth.AuthServiceImpl(new DummyCodeActivation)
    implicit val rpcApiService = system.actorOf(RpcApiService.props(Seq(service)))

    object sendAuthCode {
      val authId = createAuthId()
      val sessionId = createSessionId()
      val phoneNumber = buildPhone()

      implicit val clientData = ClientData(authId, sessionId, None)

      def e1() = {
        whenReady(service.handleSendAuthCodeObsolete(phoneNumber, 1, "apiKey")) { resp ⇒
          resp should matchPattern {
            case Ok(ResponseSendAuthCodeObsolete(_, false)) ⇒
          }
        }
      }
    }

    case class signUp() {
      def e1() = {
        val authId = createAuthId()
        val sessionId = createSessionId()
        val phoneNumber = buildPhone()
        val smsHash = getSmsHash(authId, phoneNumber)

        implicit val clientData = ClientData(authId, sessionId, None)

        val request = service.handleSignUpObsolete(
          phoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = "0000",
          name = "Wayne Brain",
          deviceHash = Array(4, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey",
          isSilent = false
        )

        whenReady(request) { resp ⇒
          resp should matchPattern {
            case Ok(ResponseAuth(_, _)) ⇒
          }
        }
      }

      def e2() = {
        val (user, authId, authSid, _) = createUser()

        val unregPhoneNumber = buildPhone()

        {

          val authId = createAuthId()
          val sessionId = createSessionId()
          val smsHash = getSmsHash(authId, unregPhoneNumber)

          Await.result(db.run(persist.contact.UnregisteredPhoneContactRepo.createIfNotExists(unregPhoneNumber, user.id, Some("Local name"))), 5.seconds)

          implicit val clientData = ClientData(authId, sessionId, None)

          val request = service.handleSignUpObsolete(
            phoneNumber = unregPhoneNumber,
            smsHash = smsHash,
            smsCode = "0000",
            name = "Wayne Brain",
            deviceHash = Array(4, 5, 6),
            deviceTitle = "Specs virtual device",
            appId = 1,
            appKey = "appKey",
            isSilent = false
          )

          whenReady(request) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseAuth(_, _)) ⇒
            }
          }
        }

        implicit val clientData = ClientData(authId, 1L, Some(AuthData(user.id, authSid)))

        expectUpdate(classOf[UpdateContactRegistered])(_ ⇒ ())

        Thread.sleep(1000)

        whenReady(db.run(persist.contact.UnregisteredPhoneContactRepo.find(unregPhoneNumber))) { unregContacts ⇒
          unregContacts shouldBe empty
        }
      }
    }

    case class signIn() {
      def unoccupied() = {
        val authId = createAuthId()
        val sessionId = createSessionId()
        val phoneNumber = buildPhone()

        implicit val clientData = ClientData(authId, sessionId, None)

        val smsHash = getSmsHash(authId, phoneNumber)

        val request = service.handleSignInObsolete(
          phoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = "0000",
          deviceHash = Array(4, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey"
        )

        whenReady(request) { resp ⇒
          resp should matchPattern {
            case -\/(AuthErrors.PhoneNumberUnoccupied) ⇒
          }
        }
      }

      def valid() = {
        val phoneNumber = buildPhone()
        createUser(phoneNumber)

        val authId = createAuthId()
        val sessionId = createSessionId()
        implicit val clientData = ClientData(authId, sessionId, None)

        val smsHash = getSmsHash(authId, phoneNumber)

        val request = service.handleSignInObsolete(
          phoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = "0000",
          deviceHash = Array(4, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey"
        )

        val rsp = whenReady(request) { resp ⇒
          resp should matchPattern {
            case Ok(rsp: ResponseAuth) ⇒
          }

          resp.toOption.get
        }

        Await.result(db.run(persist.AuthIdRepo.find(authId)), 5.seconds) should ===(Some(model.AuthId(authId, Some(rsp.user.id), None)))
      }

      def sameDeviceHash() = {
        val phoneNumber = buildPhone()
        val (user, _) = createUser(phoneNumber)

        val deviceHash = Array[Byte](4, 5, 6)

        val authId1 = createAuthId()

        {
          val sessionId = createSessionId()
          implicit val clientData = ClientData(authId1, sessionId, None)

          val smsHash = getSmsHash(authId1, phoneNumber)

          whenReady(service.handleSignInObsolete(
            phoneNumber = phoneNumber,
            smsHash = smsHash,
            smsCode = "0000",
            deviceHash = deviceHash,
            deviceTitle = "Specs virtual device",
            appId = 1,
            appKey = "appKey"
          )) { resp ⇒
            resp should matchPattern {
              case Ok(rsp: ResponseAuth) ⇒
            }
          }
        }

        val authId2 = createAuthId()

        {
          val sessionId = createSessionId()
          implicit val clientData = ClientData(authId2, sessionId, None)

          val smsHash = getSmsHash(authId2, phoneNumber)

          whenReady(service.handleSignInObsolete(
            phoneNumber = phoneNumber,
            smsHash = smsHash,
            smsCode = "0000",
            deviceHash = deviceHash,
            deviceTitle = "Specs virtual device",
            appId = 1,
            appKey = "appKey"
          )) { resp ⇒
            resp should matchPattern {
              case Ok(rsp: ResponseAuth) ⇒
            }
          }
        }

        whenReady(db.run(persist.AuthIdRepo.findByUserId(user.id))) { authIds ⇒
          val ids = authIds.map(_.id)
          ids should contain(authId2)
          ids shouldNot contain(authId1)
        }

        whenReady(db.run(persist.AuthSessionRepo.findByUserId(user.id))) { sessions ⇒
          val ids = sessions.map(_.authId)
          ids should contain(authId2)
          ids shouldNot contain(authId1)
        }
      }
    }
  }

}
