package im.actor.server.user

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }

object UserViewRegion {
  private val idExtractor: ShardRegion.IdExtractor = {
    case q: UserQuery ⇒ (q.userId.toString, q)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case q: UserQuery ⇒ (q.userId % 100).toString
  }

  val typeName = "UserView"

  private def start(props: Option[Props])(implicit system: ActorSystem): UserViewRegion =
    UserViewRegion(ClusterSharding(system).start(
      typeName = typeName,
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def start()(
    implicit
    system: ActorSystem
  ): UserViewRegion =
    start(Some(UserView.props))

  def startProxy()(implicit system: ActorSystem): UserViewRegion =
    start(None)

  def get(system: ActorSystem): UserViewRegion = UserViewRegion(ClusterSharding.get(system).shardRegion(typeName))
}

final case class UserViewRegion(val ref: ActorRef)
