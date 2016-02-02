package im.actor.server.session

import akka.testkit.TestProbe
import com.google.protobuf.ByteString
import im.actor.api.rpc.auth._
import im.actor.api.rpc.codecs._
import im.actor.api.rpc.contacts.{ RequestGetContacts, UpdateContactRegistered }
import im.actor.api.rpc.messaging.{ ResponseLoadDialogs, RequestLoadDialogs }
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.peers.ApiUserOutPeer
import im.actor.api.rpc.sequence.{ RequestGetDifference, RequestSubscribeToOnline, RequestGetState }
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.weak.UpdateUserOffline
import im.actor.api.rpc.{ AuthorizedClientData, Request, RpcOk }
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport._
import im.actor.server.persist.AuthSessionRepo
import im.actor.server.sequence.{ SeqUpdatesExtension, WeakUpdatesExtension }
import im.actor.server.user.UserExtension
import scodec.bits._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

final class SessionSpec extends BaseSessionSpec {
  behavior of "Session actor"

  it should "send Drop on message on wrong message box" in sessions().wrongMessageBox
  it should "send NewSession on first HandleMessageBox" in sessions().newSession
  it should "reply to RpcRequestBox" in sessions().rpc
  it should "handle user authorization" in sessions().auth
  it should "subscribe to sequence updates" in sessions().seq
  it should "subscribe to weak updates" in sessions().weak
  it should "subscribe to presences" in sessions().pres
  it should "receive fat updates" in sessions().fatSeq
  it should "react to SessionHello" in sessions().hello

  case class sessions() {

    implicit val probe = TestProbe()

    val weakUpdatesExt = WeakUpdatesExtension(system)
    val seqUpdExt = SeqUpdatesExtension(system)

    def wrongMessageBox() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val session = system.actorOf(Session.props, s"${authId}_$sessionId")

      probe.send(session, HandleMessageBox(ByteString.copyFrom(BitVector.empty.toByteBuffer)))

      probe watch session

      probe.expectMsg(Drop(0, 0, "Failed to parse MessageBox"))
      probe.expectTerminated(session)
    }

    def newSession() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(75553333333L, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, ProtoRpcRequest(encodedRequest))

      expectNewSession(authId, sessionId, messageId)
      probe.receiveOne(1.second)
      probe.receiveOne(1.second)
      probe.expectNoMsg()
    }

    def rpc() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(75553333334L, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, ProtoRpcRequest(encodedRequest))
      expectNewSession(authId, sessionId, messageId)

      expectRpcResult(authId, sessionId) should matchPattern {
        case RpcOk(ResponseSendAuthCodeObsolete(_, false)) ⇒
      }
      probe.expectNoMsg(20.seconds)
    }

    def auth() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val firstMessageId = Random.nextLong()
      val phoneNumber = 75550000000L

      val encodedCodeRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(phoneNumber, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, firstMessageId, ProtoRpcRequest(encodedCodeRequest))

      expectNewSession(authId, sessionId, firstMessageId)

      val smsHash = expectRpcResult(authId, sessionId).asInstanceOf[RpcOk].response.asInstanceOf[ResponseSendAuthCodeObsolete].smsHash

      val encodedSignUpRequest = RequestCodec.encode(Request(RequestSignUpObsolete(
        phoneNumber = phoneNumber,
        smsHash = smsHash,
        smsCode = "0000",
        name = "Wayne Brain",
        deviceHash = Array(4, 5, 6),
        deviceTitle = "Specs virtual device",
        appId = 1,
        appKey = "appKey",
        isSilent = false
      ))).require

      val secondMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, secondMessageId, ProtoRpcRequest(encodedSignUpRequest))

      expectRpcResult(authId, sessionId) should matchPattern {
        case RpcOk(ResponseAuth(_, _)) ⇒
      }

      {
        val encodedLoadDialogs = RequestCodec.encode(Request(RequestLoadDialogs(0L, 100))).require
        val encodedGetDifference = RequestCodec.encode(Request(RequestGetDifference(0, Array(), Vector.empty))).require
        val encodedGetContacts = RequestCodec.encode(Request(RequestGetContacts(""))).require
        sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong, ProtoRpcRequest(encodedLoadDialogs))
        sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong, ProtoRpcRequest(encodedGetDifference))
        sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong, ProtoRpcRequest(encodedGetContacts))

        expectRpcResult(authId, sessionId)
        expectRpcResult(authId, sessionId)
        expectRpcResult(authId, sessionId)
      }

      val encodedSignOutRequest = RequestCodec.encode(Request(RequestSignOut)).require

      val thirdMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, thirdMessageId, ProtoRpcRequest(encodedSignOutRequest))

      expectRpcResult(authId, sessionId) should matchPattern {
        case RpcOk(ResponseVoid) ⇒
      }
    }

    def seq() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val firstMessageId = Random.nextLong()
      val phoneNumber = 75550000000L

      val encodedCodeRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(phoneNumber, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, firstMessageId, ProtoRpcRequest(encodedCodeRequest))

      expectNewSession(authId, sessionId, firstMessageId)

      val smsHash = expectRpcResult(authId, sessionId).asInstanceOf[RpcOk].response.asInstanceOf[ResponseSendAuthCodeObsolete].smsHash

      val encodedSignUpRequest = RequestCodec.encode(Request(RequestSignUpObsolete(
        phoneNumber = phoneNumber,
        smsHash = smsHash,
        smsCode = "0000",
        name = "Wayne Brain",
        deviceHash = Array(5, 5, 6),
        deviceTitle = "Specs virtual device",
        appId = 1,
        appKey = "appKey",
        isSilent = false
      ))).require

      val secondMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, secondMessageId, ProtoRpcRequest(encodedSignUpRequest))

      val authResult = expectRpcResult(authId, sessionId)
      authResult should matchPattern {
        case RpcOk(ResponseAuth(_, _)) ⇒
      }

      val authSession = Await.result(db.run(AuthSessionRepo.findByAuthId(authId)), 5.seconds).get
      implicit val clientData = AuthorizedClientData(authId, sessionId, authResult.asInstanceOf[RpcOk].response.asInstanceOf[ResponseAuth].user.id, authSession.id)

      val encodedGetSeqRequest = RequestCodec.encode(Request(RequestGetState(Vector.empty))).require

      val thirdMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, thirdMessageId, ProtoRpcRequest(encodedGetSeqRequest))

      expectRpcResult(authId, sessionId) should matchPattern {
        case RpcOk(ResponseSeq(_, _)) ⇒
      }

      val update = UpdateContactRegistered(1, true, 1L, 2L)
      Await.result(UserExtension(system).broadcastClientUpdate(update, None, isFat = false), 5.seconds)

      expectSeqUpdate(authId, sessionId).update should ===(update.toByteArray)
    }

    def fatSeq() = {
      val (user, authId, authSid, _) = createUser()
      val sessionId = Random.nextLong

      implicit val clientData = AuthorizedClientData(authId, sessionId, user.id, authSid)

      val encodedGetSeqRequest = RequestCodec.encode(Request(RequestGetState(Vector.empty))).require

      val thirdMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, thirdMessageId, ProtoRpcRequest(encodedGetSeqRequest))

      ignoreNewSession()
      expectRpcResult(authId, sessionId) should matchPattern {
        case RpcOk(ResponseSeq(_, _)) ⇒
      }

      val update = UpdateContactRegistered(user.id, true, 1L, 2L)
      whenReady(UserExtension(system).broadcastUserUpdate(user.id, update, pushText = Some("text"), isFat = true, reduceKey = None, deliveryId = None))(identity)

      val fat = expectFatSeqUpdate(authId, sessionId)

      fat.users.head.id should ===(user.id)
      fat.update should ===(update.toByteArray)

    }

    def weak() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val firstMessageId = Random.nextLong()
      val phoneNumber = 75550000000L

      val encodedCodeRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(phoneNumber, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, firstMessageId, ProtoRpcRequest(encodedCodeRequest))

      expectNewSession(authId, sessionId, firstMessageId)

      val smsHash = expectRpcResult(authId, sessionId).asInstanceOf[RpcOk].response.asInstanceOf[ResponseSendAuthCodeObsolete].smsHash

      val encodedSignUpRequest = RequestCodec.encode(Request(RequestSignUpObsolete(
        phoneNumber = phoneNumber,
        smsHash = smsHash,
        smsCode = "0000",
        name = "Wayne Brain",
        deviceHash = Array(5, 5, 6),
        deviceTitle = "Specs virtual device",
        appId = 1,
        appKey = "appKey",
        isSilent = false
      ))).require

      val secondMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, secondMessageId, ProtoRpcRequest(encodedSignUpRequest))

      val authResult = expectRpcResult(authId, sessionId)
      authResult should matchPattern {
        case RpcOk(ResponseAuth(_, _)) ⇒
      }

      implicit val clientData = AuthorizedClientData(authId, sessionId, authResult.asInstanceOf[RpcOk].response.asInstanceOf[ResponseAuth].user.id, Await.result(db.run(AuthSessionRepo.findByAuthId(authId)), 5.seconds).get.id)

      val update = UpdateContactRegistered(1, isSilent = true, 1L, 5L)
      Await.result(weakUpdatesExt.broadcastUserWeakUpdate(clientData.userId, update, reduceKey = None), 1.second)

      expectWeakUpdate(authId, sessionId).update should ===(update.toByteArray)
    }

    def pres() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val firstMessageId = Random.nextLong()
      val phoneNumber = 75550000000L

      val encodedCodeRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(phoneNumber, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, firstMessageId, ProtoRpcRequest(encodedCodeRequest))

      expectNewSession(authId, sessionId, firstMessageId)

      val smsHash = expectRpcResult(authId, sessionId).asInstanceOf[RpcOk].response.asInstanceOf[ResponseSendAuthCodeObsolete].smsHash

      {
        val encodedSignUpRequest = RequestCodec.encode(Request(RequestSignUpObsolete(
          phoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = "0000",
          name = "Wayne Brain",
          deviceHash = Array(5, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey",
          isSilent = false
        ))).require

        val messageId = Random.nextLong()
        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, ProtoRpcRequest(encodedSignUpRequest))

        val authResult = expectRpcResult(authId, sessionId)
        authResult should matchPattern {
          case RpcOk(ResponseAuth(_, _)) ⇒
        }
      }

      {
        val userForSubscribe = 2

        // FIXME: real user and real accessHash
        val encodedSubscribeRequest = RequestCodec.encode(Request(RequestSubscribeToOnline(Vector(ApiUserOutPeer(userForSubscribe, 0L))))).require

        val messageId = Random.nextLong()
        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, ProtoRpcRequest(encodedSubscribeRequest))

        val subscribeResult = expectRpcResult(authId, sessionId)
        subscribeResult should matchPattern {
          case RpcOk(ResponseVoid) ⇒
        }
      }

      val ub = expectWeakUpdate(authId, sessionId)

      ub.updateHeader should ===(UpdateUserOffline.header)
    }

    def hello() = {
      val (_, authId, _, _) = createUser()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, SessionHello)
      expectNewSession(authId, sessionId, messageId)
      expectMessageAck(messageId)
      probe.expectNoMsg()
    }
  }
}
