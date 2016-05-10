package im.actor.server

import akka.actor.ActorSystem
import com.google.protobuf.ByteString
import im.actor.api.rpc.{ ClientData, PeersImplicits }
import im.actor.api.rpc.messaging.{ ApiDialogGroup, ApiDialogShort, ApiMessage, ApiTextMessage, _ }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.users.ApiUser
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.dialog.{ DialogExtension, DialogGroupType, DialogInfo }
import im.actor.server.model.{ DialogObsolete, Peer }
import im.actor.server.persist.dialog.DialogRepo
import im.actor.server.sequence.SeqStateDate
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.util.Random

trait MessagingSpecHelpers extends ScalaFutures with PeersImplicits with Matchers {
  implicit val system: ActorSystem

  lazy val dialogExt = DialogExtension(system)

  def sendMessageToUser(userId: Int, text: String)(
    implicit
    clientData: ClientData,
    msgService: MessagingService
  ): Long = sendMessageToUser(userId, ApiTextMessage(text, Vector.empty, None))

  def sendMessageToUser(userId: Int, message: ApiMessage)(
    implicit
    clientData: ClientData,
    msgService: MessagingService
  ): Long = {
    val randomId = Random.nextLong
    whenReady(ACLUtils.getOutPeer(ApiPeer(ApiPeerType.Private, userId), clientData.authId)) { peer ⇒
      whenReady(msgService.handleSendMessage(peer, randomId, message, None, None))(identity)
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
      whenReady(msgService.handleSendMessage(peer, randomId, message, None, None)) { resp ⇒
        val respSeqDate = resp.toOption.get
        SeqStateDate(respSeqDate.seq, ByteString.copyFrom(respSeqDate.state), respSeqDate.date)
      }
    }
  }

  def findPrivateDialog(withUserId: Int)(implicit clientData: ClientData, ec: ExecutionContext): DialogInfo = {
    clientData.authData shouldBe defined
    val clientUserId = clientData.authData.get.userId
    whenReady(dialogExt.getDialogInfo(clientUserId, Peer.privat(withUserId)))(identity)
  }

  def sendMessageToGroup(groupId: Int, message: ApiMessage)(
    implicit
    clientData: ClientData,
    msgService: MessagingService
  ): Long = {
    val randomId = Random.nextLong
    whenReady(ACLUtils.getOutPeer(ApiPeer(ApiPeerType.Group, groupId), clientData.authId)) { peer ⇒
      whenReady(msgService.handleSendMessage(peer, randomId, message, None, None))(identity)
    }
    randomId
  }

  def textMessage(text: String) = ApiTextMessage(text, Vector.empty, None)

  def getDialogGroups()(implicit clientData: ClientData, service: MessagingService): Map[String, IndexedSeq[ApiDialogShort]] = {
    whenReady(service.handleLoadGroupedDialogs(Vector.empty)) { resp ⇒
      resp.toOption.get.dialogs map {
        case ApiDialogGroup(_, key, dialogs) ⇒ key → dialogs
      } toMap
    }
  }

  def getDialogGroups(group: DialogGroupType)(implicit clientData: ClientData, service: MessagingService): IndexedSeq[ApiDialogShort] = {
    val dgs = getDialogGroups()
    dgs get DialogExtension.groupKey(group) match {
      case Some(ds) ⇒ ds
      case None     ⇒ throw new RuntimeException(s"Group $group not found in $dgs")
    }
  }

  def loadDialogs(minDate: Long = 0L, limit: Int = Int.MaxValue)(implicit clientData: ClientData, service: MessagingService): IndexedSeq[ApiDialog] = {
    whenReady(service.handleLoadDialogs(minDate, limit, Vector.empty)) { resp ⇒
      resp.toOption.get.dialogs
    }
  }

  def prepareDialogs(users: ApiUser*)(implicit clientData: ClientData, service: MessagingService): Unit = {
    users foreach { user ⇒
      sendMessageToUser(user.id, textMessage(s"Hi, I am ${user.name}!"))
      Thread.sleep(1)
    }
  }
}