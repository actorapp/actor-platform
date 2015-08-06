package im.actor.server

import akka.actor.ActorSystem
import akka.contrib.pattern.DistributedPubSubExtension
import akka.stream.ActorMaterializer
import im.actor.server.api.CommonSerialization
import im.actor.server.api.rpc.service.ServiceSpecHelpers
import im.actor.server.commons.serialization.ActorSerializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Seconds, Span }
import org.scalatest.{ FlatSpecLike, Matchers }
import slick.driver.PostgresDriver
import slick.jdbc.JdbcDataSource

import scala.concurrent.ExecutionContext

abstract class BaseAppSuite(_system: ActorSystem = {
                              ActorSpecification.createSystem()
                            })
  extends ActorSuite(_system)
  with FlatSpecLike
  with ScalaFutures
  with Matchers
  with ServiceSpecMatchers
  with SqlSpecHelpers
  with ServiceSpecHelpers {

  CommonSerialization.register()

  protected implicit val (ds: JdbcDataSource, db: PostgresDriver.api.Database) = migrateAndInitDb()
  protected implicit val materializer: ActorMaterializer = ActorMaterializer()
  protected implicit lazy val ec: ExecutionContext = _system.dispatcher

  protected lazy val mediator = DistributedPubSubExtension(system).mediator

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(30, Seconds))

  override def afterAll(): Unit = {
    super.afterAll()
    ActorSerializer.clean()
    db.close()
    ds.close()
  }
}
