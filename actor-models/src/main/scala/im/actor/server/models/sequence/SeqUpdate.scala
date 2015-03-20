package im.actor.server.models.sequence

import com.eaio.uuid.UUID
import org.joda.time.DateTime

case class Ref(id: UUID) {
  def toByteArray: Array[Byte] =
    RefCodec.encode(this).require.toByteArray
}

case class SeqUpdate(authId: Long, ref: Ref, seq: Int, date: DateTime, header: Int, serializedData: Array[Byte])

object SeqUpdate {
  def apply(authId: Long, seq: Int, header: Int, serializedData: Array[Byte]): SeqUpdate = {
    SeqUpdate(
      authId = authId,
      ref = Ref(new UUID()),
      seq = seq,
      date = new DateTime,
      header = header,
      serializedData = serializedData
    )
  }
}
