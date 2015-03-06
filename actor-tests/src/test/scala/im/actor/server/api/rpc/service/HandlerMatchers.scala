package im.actor.server.api.rpc.service

import im.actor.api.{ rpc => api }
import org.specs2._, matcher._
import scalaz._, std.either._

trait HandlerMatchers extends DisjunctionMatchers {

  def beOkLike(pattern: PartialFunction[(api.RpcResponse, Vector[(Long, api.Update)]), MatchResult[_]]) =
    be_\/-.like(pattern)

  def beErrorLike(pattern: PartialFunction[api.RpcError, MatchResult[_]]) =
    be_-\/.like(pattern)
}
