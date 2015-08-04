package im.actor.server.group

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import slick.driver.PostgresDriver.api._

import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.user.{ UserViewRegion, UserProcessorRegion }
import im.actor.server.util.FileStorageAdapter

object GroupProcessorRegion {
  private val idExtractor: ShardRegion.IdExtractor = {
    case c: GroupCommand ⇒ (c.groupId.toString, c)
    case q: GroupQuery   ⇒ (q.groupId.toString, q)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case c: GroupCommand ⇒ (c.groupId % 100).toString // TODO: configurable
    case q: GroupQuery   ⇒ (q.groupId % 100).toString
  }

  val typeName = "GroupProcessor"

  private def start(props: Option[Props])(implicit system: ActorSystem): GroupProcessorRegion =
    GroupProcessorRegion(ClusterSharding(system).start(
      typeName = typeName,
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def start()(
    implicit
    system:              ActorSystem,
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    userProcessorRegion: UserProcessorRegion,
    userViewRegion:      UserViewRegion,
    fsAdapter:           FileStorageAdapter
  ): GroupProcessorRegion = start(Some(GroupProcessor.props))

  def startProxy()(implicit system: ActorSystem): GroupProcessorRegion =
    start(None)
}

case class GroupProcessorRegion(ref: ActorRef)