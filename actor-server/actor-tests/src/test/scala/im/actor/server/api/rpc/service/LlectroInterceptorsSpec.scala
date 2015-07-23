package im.actor.server.api.rpc.service

import im.actor.server.group.{ GroupOfficeRegion, GroupOffice }
import im.actor.server.user.{ UserOfficeRegion, UserOffice }

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.google.protobuf.CodedInputStream

import im.actor.api.PeersImplicits
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ OutPeer, PeerType }
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.llectro.interceptors.MessageInterceptor
import im.actor.server.api.rpc.service.llectro.{ LlectroInterceptionConfig, LlectroServiceImpl }
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceConfig, SequenceServiceImpl }
import im.actor.server.llectro.Llectro
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager
import im.actor.server.util.ACLUtils
import im.actor.server.{ BaseAppSuite, ImplicitFileStorageAdapter }
import im.actor.utils.http.DownloadManager

class LlectroInterceptorsSpec extends BaseAppSuite with GroupsServiceHelpers with PeersImplicits with ImplicitFileStorageAdapter {
  val messageCount = 10

  behavior of "Llectro MessageInterceptor"

  it should s"insert banner in private chat after $messageCount messages" in s.e1

  it should "not do anything for non Llectro users in private chat" in s.e2

  it should "not insert Llectro banner in private chat of Llectro user and any other user" in s.e3

  it should s"insert banner in group chat after $messageCount messages for Llectro user only" in s.e4

  it should "insert banner in group chat after Llectro joins this group chat" in s.e5

  it should "work with both private and group dialogs" in s.e6

  val awsCredentials = new EnvironmentVariableCredentialsProvider()

  object s {

    implicit val sessionRegion = buildSessionRegionProxy()

    implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
    implicit val socialManagerRegion = SocialManager.startRegion()
    implicit val presenceManagerRegion = PresenceManager.startRegion()
    implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
    implicit val privatePeerManagerRegion = UserOfficeRegion.start()
    implicit val groupPeerManagerRegion = GroupOfficeRegion.start()

    val groupInviteConfig = GroupInviteConfig("http://actor.im")

    val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
    implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
    implicit val authService = buildAuthService()
    implicit val messagingService = messaging.MessagingServiceImpl(mediator)
    implicit val groupsService = new GroupsServiceImpl(groupInviteConfig)
    val sequenceConfig = SequenceServiceConfig.load().toOption.get
    val sequenceService = new SequenceServiceImpl(sequenceConfig)

    lazy val llectro = new Llectro
    lazy val downloadManager = new DownloadManager

    object Screen {
      val W = 1080
      val H = 1920
    }

    MessageInterceptor.startSingleton(llectro, downloadManager, mediator, LlectroInterceptionConfig(messageCount))
    val interceptorProxy = MessageInterceptor.startSingletonProxy()

    val llectroService = new LlectroServiceImpl(llectro)

    def e1(): Unit = {
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

      Await.result(llectroService.jhandleInitLlectro(Screen.W, Screen.H, clientData1), 5.seconds)
      Await.result(llectroService.jhandleInitLlectro(Screen.W, Screen.H, clientData2), 5.seconds)

      MessageInterceptor.reFetch(interceptorProxy)
      Thread.sleep(5000)

      sendMessages(user2Peer)(clientData1)
      Thread.sleep(5000)

      val (randomId1, seq1, state1) = checkNewAdExists(0, Array.empty, clientData1, user2Peer)
      val (randomId2, seq2, state2) = checkNewAdExists(0, Array.empty, clientData2, user2Peer)

      sendMessages(user2Peer)(clientData1)
      Thread.sleep(5000)

      checkUpdatedAdExists(randomId1, seq1, state1, clientData1, user2Peer)
      checkUpdatedAdExists(randomId2, seq2, state2, clientData2, user2Peer)
    }

    def e2(): Unit = {
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

      MessageInterceptor.reFetch(interceptorProxy)
      Thread.sleep(5000)

      sendMessages(user2Peer)(clientData1)
      Thread.sleep(5000)

      checkNOAdExists(0, Array.empty, clientData1, user2Peer)
      checkNOAdExists(0, Array.empty, clientData2, user2Peer)
    }

    def e3(): Unit = {
      val (llectroUser, authId1, _) = createUser()
      val sessionId1 = createSessionId()

      val (regularUser, authId2, _) = createUser()
      val sessionId2 = createSessionId()

      val llectroUserData = ClientData(authId1, sessionId1, Some(llectroUser.id))
      val regularUserData = ClientData(authId2, sessionId2, Some(regularUser.id))

      val user1AccessHash = ACLUtils.userAccessHash(authId2, llectroUser.id, getUserModel(llectroUser.id).accessSalt)
      val user1Peer = OutPeer(PeerType.Private, llectroUser.id, user1AccessHash)

      val user2AccessHash = ACLUtils.userAccessHash(authId1, regularUser.id, getUserModel(regularUser.id).accessSalt)
      val user2Peer = OutPeer(PeerType.Private, regularUser.id, user2AccessHash)

      Await.result(llectroService.jhandleInitLlectro(Screen.W, Screen.H, llectroUserData), 5.seconds)

      MessageInterceptor.reFetch(interceptorProxy)
      Thread.sleep(5000)

      sendMessages(user2Peer)(llectroUserData)
      Thread.sleep(5000)

      val (randomId1, seq1, state1) = checkNewAdExists(0, Array.empty, llectroUserData, user2Peer)
      checkNOAdExists(0, Array.empty, regularUserData, user2Peer)
    }

    def e4(): Unit = {
      val (user1, user1AuthId, _) = createUser()
      val (user2, user2AuthId, _) = createUser()
      val (user3, user3AuthId, _) = createUser()
      val sessionId = createSessionId()

      val clientData1 = ClientData(user1AuthId, sessionId, Some(user1.id))
      val clientData2 = ClientData(user2AuthId, sessionId, Some(user2.id))
      val clientData3 = ClientData(user3AuthId, sessionId, Some(user3.id))

      val groupOutPeer = {
        implicit val clientData = clientData1
        createGroup("partial llectro group", Set(user2.id, user3.id)).groupPeer
      }.asOutPeer

      Await.result(llectroService.jhandleInitLlectro(Screen.W, Screen.H, clientData1), 5.seconds)

      MessageInterceptor.reFetch(interceptorProxy)
      Thread.sleep(5000)

      sendMessages(groupOutPeer)(clientData2)
      Thread.sleep(5000)

      val (randomId1, seq1, state1) = checkNewAdExists(0, Array.empty, clientData1, groupOutPeer)
      val (randomId2, seq2, state2) = checkNOAdExists(0, Array.empty, clientData2, groupOutPeer)
      val (randomId3, seq3, state3) = checkNOAdExists(0, Array.empty, clientData3, groupOutPeer)

      sendMessages(groupOutPeer)(clientData1)
      Thread.sleep(5000)

      checkUpdatedAdExists(randomId1, seq1, state1, clientData1, groupOutPeer)
      checkUpdatedNOAdExists(seq2, state2, clientData2, groupOutPeer)
      checkUpdatedNOAdExists(seq3, state3, clientData3, groupOutPeer)
    }

    def e5(): Unit = {
      val (user1, user1AuthId, _) = createUser()
      val (user2, user2AuthId, _) = createUser()
      val (user3, user3AuthId, _) = createUser()
      val sessionId = createSessionId()

      val clientData1 = ClientData(user1AuthId, sessionId, Some(user1.id))
      val clientData2 = ClientData(user2AuthId, sessionId, Some(user2.id))
      val clientData3 = ClientData(user3AuthId, sessionId, Some(user3.id))

      val groupOutPeer1 = {
        implicit val clientData = clientData1
        createGroup("partial llectro group", Set(user2.id, user3.id)).groupPeer
      }.asOutPeer

      Await.result(llectroService.jhandleInitLlectro(Screen.W, Screen.H, clientData1), 5.seconds)

      refetch(3)
      Thread.sleep(5000)

      sendMessages(groupOutPeer1)(clientData2)
      Thread.sleep(5000)

      val (randomId1, seq1, state1) = checkNewAdExists(0, Array.empty, clientData1, groupOutPeer1)
      val (randomId2, seq2, state2) = checkNOAdExists(0, Array.empty, clientData2, groupOutPeer1)
      checkNOAdExists(0, Array.empty, clientData3, groupOutPeer1)

      val groupOutPeer2 = {
        implicit val clientData = clientData2
        createGroup("partial llectro group", Set(user1.id)).groupPeer
      }.asOutPeer

      refetch(5)
      Thread.sleep(5000)

      sendMessages(groupOutPeer2)(clientData1)
      Thread.sleep(5000)

      checkNewAdExists(seq1, state1, clientData1, groupOutPeer2)
      checkNOAdExists(seq2, state2, clientData2, groupOutPeer2)

    }

    def e6(): Unit = {
      val (llectroUser, user1AuthId, _) = createUser()
      val (user2, user2AuthId, _) = createUser()
      val (user3, user3AuthId, _) = createUser()

      val llectroClientData = ClientData(user1AuthId, createSessionId(), Some(llectroUser.id))
      val clientData2 = ClientData(user2AuthId, createSessionId(), Some(user2.id))
      val clientData3 = ClientData(user3AuthId, createSessionId(), Some(user3.id))

      val user2AccessHash = ACLUtils.userAccessHash(user1AuthId, user2.id, getUserModel(user2.id).accessSalt)
      val user2Peer = OutPeer(PeerType.Private, user2.id, user2AccessHash)

      val groupOutPeer1 = {
        implicit val clientData = llectroClientData
        createGroup("partial llectro group", Set(user2.id, user3.id)).groupPeer
      }.asOutPeer

      Await.result(llectroService.jhandleInitLlectro(Screen.W, Screen.H, llectroClientData), 5.seconds)

      refetch(3)
      Thread.sleep(5000)

      sendMessages(groupOutPeer1)(clientData2)
      Thread.sleep(5000)

      val (randomId1, seq1, state1) = checkNewAdExists(0, Array.empty, llectroClientData, groupOutPeer1)
      val (randomId2, seq2, state2) = checkNOAdExists(0, Array.empty, clientData2, groupOutPeer1)
      checkNOAdExists(0, Array.empty, clientData3, groupOutPeer1)

      val groupOutPeer2 = {
        implicit val clientData = clientData2
        createGroup("partial llectro group", Set(llectroUser.id)).groupPeer
      }.asOutPeer

      refetch(5)
      Thread.sleep(5000)

      sendMessages(groupOutPeer2)(llectroClientData)
      Thread.sleep(5000)

      val (_, seq11, state11) = checkNewAdExists(seq1, state1, llectroClientData, groupOutPeer2)
      val (_, seq22, state22) = checkNOAdExists(seq2, state2, clientData2, groupOutPeer2)

      sendMessages(user2Peer)(llectroClientData)
      Thread.sleep(10000 * 6)

      checkNewAdExists(seq11, state11, llectroClientData, user2Peer)
      checkNOAdExists(seq22, state22, clientData2, user2Peer)
    }

    def refetch(times: Int) = for (_ ← 1 to times) MessageInterceptor.reFetch(interceptorProxy)

    private def sendMessages(outPeer: OutPeer)(implicit clientData: ClientData): Unit = {
      val rng = ThreadLocalRandom.current()
      for (_ ← 1 to messageCount) {
        whenReady(messagingService.handleSendMessage(outPeer, rng.nextLong(), TextMessage("Hi Shiva 1", Vector.empty, None)))(_ ⇒ ())
      }
    }

    private def checkNOAdExists(seq: Int, state: Array[Byte], clientData: ClientData, peer: OutPeer) = {
      val count = if (peer.`type` == PeerType.Group) messageCount + 1 else messageCount
      whenReady(sequenceService.jhandleGetDifference(seq, state, clientData)) { result ⇒
        val resp = result.toOption.get

        val updates = resp.updates
        updates.length shouldEqual count

        val message = UpdateMessageSent.parseFrom(CodedInputStream.newInstance(updates.last.update))
        message should matchPattern {
          case Right(UpdateMessageSent(_, _, _)) ⇒
        }

        (message.right.toOption.get.randomId, resp.seq, resp.state)
      }
    }

    private def checkUpdatedNOAdExists(seq: Int, state: Array[Byte], clientData: ClientData, peer: OutPeer) = {
      val count = messageCount
      whenReady(sequenceService.jhandleGetDifference(seq, state, clientData)) { result ⇒
        val resp = result.toOption.get

        val updates = resp.updates
        updates.length shouldEqual count

        val message = UpdateMessageSent.parseFrom(CodedInputStream.newInstance(updates.last.update))
        message should matchPattern {
          case Right(UpdateMessageSent(_, _, _)) ⇒
        }

        (message.right.toOption.get.randomId, resp.seq, resp.state)
      }
    }

    private def checkNewAdExists(seq: Int, state: Array[Byte], clientData: ClientData, peer: OutPeer): (Long, Int, Array[Byte]) = {
      val count = if (peer.`type` == PeerType.Group) messageCount + 2 else messageCount + 1
      whenReady(sequenceService.jhandleGetDifference(seq, state, clientData)) { result ⇒
        val resp = result.toOption.get

        val updates = resp.updates
        updates.length shouldEqual count

        val update = UpdateMessage.parseFrom(CodedInputStream.newInstance(updates.last.update)).right.toOption.get
        update.message shouldBe a[JsonMessage]
        update.peer.id should not equal (clientData.optUserId.get)
        update.senderUserId should equal(clientData.optUserId.get)

        (update.randomId, resp.seq, resp.state)
      }
    }

    private def checkUpdatedAdExists(randomId: Long, seq: Int, state: Array[Byte], clientData: ClientData, peer: OutPeer): (Int, Array[Byte]) = {
      val count = messageCount + 2
      whenReady(sequenceService.jhandleGetDifference(seq, state, clientData)) { result ⇒
        val resp = result.toOption.get

        val updates = resp.updates
        updates.length shouldEqual count

        val Seq(diffUpdate1, diffUpdate2) = updates.takeRight(2)

        val updateMessageContentChanged =
          UpdateMessageContentChanged.parseFrom(CodedInputStream.newInstance(diffUpdate1.update)).right.toOption.get
        updateMessageContentChanged.message shouldBe a[JsonMessage]
        updateMessageContentChanged.peer.id should not equal (clientData.optUserId.get)

        val updateMessageDateChanged =
          UpdateMessageDateChanged.parseFrom(CodedInputStream.newInstance(diffUpdate2.update)).right.toOption.get
        updateMessageDateChanged.randomId shouldEqual randomId

        (resp.seq, resp.state)
      }
    }
  }

}
