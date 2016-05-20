package im.actor.server.api.rpc.service.auth

import java.net.URLEncoder
import java.time.{ LocalDateTime, ZoneOffset }

import cats.data.Xor
import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.Int32Value
import im.actor.api.rpc._
import im.actor.api.rpc.auth._
import im.actor.api.rpc.contacts.{ ApiPhoneToImport, ResponseGetContacts, UpdateContactRegistered }
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.users.{ ApiContactRecord, ApiContactType, ApiSex }
import im.actor.concurrent.FutureExt
import im.actor.server._
import im.actor.server.activation.common.ActivationConfig
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.model.contact.UserContact
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol.{ MessageBox, SessionHello }
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.persist.auth.AuthTransactionRepo
import im.actor.server.session.{ HandleMessageBox, Session, SessionConfig, SessionEnvelope }
import im.actor.server.user.UserExtension

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

final class AuthServiceSpec
  extends BaseAppSuite
  with ImplicitSequenceService
  with ImplicitSessionRegion
  with SeqUpdateMatchers {
  behavior of "AuthService"

  //phone part
  "StartPhoneAuth handler" should "respond with ok to correct phone number" in s.e1

  it should "respond with ok to phone number of registered user" in s.e2

  it should "respond with error to invalid phone number" in s.e3

  it should "respond with same transactionHash when called multiple times" in s.e33

  it should "associate authorizations from two different devices with different auth transactions" in s.e34

  it should "not allow deleted user to log in" in s.phoneDeletedUser

  "ValidateCode handler" should "respond with error to invalid transactionHash" in s.e4

  it should "respond with error to wrong sms auth code" in s.e5

  it should "respond with error to expired sms auth code" in s.e50

  it should "respond with PhoneNumberUnoccupied error when new user code validation succeed" in s.e6

  it should "complete sign in process for registered user" in s.e7

  it should "invalidate auth code after number attempts given in config" in s.e70

  "SignUp handler" should "respond with error if it was called before validateCode" in s.e8

  it should "complete sign up process for unregistered user" in s.e9

  it should "register unregistered contacts and send updates" in s.contactRegistered

  it should "register unregistered contacts with local name" in s.unregContactLocalName

  "AuthTransaction and AuthSmsCode" should "be invalidated after sign in process successfully completed" in s.e10

  it should "be invalidated after sign up process successfully completed" in s.e11

  //email part
  "StartEmailAuth handler" should "respond with ok to correct email address" in s.e12

  //  it should "respond with ok to email of registered user" in s.e13

  it should "respond with error to malformed email address" in s.malformedEmail

  it should "respond with same transactionHash when called multiple times" in s.e15

  it should "associate authorizations from two different devices with different auth transactions" in s.e155

  it should "not allow deleted user to log in" in s.emailDeletedUser

  "GetOAuth2Params handler" should "respond with error when malformed url is passed" in pendingUntilFixed(s.e16)

  it should "respond with error when wrong transactionHash is passed" in pendingUntilFixed(s.e17)

  it should "respond with correct authUrl on correct request" in pendingUntilFixed(s.e18)

  "CompleteOAuth2 handler" should "respond with error when wrong transactionHash is passed" in pendingUntilFixed(s.e19)

  it should "respond with EmailUnoccupied error when new user oauth token retreived" in pendingUntilFixed(s.e20)

  it should "respond with error when unable to get oauth2 token" in pendingUntilFixed(s.e200)

  //  it should "complete sign in process for registered user" in s.e21

  "SignUp handler" should "respond with error if it was called before completeOAuth2" in pendingUntilFixed(s.e22)

  it should "complete sign up process for unregistered user via email oauth" in pendingUntilFixed(s.e23)

  it should "register unregistered contacts and send updates for email auth" in pendingUntilFixed(s.e24)

  "Logout" should "remove authId and vendor credentials" in s.e25

  object s {
    implicit val ec = system.dispatcher

    implicit val sessionConfig = SessionConfig.load(system.settings.config.getConfig("session"))
    Session.startRegion(Session.props)
    implicit val sessionRegion = Session.startRegionProxy()

    val oauthGoogleConfig = DummyOAuth2Server.config
    implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
    val activationConfig = ActivationConfig.load.get
    implicit val service = new AuthServiceImpl
    implicit val contactService = new ContactsServiceImpl

    val correctUri = "https://actor.im/registration"
    val correctAuthCode = "0000"
    val gmail = "gmail.com"

    DummyOAuth2Server.start()

    def e1() = {
      val phoneNumber = buildPhone()
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
        inside(resp) {
          case Ok(ResponseStartPhoneAuth(hash, false, Some(ApiPhoneActivationType.CODE))) ⇒ hash should not be empty
        }
      }
    }

    def e2() = {
      val (user, authId, authSid, phoneNumber) = createUser()
      implicit val clientData = ClientData(authId, createSessionId(), Some(AuthData(user.id, authSid, 42)))

      whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
        inside(resp) {
          case Ok(ResponseStartPhoneAuth(hash, true, Some(ApiPhoneActivationType.CODE))) ⇒ hash should not be empty
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

      val deviceHash = Random.nextLong().toBinaryString.getBytes

      def q() = service.handleStartPhoneAuth(
        phoneNumber = phoneNumber,
        appId = 42,
        apiKey = "apiKey",
        deviceHash = deviceHash,
        deviceTitle = "Specs virtual device",
        timeZone = None,
        preferredLanguages = Vector.empty
      )

      val transactionHash =
        whenReady(q()) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false, Some(ApiPhoneActivationType.CODE))) ⇒ }
          resp.toOption.get.transactionHash
        }

      val seq = Future.sequence(List(q(), q(), q(), q()))

      whenReady(seq) { resps ⇒
        resps foreach {
          inside(_) {
            case Ok(ResponseStartPhoneAuth(hash, false, Some(ApiPhoneActivationType.CODE))) ⇒
              hash shouldEqual transactionHash
          }
        }
      }
    }

    def e34() = {
      val phoneNumber = buildPhone()
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash1 = whenReady(service.handleStartPhoneAuth(
        phoneNumber = phoneNumber,
        appId = 42,
        apiKey = "apiKey",
        deviceHash = Random.nextLong().toBinaryString.getBytes,
        deviceTitle = "Specs virtual device",
        timeZone = None,
        preferredLanguages = Vector.empty
      )) { resp ⇒
        resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false, Some(ApiPhoneActivationType.CODE))) ⇒ }
        resp.toOption.get.transactionHash
      }

      val transactionHash2 = whenReady(service.handleStartPhoneAuth(
        phoneNumber = phoneNumber,
        appId = 3,
        apiKey = "someKey",
        deviceHash = Random.nextLong().toBinaryString.getBytes,
        deviceTitle = "Web browser",
        timeZone = None,
        preferredLanguages = Vector.empty
      )) { resp ⇒
        resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false, Some(ApiPhoneActivationType.CODE))) ⇒ }
        resp.toOption.get.transactionHash
      }
      transactionHash1 should not equal transactionHash2
    }

    def phoneDeletedUser() = {
      val (user, authId, authSid, phoneNumber) = createUser()

      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

      whenReady(UserExtension(system).delete(user.id))(identity)

      whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.UserDeleted) ⇒
        }
      }
    }

    def e4() = {
      val phoneNumber = buildPhone()
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
        resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false, Some(ApiPhoneActivationType.CODE))) ⇒ }
      }

      whenReady(service.handleValidateCode("wrongHash123123", correctAuthCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.PhoneCodeExpired) ⇒
        }
      }
    }

    def e5() = {
      val phoneNumber = buildPhone()
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)
      val wrongCode = "12321"

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false, Some(ApiPhoneActivationType.CODE))) ⇒ }
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
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false, Some(ApiPhoneActivationType.CODE))) ⇒ }
          resp.toOption.get.transactionHash
        }

      val dateUpdate =
        persist.AuthCodeRepo.codes
          .filter(_.transactionHash === transactionHash)
          .map(_.createdAt)
          .update(LocalDateTime.now(ZoneOffset.UTC).minusHours(25))
      whenReady(db.run(dateUpdate))(_ ⇒ ())

      whenReady(service.handleValidateCode(transactionHash, correctAuthCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.PhoneCodeExpired) ⇒
        }
      }

      whenReady(db.run(AuthTransactionRepo.find(transactionHash))) {
        _ shouldBe empty
      }
      whenReady(db.run(persist.AuthCodeRepo.findByTransactionHash(transactionHash))) {
        _ shouldBe empty
      }
    }

    def e6() = {
      val phoneNumber = buildPhone()
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false, Some(ApiPhoneActivationType.CODE))) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleValidateCode(transactionHash, correctAuthCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.PhoneNumberUnoccupied) ⇒
        }
      }

      whenReady(db.run(persist.auth.AuthTransactionRepo.find(transactionHash))) { optCache ⇒
        optCache should not be empty
        optCache.get.isChecked shouldEqual true
      }
    }

    def e7() = {
      val (user, authId, authSid, phoneNumber) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

      sendSessionHello(authId, sessionId)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, true, Some(ApiPhoneActivationType.CODE))) ⇒ }
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
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false, Some(ApiPhoneActivationType.CODE))) ⇒ }
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
          case Error(AuthErrors.PhoneCodeExpired) ⇒
        }
      }
      //after code invalidation we remove authCode and AuthTransaction, thus we got InvalidAuthTransaction error
      whenReady(service.handleValidateCode(transactionHash, correctAuthCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.PhoneCodeExpired) ⇒
        }
      }
      whenReady(db.run(persist.AuthCodeRepo.findByTransactionHash(transactionHash))) { code ⇒
        code shouldBe empty
      }
      whenReady(db.run(persist.auth.AuthTransactionRepo.find(transactionHash))) { transaction ⇒
        transaction shouldBe empty
      }
    }

    def e8() = {
      val phoneNumber = buildPhone()
      val userName = "Rock Jam"
      val userSex = Some(ApiSex.Male)
      val authId = createAuthId()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, None)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false, Some(ApiPhoneActivationType.CODE))) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleSignUp(transactionHash, userName, userSex, None)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.NotValidated) ⇒
        }
      }

      whenReady(db.run(persist.auth.AuthTransactionRepo.find(transactionHash))) { optCache ⇒
        optCache should not be empty
        optCache.get.isChecked shouldEqual false
      }
    }

    def e9() = {
      val phoneNumber = buildPhone()
      val userName = "Rock Jam"
      val userSex = Some(ApiSex.Male)
      val authId = createAuthId()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, None)

      sendSessionHello(authId, sessionId)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false, Some(ApiPhoneActivationType.CODE))) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleValidateCode(transactionHash, correctAuthCode)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.PhoneNumberUnoccupied) ⇒
        }
      }

      whenReady(service.handleSignUp(transactionHash, userName, userSex, None)) { resp ⇒
        inside(resp) {
          case Ok(ResponseAuth(user, _)) ⇒
            user.name shouldEqual userName
            user.sex shouldEqual userSex
            user.contactInfo should have length 1
            user.contactInfo.head should matchPattern {
              case ApiContactRecord(ApiContactType.Phone, None, Some(phone), Some(_), None, _) ⇒
            }
        }
      }
    }

    def contactRegistered() = {
      val phoneNumber = buildPhone()
      val userName = "Rock Jam"
      val userSex = Some(ApiSex.Male)
      val authId = createAuthId()
      val sessionId = createSessionId()
      val unregClientData = ClientData(authId, sessionId, None)

      //make unregistered contact
      val (regUser, regAuthId, regAuthSid, _) = createUser()
      whenReady(db.run(persist.contact.UnregisteredPhoneContactRepo.createIfNotExists(phoneNumber, regUser.id, Some("Local name"))))(_ ⇒ ())
      val regClientData = ClientData(regAuthId, sessionId, Some(AuthData(regUser.id, regAuthSid, 42)))

      sendSessionHello(authId, sessionId)

      val user = {
        implicit val clientData = unregClientData
        val transactionHash =
          whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
            resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false, Some(ApiPhoneActivationType.CODE))) ⇒ }
            resp.toOption.get.transactionHash
          }
        whenReady(service.handleValidateCode(transactionHash, correctAuthCode))(_ ⇒ ())
        whenReady(service.handleSignUp(transactionHash, userName, userSex, None))(_.toOption.get.user)
      }

      {
        implicit val clientData = regClientData
        expectUpdate(classOf[UpdateContactRegistered])(identity)
      }

      whenReady(db.run(persist.contact.UnregisteredPhoneContactRepo.find(phoneNumber))) {
        _ shouldBe empty
      }
      whenReady(db.run(persist.contact.UserContactRepo.find(regUser.id, user.id))) { optContact ⇒
        optContact should not be empty
        optContact.get should matchPattern {
          case UserContact(_, _, Some(_), false) ⇒
        }
      }
      whenReady(db.run(persist.contact.UserContactRepo.findNotDeletedIds(user.id)))(_ shouldBe empty)
    }

    def unregContactLocalName() = {
      val phoneNumber = buildPhone()
      val userName = "Rock Jam"
      val userSex = Some(ApiSex.Male)
      val authId = createAuthId()
      val sessionId = createSessionId()
      val unregClientData = ClientData(authId, sessionId, None)

      val (regUser, regAuthId, regAuthSid, _) = createUser()
      val localName = Some("Bloody wild goat")
      val regClientData = ClientData(regAuthId, sessionId, Some(AuthData(regUser.id, regAuthSid, 42)))

      {
        implicit val clientData = regClientData
        val unregPhones = Vector(ApiPhoneToImport(phoneNumber, localName))
        whenReady(contactService.handleImportContacts(unregPhones, Vector.empty, Vector.empty))(_ ⇒ ())
      }

      sendSessionHello(authId, sessionId)

      {
        implicit val clientData = unregClientData
        val transactionHash =
          whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
            resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false, Some(ApiPhoneActivationType.CODE))) ⇒ }
            resp.toOption.get.transactionHash
          }
        whenReady(service.handleValidateCode(transactionHash, correctAuthCode))(_ ⇒ ())
        whenReady(service.handleSignUp(transactionHash, userName, userSex, None))(_.toOption.get.user)
      }

      {
        implicit val clientData = regClientData
        expectUpdate(classOf[UpdateContactRegistered])(identity)

        whenReady(db.run(persist.contact.UnregisteredPhoneContactRepo.find(phoneNumber))) {
          _ shouldBe empty
        }

        whenReady(contactService.handleGetContacts("wrongHash", Vector.empty)) { resp ⇒
          inside(resp) {
            case Ok(ResponseGetContacts(users, false, _)) ⇒
              users should have length 1
              val newUser = users.head
              newUser.name shouldEqual userName
              newUser.localName shouldEqual localName
          }
        }
      }

    }

    def e10() = {
      val (user, authId, authSid, phoneNumber) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

      sendSessionHello(authId, sessionId)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, true, Some(ApiPhoneActivationType.CODE))) ⇒ }
          resp.toOption.get.transactionHash
        }
      whenReady(service.handleValidateCode(transactionHash, correctAuthCode)) { resp ⇒
        inside(resp) { case Ok(ResponseAuth(respUser, _)) ⇒ }
      }

      whenReady(db.run(persist.auth.AuthTransactionRepo.find(transactionHash))) {
        _ shouldBe empty
      }
      whenReady(db.run(persist.AuthCodeRepo.findByTransactionHash(transactionHash))) {
        _ shouldBe empty
      }
    }

    def e11() = {
      val phoneNumber = buildPhone()
      val userName = "Rock Jam"
      val userSex = Some(ApiSex.Male)
      val authId = createAuthId()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, None)

      sendSessionHello(authId, sessionId)

      val transactionHash =
        whenReady(startPhoneAuth(phoneNumber)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartPhoneAuth(_, false, Some(ApiPhoneActivationType.CODE))) ⇒ }
          resp.toOption.get.transactionHash
        }

      whenReady(service.handleValidateCode(transactionHash, correctAuthCode)) { resp ⇒
        inside(resp) { case Error(AuthErrors.PhoneNumberUnoccupied) ⇒ }
      }

      whenReady(service.handleSignUp(transactionHash, userName, userSex, None)) { resp ⇒
        inside(resp) { case Ok(ResponseAuth(user, _)) ⇒ }
      }

      whenReady(db.run(persist.auth.AuthTransactionRepo.find(transactionHash))) {
        _ shouldBe empty
      }
      whenReady(db.run(persist.AuthCodeRepo.findByTransactionHash(transactionHash))) {
        _ shouldBe empty
      }
    }

    def e12() = {
      val email = buildEmail(gmail)
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      whenReady(startEmailAuth(email)) { resp ⇒
        inside(resp) {
          case Ok(ResponseStartEmailAuth(hash, false, ApiEmailActivationType.CODE)) ⇒
            hash should not be empty
        }
      }
    }

    def e13() = {}

    def malformedEmail() = {
      val malformedEmail = "http://sh____"
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

      val deviceHash = Random.nextLong().toBinaryString.getBytes
      def q() = {
        Thread.sleep(100)
        service.handleStartEmailAuth(
          email = email,
          appId = 42,
          apiKey = "apiKey",
          deviceHash = deviceHash,
          deviceTitle = "Specs virtual device",
          timeZone = None,
          preferredLanguages = Vector.empty
        )
      }

      val transactionHash =
        whenReady(q()) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, ApiEmailActivationType.CODE)) ⇒ }
          resp.toOption.get.transactionHash
        }

      val seq = FutureExt.ftraverse(List(q(), q(), q(), q(), q(), q()))(identity)

      whenReady(seq) { resps ⇒
        resps foreach {
          inside(_) {
            case Ok(ResponseStartEmailAuth(hash, false, ApiEmailActivationType.CODE)) ⇒
              hash shouldEqual transactionHash
          }
        }
      }
    }

    def e155() = {
      val email = buildEmail(gmail)
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash1 = whenReady(service.handleStartEmailAuth(
        email = email,
        appId = 42,
        apiKey = "apiKey",
        deviceHash = Random.nextLong().toBinaryString.getBytes,
        deviceTitle = "Specs virtual device",
        timeZone = None,
        preferredLanguages = Vector.empty
      )) { resp ⇒
        resp should matchPattern { case Ok(ResponseStartEmailAuth(_, false, _)) ⇒ }
        resp.toOption.get.transactionHash
      }

      val transactionHash2 = whenReady(service.handleStartEmailAuth(
        email = email,
        appId = 3,
        apiKey = "someKey",
        deviceHash = Random.nextLong().toBinaryString.getBytes,
        deviceTitle = "Web browser",
        timeZone = None,
        preferredLanguages = Vector.empty
      )) { resp ⇒
        resp should matchPattern { case Ok(ResponseStartEmailAuth(_, false, _)) ⇒ }
        resp.toOption.get.transactionHash
      }
      transactionHash1 should not equal transactionHash2
    }

    def emailDeletedUser() = {
      val (user, authId, authSid, _) = createUser()
      val email = buildEmail(gmail)

      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

      whenReady(UserExtension(system).addEmail(user.id, email))(identity)
      whenReady(UserExtension(system).delete(user.id))(identity)

      whenReady(startEmailAuth(email)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.UserDeleted) ⇒
        }
      }
    }

    def e16() = {
      val email = buildEmail(gmail)
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)
      val malformedUri = "ht    :/asda.rr/123"

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, ApiEmailActivationType.OAUTH2)) ⇒ }
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
        resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, ApiEmailActivationType.OAUTH2)) ⇒ }
      }

      whenReady(service.handleGetOAuth2Params("wrongHash22aksdl320d3", correctUri)) { resp ⇒
        inside(resp) { case Error(AuthErrors.EmailCodeExpired) ⇒ }
      }
    }

    def e18() = {
      val email = buildEmail(gmail)
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, ApiEmailActivationType.OAUTH2)) ⇒ }
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

      whenReady(db.run(persist.auth.AuthEmailTransactionRepo.find(transactionHash))) { optCache ⇒
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
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, ApiEmailActivationType.OAUTH2)) ⇒ }
          resp.toOption.get.transactionHash
        }
      whenReady(service.handleGetOAuth2Params(transactionHash, correctUri)) { resp ⇒
        inside(resp) { case Ok(ResponseGetOAuth2Params(url)) ⇒ }
      }
      whenReady(service.handleCompleteOAuth2("wrongTransactionHash29191djlksa", "4/YUlNIa55xSZRA4JcQkLzAh749bHAcv96aA-oVMHTQRU")) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.EmailCodeExpired) ⇒
        }
      }
    }

    def e20() = {
      val email = buildEmail(gmail)
      DummyOAuth2Server.email = email
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, ApiEmailActivationType.OAUTH2)) ⇒ }
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

      whenReady(db.run(persist.auth.AuthTransactionRepo.find(transactionHash))) { optCache ⇒
        optCache should not be empty
        optCache.get.isChecked shouldEqual true
      }

      whenReady(db.run(persist.OAuth2TokenRepo.findByUserId(email))) { optToken ⇒
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
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, ApiEmailActivationType.OAUTH2)) ⇒ }
          resp.toOption.get.transactionHash
        }
      whenReady(service.handleGetOAuth2Params(transactionHash, correctUri)) { resp ⇒
        inside(resp) { case Ok(ResponseGetOAuth2Params(url)) ⇒ }
      }
      whenReady(service.handleCompleteOAuth2(transactionHash, "wrongCode")) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.EmailCodeExpired) ⇒
        }
      }

      whenReady(db.run(persist.auth.AuthTransactionRepo.find(transactionHash))) { optCache ⇒
        optCache should not be empty
        optCache.get.isChecked shouldEqual false
      }
    }

    def e21() = {}

    def e22() = {
      val email = buildEmail(gmail)
      val userName = "Rock Jam"
      val userSex = Some(ApiSex.Male)
      implicit val clientData = ClientData(createAuthId(), createSessionId(), None)

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, ApiEmailActivationType.OAUTH2)) ⇒ }
          resp.toOption.get.transactionHash
        }
      whenReady(service.handleSignUp(transactionHash, userName, userSex, None)) { resp ⇒
        inside(resp) {
          case Error(AuthErrors.NotValidated) ⇒
        }
      }

      whenReady(db.run(persist.auth.AuthTransactionRepo.find(transactionHash))) { optCache ⇒
        optCache should not be empty
        optCache.get.isChecked shouldEqual false
      }
    }

    def e23() = {
      val email = buildEmail(gmail)
      DummyOAuth2Server.email = email
      val userName = "Rock Jam"
      val userSex = Some(ApiSex.Male)
      val authId = createAuthId()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, None)

      sendSessionHello(authId, sessionId)

      val transactionHash =
        whenReady(startEmailAuth(email)) { resp ⇒
          resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, ApiEmailActivationType.OAUTH2)) ⇒ }
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
        whenReady(service.handleSignUp(transactionHash, userName, userSex, None)) { resp ⇒
          inside(resp) {
            case Ok(ResponseAuth(u, _)) ⇒
              u.name shouldEqual userName
              u.sex shouldEqual userSex
              u.contactInfo should have length 1
              u.contactInfo.head should matchPattern {
                case ApiContactRecord(ApiContactType.Email, Some(`email`), None, Some(_), None, _) ⇒
              }
          }
          resp.toOption.get.user
        }
      whenReady(db.run(persist.UserEmailRepo.find(email))) { optEmail ⇒
        optEmail should not be empty
        optEmail.get.userId shouldEqual user.id
      }

      whenReady(db.run(persist.OAuth2TokenRepo.findByUserId(email))) { optToken ⇒
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
      val userSex = Some(ApiSex.Male)
      val authId = createAuthId()
      val sessionId = createSessionId()
      val unregClientData = ClientData(authId, sessionId, None)

      //make unregistered contact
      val (regUser, regAuthId, regAuthSid, _) = createUser()
      whenReady(db.run(persist.contact.UnregisteredEmailContactRepo.createIfNotExists(email, regUser.id, Some("Local name"))))(_ ⇒ ())
      val regClientData = ClientData(regAuthId, sessionId, Some(AuthData(regUser.id, regAuthSid, 42)))

      sendSessionHello(authId, sessionId)

      val user = {
        implicit val clientData = unregClientData
        val transactionHash =
          whenReady(startEmailAuth(email)) { resp ⇒
            resp should matchPattern { case Ok(ResponseStartEmailAuth(hash, false, ApiEmailActivationType.OAUTH2)) ⇒ }
            resp.toOption.get.transactionHash
          }

        whenReady(service.handleGetOAuth2Params(transactionHash, correctUri))(_ ⇒ ())
        whenReady(service.handleCompleteOAuth2(transactionHash, "code"))(_ ⇒ ())
        whenReady(service.handleSignUp(transactionHash, userName, userSex, None))(_.toOption.get.user)
      }

      {
        implicit val clientData = regClientData
        expectUpdate(classOf[UpdateContactRegistered])(identity)
      }

      whenReady(db.run(persist.contact.UnregisteredEmailContactRepo.find(email))) {
        _ shouldBe empty
      }
      whenReady(db.run(persist.contact.UserContactRepo.find(regUser.id, user.id))) { optContact ⇒
        optContact should not be empty
        optContact.get should matchPattern {
          case UserContact(_, _, _, false) ⇒
        }
      }
    }

    def e25() = {
      val (user, authId, authSid, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

      seqUpdExt.registerGooglePushCredentials(model.push.GooglePushCredentials(authId, 22L, "hello"))
      seqUpdExt.registerApplePushCredentials(model.push.ApplePushCredentials(authId, Some(Int32Value(22)), ByteString.copyFrom("hello".getBytes)))

      //let seqUpdateManager register credentials
      Thread.sleep(5000L)
      whenReady(db.run(persist.AuthIdRepo.find(authId))) { optAuthId ⇒
        optAuthId shouldBe defined
      }
      whenReady(db.run(persist.push.GooglePushCredentialsRepo.find(authId))) { optGoogleCreds ⇒
        optGoogleCreds shouldBe defined
      }
      whenReady(db.run(persist.push.ApplePushCredentialsRepo.find(authId))) { appleCreds ⇒
        appleCreds shouldBe defined
      }

      whenReady(service.handleSignOut()) { resp ⇒
        resp should matchPattern {
          case Ok(ResponseVoid) ⇒
        }

      }
      //let seqUpdateManager register credentials
      Thread.sleep(5000L)

      whenReady(db.run(persist.AuthIdRepo.find(authId))) { optAuthId ⇒
        optAuthId should not be defined
      }
      whenReady(db.run(persist.push.GooglePushCredentialsRepo.find(authId))) { optGoogleCreds ⇒
        optGoogleCreds should not be defined
      }
      whenReady(db.run(persist.push.ApplePushCredentialsRepo.find(authId))) { appleCreds ⇒
        appleCreds should not be defined
      }
    }

    private def startPhoneAuth(phoneNumber: Long)(implicit clientData: ClientData): Future[RpcError Xor ResponseStartPhoneAuth] = {
      service.handleStartPhoneAuth(
        phoneNumber = phoneNumber,
        appId = 42,
        apiKey = "apiKey",
        deviceHash = Random.nextLong().toBinaryString.getBytes,
        deviceTitle = "Specs virtual device",
        timeZone = None,
        preferredLanguages = Vector.empty
      )
    }

    private def startEmailAuth(email: String)(implicit clientData: ClientData): Future[RpcError Xor ResponseStartEmailAuth] = {
      service.handleStartEmailAuth(
        email = email,
        appId = 42,
        apiKey = "apiKey",
        deviceHash = Random.nextLong().toBinaryString.getBytes,
        deviceTitle = "Specs virtual device",
        timeZone = None,
        preferredLanguages = Vector.empty
      )
    }

    private def sendSessionHello(authId: Long, sessionId: Long): Unit = {
      val message = HandleMessageBox(ByteString.copyFrom(MessageBoxCodec.encode(MessageBox(Random.nextLong(), SessionHello)).require.toByteBuffer))
      sessionRegion.ref ! SessionEnvelope(authId, sessionId).withHandleMessageBox(message)
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

    def profile =
      s"""{"family_name": "Jam",
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
        get {
          complete(profile)
        }

    Http().bind("0.0.0.0", 3000).runForeach { connection ⇒
      connection handleWith Route.handlerFlow(routes)
    }
  }
}
