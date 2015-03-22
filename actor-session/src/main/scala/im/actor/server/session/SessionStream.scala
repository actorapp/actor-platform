package im.actor.server.session

import akka.actor._
import akka.stream.scaladsl._
import akka.stream.actor.{ ActorPublisher, ActorSubscriber }

import im.actor.api.rpc.ClientData
import im.actor.server.mtproto.protocol._
import im.actor.server.api.rpc.RpcApiService

import scodec.bits._

private[session] object SessionStream {
  trait SessionStreamMessage

  @SerialVersionUID(1L)
  case class HandleMessageBox(messageBox: MessageBox, clientData: ClientData) extends SessionStreamMessage

  @SerialVersionUID(1L)
  case class SubscribeToPresences(userIds: Int) extends SessionStreamMessage

  @SerialVersionUID(1L)
  case class HandleRpcRequest(messageId: Long, requestBytes: BitVector, clientData: ClientData) extends SessionStreamMessage

  def graph(in: Source[SessionStreamMessage, _], rpcApiService: ActorRef, rpcResponsePublisher: ActorRef)(implicit system: ActorSystem): RunnableFlow[_] =
    FlowGraph.closed() { implicit builder =>
      import FlowGraph.Implicits._

      val discriminator = builder.add(new SessionMessageDiscriminator)

      val rpcRequestHandlerActor = system.actorOf(RpcRequestHandler.props(rpcApiService, rpcResponsePublisher))

      val rpcRequestHandler = builder.add(Sink(ActorSubscriber[HandleRpcRequest](rpcRequestHandlerActor)))

      // @formatter:off

      in ~> discriminator.in
            discriminator.outRpc ~> rpcRequestHandler
            discriminator.outSubscribe ~> Sink.ignore
            discriminator.outUnmatched ~> Sink.ignore

      // @formatter:on
    }
}
