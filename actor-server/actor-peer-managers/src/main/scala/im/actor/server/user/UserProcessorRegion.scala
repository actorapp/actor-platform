package im.actor.server.user

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import slick.driver.PostgresDriver.api._

import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.social.SocialManagerRegion

object UserProcessorRegion {
  private val idExtractor: ShardRegion.IdExtractor = {
    case c: UserCommand ⇒ (c.userId.toString, c)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case c: UserCommand ⇒ (c.userId % 100).toString // TODO: configurable
  }

  val typeName = "UserProcessor"

  private def start(props: Option[Props])(implicit system: ActorSystem): UserProcessorRegion =
    UserProcessorRegion(ClusterSharding(system).start(
      typeName = typeName,
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def start()(
    implicit
    system:              ActorSystem,
    db:                  Database,
    userViewRegion:      UserViewRegion,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    socialManagerRegion: SocialManagerRegion
  ): UserProcessorRegion =
    start(Some(UserProcessor.props))

  def startProxy()(implicit system: ActorSystem): UserProcessorRegion =
    start(None)

  def get(system: ActorSystem): UserProcessorRegion = UserProcessorRegion(ClusterSharding.get(system).shardRegion(typeName))
}

case class UserProcessorRegion(val ref: ActorRef)