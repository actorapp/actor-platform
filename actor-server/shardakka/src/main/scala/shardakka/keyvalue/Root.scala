package shardakka.keyvalue

import akka.actor.Actor.emptyBehavior
import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.persistence.PersistentActor

import scala.reflect.ClassTag

trait Command {
  val key: String
}

trait RootQuery

trait RootEvent

private case class CreateAck[A](key: String, ack: A, replyTo: ActorRef)

private case class DeleteAck[A](key: String, ack: A, replyTo: ActorRef)

abstract class Root[CreateCommand <: Command : ClassTag, CreateCommandAck: ClassTag, DeleteCommand <: Command : ClassTag, DeleteCommandAck: ClassTag]
  extends PersistentActor
  with ActorLogging {

  import RootEvents._
  import RootQueries._

  private[this] var keys = Set.empty[String]

  protected def handleCustom: Receive = emptyBehavior

  override final def receiveCommand: Receive = handleRootCommand.orElse(handleRootQuery).orElse(handleInternal).orElse(handleCustom)

  override final def receiveRecover: Receive = {
    case e: RootEvent ⇒ updateState(e)
  }

  protected def getKeys = keys

  private def handleRootCommand: Receive = {
    case cmd: CreateCommand =>
      val replyTo = sender()
      val value = valueActorOf(cmd.key)

      context.actorOf(Props(new Actor {
        value ! cmd

        def receive = {
          case ack: CreateCommandAck =>
            context.parent ! CreateAck(cmd.key, ack, replyTo)
            context stop self
          case Failure(e) =>
            log.error(e, "Failed to create item")
        }
      }))
    case cmd: DeleteCommand =>
      val replyTo = sender()
      val value = valueActorOf(cmd.key)

      context.actorOf(Props(new Actor {
        value ! cmd

        def receive = {
          case ack: DeleteCommandAck =>
            context.parent ! DeleteAck(cmd.key, ack, replyTo)
            context stop self
          case Failure(e) =>
            log.error(e, "Failed to delete item")
        }
      }))
    case CreateAck(key, ack, replyTo) =>
      if (keyExists(key)) {
        replyTo ! ack
      } else {
        persist(KeyCreated(key)) { e =>
          updateState(e)
          replyTo ! ack
        }
      }
    case DeleteAck(key, ack, replyTo) =>
      if (!keyExists(key)) {
        replyTo ! ack
      } else {
        persist(KeyDeleted(key)) { e =>
          updateState(e)
          replyTo ! ack
        }
      }
  }

  private def handleRootQuery: Receive = {
    case GetKeys() ⇒ sender ! GetKeysResponse(keys.toSeq)
  }

  private def handleInternal: Receive = {
    case End ⇒ context stop self
  }

  private def updateState(e: RootEvent): Unit = e match {
    case KeyCreated(key) ⇒ keys += key
    case KeyDeleted(key) ⇒ keys -= key
  }

  private def keyExists(key: String): Boolean = keys.contains(key)

  protected def valueActorOf(key: String): ActorRef
}