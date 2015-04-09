package im.actor.server.session

import akka.actor._
import akka.stream.actor.{ ActorPublisher, ActorSubscriber }
import akka.stream.scaladsl._
import akka.stream.{ FlowShape, OverflowStrategy }
import scodec.bits._

import im.actor.api.rpc.ClientData
import im.actor.server.mtproto.protocol._

private[session] object SessionStream {

  trait SessionStreamMessage

  @SerialVersionUID(1L)
  case class HandleMessageBox(messageBox: MessageBox, clientData: ClientData) extends SessionStreamMessage

  @SerialVersionUID(1L)
  case class SubscribeToPresences(userIds: Int) extends SessionStreamMessage

  @SerialVersionUID(1L)
  case class HandleRpcRequest(messageId: Long, requestBytes: BitVector, clientData: ClientData) extends SessionStreamMessage

  def graph(rpcApiService: ActorRef)(implicit system: ActorSystem) =
    FlowGraph.partial() { implicit builder =>
      import FlowGraph.Implicits._

      val discriminator = builder.add(new SessionMessageDiscriminator)

      val rpcRequestHandlerActor = system.actorOf(RpcRequestHandler.props(rpcApiService))

      val rpcRequestSubscriber = builder.add(Sink(ActorSubscriber[HandleRpcRequest](rpcRequestHandlerActor)))
      val rpcResponsePublisher = builder.add(Source(ActorPublisher[ProtoMessage](rpcRequestHandlerActor)))

      // @formatter:off

      // TODO: think about buffer sizes and overflow strategies
      discriminator.in
      discriminator.outRpc.buffer(100, OverflowStrategy.backpressure) ~> rpcRequestSubscriber
      discriminator.outSubscribe.buffer(100, OverflowStrategy.backpressure) ~> Sink.ignore
      discriminator.outUnmatched.buffer(100, OverflowStrategy.backpressure) ~> Sink.ignore

      // @formatter:on

      FlowShape(discriminator.in, rpcResponsePublisher)
    }
}
