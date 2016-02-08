package im.actor.server.activation.common

import cats.data.Xor

import scala.concurrent.Future

trait ActivationProvider {
  def send(txHash: String, code: Code): Future[CodeFailure Xor Unit]
  def validate(txHash: String, code: String): Future[ValidationResponse]
  def cleanup(txHash: String): Future[Unit]
}