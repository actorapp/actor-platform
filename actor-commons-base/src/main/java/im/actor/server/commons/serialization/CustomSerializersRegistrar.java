package im.actor.server.commons.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import com.twitter.chill.IKryoRegistrar;
import com.twitter.chill.RichKryo;

class CustomSerializersRegistrar implements IKryoRegistrar {
    @Override
    public void apply(Kryo kryo) {
        RichKryo rkryo = new RichKryo(kryo);
        if (!rkryo.alreadyRegistered(TaggedFieldSerializable.class)) {
            kryo.register(TaggedFieldSerializable.class);
            kryo.addDefaultSerializer(TaggedFieldSerializable.class, TaggedFieldSerializer.class);
        }
    }
}