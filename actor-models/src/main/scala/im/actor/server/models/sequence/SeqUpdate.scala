package im.actor.server.models.sequence

import com.eaio.uuid.UUID
import org.joda.time.DateTime

case class Ref(id: UUID)
case class SeqUpdate(authId: Long, ref: Ref, date: DateTime, header: Int, serializedData: Array[Byte])
