package im.actor.server

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.messaging.{ ApiMessage, ApiTextMessage, MessagingService }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.acl.ACLUtils

import scala.concurrent.Await
import scala.util.Random

trait MessagingSpecHelpers {
  val timeout: Timeout

  def sendMessageToUser(userId: Int, message: ApiMessage)(
    implicit
    clientData: ClientData,
    msgService: MessagingService,
    system:     ActorSystem
  ): Unit = {
    val peer = Await.result(ACLUtils.getOutPeer(ApiPeer(ApiPeerType.Private, userId), clientData.authId), timeout.duration)
    Await.result(msgService.handleSendMessage(peer, Random.nextLong, message), timeout.duration)
  }

  def textMessage(text: String) = ApiTextMessage(text, Vector.empty, None)
}