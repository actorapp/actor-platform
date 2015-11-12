package im.actor.server

import akka.actor.ActorSystem
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.messaging.{ ApiDialogGroup, ApiDialogShort, ApiMessage, ApiTextMessage, _ }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.users.ApiUser
import im.actor.server.acl.ACLUtils
import im.actor.server.dialog.DialogGroup
import org.scalatest.concurrent.ScalaFutures

import scala.language.postfixOps
import scala.util.Random

trait MessagingSpecHelpers extends ScalaFutures {
  implicit val system: ActorSystem

  def sendMessageToUser(userId: Int, message: ApiMessage)(
    implicit
    clientData: ClientData,
    msgService: MessagingService
  ): Unit = {
    whenReady(ACLUtils.getOutPeer(ApiPeer(ApiPeerType.Private, userId), clientData.authId)) { peer ⇒
      whenReady(msgService.handleSendMessage(peer, Random.nextLong, message))(identity)
    }
  }

  def textMessage(text: String) = ApiTextMessage(text, Vector.empty, None)

  def getDialogGroups()(implicit clientData: ClientData, service: MessagingService): Map[String, IndexedSeq[ApiDialogShort]] = {
    whenReady(service.handleLoadGroupedDialogs()) { resp ⇒
      resp.toOption.get.dialogs map {
        case ApiDialogGroup(_, key, dialogs) ⇒ key → dialogs
      } toMap
    }
  }

  def getDialogGroups(group: DialogGroup)(implicit clientData: ClientData, service: MessagingService): IndexedSeq[ApiDialogShort] = {
    val dgs = getDialogGroups()
    dgs get group.key match {
      case Some(ds) ⇒ ds
      case None     ⇒ throw new RuntimeException(s"Group $group not found in $dgs")
    }
  }

  def prepareDialogs(users: ApiUser*)(implicit clientData: ClientData, service: MessagingService): Unit = {
    users foreach { user ⇒
      sendMessageToUser(user.id, textMessage(s"Hi, I am ${user.name}!"))
      Thread.sleep(1)
    }
  }
}