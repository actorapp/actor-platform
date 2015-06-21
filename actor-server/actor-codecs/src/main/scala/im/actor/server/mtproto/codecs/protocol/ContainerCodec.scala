package im.actor.server.mtproto.codecs.protocol

import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.codecs._
import scodec.bits.BitVector
import scodec._
import scala.annotation.tailrec

object ContainerCodec extends Codec[Container] {
  def sizeBound = SizeBound.unknown

  def encode(c: Container) = {
    val body = c.messages.map(MessageBoxCodec.encode).foldLeft(BitVector.empty)(_ ++ _.require)
    val count = varint.encode(c.messages.length.toLong).require
    Attempt.successful(count ++ body)
  }

  def decode(buf: BitVector) = {
    @inline @tailrec
    def f(count: Int, xs: BitVector)(items: Seq[MessageBox]): Attempt[DecodeResult[Seq[MessageBox]]] = {
      if (count > items.length) {
        MessageBoxCodec.decode(xs) match {
          case Attempt.Successful(DecodeResult(MessageBox(_, _: Container), bs)) ⇒
            Attempt.failure(Err("Container cannot be nested"))
          case Attempt.Successful(DecodeResult(mb, bs)) ⇒
            f(count, bs)(items.:+(mb))
          case Attempt.Failure(e) ⇒
            Attempt.failure(e)
        }
      } else Attempt.successful(DecodeResult(items, xs))
    }

    for {
      lenTup ← varint.decode(buf)
      itemsTup ← f(lenTup.value.toInt, lenTup.remainder)(Seq.empty)
    } yield DecodeResult(Container(itemsTup.value), itemsTup.remainder)
  }
}
