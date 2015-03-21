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

  def graph(source: Source[SessionStreamMessage], rpcApiService: ActorRef, rpcResponsePublisher: ActorRef)(implicit system: ActorSystem): FlowGraph =
    FlowGraph { implicit builder =>
      import FlowGraphImplicits._

      val discriminator = new SessionMessageDiscriminator

      val rpcRequestHandler = system.actorOf(RpcRequestHandler.props(rpcApiService, rpcResponsePublisher))

      val rpcRequestSink = Sink(ActorSubscriber[HandleRpcRequest](rpcRequestHandler))

      // format: OFF

      source ~> discriminator.in
                discriminator.outUnmatched ~> Sink.ignore
                discriminator.outSubscriber ~> Sink.ignore
                discriminator.outHandleRpcRequest ~> rpcRequestSink

      // format: ON
    }
}
