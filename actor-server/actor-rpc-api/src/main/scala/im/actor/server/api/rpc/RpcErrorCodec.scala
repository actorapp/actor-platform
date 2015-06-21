package im.actor.server.api.rpc

import scodec._
import scodec.bits.BitVector
import scodec.codecs._
import shapeless._

import im.actor.api.rpc.RpcError
import im.actor.server.mtproto.codecs._

object RpcErrorCodec extends Codec[RpcError] {
  def sizeBound = SizeBound.unknown

  private val codec = (int32 :: StringCodec :: StringCodec :: BooleanCodec :: BytesCodec).exmap(
    {
      case code :: tag :: userMessage :: canTryAgain :: edData :: HNil ⇒
        tag match {
          case _ ⇒
            Attempt.Successful(RpcError(code, tag, userMessage, canTryAgain, None))
        }
    }, { re: RpcError ⇒
      re match {
        case RpcError(code, tag, userMessage, canTryAgain, optEd) ⇒
          Attempt.successful(code :: tag :: userMessage :: canTryAgain :: optEd.map(ed ⇒ BitVector(ed.toByteArray)).getOrElse(BitVector.empty) :: HNil)
      }
    }
  )

  def encode(re: RpcError) = codec.encode(re)

  def decode(buf: BitVector) = codec.decode(buf)
}

