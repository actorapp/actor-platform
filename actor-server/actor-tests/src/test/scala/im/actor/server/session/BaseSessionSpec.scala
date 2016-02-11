package im.actor.server.session

import akka.actor._
import im.actor.server
import im.actor.server._
import im.actor.server.api.rpc.RpcApiExtension
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceConfig, SequenceServiceImpl }
import im.actor.server.db.DbExtension
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Seconds, Span }
import org.scalatest.{ FlatSpecLike, Matchers }
import slick.driver.PostgresDriver

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

abstract class BaseSessionSpec(_system: ActorSystem = {
                                 server.ActorSpecification.createSystem()
                               })
  extends server.ActorSuite(_system)
  with FlatSpecLike
  with ScalaFutures
  with Matchers
  with ActorSerializerPrepare
  with ServiceSpecHelpers
  with SessionSpecHelpers {

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(10, Seconds))

  implicit val ec = system.dispatcher

  protected implicit lazy val db: PostgresDriver.api.Database = {
    DbExtension(_system).db
    DbExtension(_system).clean()
    DbExtension(_system).migrate()
    DbExtension(_system).db
  }

  protected implicit val sessionConfig = SessionConfig.load(system.settings.config.getConfig("session"))

  Session.startRegion(Session.props)

  implicit val sessionRegion = Session.startRegionProxy()

  protected val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
  protected implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
  protected implicit val authService = new AuthServiceImpl
  protected val sequenceConfig = SequenceServiceConfig.load().toOption.get
  protected lazy val sequenceService = new SequenceServiceImpl(sequenceConfig)
  protected lazy val messagingService = MessagingServiceImpl()
  protected lazy val contactsService = new ContactsServiceImpl()

  override def beforeAll = {
    RpcApiExtension(system).register(Seq(authService, sequenceService, messagingService, contactsService))
  }

  protected def createAuthId(): Long = {
    val authId = Random.nextLong()
    Await.result(db.run(persist.AuthIdRepo.create(authId, None, None)), 1.second)
    authId
  }
}
