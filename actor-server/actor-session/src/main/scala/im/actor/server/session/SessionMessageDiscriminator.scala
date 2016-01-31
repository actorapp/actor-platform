package im.actor.server.session

import akka.stream._
import akka.stream.stage.{ OutHandler, GraphStage, GraphStageLogic, InHandler }
import im.actor.api.rpc.sequence.ApiUpdateOptimization
import im.actor.server.mtproto.protocol._
import im.actor.server.session.SessionStreamMessage._

object SessionMessageDiscriminator {
  type Shape = FanOutShape6[SessionStreamMessage, Set[Long], HandleRpcRequest, SubscribeCommand, RequestResend, MessageAck, ReSenderMessage]
}

private[session] final class SessionMessageDiscriminator extends GraphStage[SessionMessageDiscriminator.Shape] {
  val in = Inlet[SessionStreamMessage]("sessionStreamMessage")
  val outOutgoingAcks = Outlet[Set[Long]]("acks")
  val outRpc = Outlet[HandleRpcRequest]("rpc")
  val outSubscribe = Outlet[SubscribeCommand]("subscribe")
  val outRequestResend = Outlet[RequestResend]("requestResend")
  val outIncomingAck = Outlet[MessageAck]("incomingAck")
  val outResenderMessage = Outlet[ReSenderMessage]("resenderMessage")

  override def shape: Shape = new FanOutShape6[SessionStreamMessage, Set[Long], HandleRpcRequest, SubscribeCommand, RequestResend, MessageAck, ReSenderMessage](
    in,
    outOutgoingAcks,
    outRpc,
    outSubscribe,
    outRequestResend,
    outIncomingAck,
    outResenderMessage
  )

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    val pullIn = () ⇒ {
      if (!hasBeenPulled(in))
        pull(in)
    }

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val msg = grab(in)

        msg match {
          case SessionStreamMessage.HandleMessageBox(MessageBox(messageId, ProtoRpcRequest(bodyBytes)), clientData) ⇒
            emit(outRpc, HandleRpcRequest(messageId, bodyBytes, clientData), pullIn)
          case SessionStreamMessage.HandleMessageBox(MessageBox(messageId, m: MessageAck), clientData) ⇒
            emit(outIncomingAck, m, pullIn)
          case SessionStreamMessage.HandleMessageBox(MessageBox(messageId, m: RequestResend), _) ⇒
            emit(outRequestResend, m, pullIn)
          case SessionStreamMessage.HandleMessageBox(MessageBox(messageId, m: SessionHello), _) ⇒
            pullIn()
          case SessionStreamMessage.HandleOutgoingAck(acks) ⇒
            emit(outOutgoingAcks, acks.toSet, pullIn)
          case msg @ SessionStreamMessage.HandleSubscribe(command) ⇒
            command match {
              case SubscribeToSeq(opts) ⇒
                emit(outResenderMessage, ReSenderMessage.SetUpdateOptimizations(opts.toSet.map((id: Int) ⇒ ApiUpdateOptimization(id))), pullIn)
              case _ ⇒
            }

            emit(outSubscribe, command, pullIn)
          case unmatched ⇒ failStage(new RuntimeException(s"Unmatched message: $unmatched"))
        }
      }
    })

    val pullIt = new OutHandler {
      override def onPull(): Unit = {
        pullIn()
      }
    }

    setHandler(outOutgoingAcks, pullIt)
    setHandler(outRpc, pullIt)
    setHandler(outSubscribe, pullIt)
    setHandler(outRequestResend, pullIt)
    setHandler(outIncomingAck, pullIt)
    setHandler(outResenderMessage, pullIt)

    override def preStart(): Unit = pullIn()
  }
}
