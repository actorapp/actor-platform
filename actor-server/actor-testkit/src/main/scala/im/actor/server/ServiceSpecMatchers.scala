package im.actor.server

import im.actor.api.rpc.RpcError
import org.scalatest.Matchers
import org.scalatest.matchers.Matcher

import scalaz.{ \/, -\/ }

trait ServiceSpecMatchers extends Matchers {
  def matchNotAuthorized[T]: Matcher[\/[RpcError, T]] = matchPattern {
    case -\/(RpcError(403, "FORBIDDEN", _, _, _)) ⇒
  }
}
