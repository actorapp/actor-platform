package im.actor.server.session

import akka.testkit.TestProbe
import com.google.protobuf.ByteString
import im.actor.api.rpc.auth._
import im.actor.api.rpc.codecs._
import im.actor.api.rpc.collections.ApiStringValue
import im.actor.api.rpc.contacts.{ RequestGetContacts, UpdateContactRegistered }
import im.actor.api.rpc.messaging.RequestLoadDialogs
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.peers.ApiUserOutPeer
import im.actor.api.rpc.sequence.{ RequestGetDifference, RequestGetState, RequestSubscribeToOnline, UpdateRawUpdate }
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.raw.RequestRawRequest
import im.actor.api.rpc.weak.UpdateUserOffline
import im.actor.api.rpc.{ AuthorizedClientData, Request, RpcOk }
import im.actor.server.api.rpc.RawApiExtension
import im.actor.server.api.rpc.service.auth.AuthErrors
import im.actor.server.api.rpc.service.raw.EchoService
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport._
import im.actor.server.persist.AuthSessionRepo
import im.actor.server.sequence.{ SeqUpdatesExtension, UserSequence, WeakUpdatesExtension }
import im.actor.server.user.UserExtension
import org.scalatest.BeforeAndAfterEach
import scodec.bits._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

final class SessionSpec extends BaseSessionSpec with BeforeAndAfterEach {
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
  it should "send SeqUpdateTooLong" in sessions().seqUpdateTooLong
  it should "cache small results" in sessions().cacheSmallResults
  it should "not cache big results" in sessions().notCacheBigResults

  @volatile var count = 0

  override def beforeEach = {
    RawApiExtension(system).register("echo", new EchoService(system) { override def onEcho() = count += 1 })
  }

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

      val encodedRequest = RequestCodec.encode(Request(RequestStartPhoneAuth(
        phoneNumber = 75553333333L,
        appId = 1,
        apiKey = "apiKey",
        deviceHash = Random.nextLong.toBinaryString.getBytes,
        deviceTitle = "Specs Has You",
        timeZone = None,
        preferredLanguages = Vector.empty
      ))).require
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

      val encodedStartRequest = RequestCodec.encode(Request(RequestStartPhoneAuth(
        phoneNumber = 75553333334L,
        appId = 1,
        apiKey = "apiKey",
        deviceHash = Random.nextLong.toBinaryString.getBytes,
        deviceTitle = "Specs Has You",
        timeZone = None,
        preferredLanguages = Vector.empty
      ))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, ProtoRpcRequest(encodedStartRequest))
      expectNewSession(authId, sessionId, messageId)

      expectRpcResult(authId, sessionId) should matchPattern {
        case RpcOk(ResponseStartPhoneAuth(_, false, _)) ⇒
      }
      probe.expectNoMsg(20.seconds)
    }

    def auth() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val phoneNumber = 75550000000L
      val code = phoneNumber.toString.charAt(4).toString * 4

      val firstMessageId = Random.nextLong()
      val encodedCodeRequest = RequestCodec.encode(Request(RequestStartPhoneAuth(
        phoneNumber = phoneNumber,
        appId = 1,
        apiKey = "apiKey",
        deviceHash = Random.nextLong.toBinaryString.getBytes,
        deviceTitle = "Specs Has You",
        timeZone = None,
        preferredLanguages = Vector.empty
      ))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, firstMessageId, ProtoRpcRequest(encodedCodeRequest))

      expectNewSession(authId, sessionId, firstMessageId)
      val txHash = expectRpcResult(authId, sessionId).asInstanceOf[RpcOk].response.asInstanceOf[ResponseStartPhoneAuth].transactionHash

      val secondMessageId = Random.nextLong()
      val encodedValidateRequest = RequestCodec.encode(Request(RequestValidateCode(txHash, code))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, secondMessageId, ProtoRpcRequest(encodedValidateRequest))

      expectRpcResult(authId, sessionId) should matchPattern {
        case AuthErrors.PhoneNumberUnoccupied ⇒
      }

      val thirdMessageId = Random.nextLong()
      val encodedSignUpRequest = RequestCodec.encode(Request(RequestSignUp(txHash, "Name", None, None))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, thirdMessageId, ProtoRpcRequest(encodedSignUpRequest))

      expectRpcResult(authId, sessionId) should matchPattern {
        case RpcOk(ResponseAuth(_, _)) ⇒
      }

      {
        val encodedLoadDialogs = RequestCodec.encode(Request(RequestLoadDialogs(0L, 100, Vector.empty))).require
        val encodedGetDifference = RequestCodec.encode(Request(RequestGetDifference(0, Array(), Vector.empty))).require
        val encodedGetContacts = RequestCodec.encode(Request(RequestGetContacts("", Vector.empty))).require
        sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong, ProtoRpcRequest(encodedLoadDialogs))
        sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong, ProtoRpcRequest(encodedGetDifference))
        sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong, ProtoRpcRequest(encodedGetContacts))

        expectRpcResult(authId, sessionId)
        expectRpcResult(authId, sessionId)
        expectRpcResult(authId, sessionId)
      }

      val encodedSignOutRequest = RequestCodec.encode(Request(RequestSignOut)).require

      val forthMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, forthMessageId, ProtoRpcRequest(encodedSignOutRequest))

      expectRpcResult(authId, sessionId) should matchPattern {
        case RpcOk(ResponseVoid) ⇒
      }
    }

    def seqUpdateTooLong() = {
      val (user, authId, _, _) = createUser()
      val sessionId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong(), SessionHello)
      ignoreNewSession()

      sendRequest(authId, sessionId, sessionRegion.ref, RequestGetState(Vector.empty))
      expectMessageAck()
      expectRpcResult(authId, sessionId)

      val updatesCount = 32

      // each update is 1024 bytes
      val payload = Array(List.range(0, 1005).map(_.toByte): _*)
      val update = UpdateRawUpdate(None, payload)

      for (_ ← 1 to updatesCount) {
        whenReady(seqUpdExt.deliverSingleUpdate(user.id, update))(identity)
      }

      // expect 30Kb of updates to be pushed, then SeqUpdateTooLong (no ack)
      for (_ ← 1 until updatesCount) {
        expectSeqUpdate(authId, sessionId, None)
      }

      expectSeqUpdateTooLong(authId, sessionId)
      expectSeqUpdate(authId, sessionId)

      probe.expectNoMsg(5.seconds)
    }

    def seq() = {
      val phoneNumber = 75550000000L + Random.nextInt(100000)
      val user = createUser(phoneNumber)._1

      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val code = phoneNumber.toString.charAt(4).toString * 4

      val firstMessageId = Random.nextLong()
      val encodedCodeRequest = RequestCodec.encode(Request(RequestStartPhoneAuth(
        phoneNumber = phoneNumber,
        appId = 1,
        apiKey = "apiKey",
        deviceHash = Random.nextLong.toBinaryString.getBytes,
        deviceTitle = "Specs Has You",
        timeZone = None,
        preferredLanguages = Vector.empty
      ))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, firstMessageId, ProtoRpcRequest(encodedCodeRequest))

      expectNewSession(authId, sessionId, firstMessageId)
      val txHash = expectRpcResult(authId, sessionId).asInstanceOf[RpcOk].response.asInstanceOf[ResponseStartPhoneAuth].transactionHash

      val secondMessageId = Random.nextLong()
      val encodedValidateRequest = RequestCodec.encode(Request(RequestValidateCode(txHash, code))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, secondMessageId, ProtoRpcRequest(encodedValidateRequest))

      val authResult = expectRpcResult(authId, sessionId)
      authResult should matchPattern {
        case RpcOk(ResponseAuth(_, _)) ⇒
      }

      val authSession = Await.result(db.run(AuthSessionRepo.findByAuthId(authId)), 5.seconds).get
      implicit val clientData = AuthorizedClientData(authId, sessionId, authResult.asInstanceOf[RpcOk].response.asInstanceOf[ResponseAuth].user.id, authSession.id, 42)

      val encodedGetSeqRequest = RequestCodec.encode(Request(RequestGetState(Vector.empty))).require

      val thirdMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, thirdMessageId, ProtoRpcRequest(encodedGetSeqRequest))

      expectRpcResult(authId, sessionId) should matchPattern {
        case RpcOk(ResponseSeq(_, _)) ⇒
      }

      val update = UpdateContactRegistered(1, true, 1L, 2L)
      seqUpdExt.deliverSingleUpdate(user.id, update)
      expectSeqUpdate(authId, sessionId).update should ===(update.toByteArray)
    }

    def fatSeq() = {
      val (user, authId, authSid, _) = createUser()
      val sessionId = Random.nextLong

      implicit val clientData = AuthorizedClientData(authId, sessionId, user.id, authSid, 42)

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

      val phoneNumber = 75550000000L
      val code = phoneNumber.toString.charAt(4).toString * 4

      val firstMessageId = Random.nextLong()
      val encodedCodeRequest = RequestCodec.encode(Request(RequestStartPhoneAuth(
        phoneNumber = phoneNumber,
        appId = 1,
        apiKey = "apiKey",
        deviceHash = Random.nextLong.toBinaryString.getBytes,
        deviceTitle = "Specs Has You",
        timeZone = None,
        preferredLanguages = Vector.empty
      ))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, firstMessageId, ProtoRpcRequest(encodedCodeRequest))

      expectNewSession(authId, sessionId, firstMessageId)
      val txHash = expectRpcResult(authId, sessionId).asInstanceOf[RpcOk].response.asInstanceOf[ResponseStartPhoneAuth].transactionHash

      val secondMessageId = Random.nextLong()
      val encodedValidateRequest = RequestCodec.encode(Request(RequestValidateCode(txHash, code))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, secondMessageId, ProtoRpcRequest(encodedValidateRequest))

      val authResult = expectRpcResult(authId, sessionId)
      authResult should matchPattern {
        case RpcOk(ResponseAuth(_, _)) ⇒
      }

      implicit val clientData = AuthorizedClientData(authId, sessionId, authResult.asInstanceOf[RpcOk].response.asInstanceOf[ResponseAuth].user.id, Await.result(db.run(AuthSessionRepo.findByAuthId(authId)), 5.seconds).get.id, 42)

      val update = UpdateContactRegistered(1, isSilent = true, 1L, 5L)
      Await.result(weakUpdatesExt.broadcastUserWeakUpdate(clientData.userId, update, reduceKey = None), 1.second)

      expectWeakUpdate(authId, sessionId).update should ===(update.toByteArray)
    }

    def pres() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val phoneNumber = 75550000000L
      val code = phoneNumber.toString.charAt(4).toString * 4

      val firstMessageId = Random.nextLong()
      val encodedCodeRequest = RequestCodec.encode(Request(RequestStartPhoneAuth(
        phoneNumber = phoneNumber,
        appId = 1,
        apiKey = "apiKey",
        deviceHash = Random.nextLong.toBinaryString.getBytes,
        deviceTitle = "Specs Has You",
        timeZone = None,
        preferredLanguages = Vector.empty
      ))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, firstMessageId, ProtoRpcRequest(encodedCodeRequest))

      expectNewSession(authId, sessionId, firstMessageId)
      val txHash = expectRpcResult(authId, sessionId).asInstanceOf[RpcOk].response.asInstanceOf[ResponseStartPhoneAuth].transactionHash

      val secondMessageId = Random.nextLong()
      val encodedValidateRequest = RequestCodec.encode(Request(RequestValidateCode(txHash, code))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, secondMessageId, ProtoRpcRequest(encodedValidateRequest))

      val authResult = expectRpcResult(authId, sessionId)
      authResult should matchPattern {
        case RpcOk(ResponseAuth(_, _)) ⇒
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

    def cacheSmallResults(): Unit = {
      implicit val probe = TestProbe()

      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val helloMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, helloMessageId, SessionHello)
      expectNewSession(authId, sessionId, helloMessageId)
      expectMessageAck()

      val messageId = Random.nextLong()
      count = 0

      for (_ ← 1 to 3) {
        sendRequest(
          authId,
          sessionId,
          sessionRegion.ref,
          messageId,
          RequestRawRequest("echo", "makeEcho", Some(ApiStringValue("...")))
        )
        expectRpcResult(authId, sessionId, ignoreAcks = true)
      }

      count shouldBe 1
    }

    def notCacheBigResults(): Unit = {
      implicit val probe = TestProbe()

      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val helloMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, helloMessageId, SessionHello)
      expectNewSession(authId, sessionId, helloMessageId)
      expectMessageAck()

      val longString = List.range(1, 40000).map(_ ⇒ ".").mkString

      val messageId = Random.nextLong()
      count = 0

      for (_ ← 1 to 3) {
        sendRequest(
          authId,
          sessionId,
          sessionRegion.ref,
          messageId,
          RequestRawRequest("echo", "makeEcho", Some(ApiStringValue(longString)))
        )
        expectRpcResult(authId, sessionId, ignoreAcks = true)
      }

      count shouldBe 3
    }
  }
}
