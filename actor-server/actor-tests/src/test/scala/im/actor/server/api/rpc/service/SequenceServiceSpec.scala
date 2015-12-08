package im.actor.server.api.rpc.service

import com.google.protobuf.CodedInputStream
import com.typesafe.config.ConfigFactory
import im.actor.api.rpc._
import im.actor.api.rpc.messaging.{ ApiTextMessage, UpdateMessageContentChanged }
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.sequence.{ ApiDifferenceUpdate, ResponseGetDifference }
import im.actor.server._
import im.actor.server.api.rpc.service.sequence.SequenceServiceConfig
import im.actor.server.sequence.SeqUpdatesExtension

import scala.concurrent.duration._
import scala.concurrent.{ Future, Await }

final class SequenceServiceSpec extends BaseAppSuite({
  ActorSpecification.createSystem(
    ConfigFactory.parseString(
      """
        push.seq-updates-manager.receive-timeout = 3 seconds
      """
    )
  )
}) with ImplicitSessionRegion with ImplicitAuthService {

  behavior of "Sequence service"

  it should "get state" in getState
  it should "get difference" in getDifference
  it should "not produce empty difference if there is one update bigger than difference size limit" in bigUpdate

  private val config = SequenceServiceConfig.load().get
  private lazy val seqUpdExt = SeqUpdatesExtension(system)

  implicit lazy val service = new sequence.SequenceServiceImpl(config)
  implicit lazy val msgService = messaging.MessagingServiceImpl()

  def getState() = {
    val (user, authId, authSid, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid)))

    whenReady(service.handleGetState()) { res ⇒
      res should matchPattern { case Ok(ResponseSeq(0, _)) ⇒ }
    }
  }

  def getDifference() = {
    val (user, authId, authSid, _) = createUser()
    val (user2, _, _, _) = createUser()
    val sessionId = createSessionId()

    val user2Peer = ApiPeer(ApiPeerType.Private, user2.id)

    implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid)))

    val message = ApiTextMessage("Hello mr President. Zzz", Vector.empty, None)

    def withError(maxUpdateSize: Long): Int = {
      val example = UpdateMessageContentChanged(user2Peer, 1000L, message)
      (maxUpdateSize * (1 + 5.toDouble / example.getSerializedSize)).toInt
    }

    //serialized update size is: 40 bytes for body + 4 bytes for header, 44 bytes total
    //with max update size of 20 KiB 1281 updates should split into three parts
    Await.result(Future.sequence((0L to 1280L) map { i ⇒
      val update = UpdateMessageContentChanged(user2Peer, i, message)
      seqUpdExt.deliverSingleUpdate(user.id, update)
    }), 10.seconds)

    var totalUpdates: Seq[ApiDifferenceUpdate] = Seq.empty

    val (seq1, state1) = whenReady(service.handleGetDifference(0, Array.empty)) { res ⇒
      val diff = res.toOption.get
      inside(res) {
        case Ok(ResponseGetDifference(seq, state, users, updates, needMore, groups)) ⇒
          println(user2.id)
          println(updates.map(_.update.length))
          updates.map(_.toByteArray.length).sum should be <= withError(config.maxDifferenceSize)
          needMore shouldEqual true
          totalUpdates ++= updates
          diff.seq shouldEqual updates.length
      }
      (diff.seq, diff.state)
    }

    val (seq2, state2) = whenReady(service.handleGetDifference(seq1, state1)) { res ⇒
      val diff = res.toOption.get
      inside(res) {
        case Ok(ResponseGetDifference(seq, state, users, updates, needMore, groups)) ⇒
          (updates.map(_.toByteArray.length).sum <= withError(config.maxDifferenceSize)) shouldEqual true
          needMore shouldEqual true
          totalUpdates ++= updates
          diff.seq shouldEqual seq1 + updates.length
      }
      (diff.seq, diff.state)
    }

    val finalSeq = whenReady(service.handleGetDifference(seq2, state2)) { res ⇒
      val diff = res.toOption.get
      inside(res) {
        case Ok(ResponseGetDifference(seq, state, users, updates, needMore, groups)) ⇒
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

    totalUpdates.length shouldEqual 1281
    finalSeq shouldEqual 1281

  }

  def bigUpdate() = {
    val (user, authId, authSid, _) = createUser()
    val (user2, _, _, _) = createUser()
    val sessionId = createSessionId()

    val user2Peer = ApiPeer(ApiPeerType.Private, user2.id)

    implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid)))

    val maxSize = config.maxDifferenceSize

    val smallUpdate = UpdateMessageContentChanged(user2Peer, 1L, ApiTextMessage("Hello", Vector.empty, None))
    val bigUpdate = UpdateMessageContentChanged(user2Peer, 2L, ApiTextMessage((for (_ ← 1L to maxSize * 10) yield "b").mkString(""), Vector.empty, None))

    whenReady(seqUpdExt.deliverSingleUpdate(user.id, smallUpdate))(identity)
    whenReady(seqUpdExt.deliverSingleUpdate(user.id, bigUpdate))(identity)
    whenReady(seqUpdExt.deliverSingleUpdate(user.id, bigUpdate))(identity)

    // expect first small update and needMore == true
    val (seq1, state1) = whenReady(service.handleGetDifference(0, Array.empty)) { res ⇒
      inside(res) {
        case Ok(ResponseGetDifference(_, _, _, updates, true, _)) ⇒
          updates.size shouldEqual 1
      }

      val diff = res.toOption.get
      (diff.seq, diff.state)
    }

    // expect first big update and needMore == true
    val (seq2, state2) = whenReady(service.handleGetDifference(seq1, state1)) { res ⇒
      inside(res) {
        case Ok(ResponseGetDifference(_, _, _, updates, true, _)) ⇒
          updates.size shouldEqual 1
      }

      val diff = res.toOption.get
      (diff.seq, diff.state)
    }

    // expect second big update and needMore == false
    whenReady(service.handleGetDifference(seq2, state2)) { res ⇒
      inside(res) {
        case Ok(ResponseGetDifference(_, _, _, updates, false, _)) ⇒
          updates.size shouldEqual 1
      }
    }
  }
}
