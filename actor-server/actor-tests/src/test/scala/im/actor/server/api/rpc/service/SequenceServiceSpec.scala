package im.actor.server.api.rpc.service

import com.google.protobuf.CodedInputStream
import com.typesafe.config.ConfigFactory
import im.actor.api.rpc._
import im.actor.api.rpc.contacts.UpdateContactsAdded
import im.actor.api.rpc.counters.{ ApiAppCounters, UpdateCountersChanged }
import im.actor.api.rpc.messaging.{ ApiTextMessage, UpdateMessageContentChanged }
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.sequence.{ ApiUpdateContainer, ApiUpdateOptimization, ResponseGetDifference, UpdateEmptyUpdate }
import im.actor.server._
import im.actor.server.api.rpc.service.sequence.SequenceServiceConfig

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

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
  with ImplicitSequenceService {

  behavior of "Sequence service"

  it should "get state" in getState
  it should "get difference" in getDifference
  it should "not produce empty difference if there is one update bigger than difference size limit" in bigUpdate
  it should "map updates correctly" in mapUpdates
  it should "exclude optimized updates from sequence" in optimizedUpdates

  private val config = SequenceServiceConfig.load().get

  implicit lazy val service = new sequence.SequenceServiceImpl(config)
  implicit lazy val msgService = messaging.MessagingServiceImpl()

  def getState() = {
    val (user, authId, authSid, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

    whenReady(service.handleGetState(Vector.empty)) { res ⇒
      res should matchPattern { case Ok(ResponseSeq(0, _)) ⇒ }
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
    Await.result(Future.sequence((0L to 3840) map { i ⇒
      val update = UpdateMessageContentChanged(user2Peer, i, message)
      seqUpdExt.deliverUserUpdate(user.id, update)
    }), 10.seconds)

    var totalUpdates: Seq[ApiUpdateContainer] = Seq.empty

    val (seq1, state1) = whenReady(service.handleGetDifference(0, Array.empty, Vector.empty)) { res ⇒
      val diff = res.toOption.get
      inside(res) {
        case Ok(ResponseGetDifference(seq, state, users, updates, needMore, groups, _, _, _)) ⇒
          updates.map(_.toByteArray.length).sum should be <= withError(config.maxDifferenceSize)
          needMore shouldEqual true
          totalUpdates ++= updates
          diff.seq shouldEqual updates.length
      }
      (diff.seq, diff.state)
    }

    val (seq2, state2) = whenReady(service.handleGetDifference(seq1, state1, Vector.empty)) { res ⇒
      val diff = res.toOption.get
      inside(res) {
        case Ok(ResponseGetDifference(seq, state, users, updates, needMore, groups, _, _, _)) ⇒
          (updates.map(_.toByteArray.length).sum <= withError(config.maxDifferenceSize)) shouldEqual true
          needMore shouldEqual true
          totalUpdates ++= updates
          diff.seq shouldEqual seq1 + updates.length
      }
      (diff.seq, diff.state)
    }

    val finalSeq = whenReady(service.handleGetDifference(seq2, state2, Vector.empty)) { res ⇒
      val diff = res.toOption.get
      inside(res) {
        case Ok(ResponseGetDifference(seq, state, users, updates, needMore, groups, _, _, _)) ⇒
          (updates.map(_.toByteArray.length).sum <= withError(config.maxDifferenceSize)) shouldEqual true
          needMore shouldEqual false
          totalUpdates ++= updates
          diff.seq shouldEqual seq2 + updates.length
      }
      diff.seq
    }

    totalUpdates.zipWithIndex foreach {
      case (data, i) ⇒
        val in = CodedInputStream.newInstance(data.update)
        val id = i.toLong
        UpdateMessageContentChanged.parseFrom(in) should matchPattern {
          case Right(UpdateMessageContentChanged(_, `id`, _)) ⇒
        }
    }

    totalUpdates.length shouldEqual 3841
    finalSeq shouldEqual 3841

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
}
