package im.actor.server

import cats.data.Xor
import im.actor.api.rpc.RpcError
import org.scalatest.Matchers
import org.scalatest.matchers.Matcher

trait ServiceSpecMatchers extends Matchers {
  def matchForbidden[T]: Matcher[RpcError Xor T] = matchPattern {
    case Xor.Left(RpcError(403, "FORBIDDEN", _, _, _)) ⇒
  }
}
