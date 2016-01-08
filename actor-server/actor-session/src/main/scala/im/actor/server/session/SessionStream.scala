package im.actor.server.session

import akka.actor._
import akka.stream.actor.{ ActorPublisher, ActorSubscriber }
import akka.stream.scaladsl._
import akka.stream.{ FlowShape, OverflowStrategy }
import scodec.bits._

import im.actor.api.rpc.ClientData
import im.actor.server.mtproto.protocol._

sealed trait SessionStreamMessage

object SessionStreamMessage {
  @SerialVersionUID(1L)
  final case class HandleMessageBox(messageBox: MessageBox, clientData: ClientData) extends SessionStreamMessage

  @SerialVersionUID(1L)
  final case class HandleRpcRequest(messageId: Long, requestBytes: BitVector, clientData: ClientData) extends SessionStreamMessage

  @SerialVersionUID(1L)
  final case class HandleSubscribe(command: SubscribeCommand) extends SessionStreamMessage

  @SerialVersionUID(1L)
  final case class SendProtoMessage(message: ProtoMessage with OutgoingProtoMessage) extends SessionStreamMessage
}

private[session] object SessionStream {
  type ReduceKey = Option[String]
  type OutProtoMessage = (ProtoMessage, ReduceKey)
  type InOrOut = Either[ProtoMessage, OutProtoMessage]

  def graph(
    authId:         Long,
    sessionId:      Long,
    rpcHandler:     ActorRef,
    updatesHandler: ActorRef,
    reSender:       ActorRef
  )(implicit context: ActorContext) = {
    GraphDSL.create() { implicit builder ⇒
      import GraphDSL.Implicits._

      import SessionStreamMessage._

      val discr = builder.add(new SessionMessageDiscriminator)

      // TODO: think about buffer sizes and overflow strategies
      val rpc = discr.out1.buffer(100, OverflowStrategy.backpressure)
      val subscribe = discr.out2.buffer(100, OverflowStrategy.backpressure)
      val incomingAck = discr.out4.buffer(100, OverflowStrategy.backpressure).map(in)
      val outProtoMessages = discr.out0.buffer(100, OverflowStrategy.backpressure).map(out)
      val outRequestResend = discr.out3.buffer(100, OverflowStrategy.backpressure).map(in)
      val unmatched = discr.out5.buffer(100, OverflowStrategy.backpressure)

      val rpcRequestSubscriber = builder.add(Sink.fromSubscriber(ActorSubscriber[HandleRpcRequest](rpcHandler)))
      val rpcResponsePublisher = builder.add(Source.fromPublisher(ActorPublisher[ProtoMessage](rpcHandler)).map(out))

      val updatesSubscriber = builder.add(Sink.fromSubscriber(ActorSubscriber[SubscribeCommand](updatesHandler)))
      val updatesPublisher = builder.add(Source.fromPublisher(ActorPublisher[OutProtoMessage](updatesHandler)).map(out))

      val reSendSubscriber = builder.add(Sink.fromSubscriber(ActorSubscriber[ReSenderMessage](reSender)))
      val reSendPublisher = builder.add(Source.fromPublisher(ActorPublisher[MessageBox](reSender)))

      val mergeProto = builder.add(MergePreferred[ReSenderMessage](3))
      val mergeProtoPriority = builder.add(MergePreferred[ReSenderMessage](1))

      val logging = akka.event.Logging(context.system, s"SessionStream-$authId-$sessionId")

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

      FlowShape(discr.in, reSendPublisher.out)
    }
  }

  import ReSenderMessage._

  private def in(m: MessageAck): ReSenderMessage = IncomingAck(m.messageIds)
  private def in(m: RequestResend): ReSenderMessage = IncomingRequestResend(m.messageId)

  private def out(m: ProtoMessage): ReSenderMessage = out(m, None)
  private def out(msg: ProtoMessage, reduceKey: ReduceKey) = OutgoingMessage(msg, reduceKey)
  private def out(tup: (ProtoMessage, ReduceKey)) = (OutgoingMessage.apply _).tupled(tup)
}
