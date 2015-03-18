package im.actor.server.api.rpc.service

import im.actor.api.{ rpc => api }
import org.specs2._, matcher._

trait HandlerMatchers extends DisjunctionMatchers {
  def beOk[T] = be_\/-[T]
  def beOk[T](t: ValueCheck[T]) = be_\/-(t)

  def beOkLike(pattern: PartialFunction[api.RpcResponse, MatchResult[_]]) =
    be_\/-[api.RpcResponse].like(pattern)

  def beError[T] = be_-\/[T]
  def beError[T](t: ValueCheck[T]) = be_-\/(t)
  def beErrorLike(pattern: PartialFunction[api.RpcError, MatchResult[_]]) =
    be_-\/.like(pattern)
}
