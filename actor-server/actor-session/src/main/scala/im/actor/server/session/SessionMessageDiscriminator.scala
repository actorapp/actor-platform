package im.actor.server.session

import akka.stream.FanOutShape._
import akka.stream.scaladsl._
import akka.stream.{ FanOutShape, Attributes }

import im.actor.server.mtproto.protocol._

class SessionMessageDiscriminatorShape(_init: Init[SessionStreamMessage] = Name[SessionStreamMessage]("SessionMessageDiscriminator"))
  extends FanOutShape[SessionStreamMessage](_init) {
  import SessionStreamMessage._

  val outProtoMessage = newOutlet[ProtoMessage]("outProtoMessage")
  val outRpc = newOutlet[HandleRpcRequest]("outRpc")
  val outSubscribe = newOutlet[SubscribeCommand]("outSubscribe")
  val outRequestResend = newOutlet[ProtoMessage]("outRequestResend")
  val outIncomingAck = newOutlet[ProtoMessage]("outIncomingAck")
  val outUnmatched = newOutlet[SessionStreamMessage]("outUnmatched")

  protected override def construct(i: Init[SessionStreamMessage]) = new SessionMessageDiscriminatorShape(i)
}

class SessionMessageDiscriminator extends FlexiRoute[SessionStreamMessage, SessionMessageDiscriminatorShape](
  new SessionMessageDiscriminatorShape, Attributes.name("SessionMessageDiscriminator")
) {

  import FlexiRoute._

  import SessionStreamMessage._

  override def createRouteLogic(p: PortT) = new RouteLogic[SessionStreamMessage] {
    override def initialState = State[Any](DemandFromAll(p.outlets)) {
      (ctx, _, element) ⇒
        handleElement(ctx, element)

        SameState
    }

    override def initialCompletionHandling = eagerClose

    private def handleElement(ctx: RouteLogicContext, element: SessionStreamMessage): Unit = {
      element match {
        case HandleMessageBox(MessageBox(messageId, RpcRequestBox(bodyBytes)), clientData) ⇒
          ctx.emit(p.outRpc)(HandleRpcRequest(messageId, bodyBytes, clientData))
        case HandleMessageBox(MessageBox(messageId, m: MessageAck), clientData) ⇒
          ctx.emit(p.outIncomingAck)(MessageAck.incoming(m.messageIds))
        case HandleMessageBox(MessageBox(messageId, m: RequestResend), _) ⇒
          ctx.emit(p.outRequestResend)(m)
        case HandleMessageBox(MessageBox(messageId, m: SessionHello), _) ⇒
        // ignore
        case SendProtoMessage(message) ⇒
          ctx.emit(p.outProtoMessage)(message)
        case msg @ HandleSubscribe(command) ⇒
          ctx.emit(p.outSubscribe)(command)
        case unmatched ⇒
          ctx.emit(p.outUnmatched)(unmatched)
      }
    }
  }
}
