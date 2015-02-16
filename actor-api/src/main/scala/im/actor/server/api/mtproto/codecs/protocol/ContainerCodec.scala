package im.actor.server.api.mtproto.codecs.protocol

import im.actor.server.api.mtproto.protocol._
import im.actor.server.api.mtproto.codecs._
import scodec.bits.BitVector
import scodec.{ Codec, Err }
import scala.annotation.tailrec
import scalaz._
import Scalaz._

object ContainerCodec extends Codec[Container] {
  def encode(c: Container) = {
    val body = c.messages.map(MessageBoxCodec.encodeValid).foldLeft(BitVector.empty)(_ ++ _)
    val count = varint.encodeValid(c.messages.length)
    (count ++ body).right
  }

  def decode(buf: BitVector) = {
    @inline @tailrec
    def f(count: Int, xs: BitVector)(items: Seq[MessageBox]): Err \/ (BitVector, Seq[MessageBox]) = {
      if (count > items.length) {
        MessageBoxCodec.decode(xs) match {
          case \/-((bs, mb)) =>
            if (mb.body.isInstanceOf[Container]) Err("Container cannot be nested").left
            else f(count, bs)(items.:+(mb))
          case -\/(e) => e.left
        }
      } else (xs, items).right
    }

    for {
      lenTup <- varint.decode(buf)
      (bs, count) = lenTup
      itemsTup <- f(count.toInt, bs)(Seq())
      (xs, items) = itemsTup
    } yield (xs, Container(items))
  }
}
