package im.actor.server

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.Materializer
import akka.util.Timeout
import eu.codearte.jfairy.Fairy
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.auth.{ ResponseSendAuthCodeObsolete, AuthService }
import im.actor.api.rpc.peers.{ ApiUserOutPeer, ApiOutPeer, ApiPeerType }
import im.actor.api.rpc.users.ApiUser
import im.actor.server.api.rpc.RpcApiExtension
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.oauth.GoogleProvider
import im.actor.server.persist.{ AuthCodeRepo, AuthSessionRepo }
import im.actor.server.session.{ Session, SessionConfig, SessionRegion }
import im.actor.server.user.UserExtension
import org.scalatest.{ Inside, Suite }
import org.scalatest.concurrent.ScalaFutures
import slick.driver.PostgresDriver.api._

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Random
import scalaz.{ -\/, \/- }

trait PersistenceHelpers {
  implicit val timeout = Timeout(5.seconds)

  def getUserModel(userId: Int)(implicit db: Database) = Await.result(db.run(persist.UserRepo.find(userId)), timeout.duration).get
}

trait UserStructExtensions {
  implicit class ExtUser(user: ApiUser) {
    def asModel()(implicit db: Database): model.User =
      Await.result(db.run(persist.UserRepo.find(user.id)), 3.seconds).get
  }
}

trait ServiceSpecHelpers extends PersistenceHelpers with UserStructExtensions with ScalaFutures with Inside {
  this: Suite ⇒

  protected val system: ActorSystem

  protected val fairy = Fairy.create()

  //private implicit val patienceConfig = PatienceConfig(Span(10, Seconds))

  def buildPhone(): Long = {
    75550000000L + scala.util.Random.nextInt(999999)
  }

  def buildEmail(at: String = ""): String = {
    val email = fairy.person().email()
    if (at.isEmpty) email else email.substring(0, email.lastIndexOf("@")) + s"@$at"
  }

  def createAuthId()(implicit db: Database): Long = {
    val authId = scala.util.Random.nextLong()

    Await.result(db.run(persist.AuthIdRepo.create(authId, None, None)), 1.second)
    authId
  }

  def createAuthId(userId: Int)(implicit ec: ExecutionContext, system: ActorSystem, db: Database, service: AuthService): (Long, Int) = {
    val authId = scala.util.Random.nextLong()
    Await.result(db.run(persist.AuthIdRepo.create(authId, None, None)), 1.second)

    val phoneNumber = Await.result(db.run(persist.UserPhoneRepo.findByUserId(userId)) map (_.head.number), 1.second)

    val smsCode = getSmsCode(authId, phoneNumber)

    val res = Await.result(service.handleSignUpObsolete(
      phoneNumber = phoneNumber,
      smsHash = smsCode.smsHash,
      smsCode = smsCode.smsCode,
      name = fairy.person().fullName(),
      deviceHash = scala.util.Random.nextLong.toBinaryString.getBytes(),
      deviceTitle = "Specs virtual device",
      appId = 42,
      appKey = "appKey",
      isSilent = false
    )(ClientData(authId, scala.util.Random.nextLong(), None)), 5.seconds)

    res match {
      case \/-(rsp) ⇒ rsp
      case -\/(e)   ⇒ fail(s"Got RpcError ${e}")
    }

    (authId, Await.result(db.run(AuthSessionRepo.findByAuthId(authId)), 5.seconds).get.id)
  }

  def createSessionId(): Long =
    scala.util.Random.nextLong()

  def getSmsHash(authId: Long, phoneNumber: Long)(implicit service: AuthService, system: ActorSystem): String = withoutLogs {
    val ResponseSendAuthCodeObsolete(smsHash, _) =
      Await.result(service.handleSendAuthCodeObsolete(phoneNumber, 1, "apiKey")(ClientData(authId, scala.util.Random.nextLong(), None)), 1.second).toOption.get

    smsHash
  }

  def getSmsCode(authId: Long, phoneNumber: Long)(implicit service: AuthService, system: ActorSystem, db: Database): model.AuthSmsCodeObsolete = withoutLogs {
    val res = Await.result(service.handleSendAuthCodeObsolete(phoneNumber, 1, "apiKey")(ClientData(authId, scala.util.Random.nextLong(), None)), 1.second)
    res.toOption.get

    Await.result(db.run(persist.AuthSmsCodeObsoleteRepo.findByPhoneNumber(phoneNumber).head), 5.seconds)
  }

  def createUser()(implicit service: AuthService, db: Database, system: ActorSystem): (ApiUser, Long, Int, Long) = {
    val authId = createAuthId()
    val phoneNumber = buildPhone()
    val (user, authSid) = createUser(authId, phoneNumber)
    (user, authId, authSid, phoneNumber)
  }

  def createUser(phoneNumber: Long)(implicit service: AuthService, system: ActorSystem, db: Database): (ApiUser, Int) =
    createUser(createAuthId(), phoneNumber)

  def getOutPeer(userId: Int, clientAuthId: Long): ApiOutPeer = {
    val accessHash = Await.result(UserExtension(system).getAccessHash(userId, clientAuthId), 5.seconds)
    ApiOutPeer(ApiPeerType.Private, userId, accessHash)
  }

  def getUserOutPeer(userId: Int, clientAuthId: Long): ApiUserOutPeer = {
    val outPeer = getOutPeer(userId, clientAuthId)
    ApiUserOutPeer(outPeer.id, outPeer.accessHash)
  }

  //TODO: make same method to work with email
  def createUser(authId: Long, phoneNumber: Long)(implicit service: AuthService, system: ActorSystem, db: Database): (ApiUser, Int) =
    withoutLogs {
      implicit val clientData = ClientData(authId, Random.nextLong(), None)

      val txHash = whenReady(service.handleStartPhoneAuth(
        phoneNumber = phoneNumber,
        appId = 42,
        apiKey = "appKey",
        deviceHash = Random.nextLong.toBinaryString.getBytes,
        deviceTitle = "Specs Has You",
        timeZone = None,
        preferredLanguages = Vector.empty
      ))(_.toOption.get.transactionHash)

      val code = whenReady(db.run(AuthCodeRepo.findByTransactionHash(txHash)))(_.get.code)
      whenReady(service.handleValidateCode(txHash, code))(_ ⇒ ())

      val user = whenReady(service.handleSignUp(txHash, fairy.person().fullName(), None, None))(_.toOption.get.user)
      val authSid = whenReady(db.run(AuthSessionRepo.findByAuthId(authId)))(_.get.id)

      (user, authSid)
    }

  def buildRpcApiService(services: Seq[im.actor.api.rpc.Service])(implicit system: ActorSystem, db: Database) =
    RpcApiExtension(system).register(services)

  def buildSessionRegion()(implicit system: ActorSystem, materializer: Materializer) = {
    implicit val sessionConfig = SessionConfig.load(system.settings.config.getConfig("session"))
    Session.startRegion(Session.props)
  }

  def buildSessionRegionProxy()(implicit system: ActorSystem) = Session.startRegionProxy()

  def buildAuthService()(
    implicit
    sessionRegion: SessionRegion,
    oauth2Service: GoogleProvider,
    system:        ActorSystem
  ) = new AuthServiceImpl

  protected def withoutLogs[A](f: ⇒ A)(implicit system: ActorSystem): A = {
    val logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]

    val logLevel = logger.getLevel
    val esLogLevel = system.eventStream.logLevel

    logger.setLevel(ch.qos.logback.classic.Level.WARN)
    system.eventStream.setLogLevel(akka.event.Logging.WarningLevel)

    val res = f

    logger.setLevel(logLevel)
    system.eventStream.setLogLevel(esLogLevel)

    res
  }

  protected def futureSleep(delay: Long)(implicit ec: ExecutionContext): Future[Unit] = Future { blocking { Thread.sleep(delay) } }
}
