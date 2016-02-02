package im.actor.server.session

import akka.actor._
import akka.stream.actor.{ ActorPublisher, ActorSubscriber }
import akka.stream.scaladsl._
import akka.stream.{ FlowShape, OverflowStrategy }
import scodec.bits._

import im.actor.api.rpc.{ UpdateBox, RpcResult, ClientData }
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
  final case class HandleOutgoingAck(messageIds: Seq[Long]) extends SessionStreamMessage
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
      val outOutgoingAcks = discr.out0.buffer(100, OverflowStrategy.backpressure).map(out)
      val outRequestResend = discr.out3.buffer(100, OverflowStrategy.backpressure).map(in)
      val outResender = discr.out5.buffer(100, OverflowStrategy.backpressure)

      val rpcRequestSubscriber = builder.add(Sink.fromSubscriber(ActorSubscriber[HandleRpcRequest](rpcHandler)))
      val rpcResponsePublisher = builder.add(Source.fromPublisher(ActorPublisher[(Option[RpcResult], Long)](rpcHandler)).map(out))

      val updatesSubscriber = builder.add(Sink.fromSubscriber(ActorSubscriber[SubscribeCommand](updatesHandler)))
      val updatesPublisher = builder.add(Source.fromPublisher(ActorPublisher[(UpdateBox, Option[String])](updatesHandler)).map(out))

      val reSendSubscriber = builder.add(Sink.fromSubscriber(ActorSubscriber[ReSenderMessage](reSender)))
      val reSendPublisher = builder.add(Source.fromPublisher(ActorPublisher[MessageBox](reSender)))

      val merge = builder.add(MergePreferred[ReSenderMessage](4))
      val mergePriority = builder.add(MergePreferred[ReSenderMessage](1))

      // @format: OFF

      incomingAck      ~> mergePriority.preferred
      outOutgoingAcks  ~> mergePriority        ~> merge.preferred
                          outRequestResend     ~> merge ~> reSendSubscriber
      rpc              ~> rpcRequestSubscriber
                          rpcResponsePublisher ~> merge
      subscribe        ~> updatesSubscriber
                          updatesPublisher     ~> merge
                          outResender          ~> merge

      // @format: ON

      FlowShape(discr.in, reSendPublisher.out)
    }
  }

  private def in(m: MessageAck): ReSenderMessage = ReSenderMessage.IncomingAck(m.messageIds)
  private def in(m: RequestResend): ReSenderMessage = ReSenderMessage.IncomingRequestResend(m.messageId)

  private def out(r: (Option[RpcResult], Long)) = r match {
    case (Some(res), messageId) ⇒ ReSenderMessage.RpcResult(res, messageId)
    case (None, messageId)      ⇒ ReSenderMessage.OutgoingAck(Seq(messageId))
  }
  private def out(u: (UpdateBox, Option[String])) = ReSenderMessage.Push(u._1, u._2)
  private def out(messageIds: Set[Long]) = ReSenderMessage.OutgoingAck(messageIds.toSeq)
}
