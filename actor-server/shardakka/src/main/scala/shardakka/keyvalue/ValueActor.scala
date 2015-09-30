package shardakka.keyvalue

import akka.actor.{ Props, ReceiveTimeout }
import akka.persistence.PersistentActor
import com.google.protobuf.ByteString
import shardakka.ShardakkaExtension

trait ValueCommand extends Command

trait ValueQuery {
  val key: String
}

object ValueActor {
  def props(name: String) = Props(classOf[ValueActor], name)
}

final class ValueActor(name: String) extends PersistentActor {

  import ValueCommands._
  import ValueEvents._
  import ValueQueries._

  context.setReceiveTimeout(ShardakkaExtension.CacheTTL)

  override def persistenceId = ShardakkaExtension.KVPersistencePrefix + "_" + name + "_" + self.path.name

  private var value: Option[ByteString] = None

  override def receiveCommand: Receive = {
    case Upsert(_, newValue) ⇒
      persist(ValueUpdated(newValue)) { e ⇒
        value = Some(newValue)
        sender() ! Ack()
      }
    case Delete(_) ⇒
      if (value.isDefined) {
        persist(ValueDeleted()) { e ⇒
          value = None
          sender() ! Ack()
        }
      } else {
        sender() ! Ack()
      }
    case Get(_) ⇒
      sender() ! GetResponse(value)
    case ReceiveTimeout ⇒
      context stop self
  }

  override def receiveRecover: Receive = {
    case ValueUpdated(newValue) ⇒ value = Some(newValue)
    case ValueDeleted()         ⇒ value = None
  }
}
