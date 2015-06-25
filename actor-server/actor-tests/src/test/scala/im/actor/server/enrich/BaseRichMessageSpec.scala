package im.actor.server.enrich

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpecLike, Matchers }

import im.actor.server.SqlSpecHelpers
import im.actor.util.testing.{ ActorSpecification, ActorSuite }

abstract class BaseRichMessageSpec(_system: ActorSystem = { ActorSpecification.createSystem() })
  extends ActorSuite(_system) with FlatSpecLike with ScalaFutures with Matchers with SqlSpecHelpers {

  implicit val materializer = ActorMaterializer()

}
