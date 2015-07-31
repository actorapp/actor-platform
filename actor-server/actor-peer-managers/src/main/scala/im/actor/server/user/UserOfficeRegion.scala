package im.actor.server.user

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import slick.driver.PostgresDriver.api._

import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.social.SocialManagerRegion

object UserOfficeRegion {
  private val idExtractor: ShardRegion.IdExtractor = {
    case c: UserCommand ⇒ (c.userId.toString, c)
    case q: UserQuery   ⇒ (q.userId.toString, q)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case c: UserCommand ⇒ (c.userId % 100).toString // TODO: configurable
    case q: UserQuery   ⇒ (q.userId % 100).toString
  }

  private def start(props: Option[Props])(implicit system: ActorSystem): UserOfficeRegion =
    UserOfficeRegion(ClusterSharding(system).start(
      typeName = "UserOffice",
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def start()(
    implicit
    system:              ActorSystem,
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    socialManagerRegion: SocialManagerRegion
  ): UserOfficeRegion =
    start(Some(UserOfficeActor.props))

  def startProxy()(implicit system: ActorSystem): UserOfficeRegion =
    start(None)
}

case class UserOfficeRegion(val ref: ActorRef)