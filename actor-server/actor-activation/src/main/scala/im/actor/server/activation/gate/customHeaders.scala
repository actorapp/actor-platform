package im.actor.server.activation.gate

import akka.http.scaladsl.model.headers.CustomHeader

case class `X-Auth-Token`(value: String) extends CustomHeader {
  override def name: String = "X-Auth-Token"
}