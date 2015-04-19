package im.actor.server.api.rpc.service

import scala.concurrent._, duration._

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.FlowMaterializer
import akka.util.Timeout
import eu.codearte.jfairy.Fairy
import slick.driver.PostgresDriver.api._

import im.actor.api.{ rpc => api }
import im.actor.server.api.rpc.RpcApiService
import im.actor.server.models
import im.actor.server.persist
import im.actor.server.session.Session

trait PersistenceHelpers {
  implicit val timeout = Timeout(5.seconds)

  def getUserModel(userId: Int)(implicit db: Database) = Await.result(db.run(persist.User.find(userId).head), timeout.duration)
}

trait UserStructExtensions {
  implicit class ExtUser(user: api.users.User) {
    def asModel()(implicit db: Database): models.User =
      Await.result(db.run(persist.User.find(user.id).head), 3.seconds)
  }
}

trait ServiceSpecHelpers extends PersistenceHelpers with UserStructExtensions {
  val fairy = Fairy.create()

  def buildPhone(): Long = {
    75550000000L + scala.util.Random.nextInt(999999)
  }

  def createAuthId()(implicit db: Database): Long = {
    val authId = scala.util.Random.nextLong()

    Await.result(db.run(persist.AuthId.create(authId, None, None)), 1.second)
    authId
  }

  def createSessionId(): Long =
    scala.util.Random.nextLong()

  def getSmsHash(authId: Long, phoneNumber: Long)(implicit service: api.auth.AuthService, system: ActorSystem): String = withoutLogs {
    val api.auth.ResponseSendAuthCode(smsHash, _) =
      Await.result(service.handleSendAuthCode(phoneNumber, 1, "apiKey")(api.ClientData(authId, scala.util.Random.nextLong(), None)), 1.second).toOption.get
    smsHash
  }

  def createUser()(implicit service: api.auth.AuthService, db: Database, system: ActorSystem): (api.users.User, Long, Long) = withoutLogs {
    val authId = createAuthId()
    val phoneNumber = buildPhone()
    (createUser(authId, phoneNumber), authId, phoneNumber)
  }

  def createUser(phoneNumber: Long)(implicit service: api.auth.AuthService, system: ActorSystem, db: Database): api.users.User =
    createUser(createAuthId(), phoneNumber)

  def createUser(authId: Long, phoneNumber: Long)(implicit service: api.auth.AuthService, system: ActorSystem): api.users.User = withoutLogs {
    val smsHash = getSmsHash(authId, phoneNumber)(service, system)

    val rsp = Await.result(service.handleSignUp(
      phoneNumber = phoneNumber,
      smsHash = smsHash,
      smsCode = "0000",
      name = fairy.person().fullName(),
      publicKey = scala.util.Random.nextLong.toBinaryString.getBytes(),
      deviceHash = scala.util.Random.nextLong.toBinaryString.getBytes(),
      deviceTitle = "Specs virtual device",
      appId = 42,
      appKey = "appKey",
      isSilent = false
    )(api.ClientData(authId, scala.util.Random.nextLong(), None)), 1.second).toOption.get

    rsp.user
  }

  def buildRpcApiService()(implicit system: ActorSystem, db: Database) = system.actorOf(RpcApiService.props())

  def buildSessionRegion(rpcApiService: ActorRef,
                         seqUpdManagerRegion: ActorRef,
                         weakUpdManagerRegion: ActorRef,
                         presenceManagerRegion: ActorRef)
                        (implicit system: ActorSystem, db: Database, flowMaterializer: FlowMaterializer) =
    Session.startRegion(Some(Session.props(rpcApiService, seqUpdManagerRegion, weakUpdManagerRegion, presenceManagerRegion)))

  def buildAuthService(sessionRegion: ActorRef)(implicit system: ActorSystem, database: Database) = new auth.AuthServiceImpl(sessionRegion)

  protected def withoutLogs[A](f: => A)(implicit system: ActorSystem): A = {
    val logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
    val logLevel = logger.getLevel()
    logger.setLevel(ch.qos.logback.classic.Level.OFF)

    system.eventStream.setLogLevel(akka.event.Logging.ErrorLevel)

    val res = f

    logger.setLevel(logLevel)

    res
  }

  protected def futureSleep(delay: Long)(implicit ec: ExecutionContext): Future[Unit] = Future { blocking { Thread.sleep(delay) } }
}
