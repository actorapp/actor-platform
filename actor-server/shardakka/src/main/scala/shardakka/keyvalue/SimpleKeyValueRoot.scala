package shardakka.keyvalue

import akka.actor.{ ActorRef, Props }
import shardakka.ShardakkaExtension

object SimpleKeyValueRoot {
  def props(name: String): Props =
    Props(classOf[SimpleKeyValueRoot], name)
}

final class SimpleKeyValueRoot(name: String) extends Root {
  override def persistenceId = ShardakkaExtension.KVPersistencePrefix + "_" + name + "_root"

  protected override def handleCustom: Receive = {
    case cmd @ ValueCommands.Upsert(key, _) ⇒
      create[ValueCommands.Ack](key, cmd)
    case cmd @ ValueCommands.Delete(key) ⇒
      delete[ValueCommands.Ack](key, cmd)
    case query: ValueQuery ⇒
      valueActorOf(query.key) forward query
  }

  protected override def valueActorOf(key: String): ActorRef = {
    context.child(key).getOrElse(context.actorOf(ValueActor.props(name), key))
  }
}
