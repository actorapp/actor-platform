package im.actor.server.session

import akka.actor._
import akka.stream.actor._
import im.actor.server.mtproto.protocol.ProtoRpcRequest

class RpcRequestBoxHandler(rpcApiHandler: ActorRef) extends ActorSubscriber {
  import ActorSubscriberMessage._

  val MaxQueueSize = 10 // TODO: configurable

  val queue = Map.empty[Int, ProtoRpcRequest]

  override val requestStrategy = new MaxInFlightRequestStrategy(max = MaxQueueSize) {
    override def inFlightInternally: Int = queue.size
  }

  def receive: Receive = {
    case OnNext((client, messageId, bodyBytes)) ⇒

  }
}
