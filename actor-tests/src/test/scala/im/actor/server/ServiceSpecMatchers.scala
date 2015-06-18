package im.actor.server

import scalaz.{ -\/, \/ }

import org.scalatest.Matchers
import org.scalatest.matchers.Matcher

import im.actor.api.rpc.RpcError

trait ServiceSpecMatchers extends Matchers {
  def matchNotAuthorized[T]: Matcher[\/[RpcError, T]] = matchPattern {
    case -\/(RpcError(403, "FORBIDDEN", _, _, _)) ⇒
  }
}
