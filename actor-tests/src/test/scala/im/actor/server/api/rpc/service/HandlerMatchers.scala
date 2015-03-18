package im.actor.server.api.rpc.service

import im.actor.api.{ rpc => api }
import org.specs2._, matcher._
import scalaz._, std.either._

trait HandlerMatchers extends DisjunctionMatchers {
  def beOk[T] = be_\/-[T]
  //def beOkWhich[T]()
  def beOkLike(pattern: PartialFunction[api.RpcResponse, MatchResult[_]]) =
    be_\/-[api.RpcResponse].like(pattern)

  def beErrorLike(pattern: PartialFunction[api.RpcError, MatchResult[_]]) =
    be_-\/.like(pattern)
}
