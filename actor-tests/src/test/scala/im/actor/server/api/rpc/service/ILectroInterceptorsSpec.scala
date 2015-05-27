package im.actor.server.api.rpc.service

import scala.concurrent._
import scala.concurrent.duration._

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import com.google.protobuf.CodedInputStream

import im.actor.api.rpc.ClientData
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ OutPeer, PeerType }
import im.actor.server.api.rpc.service.llectro.{ IlectroServiceImpl, MessageInterceptor }
import im.actor.server.api.rpc.service.messaging.{ GroupPeerManager, PrivatePeerManager }
import im.actor.server.api.rpc.service.sequence.SequenceServiceImpl
import im.actor.server.ilectro.ILectro
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager
import im.actor.server.util.{ ACLUtils, UploadManager }
import im.actor.utils.http.DownloadManager

class ILectroInterceptorsSpec extends BaseServiceSuite {

  behavior of "ILectro MessageInterceptor"

  it should "insert banner after 10 messages" in s.e1

  it should "not do anything for non ILectro users" in s.e2

  implicit val sessionRegion = buildSessionRegionProxy()

  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
  implicit val privatePeerManagerRegion = PrivatePeerManager.startRegion()
  implicit val groupPeerManagerRegion = GroupPeerManager.startRegion()

  val bucketName = "actor-uploads-test"
  val awsCredentials = new EnvironmentVariableCredentialsProvider()
  implicit val transferManager = new TransferManager(awsCredentials)

  implicit val authService = buildAuthService()
  implicit val messagingService = messaging.MessagingServiceImpl(mediator)
  val sequenceService = new SequenceServiceImpl

  lazy val ilectro = new ILectro
  lazy val downloadManager = new DownloadManager
  lazy val uploadManager = new UploadManager(bucketName)

  val ilectroService = new IlectroServiceImpl(ilectro)

  object s {
    val (user1, authId1, _) = createUser()
    val sessionId1 = createSessionId()

    val (user2, authId2, _) = createUser()
    val sessionId2 = createSessionId()

    val clientData1 = ClientData(authId1, sessionId1, Some(user1.id))
    val clientData2 = ClientData(authId2, sessionId2, Some(user2.id))

    val user1AccessHash = ACLUtils.userAccessHash(authId2, user1.id, getUserModel(user1.id).accessSalt)
    val user1Peer = OutPeer(PeerType.Private, user1.id, user1AccessHash)

    val user2AccessHash = ACLUtils.userAccessHash(authId1, user2.id, getUserModel(user2.id).accessSalt)
    val user2Peer = OutPeer(PeerType.Private, user2.id, user2AccessHash)

    Await.result(ilectroService.jhandleGetAvailableInterests(clientData1), 15.seconds)
    Await.result(ilectroService.jhandleGetAvailableInterests(clientData2), 15.seconds)

    MessageInterceptor.startSingleton(ilectro, downloadManager, uploadManager)

    def e1(): Unit = {

      Thread.sleep(1000)

      sendMessages(user2Peer)(clientData1)
      Thread.sleep(5000)

      val (randomId1, seq1, state1) = checkNewAdExists(0, Array.empty, clientData1)
      val (randomId2, seq2, state2) = checkNewAdExists(0, Array.empty, clientData2)

      sendMessages(user2Peer)(clientData1)
      Thread.sleep(5000)

      checkUpdatedAdExists(randomId1, seq1, state1, clientData1)
      checkUpdatedAdExists(randomId2, seq2, state2, clientData2)
    }

    val (user3, authId3, _) = createUser()
    val sessionId3 = createSessionId()

    val (user4, authId4, _) = createUser()
    val sessionId4 = createSessionId()

    val clientData3 = ClientData(authId3, sessionId3, Some(user3.id))
    val clientData4 = ClientData(authId4, sessionId4, Some(user4.id))

    val user3AccessHash = ACLUtils.userAccessHash(authId4, user3.id, getUserModel(user3.id).accessSalt)
    val user3Peer = OutPeer(PeerType.Private, user3.id, user3AccessHash)

    val user4AccessHash = ACLUtils.userAccessHash(authId3, user4.id, getUserModel(user4.id).accessSalt)
    val user4Peer = OutPeer(PeerType.Private, user4.id, user4AccessHash)

    Await.result(ilectroService.jhandleGetAvailableInterests(clientData3), 15.seconds)
    Await.result(ilectroService.jhandleGetAvailableInterests(clientData4), 15.seconds)

    def e2(): Unit = {

      sendMessages(user4Peer)(clientData3)
      Thread.sleep(7000)

      checkNoAdExists(0, Array.empty, clientData3)
      checkNoAdExists(0, Array.empty, clientData4)
    }

    private def sendMessages(outPeer: OutPeer)(implicit clientData: ClientData): Unit = {
      for (_ ← 1 to 10) {

        whenReady(messagingService.handleSendMessage(outPeer, 1L, TextMessage("Hi Shiva 1", Vector.empty, None)))(_ ⇒ ())
      }
    }

    private def checkNoAdExists(seq: Int, state: Array[Byte], clientData: ClientData) = {
      whenReady(sequenceService.jhandleGetDifference(seq, state, clientData)) { result ⇒
        val resp = result.toOption.get

        val updates = resp.updates
        updates.length shouldEqual 10

        val message = TextMessage.parseFrom(CodedInputStream.newInstance(updates.last.update)).right.toOption.get
        message shouldBe a[TextMessage]
      }
    }

    private def checkNewAdExists(seq: Int, state: Array[Byte], clientData: ClientData): (Long, Int, Array[Byte]) = {
      whenReady(sequenceService.jhandleGetDifference(seq, state, clientData)) { result ⇒
        val resp = result.toOption.get

        val updates = resp.updates
        updates.length shouldEqual 11

        val update = UpdateMessage.parseFrom(CodedInputStream.newInstance(updates.last.update)).right.toOption.get
        update.message shouldBe a[JsonMessage]
        update.peer.id should not equal (clientData.optUserId.get)
        update.senderUserId should equal(clientData.optUserId.get)

        (update.randomId, resp.seq, resp.state)
      }
    }

    private def checkUpdatedAdExists(randomId: Long, seq: Int, state: Array[Byte], clientData: ClientData): (Int, Array[Byte]) = {
      whenReady(sequenceService.jhandleGetDifference(seq, state, clientData)) { result ⇒
        val resp = result.toOption.get

        val updates = resp.updates
        updates.length shouldEqual 12

        val Seq(diffUpdate1, diffUpdate2) = updates.takeRight(2)

        val updateMessageDateChanged =
          UpdateMessageDateChanged.parseFrom(CodedInputStream.newInstance(diffUpdate1.update)).right.toOption.get
        updateMessageDateChanged.randomId shouldEqual randomId

        val updateMessageContentChanged =
          UpdateMessageContentChanged.parseFrom(CodedInputStream.newInstance(diffUpdate2.update)).right.toOption.get
        updateMessageContentChanged.message shouldBe a[JsonMessage]
        updateMessageContentChanged.peer.id should not equal (clientData.optUserId.get)

        (resp.seq, resp.state)
      }
    }
  }

}
