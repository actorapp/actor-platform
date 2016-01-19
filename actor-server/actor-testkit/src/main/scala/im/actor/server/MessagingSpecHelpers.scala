package im.actor.server

import akka.actor.ActorSystem
import com.google.protobuf.ByteString
import im.actor.api.rpc.{ PeersImplicits, ClientData }
import im.actor.api.rpc.messaging.{ ApiDialogGroup, ApiDialogShort, ApiMessage, ApiTextMessage, _ }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.users.ApiUser
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogGroup
import im.actor.server.model.Dialog
import im.actor.server.persist.DialogRepo
import im.actor.server.sequence.SeqStateDate
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures

import scala.language.postfixOps
import scala.util.Random

trait MessagingSpecHelpers extends ScalaFutures with PeersImplicits with Matchers {
  implicit val system: ActorSystem

  def sendMessageToUser(userId: Int, message: ApiMessage)(
    implicit
    clientData: ClientData,
    msgService: MessagingService
  ): Long = {
    val randomId = Random.nextLong
    whenReady(ACLUtils.getOutPeer(ApiPeer(ApiPeerType.Private, userId), clientData.authId)) { peer ⇒
      whenReady(msgService.handleSendMessage(peer, randomId, message))(identity)
    }
    randomId
  }

  //the only difference with previous method - this one returns SeqStateDate
  def sendPrivateMessage(userId: Int, message: ApiMessage)(
    implicit
    clientData: ClientData,
    msgService: MessagingService
  ): SeqStateDate = {
    val randomId = Random.nextLong
    whenReady(ACLUtils.getOutPeer(ApiPeer(ApiPeerType.Private, userId), clientData.authId)) { peer ⇒
      whenReady(msgService.handleSendMessage(peer, randomId, message)) { resp ⇒
        val respSeqDate = resp.toOption.get
        SeqStateDate(respSeqDate.seq, ByteString.copyFrom(respSeqDate.state), respSeqDate.date)
      }
    }
  }

  def findPrivateDialog(withUserId: Int)(implicit clientData: ClientData): Dialog = {
    clientData.authData shouldBe defined
    val clientUserId = clientData.authData.get.userId
    whenReady(ACLUtils.getOutPeer(ApiPeer(ApiPeerType.Private, withUserId), clientData.authId)) { peer ⇒
      whenReady(DbExtension(system).db.run(DialogRepo.find(clientUserId, peer.asModel))) { resp ⇒
        resp shouldBe defined
        resp.get
      }
    }
  }

  def sendMessageToGroup(groupId: Int, message: ApiMessage)(
    implicit
    clientData: ClientData,
    msgService: MessagingService
  ): Long = {
    val randomId = Random.nextLong
    whenReady(ACLUtils.getOutPeer(ApiPeer(ApiPeerType.Group, groupId), clientData.authId)) { peer ⇒
      whenReady(msgService.handleSendMessage(peer, randomId, message))(identity)
    }
    randomId
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