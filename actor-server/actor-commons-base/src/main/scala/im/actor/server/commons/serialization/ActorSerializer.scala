package im.actor.server.commons.serialization

import akka.serialization._
import com.google.protobuf.{ GeneratedMessage ⇒ GGeneratedMessage, ByteString }
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap.{ Builder ⇒ MapBuilder }
import com.trueaccord.scalapb.GeneratedMessage

import scala.util.{ Failure, Success }

object ActorSerializer {
  // FIXME: dynamically increase capacity
  private val map = new MapBuilder[Int, Class[_]].maximumWeightedCapacity(1024).build()
  private val reverseMap = new MapBuilder[Class[_], Int].maximumWeightedCapacity(1024).build()

  def register(id: Int, clazz: Class[_]): Unit = {
    if (map.containsKey(id))
      throw new IllegalArgumentException(s"There is already a mapping with id ${id}")

    map.put(id, Class.forName(clazz.getName + '$'))
    reverseMap.put(clazz, id)
  }

  def get(id: Int): Option[Class[_]] = Option(map.get(id))

  def get(clazz: Class[_]) = Option(reverseMap.get(clazz))
}

class ActorSerializer extends Serializer {
  private val ARRAY_OF_BYTE_ARRAY = Array[Class[_]](classOf[Array[Byte]])

  override def identifier: Int = 3456

  override def includeManifest: Boolean = false

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = {
    val SerializedMessage(id, bodyBytes) = SerializedMessage.parseFrom(bytes)

    ActorSerializer.get(id) match {
      case Some(clazz) ⇒
        val field = clazz.getField("MODULE$").get(null)

        clazz
          .getDeclaredMethod("validate", ARRAY_OF_BYTE_ARRAY: _*)
          .invoke(field, bodyBytes.toByteArray) match {
            case Success(msg) ⇒ msg.asInstanceOf[GeneratedMessage]
            case Failure(e)   ⇒ throw e
          }
      case None ⇒ throw new IllegalArgumentException(s"Can't find mapping for id ${id}")
    }
  }

  override def toBinary(o: AnyRef): Array[Byte] = {
    ActorSerializer.get(o.getClass) match {
      case Some(id) ⇒
        o match {
          case m: GeneratedMessage  ⇒ SerializedMessage(id, ByteString.copyFrom(m.toByteArray)).toByteArray
          case m: GGeneratedMessage ⇒ SerializedMessage(id, ByteString.copyFrom(m.toByteArray)).toByteArray
          case _                    ⇒ throw new IllegalArgumentException(s"Can't serialize non-scalapb message [${o}]")
        }
      case None ⇒
        throw new IllegalArgumentException(s"Can't find mapping for message [${o}]")
    }
  }
}
