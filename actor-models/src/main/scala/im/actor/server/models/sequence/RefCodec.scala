package im.actor.server.models.sequence

import com.eaio.uuid.UUID
import scodec.bits.BitVector
import scodec.codecs._
import scodec.Codec

object RefCodec extends Codec[Ref] {
  val codec = int64 ~ int64

  override def sizeBound = codec.sizeBound

  override def encode(ref: Ref) =
    codec.encode((ref.id.time, ref.id.clockSeqAndNode))

  override def decode(bits: BitVector) = {
    codec.decode(bits) map (_ map { case (m, l) => Ref(new UUID(m, l)) })
  }
}
