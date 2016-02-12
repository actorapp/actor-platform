package im.actor.server

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Seconds, Span }
import org.scalatest.{ Inside, FlatSpecLike, Matchers }
import slick.driver.PostgresDriver

import scala.concurrent.ExecutionContext

import im.actor.server.db.DbExtension

abstract class BaseAppSuite(_system: ActorSystem = {
                              ActorSpecification.createSystem()
                            })
  extends ActorSuite(_system)
  with FlatSpecLike
  with ScalaFutures
  with MessagingSpecHelpers
  with Matchers
  with Inside
  with ServiceSpecMatchers
  with ServiceSpecHelpers
  with ActorSerializerPrepare {

  protected implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit lazy val ec: ExecutionContext = _system.dispatcher

  protected implicit lazy val db: PostgresDriver.api.Database = {
    DbExtension(_system).clean()
    DbExtension(_system).migrate()
    DbExtension(_system).db
  }

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(15, Seconds))

}
