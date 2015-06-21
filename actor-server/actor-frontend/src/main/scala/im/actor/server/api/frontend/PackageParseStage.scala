package im.actor.server.api.frontend

import scala.annotation.tailrec
import scala.util.control.NoStackTrace

import akka.actor.ActorSystem
import akka.stream.stage.{ Context, StatefulStage }
import akka.util.ByteString
import scodec.DecodeResult
import scodec.bits.BitVector

import im.actor.server.mtproto.codecs._
import im.actor.server.mtproto.codecs.transport._
import im.actor.server.mtproto.transport.{ Drop, TransportPackage }

private[frontend] final class PackageParseStage(implicit system: ActorSystem)
  extends StatefulStage[ByteString, TransportPackage] {

  private val MaxPackageLength = 1024 * 1024 // TODO: configurable

  sealed trait ParserStep

  @SerialVersionUID(1L)
  case object AwaitPackageHeader extends ParserStep

  @SerialVersionUID(1L)
  case class AwaitPackageBody(header: TransportPackageHeader) extends ParserStep

  @SerialVersionUID(1L)
  case class FailedState(msg: String) extends ParserStep

  type ParseState = (ParserStep, BitVector)

  private[this] var parserState: ParseState = (AwaitPackageHeader, BitVector.empty)

  @inline
  private def failedState(msg: String) = ((FailedState(msg), BitVector.empty), Vector.empty)

  def initial = new State {
    override def onPush(chunk: ByteString, ctx: Context[TransportPackage]) = {
      val (newState, res) = doParse(parserState._1, parserState._2 ++ BitVector(chunk.toByteBuffer))(Vector.empty)
      newState._1 match {
        case FailedState(msg) ⇒
          system.log.debug("Failed to parse connection-level {}", msg)
          // ctx.fail(new IllegalStateException(msg))
          ctx.fail(new Exception(msg) with NoStackTrace)
        case _ ⇒
          parserState = newState
          emit(res.iterator, ctx)
      }
    }

    @tailrec
    private def doParse(state: ParserStep, buf: BitVector)(pSeq: Vector[TransportPackage]): (ParseState, Vector[TransportPackage]) = {
      if (buf.isEmpty) {
        ((state, buf), pSeq)
      } else state match {
        case AwaitPackageHeader ⇒
          if (buf.length < transportPackageHeader.sizeBound.lowerBound) {
            ((AwaitPackageHeader, buf), pSeq)
          } else {
            transportPackageHeader.decode(buf).toEither match {
              case Right(headerRes) ⇒
                if (headerRes.value.bodyLength <= MaxPackageLength) {
                  //system.log.debug("Transport package header {}", headerRes)
                  doParse(AwaitPackageBody(headerRes.value), headerRes.remainder)(pSeq)
                } else {
                  val message = "Transport package length is more than expected"
                  system.log.warning(message)
                  failedState(message)
                }
              case Left(e) ⇒
                system.log.debug("failed to parse package header", e)
                failedState(e.message)
            }
          }
        case state @ AwaitPackageBody(TransportPackageHeader(index, header, size)) ⇒
          val bitsLength = size * byteSize + int32Bits

          if (buf.size < bitsLength) {
            ((state, buf), pSeq)
          } else {
            val (body, remainder) = buf.splitAt(bitsLength)
            (new SignedMTProtoDecoder(header, size)).decode(body).toEither match {
              case Right(DecodeResult(body, BitVector.empty)) ⇒
                doParse(AwaitPackageHeader, remainder)(pSeq :+ TransportPackage(index, body))
              case Right(_) ⇒
                failedState("Body length is more than body itself")
              case Left(e) ⇒
                failedState(e.message)
            }
          }
        case _: FailedState ⇒
          ((state, BitVector.empty), Vector.empty)
      }
    }
  }
}

