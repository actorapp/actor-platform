package im.actor.server.session

import akka.actor._
import akka.stream.actor.{ ActorPublisher, ActorSubscriber }
import akka.stream.scaladsl._
import akka.stream.{ FlowShape, OverflowStrategy }
import scodec.bits._

import im.actor.api.rpc.ClientData
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.MTPackage

sealed trait SessionStreamMessage

object SessionStreamMessage {
  @SerialVersionUID(1L)
  case class HandleMessageBox(messageBox: MessageBox, clientData: ClientData) extends SessionStreamMessage

  @SerialVersionUID(1L)
  case class HandleRpcRequest(messageId: Long, requestBytes: BitVector, clientData: ClientData) extends SessionStreamMessage

  @SerialVersionUID(1L)
  case class HandleSubscribe(command: SubscribeCommand) extends SessionStreamMessage

  @SerialVersionUID(1L)
  case class SendProtoMessage(message: ProtoMessage with OutgoingProtoMessage) extends SessionStreamMessage
}

private[session] object SessionStream {

  def graph(
    authId:         Long,
    sessionId:      Long,
    rpcHandler:     ActorRef,
    updatesHandler: ActorRef,
    reSender:       ActorRef
  )(implicit context: ActorContext) = {
    FlowGraph.partial() { implicit builder ⇒
      import FlowGraph.Implicits._

      import SessionStreamMessage._

      val discr = builder.add(new SessionMessageDiscriminator)

      // TODO: think about buffer sizes and overflow strategies
      val rpc = discr.outRpc.buffer(100, OverflowStrategy.backpressure)
      val subscribe = discr.outSubscribe.buffer(100, OverflowStrategy.backpressure)
      val incomingAck = discr.outIncomingAck.buffer(100, OverflowStrategy.backpressure)
      val outProtoMessages = discr.outProtoMessage.buffer(100, OverflowStrategy.backpressure)
      val outRequestResend = discr.outRequestResend.buffer(100, OverflowStrategy.backpressure)
      val unmatched = discr.outUnmatched.buffer(100, OverflowStrategy.backpressure)

      val rpcRequestSubscriber = builder.add(Sink(ActorSubscriber[HandleRpcRequest](rpcHandler)))
      val rpcResponsePublisher = builder.add(Source(ActorPublisher[ProtoMessage](rpcHandler)))

      val updatesSubscriber = builder.add(Sink(ActorSubscriber[SubscribeCommand](updatesHandler)))
      val updatesPublisher = builder.add(Source(ActorPublisher[ProtoMessage](updatesHandler)))

      val reSendSubscriber = builder.add(Sink(ActorSubscriber[ProtoMessage](reSender)))
      val reSendPublisher = builder.add(Source(ActorPublisher[MTPackage](reSender)))

      val mergeProto = builder.add(MergePreferred[ProtoMessage](3))
      val mergeProtoPriority = builder.add(MergePreferred[ProtoMessage](1))

      val logging = akka.event.Logging(context.system, s"SessionStream-${authId}-${sessionId}")

      val log = Sink.foreach[SessionStreamMessage](logging.warning("Unmatched {}", _))

      // @format: OFF

      incomingAck      ~> mergeProtoPriority.preferred
      outProtoMessages ~> mergeProtoPriority   ~> mergeProto.preferred
                          outRequestResend     ~> mergeProto ~> reSendSubscriber
      rpc              ~> rpcRequestSubscriber
                          rpcResponsePublisher ~> mergeProto
      subscribe        ~> updatesSubscriber
                          updatesPublisher     ~> mergeProto
      unmatched        ~> log

      // @format: ON

      FlowShape(discr.in, reSendPublisher.outlet)
    }
  }
}
