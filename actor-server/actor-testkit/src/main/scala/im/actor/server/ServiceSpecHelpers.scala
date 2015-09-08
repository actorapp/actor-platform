package im.actor.server

import akka.actor.{ ActorRef, ActorSystem }
import akka.contrib.pattern.DistributedPubSubExtension
import akka.stream.Materializer
import akka.util.Timeout
import eu.codearte.jfairy.Fairy
import im.actor.api.rpc.peers.{ ApiPeerType, ApiOutPeer }
import im.actor.api.{ rpc ⇒ rpcapi }
import im.actor.server.api.rpc.service.auth
import im.actor.server.api.rpc.RpcApiService
import im.actor.server.oauth.GoogleProvider
import im.actor.server.presences.{ PresenceManagerRegion, GroupPresenceManagerRegion }
import im.actor.server.sequence.WeakUpdatesManagerRegion
import im.actor.server.session.{ SessionRegion, Session, SessionConfig }
import im.actor.server.user.{ UserExtension, UserViewRegion, UserOffice }
import org.scalatest.Suite
import slick.driver.PostgresDriver.api._

import scala.concurrent._
import scala.concurrent.duration._
import scalaz.{ -\/, \/- }

trait PersistenceHelpers {
  protected implicit val timeout = Timeout(5.seconds)

  def getUserModel(userId: Int)(implicit db: Database) = Await.result(db.run(persist.User.find(userId).head), timeout.duration)
}

trait UserStructExtensions {
  implicit class ExtUser(user: rpcapi.users.ApiUser) {
    def asModel()(implicit db: Database): models.User =
      Await.result(db.run(persist.User.find(user.id).head), 3.seconds)
  }
}

trait ServiceSpecHelpers extends PersistenceHelpers with UserStructExtensions {
  this: Suite ⇒

  protected val system: ActorSystem
  protected lazy val mediator: ActorRef = DistributedPubSubExtension(system).mediator

  protected val fairy = Fairy.create()

  import system._

  def buildPhone(): Long = {
    75550000000L + scala.util.Random.nextInt(999999)
  }

  def buildEmail(at: String = ""): String = {
    val email = fairy.person().email()
    if (at.isEmpty) email else email.substring(0, email.lastIndexOf("@")) + s"@$at"
  }

  def createAuthId()(implicit db: Database): Long = {
    val authId = scala.util.Random.nextLong()

    Await.result(db.run(persist.AuthId.create(authId, None, None)), 1.second)
    authId
  }

  def createAuthId(userId: Int)(implicit ec: ExecutionContext, system: ActorSystem, db: Database, service: rpcapi.auth.AuthService): Long = {
    val authId = scala.util.Random.nextLong()
    Await.result(db.run(persist.AuthId.create(authId, None, None)), 1.second)

    val phoneNumber = Await.result(db.run(persist.UserPhone.findByUserId(userId)) map (_.head.number), 1.second)

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
    )(rpcapi.ClientData(authId, scala.util.Random.nextLong(), None)), 5.seconds)

    res match {
      case \/-(rsp) ⇒ rsp
      case -\/(e)   ⇒ fail(s"Got RpcError ${e}")
    }

    authId
  }

  def createSessionId(): Long =
    scala.util.Random.nextLong()

  def getSmsHash(authId: Long, phoneNumber: Long)(implicit service: rpcapi.auth.AuthService, system: ActorSystem): String = withoutLogs {
    val rpcapi.auth.ResponseSendAuthCodeObsolete(smsHash, _) =
      Await.result(service.handleSendAuthCodeObsolete(phoneNumber, 1, "apiKey")(rpcapi.ClientData(authId, scala.util.Random.nextLong(), None)), 1.second).toOption.get

    smsHash
  }

  def getSmsCode(authId: Long, phoneNumber: Long)(implicit service: rpcapi.auth.AuthService, system: ActorSystem, db: Database): models.AuthSmsCodeObsolete = withoutLogs {
    val res = Await.result(service.handleSendAuthCodeObsolete(phoneNumber, 1, "apiKey")(rpcapi.ClientData(authId, scala.util.Random.nextLong(), None)), 1.second)
    res.toOption.get

    Await.result(db.run(persist.AuthSmsCodeObsolete.findByPhoneNumber(phoneNumber).head), 5.seconds)
  }

  def createUser()(implicit service: rpcapi.auth.AuthService, db: Database, system: ActorSystem): (rpcapi.users.ApiUser, Long, Long) = {
    val authId = createAuthId()
    val phoneNumber = buildPhone()
    (createUser(authId, phoneNumber), authId, phoneNumber)
  }

  def createUser(phoneNumber: Long)(implicit service: rpcapi.auth.AuthService, system: ActorSystem, db: Database): rpcapi.users.ApiUser =
    createUser(createAuthId(), phoneNumber)

  def getOutPeer(userId: Int, clientAuthId: Long): ApiOutPeer = {
    implicit val userViewRegion: UserViewRegion = UserExtension(system).viewRegion
    val accessHash = Await.result(UserOffice.getAccessHash(userId, clientAuthId), 5.seconds)
    ApiOutPeer(ApiPeerType.Private, userId, accessHash)
  }

  //TODO: make same method to work with email
  def createUser(authId: Long, phoneNumber: Long)(implicit service: rpcapi.auth.AuthService, system: ActorSystem, db: Database): rpcapi.users.ApiUser =
    withoutLogs {
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
      )(rpcapi.ClientData(authId, scala.util.Random.nextLong(), None)), 5.seconds)

      res match {
        case \/-(rsp) ⇒ rsp.user
        case -\/(e)   ⇒ fail(s"Got RpcError ${e}")
      }
    }

  def buildRpcApiService(services: Seq[im.actor.api.rpc.Service])(implicit system: ActorSystem, db: Database) =
    system.actorOf(RpcApiService.props(services), "rpcApiService")

  def buildSessionRegion(rpcApiService: ActorRef)(
    implicit
    weakUpdManagerRegion:       WeakUpdatesManagerRegion,
    presenceManagerRegion:      PresenceManagerRegion,
    groupPresenceManagerRegion: GroupPresenceManagerRegion,
    system:                     ActorSystem,
    materializer:               Materializer
  ) = {
    implicit val sessionConfig = SessionConfig.load(system.settings.config.getConfig("session"))
    Session.startRegion(Some(Session.props(mediator)))
  }

  def buildSessionRegionProxy()(implicit system: ActorSystem) = Session.startRegionProxy()

  def buildAuthService()(
    implicit
    sessionRegion: SessionRegion,
    oauth2Service: GoogleProvider,
    system:        ActorSystem
  ) = new auth.AuthServiceImpl(new DummyCodeActivation, mediator)

  protected def withoutLogs[A](f: ⇒ A)(implicit system: ActorSystem): A = {
    val logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]

    val logLevel = logger.getLevel()
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
