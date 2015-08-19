package shardakka.keyvalue

import akka.actor.{ ActorRef, ActorLogging, Props }
import akka.persistence.PersistentActor
import com.eaio.uuid.UUID
import shardakka.ShardakkaExtension

trait RootCommand {
  val key: String
}

trait RootQuery

trait RootEvent

object SimpleKeyValueRoot {
  def props(name: String): Props =
    Props(classOf[SimpleKeyValueRoot], name)
}

final class SimpleKeyValueRoot(name: String) extends PersistentActor with ActorLogging {

  import RootCommands._
  import RootQueries._
  import RootEvents._

  type PendingCommand = (RootCommand, ActorRef)

  private[this] var keys = Set.empty[String]
  private[this] var pendingCommands = Map.empty[UUID, PendingCommand]

  override def persistenceId = ShardakkaExtension.KVPersistencePrefix + "_" + name + "_root"

  override def receiveCommand: Receive = {
    case cmd: RootCommand        ⇒ handleRootCommand(cmd, sender())
    case query: RootQuery        ⇒ handleRootQuery(query, sender())
    case ValueCommands.Ack(uuid) ⇒ ack(uuid)
    case query: ValueQuery       ⇒ handleQuery(query)
    case End                     ⇒ context stop self
  }

  override def receiveRecover: Receive = {
    case e: RootEvent ⇒ updateState(e)
  }

  private def handleRootCommand(cmd: RootCommand, sender: ActorRef): Unit = {
    val uuid = new UUID()
    // FIXME: handle UUID collisions
    pendingCommands += (uuid → (cmd → sender))

    val valueCmd = cmd match {
      case cmd @ Upsert(key, value) ⇒ ValueCommands.Upsert(uuid, key, value)
      case cmd @ Delete(key)        ⇒ ValueCommands.Delete(uuid, key)
    }

    valueActorOf(cmd.key) ! valueCmd
  }

  private def ack(uuid: UUID): Unit = {
    pendingCommands get (uuid) match {
      case Some((cmd, sender)) ⇒
        cmd match {
          case Upsert(key, _) ⇒
            if (keyExists(key)) {
              sender ! Ack()
            } else {
              persist(KeyCreated(key)) { e ⇒
                updateState(e)
                sender ! Ack()
              }
            }
          case Delete(key) ⇒
            if (!keyExists(key)) {
              sender ! Ack()
            } else {
              persist(KeyDeleted(key)) { e ⇒
                updateState(e)
                sender ! Ack()
              }
            }
        }
      case None ⇒ log.error("Got ack to a non-existent command")
    }
  }

  private def handleRootQuery(query: RootQuery, sender: ActorRef) = {
    query match {
      case GetKeys() ⇒ sender ! GetKeysResponse(keys.toSeq)
    }
  }

  private def handleQuery(query: ValueQuery) = valueActorOf(query.key) forward query

  private def updateState(e: RootEvent): Unit = e match {
    case KeyCreated(key) ⇒ keys += key
    case KeyDeleted(key) ⇒ keys -= key
  }

  private def keyExists(key: String): Boolean = keys.contains(key)

  private def valueActorOf(key: String): ActorRef = {
    context.child(key).getOrElse(context.actorOf(ValueActor.props(name), key))
  }
}

