package im.actor.server.session

import akka.actor._
import akka.stream.FlowShape
import akka.stream.actor.{ActorPublisher, ActorSubscriber}
import akka.stream.scaladsl._
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

      discriminator.in
      discriminator.outRpc ~> rpcRequestSubscriber
      discriminator.outSubscribe ~> Sink.ignore
      discriminator.outUnmatched ~> Sink.ignore

      // @formatter:on

      FlowShape(discriminator.in, rpcResponsePublisher)
    }
}
