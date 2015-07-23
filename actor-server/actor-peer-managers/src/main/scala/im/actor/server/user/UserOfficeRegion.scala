package im.actor.server.user

import akka.actor.{ ActorSystem, Props, ActorRef }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import slick.driver.PostgresDriver.api._

import im.actor.server.office.user.UserEnvelope
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.social.SocialManagerRegion

object UserOfficeRegion {
  private val idExtractor: ShardRegion.IdExtractor = {
    case UserEnvelope(userId, payload) ⇒ (userId.toString, payload)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case UserEnvelope(userId, _) ⇒ (userId % 100).toString // TODO: configurable
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