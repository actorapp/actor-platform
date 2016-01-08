package im.actor.server.mtproto.codecs.transport

import java.util.zip.CRC32

import scodec.bits._
import scodec.{ codecs ⇒ C, _ }
import shapeless._

import im.actor.server.mtproto.codecs._
import im.actor.server.mtproto.transport._

class MTProtoDecoder(header: Int) extends Decoder[MTProto] {
  override def decode(bits: BitVector) = {
    val decoder: Decoder[MTProto] = header match {
      case Handshake.header         ⇒ HandshakeCodec.asDecoder
      case HandshakeResponse.header ⇒ HandshakeResponseCodec.asDecoder
      case Package.header           ⇒ PackageCodec.asDecoder
      case Ping.header              ⇒ PingCodec.asDecoder
      case Pong.header              ⇒ PongCodec.asDecoder
      case Drop.header              ⇒ DropCodec.asDecoder
      case Redirect.header          ⇒ RedirectCodec.asDecoder
      case InternalError.header     ⇒ InternalErrorCodec.asDecoder
      case Ack.header               ⇒ AckCodec.asDecoder
    }

    decoder.decode(bits)
  }
}

object MTProtoEncoder extends Encoder[MTProto] {
  override val sizeBound = SizeBound.unknown

  override def encode(mtp: MTProto) = {
    mtp match {
      case x: Handshake         ⇒ HandshakeCodec.encode(x)
      case x: HandshakeResponse ⇒ HandshakeResponseCodec.encode(x)
      case x: Package           ⇒ PackageCodec.encode(x)
      case x: Ping              ⇒ PingCodec.encode(x)
      case x: Pong              ⇒ PongCodec.encode(x)
      case x: Drop              ⇒ DropCodec.encode(x)
      case x: Redirect          ⇒ RedirectCodec.encode(x)
      case x: InternalError     ⇒ InternalErrorCodec.encode(x)
      case x: Ack               ⇒ AckCodec.encode(x)
    }
  }
}

class SignedMTProtoDecoder(header: Int, size: Int) extends Decoder[MTProto] {
  private val codec = C.fixedSizeBytes(size.toLong, C.bytes) :: C.uint32

  override def decode(bits: BitVector) = {
    codec.decode(bits) flatMap {
      case DecodeResult(mtprotoBytes :: crc32 :: HNil, remainder) ⇒
        val c = new CRC32
        c.update(mtprotoBytes.toArray)

        if (c.getValue != crc32) {
          Attempt.failure(Err("Invalid package CRC32"))
        } else {
          val decoder = new MTProtoDecoder(header)

          decoder.decode(mtprotoBytes.toBitVector) flatMap {
            case DecodeResult(mtproto, rem) if rem.isEmpty ⇒
              Attempt.Successful(DecodeResult(mtproto, remainder))
            case _ ⇒ Attempt.failure(Err("Excess bytes in mtproto body"))
          }
        }
    }
  }
}

object TransportPackageCodec extends Codec[TransportPackage] {
  override def sizeBound = SizeBound.unknown

  override def encode(tp: TransportPackage) = {
    for {
      indexBits ← C.int32.encode(tp.index)
      headerBits ← C.uint8.encode(tp.body.header)
      bodyBits ← MTProtoEncoder.encode(tp.body)
      // FIXME: validate if body length fits in int32
      lengthBits ← C.int32.encode((bodyBits.size / byteSize).toInt)
      crc32 = new CRC32
      _ = crc32.update(bodyBits.toByteBuffer)
      crc32Bits ← C.uint32.encode(crc32.getValue)
    } yield indexBits ++ headerBits ++ lengthBits ++ bodyBits ++ crc32Bits
  }

  override def decode(bits: BitVector) = {
    for {
      indexRes ← C.int32.decode(bits)
      headerRes ← C.uint8.decode(indexRes.remainder)
      lengthRes ← C.int32.decode(headerRes.remainder)
      bodyRes ← (new SignedMTProtoDecoder(headerRes.value, lengthRes.value)).decode(lengthRes.remainder)
    } yield {
      DecodeResult(TransportPackage(indexRes.value, bodyRes.value), bodyRes.remainder)
    }
  }
}
