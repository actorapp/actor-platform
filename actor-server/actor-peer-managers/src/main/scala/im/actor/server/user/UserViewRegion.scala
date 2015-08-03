package im.actor.server.user

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }

object UserViewRegion {
  private def start(props: Option[Props])(implicit system: ActorSystem): UserViewRegion =
    /*
    UserViewRegion(ClusterSharding(system).start(
      typeName = typeName,
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))*/
    // View is broken, use processor instead
    UserViewRegion(UserProcessorRegion.startProxy().ref)

  def start()(
    implicit
    system: ActorSystem
  ): UserViewRegion =
    start(Some(UserView.props))

  def startProxy()(implicit system: ActorSystem): UserViewRegion =
    start(None)
}

final case class UserViewRegion(val ref: ActorRef)
