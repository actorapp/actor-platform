package im.actor.server

import java.net.InetSocketAddress

import im.actor.server.group.GroupOffice
import im.actor.server.user.UserOffice

import scala.concurrent.ExecutionContext
import scala.util.Random

import akka.contrib.pattern.DistributedPubSubExtension
import akka.stream.ActorMaterializer
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import com.typesafe.config.ConfigFactory

import im.actor.api.rpc.auth._
import im.actor.api.rpc.codecs.RequestCodec
import im.actor.api.rpc.sequence.RequestGetDifference
import im.actor.api.rpc.{ Request, RpcOk, RpcResult }
import im.actor.server.activation.internal.DummyCodeActivation
import im.actor.server.api.frontend.TcpFrontend
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceConfig, SequenceServiceImpl }
import im.actor.server.api.rpc.{ RpcApiService, RpcResultCodec }
import im.actor.server.db.DbInit
import im.actor.server.mtproto.codecs.protocol._
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.{ MTPackage, TransportPackage }
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.push._
import im.actor.server.session.{ Session, SessionConfig }
import im.actor.server.social.SocialManager

class SimpleServerE2eSpec extends ActorFlatSuite(
  ActorSpecification.createSystem(ConfigFactory.parseString(
    """
    |session {
    |  idle-timeout = 5 seconds
    |}
  """.stripMargin
  ))
) with DbInit with SqlSpecHelpers {
  behavior of "Server"

  it should "connect and Handshake" in Server.e1

  it should "respond to RPC requests" in Server.e2

  it should "notify about lost session" in Server.e3

  it should "throw AuthIdInvalid if sending wrong AuthId" in Server.e4

  it should "throw AuthIdInvalid if valid AuthId invalidated by some reason" in Server.e5

  implicit lazy val (ds, db) = migrateAndInitDb()

  object Server {
    val serverConfig = system.settings.config

    implicit val materializer = ActorMaterializer()

    val gcmConfig = system.settings.config.getConfig("push.google")
    val apnsConfig = system.settings.config.getConfig("push.apple")
    val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
    val sequenceConfig = SequenceServiceConfig.load.toOption.get

    implicit val googlePushManager = new GooglePushManager(GooglePushManagerConfig(List.empty))

    implicit val apnsManager = new ApplePushManager(ApplePushManagerConfig.load(apnsConfig), system)

    implicit val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
    implicit val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
    implicit val presenceManagerRegion = PresenceManager.startRegion()
    implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
    implicit val socialManagerRegion = SocialManager.startRegion()
    implicit val privatePeerManagerRegion = UserOffice.startRegion()
    implicit val groupPeerManagerRegion = GroupOffice.startRegion()

    val mediator = DistributedPubSubExtension(system).mediator

    implicit val sessionConfig = SessionConfig.load(system.settings.config.getConfig("session"))
    Session.startRegion(Some(Session.props(mediator)))
    implicit val sessionRegion = Session.startRegionProxy()

    val bucketName = "actor-uploads-test"
    val awsCredentials = new EnvironmentVariableCredentialsProvider()
    implicit val transferManager = new TransferManager(awsCredentials)
    implicit val ec: ExecutionContext = system.dispatcher
    implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)

    val services = Seq(
      new AuthServiceImpl(new DummyCodeActivation, mediator),
      new ContactsServiceImpl,
      MessagingServiceImpl(mediator),
      new SequenceServiceImpl(sequenceConfig)
    )

    system.actorOf(RpcApiService.props(services), "rpcApiService")

    TcpFrontend.start("127.0.0.1", 9070, None)

    val remote = new InetSocketAddress("127.0.0.1", 9070)

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

      signUp(authId, sessionId, phoneNumber)

      val messageId = Random.nextLong()

      val requestBytes = RequestCodec.encode(Request(RequestGetDifference(999, Array()))).require
      val mbBytes = MessageBoxCodec.encode(MessageBox(messageId, RpcRequestBox(requestBytes))).require
      val mtPackage = MTPackage(authId, sessionId, mbBytes)

      client.send(mtPackage)

      expectMessageAck(messageId)

      val result = receiveRpcResult(messageId)
      result shouldBe an[RpcOk]

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

      client.close()
    }

    def e4() = {
      implicit val client = MTProtoClient()

      client.connectAndHandshake(remote)

      val authId = Random.nextLong()
      val sessionId = Random.nextLong()

      client.send(MTPackage(authId, sessionId, MessageBoxCodec.encode(MessageBox(Random.nextLong, SessionHello)).require))
      expectAuthIdInvalid()

      client.close()
    }

    def e5() = {
      val phoneNumber = 75551234567L

      val client1 = MTProtoClient()
      client1.connectAndHandshake(remote)
      val authId1 = requestAuthId()(client1)
      val sessionId1 = Random.nextLong()

      val client2 = MTProtoClient()
      client2.connectAndHandshake(remote)
      val authId2 = requestAuthId()(client2)
      val sessionId2 = Random.nextLong()

      {
        implicit val client = client1
        signUp(authId1, sessionId1, phoneNumber)
      }

      {
        implicit val client = client2
        signUp(authId2, sessionId2, phoneNumber)
        val requestBits = RequestCodec.encode(Request(RequestTerminateAllSessions)).require
        client.send(MTPackage(authId2, Random.nextLong(), MessageBoxCodec.encode(MessageBox(Random.nextLong, RpcRequestBox(requestBits))).require))
      }

      {
        implicit val client = client1
        expectAuthIdInvalid()
        expectSessionLost()

        client.send(MTPackage(authId1, sessionId1, MessageBoxCodec.encode(MessageBox(Random.nextLong, SessionHello)).require))
        expectAuthIdInvalid()
      }

      client1.close()
      client2.close()
    }

    private def signUp(authId: Long, sessionId: Long, phoneNumber: Long)(implicit client: MTProtoClient): Int = {
      require(phoneNumber.toString.startsWith("7555")) // to be able to generate code
      require(phoneNumber.toString.length >= 5)

      val smsHash = {
        val helloMessageId = Random.nextLong()
        val helloMbBytes = MessageBoxCodec.encode(MessageBox(helloMessageId, SessionHello)).require
        val helloMtPackage = MTPackage(authId, sessionId, helloMbBytes)
        client.send(helloMtPackage)
        expectNewSession(sessionId, helloMessageId)
        expectMessageAck(helloMessageId)

        val messageId = Random.nextLong()

        val requestBytes = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(phoneNumber, 1, "apiKey"))).require
        val mbBytes = MessageBoxCodec.encode(MessageBox(messageId, RpcRequestBox(requestBytes))).require
        val mtPackage = MTPackage(authId, sessionId, mbBytes)

        client.send(mtPackage)

        expectMessageAck(messageId)

        val result = receiveRpcResult(messageId)
        result shouldBe an[RpcOk]

        result.asInstanceOf[RpcOk].response.asInstanceOf[ResponseSendAuthCodeObsolete].smsHash
      }

      {
        val messageId = Random.nextLong()

        val code = phoneNumber.toString.charAt(4).toString * 4

        val requestBytes = RequestCodec.encode(Request(RequestSignUpObsolete(
          phoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = code,
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

        result.asInstanceOf[RpcOk].response.asInstanceOf[ResponseAuth].user.id
      }
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

    private def expectMessageAck()(implicit client: MTProtoClient): MessageAck = {
      val mb = receiveMessageBox()
      mb.body shouldBe a[MessageAck]

      val ack = mb.body.asInstanceOf[MessageAck]

      ack
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

    private def expectNewSession()(implicit client: MTProtoClient): NewSession = {
      val mtp = receiveMTPackage()

      val mb = MessageBoxCodec.decode(mtp.messageBytes).require.value
      mb.body shouldBe a[NewSession]
      mb.body.asInstanceOf[NewSession]
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
