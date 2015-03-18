package im.actor.server.api.rpc.service

import scala.concurrent._, duration._

import akka.actor.ActorSystem
import akka.util.Timeout
import eu.codearte.jfairy.Fairy
import slick.driver.PostgresDriver.api._

import im.actor.api.{ rpc => api }
import im.actor.server.models
import im.actor.server.persist

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
    val authId = scala.util.Random.nextLong

    Await.result(db.run(persist.AuthId.create(authId, None)), 1.second)
    authId
  }

  def getSmsHash(authId: Long, phoneNumber: Long)(implicit service: api.auth.AuthService, system: ActorSystem): String = withoutLogs {
    val api.auth.ResponseSendAuthCode(smsHash, _) =
      Await.result(service.handleSendAuthCode(phoneNumber, 1, "apiKey")(api.ClientData(authId, None)), 1.second).toOption.get
    smsHash
  }

  def createUser()(implicit service: api.auth.AuthService, db: Database, system: ActorSystem): (api.users.User, Long, Long) = withoutLogs {
    val authId = createAuthId()
    val phoneNumber = buildPhone()
    (createUser(authId, phoneNumber), authId, phoneNumber)
  }

  def createUser(authId: Long, phoneNumber: Long)(implicit service: api.auth.AuthService, system: ActorSystem) = withoutLogs {
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
    )(api.ClientData(authId, None)), 1.second).toOption.get

    rsp.user
  }

  def buildAuthService()(implicit system: ActorSystem, database: Database) = new auth.AuthServiceImpl {
    override implicit val ec = system.dispatcher
    override implicit val actorSystem = system

    val db = database
  }

  protected def withoutLogs[A](f: => A)(implicit system: ActorSystem): A = {
    val logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
    val logLevel = logger.getLevel()
    logger.setLevel(ch.qos.logback.classic.Level.OFF)

    system.eventStream.setLogLevel(akka.event.Logging.ErrorLevel)

    val res = f

    logger.setLevel(logLevel)

    res
  }
}
