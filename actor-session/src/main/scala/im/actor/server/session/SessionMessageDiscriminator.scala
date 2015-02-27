package im.actor.server.session

import akka.actor.ActorRef
import akka.stream.scaladsl._
import im.actor.server.mtproto.protocol._
import scodec.bits._

class SessionMessageDiscriminator extends FlexiRoute[SessionStream.SessionStreamMessage] {
  import FlexiRoute._
  import SessionStream._

  val outHandleRpcRequest = createOutputPort[HandleRpcRequest]()
  val outSubscriber = createOutputPort[SubscribeToPresences]()
  val outUnmatched = createOutputPort[SessionStreamMessage]

  val handles = Vector(outHandleRpcRequest, outSubscriber, outUnmatched)

  override def createRouteLogic() = new RouteLogic[SessionStreamMessage] {
    override def outputHandles(outputCount: Int) = {
      require(outputCount == 3, s"Must have three connected outputs, was $outputCount")
      handles
    }

    override def initialState = State[Any](DemandFromAny(handles: _*)) {
      (ctx, _, element) =>
        element match {
          case HandleMessageBox(MessageBox(messageId, RpcRequestBox(bodyBytes))) =>
            ctx.emit(outHandleRpcRequest, HandleRpcRequest(messageId, bodyBytes))
          case e: SubscribeToPresences => ctx.emit(outSubscriber, e)
          case unmatched =>
            ctx.emit(outUnmatched, unmatched)
        }

        SameState
    }

    override def initialCompletionHandling = eagerClose
  }
}
