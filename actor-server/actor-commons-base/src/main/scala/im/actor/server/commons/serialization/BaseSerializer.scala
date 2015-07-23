package im.actor.server.commons.serialization

import scala.collection.JavaConversions._

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer
import com.typesafe.config.ConfigFactory

class KryoInit {
  def customize(kryo: Kryo): Unit = {
    ConfigFactory.load().getConfig("akka.actor.kryo.tagged-mappings").root.unwrapped() foreach {
      case (className, id: java.lang.Integer) ⇒
        val clazz: Class[_ <: AnyRef] = Class.forName(className).asInstanceOf[Class[_ <: AnyRef]]
        val serializer = new TaggedFieldSerializer(kryo, clazz)
        kryo.register(clazz, serializer, id)
    }
  }
}

trait KryoSerializable extends Serializable
