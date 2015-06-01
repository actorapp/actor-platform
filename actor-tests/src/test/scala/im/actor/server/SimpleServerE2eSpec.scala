package im.actor.server

import java.net.InetSocketAddress

import scala.util.Random

import akka.contrib.pattern.DistributedPubSubExtension
import akka.stream.ActorFlowMaterializer
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import com.google.android.gcm.server.Sender
import com.typesafe.config.ConfigFactory

import im.actor.api.rpc.auth.{ RequestSendAuthCode, RequestSignUp, ResponseSendAuthCode }
import im.actor.api.rpc.codecs.RequestCodec
import im.actor.api.rpc.sequence.RequestGetDifference
import im.actor.api.rpc.{ Request, RpcOk, RpcResult }
import im.actor.server.api.frontend.TcpFrontend
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.messaging.{ GroupPeerManager, PrivatePeerManager, MessagingServiceImpl }
import im.actor.server.api.rpc.service.sequence.SequenceServiceImpl
import im.actor.server.api.rpc.{ RpcApiService, RpcResultCodec }
import im.actor.server.db.DbInit
import im.actor.server.mtproto.codecs.protocol._
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.{ MTPackage, TransportPackage }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.push.{ ApplePushManager, ApplePushManagerConfig, SeqUpdatesManager, WeakUpdatesManager }
import im.actor.server.session.{ Session, SessionConfig }
import im.actor.server.sms.DummyActivationContext
import im.actor.server.social.SocialManager
import im.actor.util.testing._

class SimpleServerE2eSpec extends ActorFlatSuite(
  ActorSpecification.createSystem(ConfigFactory.parseString(
    """
    |session {
    |  idle-timeout = 5 seconds
    |}
  """.stripMargin
  ))
) with DbInit with KafkaSpec with SqlSpecHelpers {
  behavior of "Server"

  it should "connect and Handshake" in Server.e1

  it should "respond to RPC requests" in Server.e2

  it should "notify about lost session" in Server.e3

  it should "throw AuthIdInvalid and close connection if sending wrong AuthId" in Server.e4

  implicit lazy val (ds, db) = migrateAndInitDb()

  object Server {
    val serverConfig = system.settings.config

    implicit val flowMaterializer = ActorFlowMaterializer()

    val gcmConfig = system.settings.config.getConfig("push.google")
    val apnsConfig = system.settings.config.getConfig("push.apple")

    implicit val gcmSender = new Sender(gcmConfig.getString("key"))

    implicit val apnsManager = new ApplePushManager(ApplePushManagerConfig.fromConfig(apnsConfig), system)

    implicit val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
    implicit val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
    implicit val presenceManagerRegion = PresenceManager.startRegion()
    implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
    implicit val socialManagerRegion = SocialManager.startRegion()
    implicit val privatePeerManagerRegion = PrivatePeerManager.startRegion()
    implicit val groupPeerManagerRegion = GroupPeerManager.startRegion()

    implicit val sessionConfig = SessionConfig.fromConfig(system.settings.config.getConfig("session"))
    Session.startRegion(Some(Session.props))
    implicit val sessionRegion = Session.startRegionProxy()

    val mediator = DistributedPubSubExtension(system).mediator

    val bucketName = "actor-uploads-test"
    val awsCredentials = new EnvironmentVariableCredentialsProvider()
    implicit val transferManager = new TransferManager(awsCredentials)

    val services = Seq(
      new AuthServiceImpl(new DummyActivationContext),
      new ContactsServiceImpl,
      MessagingServiceImpl(mediator),
      new SequenceServiceImpl
    )

    system.actorOf(RpcApiService.props(services), "rpcApiService")

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

      val authId = requestAuthId()
      val sessionId = 2L
      val phoneNumber = 75550000000L

      val smsHash = {
        val helloMessageId = 4L
        val helloMbBytes = MessageBoxCodec.encode(MessageBox(helloMessageId, SessionHello)).require
        val helloMtPackage = MTPackage(authId, sessionId, helloMbBytes)
        client.send(helloMtPackage)
        expectNewSession(sessionId, helloMessageId)
        expectMessageAck(helloMessageId)

        val messageId = 3L

        val requestBytes = RequestCodec.encode(Request(RequestSendAuthCode(phoneNumber, 1, "apiKey"))).require
        val mbBytes = MessageBoxCodec.encode(MessageBox(messageId, RpcRequestBox(requestBytes))).require
        val mtPackage = MTPackage(authId, sessionId, mbBytes)

        client.send(mtPackage)

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

    def e3() = {
      implicit val client = MTProtoClient()

      client.connectAndHandshake(remote)

      val authId = requestAuthId()
      val sessionId = Random.nextLong()

      {
        val helloMessageId = Random.nextLong()
        val helloMbBytes = MessageBoxCodec.encode(MessageBox(helloMessageId, SessionHello)).require
        val helloMtPackage = MTPackage(authId, sessionId, helloMbBytes)
        client.send(helloMtPackage)
        expectNewSession(sessionId, helloMessageId)
        expectMessageAck(helloMessageId)
      }

      Thread.sleep(3000)
      expectSessionLost()

      {
        val helloMessageId = Random.nextLong()
        val helloMbBytes = MessageBoxCodec.encode(MessageBox(helloMessageId, SessionHello)).require
        val helloMtPackage = MTPackage(authId, sessionId, helloMbBytes)
        client.send(helloMtPackage)
        expectNewSession(sessionId, helloMessageId)
        expectMessageAck(helloMessageId)
      }
    }

    def e4() = {
      implicit val client = MTProtoClient()

      client.connectAndHandshake(remote)

      val authId = Random.nextLong()
      val sessionId = Random.nextLong()

      client.send(MTPackage(authId, sessionId, MessageBoxCodec.encode(MessageBox(Random.nextLong, SessionHello)).require))
      expectAuthIdInvalid()
    }

    private def requestAuthId()(implicit client: MTProtoClient): Long = {
      val messageId = Random.nextLong()
      val mbBytes = MessageBoxCodec.encode(MessageBox(messageId, RequestAuthId)).require
      client.send(MTPackage(0, 0, mbBytes))

      receiveMessageBox().body match {
        case ResponseAuthId(authId) ⇒ authId
        case unmatched              ⇒ fail(s"Expected ResponseAuthId, received ${unmatched}")
      }
    }

    private def expectAuthIdInvalid()(implicit client: MTProtoClient): Unit = {
      val mb = receiveMessageBox()
      mb.body shouldBe an[AuthIdInvalid]
    }

    private def expectMessageAck(messageId: Long)(implicit client: MTProtoClient): MessageAck = {
      val mb = receiveMessageBox()
      mb.body shouldBe a[MessageAck]

      val expectedAck = MessageAck(Vector(messageId))

      val ack = mb.body.asInstanceOf[MessageAck]
      ack should ===(expectedAck)

      ack
    }

    private def expectSessionLost()(implicit client: MTProtoClient): SessionLost = {
      val mb = receiveMessageBox()
      mb.body shouldBe a[SessionLost]

      mb.body.asInstanceOf[SessionLost]
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
        case Some(TransportPackage(_, body)) ⇒ body
        case None                            ⇒ throw new Exception("Transport package not received")
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
  }

  override def afterAll = {
    super.afterAll()
    system.awaitTermination()
    ds.close()
  }
}
