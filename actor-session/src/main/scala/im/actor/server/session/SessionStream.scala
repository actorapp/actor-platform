package im.actor.server.session

import akka.actor._
import akka.stream.actor.{ ActorPublisher, ActorSubscriber }
import akka.stream.scaladsl._
import akka.stream.{ FlowShape, OverflowStrategy }
import scodec.bits._

import im.actor.api.rpc.ClientData
import im.actor.server.mtproto.protocol._
import im.actor.server.presences.PresenceManagerRegion
import im.actor.server.push.{ SeqUpdatesManagerRegion, WeakUpdatesManagerRegion }
import im.actor.server.session.SessionMessage.SubscribeCommand

sealed trait SessionStreamMessage

object SessionStreamMessage {
  @SerialVersionUID(1L)
  case class HandleMessageBox(messageBox: MessageBox, clientData: ClientData) extends SessionStreamMessage

  @SerialVersionUID(1L)
  case class HandleRpcRequest(messageId: Long, requestBytes: BitVector, clientData: ClientData) extends SessionStreamMessage

  @SerialVersionUID(1L)
  case class HandleSubscribe(command: SubscribeCommand) extends SessionStreamMessage
}

private[session] object SessionStream {

  def graph(
    authId:            Long,
    sessionId:         Long,
    rpcApiService:     ActorRef,
    rpcRequestHandler: ActorRef,
    updatesHandler:    ActorRef
  )(implicit context: ActorContext) = {
    FlowGraph.partial() { implicit builder ⇒
      import FlowGraph.Implicits._
      import SessionStreamMessage._

      val discr = builder.add(new SessionMessageDiscriminator)

      // TODO: think about buffer sizes and overflow strategies
      val rpc = discr.outRpc.buffer(100, OverflowStrategy.backpressure)
      val subscribe = discr.outSubscribe.buffer(100, OverflowStrategy.backpressure)
      val unmatched = discr.outUnmatched.buffer(100, OverflowStrategy.backpressure)

      val rpcRequestSubscriber = builder.add(Sink(ActorSubscriber[HandleRpcRequest](rpcRequestHandler)))
      val rpcResponsePublisher = builder.add(Source(ActorPublisher[ProtoMessage](rpcRequestHandler)))

      val updatesSubscriber = builder.add(Sink(ActorSubscriber[SubscribeCommand](updatesHandler)))
      val updatesPublisher = builder.add(Source(ActorPublisher[ProtoMessage](updatesHandler)))

      val merge = builder.add(Merge[ProtoMessage](2))

      val logging = akka.event.Logging(context.system, s"SessionStream-${authId}-${sessionId}")

      val log = Sink.foreach[SessionStreamMessage](logging.warning("Unmatched {}", _))

      // @format: OFF

      rpc       ~> rpcRequestSubscriber
                   rpcResponsePublisher ~> merge
      subscribe ~> updatesSubscriber
                   updatesPublisher     ~> merge
      unmatched ~> log

      // @format: ON

      FlowShape(discr.in, merge.out)
    }
  }
}
