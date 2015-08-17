package shardakka.keyvalue

import akka.actor._
import akka.contrib.pattern.{ ClusterSingletonProxy, ClusterSingletonManager }
import akka.pattern.ask
import akka.util.Timeout
import im.actor.server.commons.serialization.ActorSerializer
import shardakka.{ StringCodec, Codec, ShardakkaExtension }

import scala.collection.immutable
import scala.concurrent.{ ExecutionContext, Future }

private case object End

case class SimpleKeyValue[A](
  name:              String,
  private val root:  ActorRef,
  private val proxy: ActorRef,
  private val codec: Codec[A]
) {
  def upsert(key: String, value: A)(implicit ec: ExecutionContext, timeout: Timeout): Future[Unit] =
    (proxy ? RootCommands.Upsert(key, codec.toBytes(value))) map (_ ⇒ ())

  def delete(key: String)(implicit ec: ExecutionContext, timeout: Timeout): Future[Unit] =
    (proxy ? RootCommands.Delete(key)) map (_ ⇒ ())

  def get(key: String)(implicit ec: ExecutionContext, timeout: Timeout): Future[Option[A]] =
    (proxy ? ValueQueries.Get(key)).mapTo[ValueQueries.GetResponse] map (_.value.map(codec.fromBytes))

  def getKeys()(implicit ec: ExecutionContext, timeout: Timeout): Future[Seq[String]] =
    (proxy ? RootQueries.GetKeys()).mapTo[RootQueries.GetKeysResponse] map (_.keys)

  private[keyvalue] def shutdown(): Unit = {
    proxy ! End
    root ! PoisonPill
    proxy ! PoisonPill
  }
}

trait SimpleKeyValueExtension {
  this: ShardakkaExtension ⇒

  ActorSerializer.register(10001, classOf[RootCommands.Upsert])
  ActorSerializer.register(10002, classOf[RootCommands.Delete])
  ActorSerializer.register(10003, classOf[RootCommands.Ack])

  ActorSerializer.register(12001, classOf[RootEvents.KeyCreated])
  ActorSerializer.register(12002, classOf[RootEvents.KeyDeleted])

  ActorSerializer.register(13001, classOf[ValueCommands.Upsert])
  ActorSerializer.register(13002, classOf[ValueCommands.Delete])
  ActorSerializer.register(13003, classOf[ValueCommands.Ack])

  ActorSerializer.register(14001, classOf[ValueQueries.Get])
  ActorSerializer.register(14002, classOf[ValueQueries.GetResponse])

  ActorSerializer.register(15001, classOf[ValueEvents.ValueUpdated])
  ActorSerializer.register(15002, classOf[ValueEvents.ValueDeleted])

  @volatile
  private var kvs = immutable.Map.empty[String, SimpleKeyValue[_]]

  def simpleKeyValue[A](name: String, codec: Codec[A])(implicit system: ActorSystem): SimpleKeyValue[A] = {
    kvs.get(name) match {
      case Some(kv) ⇒ kv.asInstanceOf[SimpleKeyValue[A]]
      case None ⇒
        val manager = system.actorOf(
          ClusterSingletonManager.props(
            singletonProps = SimpleKeyValueRoot.props(name),
            singletonName = name,
            terminationMessage = End,
            role = None
          ), name = s"SimpleKeyValueRoot-$name"
        )

        val proxy = system.actorOf(
          ClusterSingletonProxy.props(singletonPath = s"/user/SimpleKeyValueRoot-$name/$name", role = None),
          name = s"SimpleKeyValueRoot-$name-Proxy"
        )

        val kv = SimpleKeyValue(name, manager, proxy, codec)
        kvs += (name → kv)
        kv
    }
  }

  def simpleKeyValue(name: String)(implicit system: ActorSystem): SimpleKeyValue[String] =
    simpleKeyValue(name, StringCodec)

  def shutdownKeyValue(name: String) = kvs.get(name) foreach { kv ⇒
    kv.shutdown()
    kvs -= name
  }
}

