package im.actor.server

import java.net.InetSocketAddress

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Promise, Future, Await}

import akka.stream.ActorFlowMaterializer
import akka.testkit._
import org.specs2.matcher.ThrownExpectations
import org.specs2.specification.core.Fragments

import im.actor.api.rpc.{ RpcOk, RpcResult, Request }
import im.actor.api.rpc.auth.RequestSendAuthCode
import im.actor.api.rpc.codecs.RequestCodec
import im.actor.server.api.frontend.{ Tcp => TcpFrontend }
import im.actor.server.api.rpc.{ RpcResultCodec, RpcApiService }
import im.actor.server.api.rpc.RpcApiService.AttachService
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.db.DbInit
import im.actor.server.mtproto.codecs.protocol._
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.{ MTPackage, ProtoPackage, TransportPackage }
import im.actor.server.push.SeqUpdatesManager
import im.actor.server.session.Session
import im.actor.util.testing._

class SimpleServerE2eSpec extends ActorFlatSuite with DbInit {
  behavior of "Server"

  it should "connect and Handshake" in e1

  it should "respond to RPC request" in e2

  val serverConfig = system.settings.config
  val sqlConfig = serverConfig.getConfig("persist.sql")
  val ds = initDs(sqlConfig)

  implicit val db = initDb(ds)
  implicit val flowMaterializer = ActorFlowMaterializer()

  val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
  val rpcApiService = system.actorOf(RpcApiService.props())
  val sessionRegion = Session.startRegion(Some(Session.props(rpcApiService, seqUpdManagerRegion)))

  val authService = new AuthServiceImpl(sessionRegion)
  rpcApiService ! AttachService(authService)

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

    val messageId = 3L

    val requestBytes = RequestCodec.encode(Request(RequestSendAuthCode(phoneNumber, 1, "apiKey"))).require
    val mbBytes = MessageBoxCodec.encode(MessageBox(messageId, RpcRequestBox(requestBytes))).require
    val mtPackage = MTPackage(authId, sessionId, mbBytes)

    client.send(ProtoPackage(mtPackage))

    expectNewSession(sessionId, messageId)
    expectMessageAck(messageId)

    val result = receiveRpcResult(messageId)
    result shouldBe an [RpcOk]

    client.close()
  }

  private def expectMessageAck(messageId: Long)(implicit client: MTProtoClient): MessageAck = {
    val mb = receiveMessageBox()
    mb.body shouldBe a [MessageAck]

    val expectedAck = MessageAck(Vector(messageId))

    val ack = mb.body.asInstanceOf[MessageAck]
    ack should === (expectedAck)

    ack
  }

  private def receiveRpcResult(messageId: Long)(implicit client: MTProtoClient): RpcResult = {
    val mb = receiveMessageBox()
    mb.body shouldBe an [RpcResponseBox]

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

    body shouldBe a [MTPackage]
    body.asInstanceOf[MTPackage]
  }

  private def expectNewSession(sessionId: Long, messageId: Long)(implicit client: MTProtoClient): Unit = {
    val mtp = receiveMTPackage()

    val expectedNewSession = NewSession(sessionId, messageId)

    val mb = MessageBoxCodec.decode(mtp.messageBytes).require.value
    mb.body shouldBe a [NewSession]
    mb.body should === (expectedNewSession)
  }

  override def afterAll = {
    super.afterAll()
    ds.close()
  }
}
