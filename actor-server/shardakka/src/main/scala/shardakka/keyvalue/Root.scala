package shardakka.keyvalue

import akka.actor.Actor.emptyBehavior
import akka.actor.Status.Failure
import akka.actor._
import akka.persistence.PersistentActor

import scala.reflect.ClassTag

trait Command {
  val key: String
}

trait RootQuery

trait RootEvent

private case class CreateAck[A](key: String, ack: A, replyTo: ActorRef)

private case class DeleteAck[A](key: String, ack: A, replyTo: ActorRef)

abstract class Root
  extends PersistentActor
  with ActorLogging {

  import RootEvents._
  import RootQueries._

  private[this] var keys = Set.empty[String]

  protected def handleCustom: Receive = emptyBehavior
  protected def handleCustomRecover: Receive = emptyBehavior
  protected def onKeyCreate(key: String): Unit = ()
  protected def onKeyDelete(key: String): Unit = ()

  override final def receiveCommand: Receive = handleRootCommand.orElse(handleRootQuery).orElse(handleInternal).orElse(handleCustom)

  override final def receiveRecover: Receive = handleCustomRecover orElse {
    case e: RootEvent ⇒ updateState(e)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    log.error(reason, "Failure while processing {} from {}", message, sender())
    sender() ! Status.Failure(reason)
  }

  protected def getKeys = keys

  private def handleRootCommand: Receive = {
    case CreateAck(key, ack, replyTo) ⇒
      createKey(key) {
        replyTo ! ack
      }
    case DeleteAck(key, ack, replyTo) ⇒
      deleteKey(key) {
        replyTo ! ack
      }
  }

  protected def create[Ack: ClassTag](key: String, cmd: Any): Unit = {
    val replyTo = sender()
    val value = valueActorOf(key)

    context.actorOf(Props(new Actor {
      value ! cmd

      def receive = {
        case ack: Ack ⇒
          context.parent ! CreateAck(key, ack, replyTo)
          context stop self
        case Failure(e) ⇒
          log.error(e, "Failed to create item")
      }
    }))
  }

  protected def delete[Ack: ClassTag](key: String, cmd: Any): Unit = {
    val replyTo = sender()
    val value = valueActorOf(key)

    context.actorOf(Props(new Actor {
      value ! cmd

      def receive = {
        case ack: Ack ⇒
          context.parent ! DeleteAck(key, ack, replyTo)
          context stop self
        case Failure(e) ⇒
          log.error(e, "Failed to delete item")
      }
    }))
  }

  protected def createKey(key: String)(f: ⇒ Unit): Unit = {
    if (keyExists(key)) {
      log.error("Key {} already exists", key)
      f
    } else {
      persist(KeyCreated(key)) { e ⇒
        updateState(e)
        f
      }
    }
  }

  protected def deleteKey(key: String)(f: ⇒ Unit): Unit = {
    if (keyExists(key)) {
      persist(KeyDeleted(key)) { e ⇒
        updateState(e)
        f
      }
    } else {
      log.error("Key {} is already deleted", key)
      f
    }
  }

  protected def transactional(key: String, cmd: Any)(f: Receive): Unit = {
    val replyTo = sender()
    val valueActor = valueActorOf(key)
    context watch valueActor
    valueActor ! cmd
    context become (f orElse {
      case Terminated(`valueActor`) ⇒
        replyTo ! Status.Failure(new Exception(s"Value actor ${key} is terminated"))
        end()
      case failure: Status.Failure ⇒
        replyTo ! failure
        end()
      case _ ⇒ stash()
    }, discardOld = false)
  }

  protected def end(): Unit = {
    unstashAll()
    context.unbecome()
  }

  private def handleRootQuery: Receive = {
    case GetKeys() ⇒ sender ! GetKeysResponse(keys.toSeq)
  }

  private def handleInternal: Receive = {
    case End ⇒ context stop self
  }

  private def updateState(e: RootEvent): Unit = e match {
    case KeyCreated(key) ⇒
      keys += key
      onKeyCreate(key)
    case KeyDeleted(key) ⇒
      keys -= key
      onKeyDelete(key)
  }

  private def keyExists(key: String): Boolean = keys.contains(key)

  protected def valueActorOf(key: String): ActorRef
}