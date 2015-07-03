package im.actor.server.api.rpc.service

import java.net.URLEncoder
import java.time.{ LocalDateTime, ZoneOffset }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random
import scalaz.\/

import akka.contrib.pattern.DistributedPubSubExtension
import com.google.protobuf.CodedInputStream
import org.scalatest.Inside._

import im.actor.api.rpc._
import im.actor.api.rpc.auth._
import im.actor.api.rpc.contacts.UpdateContactRegistered
import im.actor.api.rpc.users.{ ContactRecord, ContactType, Sex }
import im.actor.server.activation.DummyActivationContext
import im.actor.server.api.rpc.RpcApiService
import im.actor.server.api.rpc.service.auth.{ AuthErrors, AuthConfig }
import im.actor.server.api.rpc.service.sequence.SequenceServiceImpl
import im.actor.server.models.contact.UserContact
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol.{ MessageBox, SessionHello }
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.persist.auth.AuthTransaction
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.push.WeakUpdatesManager
import im.actor.server.session.SessionMessage._
import im.actor.server.session.{ Session, SessionConfig }
import im.actor.server.social.SocialManager
import im.actor.server.{ BaseAppSuite, persist }

class AuthServiceSpec extends BaseAppSuite {
  behavior of "AuthService"

  //phone part
  "StartPhoneAuth handler" should "respond with ok to correct phone number" in s.e1

  it should "respond with ok to phone number of registered user" in s.e2

  it should "respond with error to invalid phone number" in s.e3

  it should "respond with same transactionHash when called multiple times" in s.e33

  "ValidateCode handler" should "respond with error to invalid transactionHash" in s.e4

  it should "respond with error to wrong sms auth code" in s.e5

  it should "respond with error to expired sms auth code" in s.e50

  it should "respond with PhoneNumberUnoccupied error when new user code validation succeed" in s.e6

  it should "complete sign in process for registered user" in s.e7

  it should "invalidate auth code after number attempts given in config" in s.e70

  "SignUp handler" should "respond with error if it was called before validateCode" in s.e8

  it should "complete sign up process for unregistered user" in s.e9

  it should "register unregistered contacts and send updates" in s.e90

  "AuthTransaction and AuthSmsCode" should "be invalidated after sign in process successfully completed" in s.e10

  it should "be invalidated after sign up process successfully completed" in s.e11

  //email part
  "StartEmailAuth handler" should "respond with ok to correct email address" in s.e12

  //  it should "respond with ok to email of registered user" in s.e13

  it should "respond with error to malformed email address" in s.e14

  it should "respond with same transactionHash when called multiple times" in s.e15

  "GetOAuth2Params handler" should "respond with error when malformed url is passed" in s.e16

  it should "respond with error when wrong transactionHash is passed" in s.e17

  it should "respond with correct authUrl on correct request" in s.e18

  "CompleteOAuth2 handler" should "respond with error when wrong transactionHash is passed" in s.e19

  it should "respond with EmailUnoccupied error when new user oauth token retreived" in s.e20

  it should "respond with error when unable to get oauth2 token" in s.e200

  //  it should "complete sign in process for registered user" in s.e21

  "SignUp handler" should "respond with error if it was called before completeOAuth2" in s.e22

  it should "complete sign up process for unregistered user via email oauth" in s.e23

  it should "register unregistered contacts and send updates for email auth" in s.e24

  object s {
    implicit val ec = system.dispatcher

    implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
    implicit val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
    implicit val presenceManagerRegion = PresenceManager.startRegion()
    implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
    implicit val socialManagerRegion = SocialManager.startRegion()

    val mediator = DistributedPubSubExtension(system).mediator
    implicit val sessionConfig = SessionConfig.load(system.settings.config.getConfig("session"))
    Session.startRegion(Some(Session.props(mediator)))
    implicit val sessionRegion = Session.startRegionProxy()

    val oauthGoogleConfig = DummyOAuth2Server.config
    val authConfig = AuthConfig.fromConfig(system.settings.config.getConfig("auth"))
    implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
    implicit val service = new auth.AuthServiceImpl(new DummyActivationContext, mediator, authConfig)
    implicit val rpcApiService = system.actorOf(RpcApiService.props(Seq(service)))
    val sequenceService = new SequenceServiceImpl

    val correctUri = "https://actor.im/registration"
    val correctAuthCode = "0000"
    val gmail = "gmail.com"

    DummyOAuth2Server.start()

    def e1() = {
      val phoneNumber = buildPhone()
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
        inside(resp) {
          case Ok(ResponseStartPhoneAuth(hash, false)) ⇒ hash should not be empty
        }
      }
    }

    def e2() = {
      val (user, authId, phoneNumber) = createUser()
      implicit val clientData = ClientData(authId, createSessionId(), Some(user.id))

      whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
        inside(resp) {
          case Ok(ResponseStartPhoneAuth(hash, true)) ⇒ hash should not be empty
        }
      }
    }

    def e3() = {
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      whenReady(startPhoneAuth(2)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.PhoneNumberInvalid) ⇒
        }
      }
    }

    def e33() = {
      val phoneNumber = buildPhone()
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false)) ⇒ }
          resp.toOption.get.transactionHash
        }

      val seq = Future.sequence(List(
        startPhoneAuth(phoneNumber),
        startPhoneAuth(phoneNumber),
        startPhoneAuth(phoneNumber),
        startPhoneAuth(phoneNumber)
      ))

      whenReady(seq) { resps ⇒
        resps foreach {
          inside(_) {
            case Ok(ResponseStartPhoneAuth(hash, false)) ⇒
              hash shouldEqual transactionHash
          }
        }
      }
    }

    def e4() = {
      val phoneNumber = buildPhone()
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
        resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false)) ⇒ }
      }

      whenReady(service.handleValidateCode("wrongHash123123", correctAuthCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.InvalidAuthTransaction) ⇒
        }
      }
    }

    def e5() = {
      val phoneNumber = buildPhone()
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)
      val wrongCode = "12321"

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false)) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleValidateCode(transactionHash, wrongCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.PhoneCodeInvalid) ⇒
        }
      }
    }

    def e50() = {
      import im.actor.server.db.ActorPostgresDriver.api._

      val phoneNumber = buildPhone()
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false)) ⇒ }
          resp.toOption.get.transactionHash
        }

      val dateUpdate =
        persist.AuthCode.codes
          .filter(_.transactionHash === transactionHash)
          .map(_.createdAt)
          .update(LocalDateTime.now(ZoneOffset.UTC).minusHours(25))
      whenReady(db.run(dateUpdate))(_ ⇒ ())

      whenReady(service.handleValidateCode(transactionHash, correctAuthCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.PhoneCodeExpired) ⇒
        }
      }

      whenReady(db.run(AuthTransaction.find(transactionHash))) { _ shouldBe empty }
      whenReady(db.run(persist.AuthCode.findByTransactionHash(transactionHash))) { _ shouldBe empty }
    }

    def e6() = {
      val phoneNumber = buildPhone()
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false)) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleValidateCode(transactionHash, correctAuthCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.PhoneNumberUnoccupied) ⇒
        }
      }

      whenReady(db.run(persist.auth.AuthTransaction.find(transactionHash))) { optCache ⇒
        optCache should not be empty
        optCache.get.isChecked shouldEqual true
      }
    }

    def e7() = {
      val (user, authId, phoneNumber) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(user.id))

      sendSessionHello(authId, sessionId)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, true)) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleValidateCode(transactionHash, correctAuthCode)) { resp ⇒
        inside(resp) {
          case Ok(ResponseAuth(respUser, _)) ⇒
            respUser.name shouldEqual user.name
            respUser.sex shouldEqual user.sex
        }
      }
    }

    def e70() = {
      val phoneNumber = buildPhone()
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)
      val wrongCode = "12321"

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false)) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleValidateCode(transactionHash, wrongCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.PhoneCodeInvalid) ⇒
        }
      }
      whenReady(service.handleValidateCode(transactionHash, wrongCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.PhoneCodeInvalid) ⇒
        }
      }
      whenReady(service.handleValidateCode(transactionHash, wrongCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.PhoneCodeInvalid) ⇒
        }
      }
      //after code invalidation we remove authCode and AuthTransaction, thus we got InvalidAuthTransaction error
      whenReady(service.handleValidateCode(transactionHash, correctAuthCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.InvalidAuthTransaction) ⇒
        }
      }
      whenReady(db.run(persist.AuthCode.findByTransactionHash(transactionHash))) { code ⇒
        code shouldBe empty
      }
      whenReady(db.run(persist.auth.AuthTransaction.find(transactionHash))) { transaction ⇒
        transaction shouldBe empty
      }
    }

    def e8() = {
      val phoneNumber = buildPhone()
      val userName = "Rock Jam"
      val userSex = Some(Sex.Male)
      val authId = createAuthId()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, None)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false)) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleSignUp(transactionHash, userName, userSex)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.NotValidated) ⇒
        }
      }

      whenReady(db.run(persist.auth.AuthTransaction.find(transactionHash))) { optCache ⇒
        optCache should not be empty
        optCache.get.isChecked shouldEqual false
      }
    }

    def e9() = {
      val phoneNumber = buildPhone()
      val userName = "Rock Jam"
      val userSex = Some(Sex.Male)
      val authId = createAuthId()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, None)

      sendSessionHello(authId, sessionId)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false)) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleValidateCode(transactionHash, correctAuthCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.PhoneNumberUnoccupied) ⇒
        }
      }

      whenReady(service.handleSignUp(transactionHash, userName, userSex)) { resp ⇒
        inside(resp) {
          case Ok(ResponseAuth(user, _)) ⇒
            user.name shouldEqual userName
            user.sex shouldEqual userSex
            user.phone shouldEqual Some(phoneNumber)
            user.contactInfo should have length 1
            user.contactInfo.head should matchPattern {
              case ContactRecord(ContactType.Phone, None, Some(phone), Some(_), None) ⇒
            }
        }
      }
    }

    def e90() = {
      val phoneNumber = buildPhone()
      val userName = "Rock Jam"
      val userSex = Some(Sex.Male)
      val authId = createAuthId()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, None)

      //make unregistered contact
      val (regUser, regAuthId, _) = createUser()
      whenReady(db.run(persist.contact.UnregisteredPhoneContact.createIfNotExists(phoneNumber, regUser.id, Some("Local name"))))(_ ⇒ ())
      val regClientData = ClientData(regAuthId, sessionId, Some(regUser.id))

      sendSessionHello(authId, sessionId)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false)) ⇒ }
          resp.toOption.get.transactionHash
        }
      whenReady(service.handleValidateCode(transactionHash, correctAuthCode))(_ ⇒ ())
      val user = whenReady(service.handleSignUp(transactionHash, userName, userSex))(_.toOption.get.user)

      Thread.sleep(1000)

      whenReady(sequenceService.jhandleGetDifference(0, Array.empty, regClientData)) { result ⇒
        val resp = result.toOption.get
        val updates = resp.updates

        updates should have length 1
        val message = UpdateContactRegistered.parseFrom(CodedInputStream.newInstance(updates.head.update))
        message should matchPattern {
          case Right(UpdateContactRegistered(_, _, _, _)) ⇒
        }
      }
      whenReady(db.run(persist.contact.UnregisteredPhoneContact.find(phoneNumber))) { _ shouldBe empty }
      whenReady(db.run(persist.contact.UserContact.find(regUser.id, user.id))) { optContact ⇒
        optContact should not be empty
        optContact.get should matchPattern {
          case UserContact(_, _, Some(_), _, false) ⇒
        }
      }
    }

    def e10() = {
      val (user, authId, phoneNumber) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(user.id))

      sendSessionHello(authId, sessionId)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, true)) ⇒ }
          resp.toOption.get.transactionHash
        }
      whenReady(service.handleValidateCode(transactionHash, correctAuthCode)) { resp ⇒
        inside(resp) { case Ok(ResponseAuth(respUser, _)) ⇒ }
      }

      whenReady(db.run(persist.auth.AuthTransaction.find(transactionHash))) { _ shouldBe empty }
      whenReady(db.run(persist.AuthCode.findByTransactionHash(transactionHash))) { _ shouldBe empty }
    }

    def e11() = {
      val phoneNumber = buildPhone()
      val userName = "Rock Jam"
      val userSex = Some(Sex.Male)
      val authId = createAuthId()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, None)

      sendSessionHello(authId, sessionId)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false)) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleValidateCode(transactionHash, correctAuthCode)) { resp ⇒
        inside(resp) { case Error(AuthErrors.PhoneNumberUnoccupied) ⇒ }
      }

      whenReady(service.handleSignUp(transactionHash, userName, userSex)) { resp ⇒
        inside(resp) { case Ok(ResponseAuth(user, _)) ⇒ }
      }

      whenReady(db.run(persist.auth.AuthTransaction.find(transactionHash))) { _ shouldBe empty }
      whenReady(db.run(persist.AuthCode.findByTransactionHash(transactionHash))) { _ shouldBe empty }
    }

    def e12() = {
      val email = buildEmail(gmail)
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      whenReady(startEmailAuth(email)) { resp ⇒
        inside(resp) {
          case Ok(ResponseStartEmailAuth(hash, false, EmailActivationType.OAUTH2)) ⇒
            hash should not be empty
        }
      }
    }

    def e13() = {}

    def e14() = {
      val malformedEmail = "foo@bar"
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      whenReady(startEmailAuth(malformedEmail)) { resp ⇒
        inside(resp) {
          case Error(err) ⇒ err.tag shouldEqual "EMAIL_INVALID"
        }
      }
    }

    def e15() = {
      val email = buildEmail(gmail)
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, EmailActivationType.OAUTH2)) ⇒ }
          resp.toOption.get.transactionHash
        }

      val seq = Future.sequence(List(
        startEmailAuth(email),
        startEmailAuth(email),
        startEmailAuth(email),
        startEmailAuth(email),
        startEmailAuth(email),
        startEmailAuth(email)
      ))

      whenReady(seq) { resps ⇒
        resps foreach {
          inside(_) {
            case Ok(ResponseStartEmailAuth(hash, false, EmailActivationType.OAUTH2)) ⇒
              hash shouldEqual transactionHash
          }
        }
      }
    }

    def e16() = {
      val email = buildEmail(gmail)
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)
      val malformedUri = "ht    :/asda.rr/123"

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, EmailActivationType.OAUTH2)) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleGetOAuth2Params(transactionHash, malformedUri)) { resp ⇒
        inside(resp) { case Error(AuthErrors.RedirectUrlInvalid) ⇒ }
      }
    }

    def e17() = {
      val email = buildEmail(gmail)
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      whenReady(startEmailAuth(email)) { resp ⇒
        resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, EmailActivationType.OAUTH2)) ⇒ }
      }

      whenReady(service.handleGetOAuth2Params("wrongHash22aksdl320d3", correctUri)) { resp ⇒
        inside(resp) { case Error(AuthErrors.InvalidAuthTransaction) ⇒ }
      }
    }

    def e18() = {
      val email = buildEmail(gmail)
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, EmailActivationType.OAUTH2)) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleGetOAuth2Params(transactionHash, correctUri)) { resp ⇒
        inside(resp) {
          case Ok(ResponseGetOAuth2Params(url)) ⇒
            url should not be empty
            url should include(oauthGoogleConfig.authUri)
            url should include(URLEncoder.encode(correctUri, "utf-8"))
        }
      }

      whenReady(db.run(persist.auth.AuthEmailTransaction.find(transactionHash))) { optCache ⇒
        optCache should not be empty
        val cache = optCache.get
        cache.redirectUri shouldEqual Some(correctUri)
      }
    }

    def e19() = {
      val email = buildEmail(gmail)
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, EmailActivationType.OAUTH2)) ⇒ }
          resp.toOption.get.transactionHash
        }
      whenReady(service.handleGetOAuth2Params(transactionHash, correctUri)) { resp ⇒
        inside(resp) { case Ok(ResponseGetOAuth2Params(url)) ⇒ }
      }
      whenReady(service.handleCompleteOAuth2("wrongTransactionHash29191djlksa", "4/YUlNIa55xSZRA4JcQkLzAh749bHAcv96aA-oVMHTQRU")) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.InvalidAuthTransaction) ⇒
        }
      }
    }

    def e20() = {
      val email = buildEmail(gmail)
      DummyOAuth2Server.email = email
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, EmailActivationType.OAUTH2)) ⇒ }
          resp.toOption.get.transactionHash
        }
      whenReady(service.handleGetOAuth2Params(transactionHash, correctUri)) { resp ⇒
        inside(resp) { case Ok(ResponseGetOAuth2Params(url)) ⇒ }
      }
      whenReady(service.handleCompleteOAuth2(transactionHash, "code")) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.EmailUnoccupied) ⇒
        }
      }

      whenReady(db.run(persist.auth.AuthTransaction.find(transactionHash))) { optCache ⇒
        optCache should not be empty
        optCache.get.isChecked shouldEqual true
      }

      whenReady(db.run(persist.OAuth2Token.findByUserId(email))) { optToken ⇒
        optToken should not be empty
        val token = optToken.get
        token.accessToken should not be empty
        token.refreshToken should not be empty
      }

    }

    def e200() = {
      val email = buildEmail(gmail)
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, EmailActivationType.OAUTH2)) ⇒ }
          resp.toOption.get.transactionHash
        }
      whenReady(service.handleGetOAuth2Params(transactionHash, correctUri)) { resp ⇒
        inside(resp) { case Ok(ResponseGetOAuth2Params(url)) ⇒ }
      }
      whenReady(service.handleCompleteOAuth2(transactionHash, "wrongCode")) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.FailedToGetOAuth2Token) ⇒
        }
      }

      whenReady(db.run(persist.auth.AuthTransaction.find(transactionHash))) { optCache ⇒
        optCache should not be empty
        optCache.get.isChecked shouldEqual false
      }
    }

    def e21() = {}

    def e22() = {
      val email = buildEmail(gmail)
      val userName = "Rock Jam"
      val userSex = Some(Sex.Male)
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, EmailActivationType.OAUTH2)) ⇒ }
          resp.toOption.get.transactionHash
        }
      whenReady(service.handleSignUp(transactionHash, userName, userSex)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.NotValidated) ⇒
        }
      }

      whenReady(db.run(persist.auth.AuthTransaction.find(transactionHash))) { optCache ⇒
        optCache should not be empty
        optCache.get.isChecked shouldEqual false
      }
    }

    def e23() = {
      val email = buildEmail(gmail)
      DummyOAuth2Server.email = email
      val userName = "Rock Jam"
      val userSex = Some(Sex.Male)
      val authId = createAuthId()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, None)

      sendSessionHello(authId, sessionId)

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, EmailActivationType.OAUTH2)) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleGetOAuth2Params(transactionHash, correctUri)) { resp ⇒
        inside(resp) { case Ok(ResponseGetOAuth2Params(url)) ⇒ }
      }
      whenReady(service.handleCompleteOAuth2(transactionHash, "code")) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.EmailUnoccupied) ⇒
        }
      }
      val user =
        whenReady(service.handleSignUp(transactionHash, userName, userSex)) { resp ⇒
          inside(resp) {
            case Ok(ResponseAuth(u, _)) ⇒
              u.name shouldEqual userName
              u.sex shouldEqual userSex
              u.contactInfo should have length 1
              u.contactInfo.head should matchPattern {
                case ContactRecord(ContactType.Email, Some(`email`), None, Some(_), None) ⇒
              }
          }
          resp.toOption.get.user
        }
      whenReady(db.run(persist.UserEmail.find(email))) { optEmail ⇒
        optEmail should not be empty
        optEmail.get.userId shouldEqual user.id
      }

      whenReady(db.run(persist.OAuth2Token.findByUserId(email))) { optToken ⇒
        optToken should not be empty
        val token = optToken.get
        token.accessToken should not be empty
        token.refreshToken should not be empty
      }

    }

    def e24() = {
      val email = buildEmail(gmail)
      DummyOAuth2Server.email = email
      val userName = "Rock Jam"
      val userSex = Some(Sex.Male)
      val authId = createAuthId()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, None)

      //make unregistered contact
      val (regUser, regAuthId, _) = createUser()
      whenReady(db.run(persist.contact.UnregisteredEmailContact.createIfNotExists(email, regUser.id, Some("Local name"))))(_ ⇒ ())
      val regClientData = ClientData(regAuthId, sessionId, Some(regUser.id))

      sendSessionHello(authId, sessionId)

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, EmailActivationType.OAUTH2)) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleGetOAuth2Params(transactionHash, correctUri))(_ ⇒ ())
      whenReady(service.handleCompleteOAuth2(transactionHash, "code"))(_ ⇒ ())
      val user = whenReady(service.handleSignUp(transactionHash, userName, userSex))(_.toOption.get.user)

      Thread.sleep(1000)

      whenReady(sequenceService.jhandleGetDifference(0, Array.empty, regClientData)) { result ⇒
        val resp = result.toOption.get
        val updates = resp.updates

        updates should have length 1
        val message = UpdateContactRegistered.parseFrom(CodedInputStream.newInstance(updates.head.update))
        message should matchPattern {
          case Right(UpdateContactRegistered(_, _, _, _)) ⇒
        }
      }
      whenReady(db.run(persist.contact.UnregisteredEmailContact.find(email))) {
        _ shouldBe empty
      }
      whenReady(db.run(persist.contact.UserContact.find(regUser.id, user.id))) { optContact ⇒
        optContact should not be empty
        optContact.get should matchPattern {
          case UserContact(_, _, _, _, false) ⇒
        }
      }
    }

    private def startPhoneAuth(phoneNumber: Long)(implicit clientData: ClientData): Future[\/[RpcError, ResponseStartPhoneAuth]] = {
      service.handleStartPhoneAuth(
        phoneNumber = phoneNumber,
        appId = 42,
        apiKey = "apiKey",
        deviceHash = Random.nextLong().toBinaryString.getBytes,
        deviceTitle = "Specs virtual device"
      )
    }

    private def startEmailAuth(email: String)(implicit clientData: ClientData): Future[\/[RpcError, ResponseStartEmailAuth]] = {
      service.handleStartEmailAuth(
        email = email,
        appId = 42,
        apiKey = "apiKey",
        deviceHash = Random.nextLong().toBinaryString.getBytes,
        deviceTitle = "Specs virtual device"
      )
    }

    private def sendSessionHello(authId: Long, sessionId: Long): Unit = {
      val message = HandleMessageBox(MessageBoxCodec.encode(MessageBox(Random.nextLong(), SessionHello)).require.toByteArray)
      sessionRegion.ref ! envelope(authId, sessionId, message)
    }
  }

}

object DummyOAuth2Server {
  import akka.actor.ActorSystem
  import akka.http.scaladsl.Http
  import akka.http.scaladsl.model.FormData
  import akka.http.scaladsl.server.Directives._
  import akka.http.scaladsl.server.Route
  import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._
  import akka.stream.Materializer
  import org.apache.commons.codec.digest.DigestUtils

  val config = OAuth2GoogleConfig(
    "http://localhost:3000/o/oauth2/auth",
    "http://localhost:3000",
    "http://localhost:3000",
    "actor",
    "AA1865139A1CACEABFA45E6635AA7761",
    "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile"
  )

  var email: String = ""

  private def response() = {
    val bytes = Random.nextLong().toBinaryString.getBytes
    val refreshToken = DigestUtils.md5Hex(bytes)
    val accessToken = DigestUtils.sha256Hex(bytes)
    s"""{"access_token": "$accessToken",
      |  "token_type": "Bearer",
      |  "expires_in": 3600,
      |  "refresh_token": "$refreshToken"}""".stripMargin
  }

  def start()(
    implicit
    system:       ActorSystem,
    materializer: Materializer
  ): Unit = {

    implicit val ec: ExecutionContext = system.dispatcher

    def profile = s"""{"family_name": "Jam",
                    |  "name": "Rock Jam",
                    |  "picture": "https://lh3.googleusercontent.com/-LL4HijQ2VDo/AAAAAAAAAAI/AAAAAAAAAAc/Lo5E9bw1Loc/s170-c-k-no/photo.jpg",
                    |  "locale": "ru",
                    |  "gender": "male",
                    |  "email": "$email",
                    |  "link": "https://plus.google.com/108764816638640823343",
                    |  "given_name": "Rock",
                    |  "id": "108764816638640823343",
                    |  "verified_email": true}""".stripMargin

    def routes: Route =
      post {
        entity(as[FormData]) { data ⇒
          data.fields.get("code") match {
            case Some("wrongCode") ⇒ complete("{}")
            case Some(_)           ⇒ complete(response())
            case None              ⇒ throw new Exception("invalid request!")
          }
        }
      } ~
        get { complete(profile) }

    Http().bind("0.0.0.0", 3000).runForeach { connection ⇒
      connection handleWith Route.handlerFlow(routes)
    }
  }
}
