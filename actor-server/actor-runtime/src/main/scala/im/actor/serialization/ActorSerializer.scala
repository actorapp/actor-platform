package im.actor.serialization

import akka.serialization._
import com.google.protobuf.{ ByteString, GeneratedMessage ⇒ GGeneratedMessage }
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap.{ Builder ⇒ MapBuilder }
import com.trueaccord.scalapb.GeneratedMessage

import scala.util.{ Failure, Success }

object ActorSerializer {
  private val ARRAY_OF_BYTE_ARRAY = Array[Class[_]](classOf[Array[Byte]])

  // FIXME: dynamically increase capacity
  private val map = new MapBuilder[Int, Class[_]].maximumWeightedCapacity(1024).build()
  private val reverseMap = new MapBuilder[Class[_], Int].maximumWeightedCapacity(1024).build()

  def clean(): Unit = {
    map.clear()
    reverseMap.clear()
  }

  def register(id: Int, clazz: Class[_]): Unit = {
    get(id) match {
      case None ⇒
        get(clazz) match {
          case Some(regId) ⇒ throw new IllegalArgumentException(s"There is already a mapping for class: ${clazz}, id: ${regId}")
          case None ⇒
            map.put(id, Class.forName(clazz.getName + '$'))
            reverseMap.put(clazz, id)
        }
      case Some(registered) ⇒
        if (!get(clazz).exists(_ == id))
          throw new IllegalArgumentException(s"There is already a mapping with id ${id}: ${map.get(id)}")
    }
  }

  def register(items: (Int, Class[_])*): Unit =
    items foreach { case (id, clazz) ⇒ register(id, clazz) }

  def get(id: Int): Option[Class[_]] = Option(map.get(id))

  def get(clazz: Class[_]) = Option(reverseMap.get(clazz))

  def fromBinary(bytes: Array[Byte]): AnyRef = {
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

  def toBinary(o: AnyRef): Array[Byte] = {
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

class ActorSerializer extends Serializer {

  override def identifier: Int = 3456

  override def includeManifest: Boolean = false

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = ActorSerializer.fromBinary(bytes)

  override def toBinary(o: AnyRef): Array[Byte] = ActorSerializer.toBinary(o)
}
