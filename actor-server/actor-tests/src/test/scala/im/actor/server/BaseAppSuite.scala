package im.actor.server

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import akka.contrib.pattern.DistributedPubSubExtension
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Seconds, Span }
import org.scalatest.{ FlatSpecLike, Matchers }
import slick.driver.PostgresDriver
import slick.jdbc.JdbcDataSource

import im.actor.server.api.ActorSpecHelpers
import im.actor.server.api.rpc.service.ServiceSpecHelpers
import im.actor.util.testing.{ ActorSpecification, ActorSuite }

abstract class BaseAppSuite(_system: ActorSystem = { ActorSpecification.createSystem() })
  extends ActorSuite(_system)
  with FlatSpecLike
  with ScalaFutures
  with Matchers
  with ServiceSpecMatchers
  with SqlSpecHelpers
  with ServiceSpecHelpers
  with ActorSpecHelpers {

  implicit val (ds: JdbcDataSource, db: PostgresDriver.api.Database) = migrateAndInitDb()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit lazy val ec: ExecutionContext = _system.dispatcher

  lazy val mediator = DistributedPubSubExtension(system).mediator

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(10, Seconds))

  override def afterAll(): Unit = {
    super.afterAll()
    db.close()
    ds.close()
  }
}
