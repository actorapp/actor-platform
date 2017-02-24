package im.actor.server.activation.fake

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.util.FastFuture
import cats.data.Xor
import im.actor.server.activation.common._
import im.actor.server.db.DbExtension
import im.actor.server.persist.auth.GateAuthCodeRepo
import spray.client.pipelining._
import spray.http.HttpMethods.{ GET, POST }
import spray.http._
import spray.httpx.PlayJsonSupport
import spray.httpx.marshalling._
import spray.httpx.unmarshalling._

import scala.concurrent.Future
import scala.reflect.ClassTag

import scala.concurrent.ExecutionContext.Implicits.global

private[activation] final class FakeSmsProvider(implicit system: ActorSystem)
  extends ActivationProvider
  with PlayJsonSupport {

  private val log = Logging(system, getClass)

  override def send(txHash: String, code: Code): Future[CodeFailure Xor Unit] = {
    Future {
      Xor.right(None)
    }
  }

  override def validate(txHash: String, code: String): Future[ValidationResponse] = {

    Future {
      Validated
    }
  }

  override def cleanup(txHash: String): Future[Unit] = Future {}

}