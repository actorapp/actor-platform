package im.actor.server.group

import akka.actor.{ ActorSystem, Props, ActorRef }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }

import slick.driver.PostgresDriver.api._

import im.actor.server.office.group.GroupEnvelope
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.user.UserOfficeRegion

object GroupOfficeRegion {
  private val idExtractor: ShardRegion.IdExtractor = {
    case GroupEnvelope(groupId, payload) ⇒ (groupId.toString, payload)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case GroupEnvelope(groupId, _) ⇒ (groupId % 100).toString // TODO: configurable
  }

  private def start(props: Option[Props])(implicit system: ActorSystem): GroupOfficeRegion =
    GroupOfficeRegion(ClusterSharding(system).start(
      typeName = "GroupOffice",
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def start()(
    implicit
    system:              ActorSystem,
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    userOfficeRegion:    UserOfficeRegion
  ): GroupOfficeRegion =
    start(Some(GroupOfficeActor.props))

  def startProxy()(implicit system: ActorSystem): GroupOfficeRegion =
    start(None)
}

case class GroupOfficeRegion(ref: ActorRef)