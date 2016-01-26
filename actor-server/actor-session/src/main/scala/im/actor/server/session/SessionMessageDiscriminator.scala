package im.actor.server.session

import akka.stream._
import akka.stream.stage.{ OutHandler, GraphStage, GraphStageLogic, InHandler }
import im.actor.server.mtproto.protocol._
import im.actor.server.session.SessionStreamMessage._

object SessionMessageDiscriminator {
  type Shape = FanOutShape6[SessionStreamMessage, ProtoMessage, HandleRpcRequest, SubscribeCommand, RequestResend, MessageAck, SessionStreamMessage]
}

private[session] final class SessionMessageDiscriminator extends GraphStage[SessionMessageDiscriminator.Shape] {
  val in = Inlet[SessionStreamMessage]("sessionStreamMessage")
  val outProtoMessage = Outlet[ProtoMessage]("protoMessage")
  val outRpc = Outlet[HandleRpcRequest]("rpc")
  val outSubscribe = Outlet[SubscribeCommand]("subscribe")
  val outRequestResend = Outlet[RequestResend]("requestResend")
  val outIncomingAck = Outlet[MessageAck]("incomingAck")
  val outUnmatched = Outlet[SessionStreamMessage]("unmatched")

  override def shape: Shape = new FanOutShape6[SessionStreamMessage, ProtoMessage, HandleRpcRequest, SubscribeCommand, RequestResend, MessageAck, SessionStreamMessage](
    in,
    outProtoMessage,
    outRpc,
    outSubscribe,
    outRequestResend,
    outIncomingAck,
    outUnmatched
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
          case SessionStreamMessage.SendProtoMessage(message) ⇒
            emit(outProtoMessage, message, pullIn)
          case msg @ SessionStreamMessage.HandleSubscribe(command) ⇒
            emit(outSubscribe, command, pullIn)
          case unmatched ⇒
            emit(outUnmatched, unmatched, pullIn)
        }
      }
    })

    val pullIt = new OutHandler {
      override def onPull(): Unit = {
        pullIn()
      }
    }

    setHandler(outProtoMessage, pullIt)
    setHandler(outRpc, pullIt)
    setHandler(outSubscribe, pullIt)
    setHandler(outRequestResend, pullIt)
    setHandler(outIncomingAck, pullIt)
    setHandler(outUnmatched, pullIt)

    override def preStart(): Unit = pullIn()
  }
}
