package im.actor.server.api.rpc.service

import akka.stream.ActorFlowMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FlatSpecLike, Matchers}
import org.specs2.matcher.ThrownExpectations
import org.specs2.specification.core.Fragments

import im.actor.server.SqlSpecHelpers
import im.actor.util.testing._

trait BaseServiceSpec
  extends ActorSpecification
  with ThrownExpectations
  with SqlSpecHelpers
  with ServiceSpecHelpers
  with HandlerMatchers {
  implicit lazy val (ds, db) = migrateAndInitDb()
  implicit val flowMaterializer = ActorFlowMaterializer()

  override def map(fragments: => Fragments) =
    super.map(fragments) ^ step(closeDb())

  def closeDb() = {
    ds.close()
  }
}

trait BaseServiceSuite
  extends ActorSuite
  with FlatSpecLike
  with ScalaFutures
  with Matchers
  with SqlSpecHelpers
  with ServiceSpecHelpers {
  implicit lazy val (ds, db) = migrateAndInitDb()
  implicit val flowMaterializer = ActorFlowMaterializer()

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(5, Seconds))

  override def afterAll(): Unit = {
    super.afterAll()
    ds.close()
  }
}
