package im.actor.server.commons.serialization

import akka.actor.ExtendedActorSystem
import com.esotericsoftware.kryo.Kryo
import com.twitter.chill.akka.AkkaSerializer
import com.twitter.chill.{ IKryoRegistrar, KryoInstantiator, toRich }
import de.javakaffee.kryoserializers.jodatime.JodaDateTimeSerializer

trait TaggedFieldSerializable

class BaseSerializer(system: ExtendedActorSystem) extends AkkaSerializer(system) {

  override def kryoInstantiator: KryoInstantiator = {
    super
      .kryoInstantiator
      .withRegistrar(new JodaDateTimeRegistrar)
      .withRegistrar(new CustomSerializersRegistrar)
  }
}

class JodaDateTimeRegistrar extends JodaDateTimeSerializer with IKryoRegistrar {
  override def apply(kryo: Kryo): Unit = {
    if (!kryo.alreadyRegistered(classOf[org.joda.time.DateTime])) {
      kryo.forClass[org.joda.time.DateTime](this)
    }
  }
}
