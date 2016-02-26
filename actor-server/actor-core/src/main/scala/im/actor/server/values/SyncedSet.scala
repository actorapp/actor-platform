package im.actor.server.values

import akka.actor.{ ActorSystem, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings }
import akka.pattern.ask
import akka.util.Timeout
import im.actor.api.rpc.values.{ ApiSyncedValue, UpdateSyncedSetAddedOrUpdated, UpdateSyncedSetRemoved, UpdateSynedSetUpdated }
import im.actor.concurrent.AlertingActor
import im.actor.server.sequence.WeakUpdatesExtension

import scala.concurrent.Future

private[values] trait SyncedSet {
  import SyncedSetValue._

  val system: ActorSystem
  import system.dispatcher

  implicit val defaultTimeout: Timeout

  object syncedSet {
    private val region =
      ClusterSharding(system)
        .start(
          "Values.SyncedSet",
          props,
          ClusterShardingSettings(system),
          {
            case Envelope(userId, name, msg) ⇒ (s"${userId}_$name", msg)
          },
          {
            case Envelope(userId, _, _) ⇒ (userId % 100).toString
          }
        )

    def weakGroup(name: String) = name

    def loadApiValues(userId: Int, name: String): Future[Vector[ApiSyncedValue]] =
      (region ? Envelope(userId, name, LoadApiValues)).mapTo[LoadApiValuesAck] map (_.values)

    def put(userId: Int, name: String, key: Long, value: Array[Byte]): Future[Unit] =
      (region ? Envelope(userId, name, Put(Seq(key → Some(value))))) map (_ ⇒ ())

    def delete(userId: Int, name: String, key: Long): Future[Unit] =
      (region ? Envelope(userId, name, Delete(Seq(key)))) map (_ ⇒ ())
  }
}

private object SyncedSetValue {
  sealed trait Message
  final case class Envelope(userId: Int, name: String, message: Message)

  final case class Put(values: Seq[(Long, Option[Array[Byte]])]) extends Message
  case object PutAck

  final case class Delete(ids: Seq[Long]) extends Message
  case object DeleteAck

  final case class ReplaceAll(values: Seq[(Long, Option[Array[Byte]])]) extends Message
  case object ReplaceAllAck

  case object LoadApiValues extends Message
  final case class LoadApiValuesAck(values: Vector[ApiSyncedValue])

  def props = Props(classOf[SyncedSetValue])
}

private final class SyncedSetValue extends AlertingActor {
  import SyncedSetValue._

  private val (userId, name) = self.path.name.split("_").toList match {
    case id :: n :: Nil ⇒ (id.toInt, n)
    case _              ⇒ throw new RuntimeException("Wrong path")
  }

  private val weakUpdExt = WeakUpdatesExtension(context.system)

  private var set = Map.empty[Long, Option[Array[Byte]]]

  def receive = {
    case Put(values) ⇒
      set ++= values
      val update = UpdateSyncedSetAddedOrUpdated(name, apiValues(values))
      weakUpdExt.broadcastUserWeakUpdate(userId, update)
      sender() ! PutAck
    case Delete(ids) ⇒
      set --= ids
      val update = UpdateSyncedSetRemoved(name, ids.toVector)
      weakUpdExt.broadcastUserWeakUpdate(userId, update)
      sender() ! DeleteAck
    case ReplaceAll(values) ⇒
      set = values.toMap
      val update = UpdateSynedSetUpdated(name, apiValues(set.toSeq), isStrong = Some(false))
      weakUpdExt.broadcastUserWeakUpdate(userId, update)
      sender() ! ReplaceAllAck
    case LoadApiValues ⇒
      sender() ! LoadApiValuesAck(apiValues(set.toSeq))
  }

  private def apiValues(values: Seq[(Long, Option[Array[Byte]])]): Vector[ApiSyncedValue] =
    values.toVector map { case (id, value) ⇒ ApiSyncedValue(id, value) }
}