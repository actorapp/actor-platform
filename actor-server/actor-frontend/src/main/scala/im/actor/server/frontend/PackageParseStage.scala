package im.actor.server.frontend

import akka.stream.{ Outlet, Inlet, Attributes, FlowShape }

import scala.annotation.tailrec
import scala.util.control.NoStackTrace

import akka.actor.ActorSystem
import akka.stream.stage._
import akka.util.ByteString
import scodec.DecodeResult
import scodec.bits.BitVector

import im.actor.server.mtproto.codecs._
import im.actor.server.mtproto.codecs.transport._
import im.actor.server.mtproto.transport.TransportPackage

private[frontend] final class PackageParseStage(implicit system: ActorSystem)
  extends GraphStage[FlowShape[ByteString, TransportPackage]] {

  private val in = Inlet[ByteString]("in")
  private val out = Outlet[TransportPackage]("out")

  override def shape = FlowShape(in, out)

  private val MaxPackageLength = 1024 * 1024 // TODO: configurable

  sealed trait ParserStep

  @SerialVersionUID(1L)
  final case object AwaitPackageHeader extends ParserStep

  @SerialVersionUID(1L)
  final case class AwaitPackageBody(header: TransportPackageHeader) extends ParserStep

  @SerialVersionUID(1L)
  final case class FailedState(msg: String) extends ParserStep

  type ParseState = (ParserStep, BitVector)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    private[this] var parserState: ParseState = (AwaitPackageHeader, BitVector.empty)

    @inline
    private def failedState(msg: String) = ((FailedState(msg), BitVector.empty), Vector.empty)

    val pullIn = () ⇒ {
      if (!hasBeenPulled(in))
        pull(in)
    }

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val chunk = grab(in)
        val (newState, res) = doParse(parserState._1, parserState._2 ++ BitVector(chunk.toByteBuffer))(Vector.empty)

        newState._1 match {
          case FailedState(msg) ⇒
            system.log.debug("Failed to parse connection-level {}", msg)
            // ctx.fail(new IllegalStateException(msg))
            failStage(new Exception(msg) with NoStackTrace)
          case _ ⇒
            parserState = newState
            emitMultiple(out, res.iterator, pullIn)
        }
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = pullIn()
    })

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
            new SignedMTProtoDecoder(header, size).decode(body).toEither match {
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

