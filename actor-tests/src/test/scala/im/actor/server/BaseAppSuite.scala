package im.actor.server

import akka.actor.ActorSystem
import akka.contrib.pattern.DistributedPubSubExtension
import akka.stream.ActorFlowMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Seconds, Span }
import org.scalatest.{ FlatSpecLike, Matchers }

import im.actor.server.api.ActorSpecHelpers
import im.actor.server.api.rpc.service.ServiceSpecHelpers
import im.actor.util.testing.{ ActorSpecification, ActorSuite }

abstract class BaseAppSuite(_system: ActorSystem = { ActorSpecification.createSystem() })
  extends ActorSuite(_system)
  with FlatSpecLike
  with ScalaFutures
  with Matchers
  with ServiceSpecMatchers
  with KafkaSpec
  with SqlSpecHelpers
  with ServiceSpecHelpers
  with ActorSpecHelpers {
  implicit lazy val (ds, db) = migrateAndInitDb()
  implicit val flowMaterializer = ActorFlowMaterializer()
  lazy val mediator = DistributedPubSubExtension(system).mediator

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(10, Seconds))

  override def afterAll(): Unit = {
    super.afterAll()
    db.close()
    ds.close()
  }
}
