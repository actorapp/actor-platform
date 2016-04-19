package im.actor.server.social

import im.actor.config.ActorConfig
import im.actor.server.persist.social.RelationRepo

import scala.concurrent.Future
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{ Success, Failure }

import akka.actor._
import akka.cluster.sharding.{ ClusterShardingSettings, ClusterSharding, ShardRegion }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import slick.driver.PostgresDriver.api._

import im.actor.server.db.DbExtension

sealed trait SocialExtension extends Extension {
  val region: SocialManagerRegion
}

final class SocialExtensionImpl(system: ActorSystem, db: Database) extends SocialExtension {
  import system.dispatcher
  implicit lazy val region: SocialManagerRegion = SocialManager.startRegion()(system, db)
  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  def getRelations(userId: Int) = SocialManager.getRelations(userId)
}

object SocialExtension extends ExtensionId[SocialExtensionImpl] with ExtensionIdProvider {
  override def lookup = SocialExtension

  override def createExtension(system: ExtendedActorSystem) = new SocialExtensionImpl(system, DbExtension(system).db)
}

@SerialVersionUID(1L)
case class SocialManagerRegion(ref: ActorRef)

object SocialManager {
  private case class Envelope(userId: Int, payload: Message)

  private sealed trait Message

  @SerialVersionUID(1L)
  private case class RelationsNoted(userIds: Set[Int]) extends Message

  @SerialVersionUID(1L)
  private case class RelationNoted(userId: Int) extends Message

  @SerialVersionUID(1L)
  private case object GetRelations extends Message

  @SerialVersionUID(1L)
  private case class Relations(userIds: Set[Int])

  @SerialVersionUID(1L)
  private case class Initiated(userIds: Set[Int])

  private val extractEntityId: ShardRegion.ExtractEntityId = {
    case env @ Envelope(userId, payload) ⇒ (userId.toString, env)
  }

  private val extractShardId: ShardRegion.ExtractShardId = msg ⇒ msg match {
    case Envelope(userId, _) ⇒ (userId % 100).toString // TODO: configurable
  }

  private val typeName = "SocialManager"

  private def startRegion(props: Props)(implicit system: ActorSystem): SocialManagerRegion =
    SocialManagerRegion(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))

  def startRegion()(implicit system: ActorSystem, db: Database): SocialManagerRegion =
    startRegion(props)

  def startRegionProxy()(implicit system: ActorSystem): SocialManagerRegion =
    SocialManagerRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))

  def props(implicit db: Database) = Props(classOf[SocialManager], db)

  def recordRelations(userId: Int, relatedTo: Set[Int])(implicit region: SocialManagerRegion): Unit = {
    region.ref ! Envelope(userId, RelationsNoted(relatedTo))
  }

  def recordRelation(userId: Int, relatedTo: Int)(implicit region: SocialManagerRegion): Unit = {
    region.ref ! Envelope(userId, RelationNoted(relatedTo))
  }

  def getRelations(userId: Int)(
    implicit
    region:  SocialManagerRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Set[Int]] = {
    region.ref.ask(Envelope(userId, GetRelations)).mapTo[Relations] map (_.userIds)
  }
}

private class SocialManager(implicit db: Database) extends Actor with ActorLogging with Stash {
  import SocialManager._

  implicit val ec: ExecutionContext = context.dispatcher

  context.setReceiveTimeout(15.minutes) // TODO: configurable

  def receive = {
    case env @ Envelope(userId, _) ⇒
      stash()

      db.run(RelationRepo.fetch(userId)) onComplete {
        case Success(userIds) ⇒
          self ! Initiated(userIds.toSet)
        case Failure(e) ⇒
          log.error(e, "Failed to load realations")
          context.stop(self)
      }

      context.become(stashing)
    case msg ⇒
      stash()
  }

  def stashing: Receive = {
    case Initiated(userIds: Set[Int]) ⇒
      unstashAll()
      context.become(working(userIds))
    case msg ⇒
      stash()
  }

  def working(userIds: Set[Int]): Receive = {
    case env @ Envelope(userId, RelationsNoted(notedUserIds)) ⇒
      val uniqUserIds = notedUserIds.diff(userIds).filterNot(_ == userId)

      if (uniqUserIds.nonEmpty) {
        context.become(working(userIds ++ uniqUserIds))
        db.run(RelationRepo.create(userId, uniqUserIds))
      }
    case env @ Envelope(userId, RelationNoted(notedUserId)) ⇒
      if (!userIds.contains(notedUserId) && userId != notedUserId) {
        context.become(working(userIds + notedUserId))

        db.run(RelationRepo.create(userId, notedUserId))
      }
    case env @ Envelope(userId, GetRelations) ⇒
      sender() ! Relations(userIds)
    case ReceiveTimeout ⇒
      context.parent ! ShardRegion.Passivate(stopMessage = PoisonPill)
  }
}