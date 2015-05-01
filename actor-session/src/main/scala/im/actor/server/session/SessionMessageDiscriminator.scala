package im.actor.server.session

import akka.stream.FanOutShape._
import akka.stream.scaladsl._
import akka.stream.{ FanOutShape, OperationAttributes }

import im.actor.server.mtproto.protocol._
import im.actor.server.session.SessionMessage.SubscribeCommand
import im.actor.server.session.SessionStream._

class SessionMessageDiscriminatorShape(_init: Init[SessionStreamMessage] = Name[SessionStreamMessage]("SessionMessageDiscriminator"))
  extends FanOutShape[SessionStreamMessage](_init) {
  val outRpc = newOutlet[HandleRpcRequest]("outRpc")
  val outSubscribe = newOutlet[SubscribeCommand]("outSubscribe")
  val outUnmatched = newOutlet[SessionStreamMessage]("outUnmatched")

  protected override def construct(i: Init[SessionStreamMessage]) = new SessionMessageDiscriminatorShape(i)
}

class SessionMessageDiscriminator extends FlexiRoute[SessionStreamMessage, SessionMessageDiscriminatorShape](
  new SessionMessageDiscriminatorShape, OperationAttributes.name("SessionMessageDiscriminator")
) {

  import FlexiRoute._

  import SessionStream._

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
        case msg @ HandleSubscribe(command) ⇒
          ctx.emit(p.outSubscribe)(command)
        case unmatched ⇒
          ctx.emit(p.outUnmatched)(unmatched)
      }
    }
  }
}
