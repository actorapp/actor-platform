package im.actor.server.api.rpc.service

import com.typesafe.config.ConfigFactory
import im.actor.api.rpc._
import im.actor.api.rpc.contacts.UpdateContactsAdded
import im.actor.api.rpc.counters.{ ApiAppCounters, UpdateCountersChanged }
import im.actor.api.rpc.messaging.{ ApiTextMessage, UpdateChatGroupsChanged, UpdateMessageContentChanged }
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.sequence.{ ApiUpdateContainer, ApiUpdateOptimization, ResponseGetDifference, UpdateEmptyUpdate }
import im.actor.server._
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceConfig, SequenceServiceImpl }
import im.actor.server.dialog.{ DialogExtension, DialogGroupKeys }
import im.actor.server.sequence.{ CommonState, CommonStateVersion, UserSequence }

import scala.concurrent.Future

final class SequenceServiceSpec extends BaseAppSuite({
  ActorSpecification.createSystem(
    ConfigFactory.parseString(
      """
        push.seq-updates-manager.receive-timeout = 3 seconds
      """
    )
  )
}) with ImplicitSessionRegion
  with ImplicitAuthService
  with SeqUpdateMatchers
  with GroupsServiceHelpers
  with ImplicitSequenceService {

  behavior of "Sequence service"

  it should "get state" in getState
  it should "get difference" in getDifference
  it should "not produce empty difference if there is one update bigger than difference size limit" in bigUpdate
  it should "map updates correctly" in mapUpdates
  it should "exclude optimized updates from sequence" in optimizedUpdates
  it should "return single UpdateChatGroupsChanged in difference as if it was applied after all message reads" in chatGroupChangedRead
  it should "return single UpdateChatGroupsChanged in difference as if it was aplied after all received messages" in chatGroupChangedReceived

  private val config = SequenceServiceConfig.load().get

  implicit lazy val service = new SequenceServiceImpl(config)
  implicit lazy val msgService = MessagingServiceImpl()
  implicit lazy val groupService = new GroupsServiceImpl(GroupInviteConfig(""))

  def getState() = {
    val (user, authId, authSid, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

    whenReady(service.handleGetState(Vector.empty)) { res ⇒
      res should matchPattern { case Ok(ResponseSeq(1, _)) ⇒ }
    }
  }

  def getDifference() = {
    val (user, authId, authSid, _) = createUser()
    val (user2, _, _, _) = createUser()
    val sessionId = createSessionId()

    val user2Peer = ApiPeer(ApiPeerType.Private, user2.id)

    implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

    val message = ApiTextMessage("Hello mr President. Zzz", Vector.empty, None)

    def withError(maxUpdateSize: Long): Int = {
      val example = UpdateMessageContentChanged(user2Peer, 1000L, message)
      (maxUpdateSize * (1 + 5.toDouble / example.getSerializedSize)).toInt
    }

    // serialized update size is: 40 bytes for body + 4 bytes for header, 44 bytes total
    // with max update size of 60 KiB 3840 updates should split into three parts
    whenReady(Future.sequence((1000L to 4840) map { i ⇒
      val update = UpdateMessageContentChanged(user2Peer, i, message)
      seqUpdExt.deliverUserUpdate(user.id, update)
    }))(identity)

    var totalUpdates: Seq[ApiUpdateContainer] = Seq.empty

    val (seq1, state1) = repeatAfterSleep(5) {
      whenReady(service.handleGetDifference(0, Array.empty, Vector.empty)) { res ⇒
        val diff = res.toOption.get
        inside(res) {
          case Ok(ResponseGetDifference(_, _, _, updates, needMore, _, _, _, _)) ⇒
            updates.map(_.toByteArray.length).sum should be <= withError(config.maxDifferenceSize)
            needMore shouldEqual true
            totalUpdates ++= updates
            diff.seq shouldEqual updates.length
        }
        val commonState = CommonState.parseFrom(diff.state)
        commonState.version shouldEqual CommonStateVersion.V1
        commonState.seq shouldEqual diff.seq
        (diff.seq, diff.state)
      }
    }

    val (seq2, state2) = repeatAfterSleep(5) {
      whenReady(service.handleGetDifference(seq1, state1, Vector.empty)) { res ⇒
        val diff = res.toOption.get
        inside(res) {
          case Ok(ResponseGetDifference(_, _, _, updates, needMore, _, _, _, _)) ⇒
            (updates.map(_.toByteArray.length).sum <= withError(config.maxDifferenceSize)) shouldEqual true
            needMore shouldEqual true
            totalUpdates ++= updates
            diff.seq shouldEqual seq1 + updates.length
        }
        val commonState = CommonState.parseFrom(diff.state)
        commonState.version shouldEqual CommonStateVersion.V1
        commonState.seq shouldEqual diff.seq
        (diff.seq, diff.state)
      }
    }

    val (finalSeq, finalState) = repeatAfterSleep(5) {
      whenReady(service.handleGetDifference(seq2, state2, Vector.empty)) { res ⇒
        val diff = res.toOption.get
        val comState = CommonState.parseFrom(diff.state)
        inside(res) {
          case Ok(ResponseGetDifference(_, _, _, updates, needMore, _, _, _, _)) ⇒
            (updates.map(_.toByteArray.length).sum <= withError(config.maxDifferenceSize)) shouldEqual true
            needMore shouldEqual false
            totalUpdates ++= updates
            diff.seq shouldEqual seq2 + updates.length + UserSequence.Const.SeqStart // this is real user's seq
        }
        val commonState = CommonState.parseFrom(diff.state)
        commonState.version shouldEqual CommonStateVersion.V1
        commonState.seq shouldEqual diff.seq - UserSequence.Const.SeqStart // user's seq starts from `UserSequence.Const.SeqStart` - take away difference
        (diff.seq, diff.state)
      }
    }

    totalUpdates.zipWithIndex foreach {
      case (data, i) ⇒
        val id = i.toLong + 1000L
        UpdateMessageContentChanged.parseFrom(data.update) should matchPattern {
          case Right(UpdateMessageContentChanged(_, `id`, _)) ⇒
        }
    }

    totalUpdates.length shouldEqual 3841
    val current = getCurrentState
    finalSeq shouldEqual current.seq
    CommonState.parseFrom(finalState).seq shouldEqual CommonState.parseFrom(current.state.toByteArray).seq
  }

  def bigUpdate() = {
    val (user, authId, authSid, _) = createUser()
    val (user2, _, _, _) = createUser()
    val sessionId = createSessionId()

    val user2Peer = ApiPeer(ApiPeerType.Private, user2.id)

    implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

    val maxSize = config.maxDifferenceSize

    val smallUpdate = UpdateMessageContentChanged(user2Peer, 1L, ApiTextMessage("Hello", Vector.empty, None))
    val bigUpdate = UpdateMessageContentChanged(user2Peer, 2L, ApiTextMessage((for (_ ← 1L to maxSize * 10) yield "b").mkString(""), Vector.empty, None))

    whenReady(seqUpdExt.deliverUserUpdate(user.id, smallUpdate))(identity)
    whenReady(seqUpdExt.deliverUserUpdate(user.id, bigUpdate))(identity)
    whenReady(seqUpdExt.deliverUserUpdate(user.id, bigUpdate))(identity)

    // expect first small update and needMore == true
    val (seq1, state1) = whenReady(service.handleGetDifference(0, Array.empty, Vector.empty)) { res ⇒
      inside(res) {
        case Ok(ResponseGetDifference(_, _, _, updates, true, _, _, _, _)) ⇒
          updates.size shouldEqual 1
      }

      val diff = res.toOption.get
      (diff.seq, diff.state)
    }

    // expect first big update and needMore == true
    val (seq2, state2) = whenReady(service.handleGetDifference(seq1, state1, Vector.empty)) { res ⇒
      inside(res) {
        case Ok(ResponseGetDifference(_, _, _, updates, true, _, _, _, _)) ⇒
          updates.size shouldEqual 1
      }

      val diff = res.toOption.get
      (diff.seq, diff.state)
    }

    // expect second big update and needMore == false
    whenReady(service.handleGetDifference(seq2, state2, Vector.empty)) { res ⇒
      inside(res) {
        case Ok(ResponseGetDifference(_, _, _, updates, false, _, _, _, _)) ⇒
          updates.size shouldEqual 1
      }
    }
  }

  def mapUpdates() = {
    val (user, authId1, authSid1, _) = createUser()
    val (authId2, authSid2) = createAuthId(user.id)

    val clientData1 = ClientData(authId1, 1L, Some(AuthData(user.id, authSid1, 42)))
    val clientData2 = ClientData(authId2, 2L, Some(AuthData(user.id, authSid2, 42)))

    // when we deliver empty update as default, it won't show up in user's sequence
    whenReady(seqUpdExt.deliverCustomUpdate(
      userId = user.id,
      authId = authId1,
      default = Some(UpdateEmptyUpdate),
      custom = Map(authId2 → UpdateContactsAdded(Vector(1)))
    ))(identity)

    {
      implicit val clientData = clientData1

      expectNoUpdate(emptyState, classOf[UpdateEmptyUpdate])
      whenReady(service.handleGetDifference(0, Array.empty, Vector.empty)) { res ⇒
        inside(res) {
          case Ok(rsp: ResponseGetDifference) ⇒
            rsp.updates shouldBe empty
        }
      }
    }

    {
      implicit val clientData = clientData2

      expectUpdate(classOf[UpdateContactsAdded])(identity)
      whenReady(service.handleGetDifference(0, Array.empty, Vector.empty)) { res ⇒
        inside(res) {
          case Ok(rsp: ResponseGetDifference) ⇒
            rsp.updates should have length 1
            rsp.updates.head.updateHeader should be(UpdateContactsAdded.header)
        }
      }
    }
  }

  def optimizedUpdates() = {
    val (user, authId1, authSid1, _) = createUser()
    val (authId2, authSid2) = createAuthId(user.id)

    val clientData1 = ClientData(authId1, 1L, Some(AuthData(user.id, authSid1, 42))) // this one will come with counters optimization
    val clientData2 = ClientData(authId2, 2L, Some(AuthData(user.id, authSid2, 42))) // this will be without optimizations at all

    {
      implicit val cd = clientData1
      whenReady(sequenceService.handleGetState(Vector(ApiUpdateOptimization.STRIP_COUNTERS)))(identity)
    }

    val toBeOptimized = UpdateCountersChanged(ApiAppCounters(Some(22)))
    whenReady(seqUpdExt.deliverUserUpdate(user.id, toBeOptimized))(identity)

    {
      implicit val cd = clientData1

      expectNoUpdate(emptyState, classOf[UpdateCountersChanged])
      whenReady(service.handleGetDifference(0, Array.empty, Vector.empty)) { res ⇒
        inside(res) {
          case Ok(rsp: ResponseGetDifference) ⇒
            rsp.updates shouldBe empty
        }
      }
    }

    {
      implicit val clientData = clientData2

      expectUpdate(classOf[UpdateCountersChanged])(identity)
      whenReady(service.handleGetDifference(0, Array.empty, Vector.empty)) { res ⇒
        inside(res) {
          case Ok(rsp: ResponseGetDifference) ⇒
            rsp.updates should have length 1
            rsp.updates.head.updateHeader should be(UpdateCountersChanged.header)
        }
      }
    }
  }

  def chatGroupChangedRead(): Unit = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()

    val sessionId = createSessionId()
    val aliceClientData = ClientData(aliceAuthId, sessionId, Some(AuthData(alice.id, aliceAuthSid, 42)))

    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val group = {
      implicit val cd = aliceClientData
      createGroup("Some group", Set(bob.id)).groupPeer
    }

    val groupReadDates = {
      implicit val cd = ClientData(bobAuthId, sessionId, Some(AuthData(bob.id, bobAuthSid, 42)))
      1 to 20 map { i ⇒
        sendMessageToGroup(group.groupId, textMessage(s"Hello in group $i"))._2.date
      }
    }

    val bobReadDates = {
      implicit val cd = ClientData(bobAuthId, sessionId, Some(AuthData(bob.id, bobAuthSid, 42)))
      1 to 20 map { i ⇒
        sendMessageToUser(alice.id, s"Hello $i")._2.date
      }
    }

    {
      implicit val cd = aliceClientData
      // FAVOURITE
      whenReady(msgService.handleFavouriteDialog(getOutPeer(bob.id, aliceAuthId)))(identity)

      // read 5 messages, 15 left
      whenReady(msgService.handleMessageRead(getOutPeer(bob.id, aliceAuthId), bobReadDates(4)))(identity)

      // UNFAVOURITE
      whenReady(msgService.handleUnfavouriteDialog(getOutPeer(bob.id, aliceAuthId)))(identity)

      // read 10 messages, 10 left
      whenReady(msgService.handleMessageRead(getOutPeer(bob.id, aliceAuthId), bobReadDates(9)))(identity)

      // read all messages in group
      whenReady(msgService.handleMessageRead(group.asOutPeer, groupReadDates.last))(identity)
    }

    {
      implicit val cd = ClientData(bobAuthId, sessionId, Some(AuthData(bob.id, bobAuthSid, 42)))
      1 to 10 map { i ⇒
        sendMessageToUser(alice.id, s"Hello $i")._2.date
      }
    }

    {
      implicit val cd = aliceClientData

      whenReady(service.handleGetDifference(0, Array.empty, Vector.empty)) { res ⇒
        inside(res) {
          case Ok(rsp: ResponseGetDifference) ⇒
            val groupedChatsUpdates = rsp.updates.filter(_.updateHeader == UpdateChatGroupsChanged.header)
            groupedChatsUpdates should have length 1
        }
      }

      expectUpdate(classOf[UpdateChatGroupsChanged]) { upd ⇒
        val optDirect = upd.dialogs.find(_.key == DialogGroupKeys.Direct)
        optDirect shouldBe defined

        val bobsDialog = optDirect.get.dialogs.find(_.peer.id == bob.id)
        bobsDialog.get.counter shouldEqual 20

        val optGroups = upd.dialogs.find(_.key == DialogGroupKeys.Groups)
        optGroups shouldBe defined

        val groupDialog = optGroups.get.dialogs.find(_.peer.id == group.groupId)
        groupDialog.get.counter shouldEqual 0
      }
    }

  }

  def chatGroupChangedReceived(): Unit = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()

    val sessionId = createSessionId()
    val aliceClientData = ClientData(aliceAuthId, sessionId, Some(AuthData(alice.id, aliceAuthSid, 42)))

    val (bob, bobAuthId, bobAuthSid, _) = createUser()
    val bobClientData = ClientData(bobAuthId, sessionId, Some(AuthData(bob.id, bobAuthSid, 42)))

    {
      implicit val cd = bobClientData
      1 to 10 map { i ⇒
        sendMessageToUser(alice.id, s"Hello $i")._2.date
      }
    }

    {
      implicit val cd = aliceClientData
      // FAVOURITE
      whenReady(msgService.handleFavouriteDialog(getOutPeer(bob.id, aliceAuthId)))(identity)

      // UNFAVOURITE
      whenReady(msgService.handleUnfavouriteDialog(getOutPeer(bob.id, aliceAuthId)))(identity)
    }

    {
      implicit val cd = bobClientData
      1 to 5 map { i ⇒
        sendMessageToUser(alice.id, s"Hello $i")._2.date
      }
    }

    {
      implicit val cd = aliceClientData

      whenReady(service.handleGetDifference(0, Array.empty, Vector.empty)) { res ⇒
        inside(res) {
          case Ok(rsp: ResponseGetDifference) ⇒
            val groupedChatsUpdates = rsp.updates.filter(_.updateHeader == UpdateChatGroupsChanged.header)
            groupedChatsUpdates should have length 1
        }
      }

      expectUpdate(classOf[UpdateChatGroupsChanged]) { upd ⇒
        val optDirect = upd.dialogs.find(_.key == DialogGroupKeys.Direct)
        optDirect shouldBe defined

        val bobsDialog = optDirect.get.dialogs.find(_.peer.id == bob.id)
        bobsDialog.get.counter shouldEqual 15
      }
    }

  }

}
