package im.actor.server.enrich

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import im.actor.server
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpecLike, Matchers }

import im.actor.server.{ ActorSpecification, SqlSpecHelpers }

abstract class BaseRichMessageSpec(_system: ActorSystem = { ActorSpecification.createSystem() })
  extends server.ActorSuite(_system) with FlatSpecLike with ScalaFutures with Matchers with SqlSpecHelpers {

  implicit val materializer = ActorMaterializer()

}
