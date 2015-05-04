package im.actor.server

import java.net.InetSocketAddress

import akka.stream.ActorFlowMaterializer
import com.google.android.gcm.server.Sender
import com.relayrides.pushy.apns.util.{ SSLContextUtil, SimpleApnsPushNotification }
import com.relayrides.pushy.apns.{ ApnsEnvironment, PushManager, PushManagerConfiguration }

import im.actor.api.rpc.auth.{ RequestSendAuthCode, RequestSignUp, ResponseSendAuthCode }
import im.actor.api.rpc.codecs.RequestCodec
import im.actor.api.rpc.sequence.RequestGetDifference
import im.actor.api.rpc.{ Request, RpcOk, RpcResult }
import im.actor.server.api.frontend.TcpFrontend
import im.actor.server.api.rpc.RpcApiService.AttachService
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.groups.GroupsServiceImpl
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.api.rpc.service.sequence.SequenceServiceImpl
import im.actor.server.api.rpc.{ RpcApiService, RpcResultCodec }
import im.actor.server.db.DbInit
import im.actor.server.mtproto.codecs.protocol._
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.{ MTPackage, ProtoPackage, TransportPackage }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.push.{ SeqUpdatesManager, WeakUpdatesManager }
import im.actor.server.session.{ SessionConfig, Session }
import im.actor.server.sms.DummyActivationContext
import im.actor.server.social.SocialManager
import im.actor.util.testing._

class SimpleServerE2eSpec extends ActorFlatSuite with DbInit {
  behavior of "Server"

  it should "connect and Handshake" in e1

  it should "respond to RPC requests" in e2

  val serverConfig = system.settings.config
  val sqlConfig = serverConfig.getConfig("persist.sql")
  val ds = initDs(sqlConfig)

  implicit val db = initDb(ds)
  implicit val flowMaterializer = ActorFlowMaterializer()

  val gcmConfig = system.settings.config.getConfig("push.gcm")
  val apnsConfig = system.settings.config.getConfig("push.apns")

  implicit val gcmSender = new Sender(gcmConfig.getString("key"))

  implicit val apnsManager = new PushManager[SimpleApnsPushNotification](
    ApnsEnvironment.getProductionEnvironment,
    SSLContextUtil.createDefaultSSLContext(apnsConfig.getString("cert.path"), apnsConfig.getString("cert.password")),
    null,
    null,
    null,
    new PushManagerConfiguration(),
    "ActorPushManager"
  )

  implicit val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
  implicit val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()
  val rpcApiService = system.actorOf(RpcApiService.props())
  implicit val sessionConfig = SessionConfig.fromConfig(system.settings.config.getConfig("session"))
  implicit val sessionRegion = Session.startRegion(Some(Session.props(rpcApiService)))

  val services = Seq(
    new AuthServiceImpl(new DummyActivationContext),
    new ContactsServiceImpl,
    new MessagingServiceImpl,
    new SequenceServiceImpl
  )

  services foreach { service =>
    rpcApiService ! AttachService(service)
  }

  TcpFrontend.start(serverConfig, sessionRegion)

  val remote = new InetSocketAddress("localhost", 8080)

  def e1() = {
    val client = MTProtoClient()
    client.connectAndHandshake(remote)
    client.close()
  }

  def e2() = {
    implicit val client = MTProtoClient()

    client.connectAndHandshake(remote)

    val authId = 1L
    val sessionId = 2L
    val phoneNumber = 75550000000L

    val smsHash = {
      val messageId = 3L

      val requestBytes = RequestCodec.encode(Request(RequestSendAuthCode(phoneNumber, 1, "apiKey"))).require
      val mbBytes = MessageBoxCodec.encode(MessageBox(messageId, RpcRequestBox(requestBytes))).require
      val mtPackage = MTPackage(authId, sessionId, mbBytes)

      client.send(mtPackage)

      expectNewSession(sessionId, messageId)
      expectMessageAck(messageId)

      val result = receiveRpcResult(messageId)
      result shouldBe an[RpcOk]

      result.asInstanceOf[RpcOk].response.asInstanceOf[ResponseSendAuthCode].smsHash
    }

    {
      val messageId = 4L

      val requestBytes = RequestCodec.encode(Request(RequestSignUp(
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
      val mbBytes = MessageBoxCodec.encode(MessageBox(messageId, RpcRequestBox(requestBytes))).require
      val mtPackage = MTPackage(authId, sessionId, mbBytes)

      client.send(mtPackage)

      expectMessageAck(messageId)

      val result = receiveRpcResult(messageId)
      result shouldBe an[RpcOk]
    }

    {
      val messageId = 5L

      val requestBytes = RequestCodec.encode(Request(RequestGetDifference(999, Array()))).require
      val mbBytes = MessageBoxCodec.encode(MessageBox(messageId, RpcRequestBox(requestBytes))).require
      val mtPackage = MTPackage(authId, sessionId, mbBytes)

      client.send(mtPackage)

      expectMessageAck(messageId)

      val result = receiveRpcResult(messageId)
      result shouldBe an[RpcOk]
    }

    client.close()
  }

  private def expectMessageAck(messageId: Long)(implicit client: MTProtoClient): MessageAck = {
    val mb = receiveMessageBox()
    mb.body shouldBe a[MessageAck]

    val expectedAck = MessageAck(Vector(messageId))

    val ack = mb.body.asInstanceOf[MessageAck]
    ack should ===(expectedAck)

    ack
  }

  private def receiveRpcResult(messageId: Long)(implicit client: MTProtoClient): RpcResult = {
    val mb = receiveMessageBox()
    mb.body shouldBe an[RpcResponseBox]

    val rspBox = mb.body.asInstanceOf[RpcResponseBox]
    rspBox.messageId should ===(messageId)

    RpcResultCodec.decode(rspBox.bodyBytes).require.value
  }

  private def receiveMessageBox()(implicit client: MTProtoClient): MessageBox = {
    val mtp = receiveMTPackage()
    MessageBoxCodec.decode(mtp.messageBytes).require.value
  }

  private def receiveMTPackage()(implicit client: MTProtoClient): MTPackage = {
    val body = client.receiveTransportPackage() match {
      case Some(TransportPackage(_, body)) => body
      case None => throw new Exception("Transport package not received")
    }

    body shouldBe a[MTPackage]
    body.asInstanceOf[MTPackage]
  }

  private def expectNewSession(sessionId: Long, messageId: Long)(implicit client: MTProtoClient): Unit = {
    val mtp = receiveMTPackage()

    val expectedNewSession = NewSession(sessionId, messageId)

    val mb = MessageBoxCodec.decode(mtp.messageBytes).require.value
    mb.body shouldBe a[NewSession]
    mb.body should ===(expectedNewSession)
  }

  override def afterAll = {
    super.afterAll()
    ds.close()
  }
}
