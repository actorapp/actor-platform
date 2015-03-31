package im.actor.server

import java.net.InetSocketAddress

import scala.concurrent.duration._
import scala.util.Random

import akka.actor.ActorRef
import akka.io._
import akka.stream.ActorFlowMaterializer
import akka.testkit._
import akka.util.ByteString
import org.apache.commons.codec.digest.DigestUtils
import org.specs2.matcher.ThrownExpectations
import org.specs2.specification.core.Fragments
import scodec.bits.BitVector

import im.actor.api.rpc.Request
import im.actor.api.rpc.auth.RequestSendAuthCode
import im.actor.api.rpc.codecs.RequestCodec
import im.actor.server.api.frontend.{ Tcp => TcpFrontend }
import im.actor.server.db.DbInit
import im.actor.server.mtproto.codecs.protocol._
import im.actor.server.mtproto.protocol.{ MessageAck, MessageBox, RpcRequestBox }
import im.actor.server.mtproto.transport.{ TransportPackage, Handshake, MTPackage }
import im.actor.util.testing._

class SimpleServerE2eSpec extends ActorSpecification with DbInit with ThrownExpectations {
  def is =
    s2"""
    Server should
      accept connections and properly close them $e1
      respond to Handshake $e2
      respond to RPC request $e3
      """.stripMargin

  import Tcp._

  import im.actor.server.mtproto.codecs.transport._

  val serverConfig = system.settings.config
  val sqlConfig = serverConfig.getConfig("persist.sql")
  val ds = initDs(sqlConfig)

  implicit val db = initDb(ds)
  implicit val flowMaterializer = ActorFlowMaterializer()

  TcpFrontend.start(serverConfig)

  val remote = new InetSocketAddress("localhost", 8080)

  def e1() = {
    implicit val probe = TestProbe()
    close(connect())
  }

  def e2() = {
    implicit val probe = TestProbe()
    close(connectAndHandshake())
  }

  def e3() = {
    implicit val probe = TestProbe()

    val connection = connectAndHandshake()

    val authId = 1L
    val sessionId = 2L
    val phoneNumber = 75550000000L

    val requestBytes = RequestCodec.encode(Request(RequestSendAuthCode(phoneNumber, 1, "apiKey"))).require
    val mbBytes = MessageBoxCodec.encode(MessageBox(1L, RpcRequestBox(requestBytes))).require
    val mtPackage = MTPackage(authId, sessionId, mbBytes)
    val transportPackage = TransportPackage(1, mtPackage)
    val transportPackageBytes = TransportPackageCodec.encode(transportPackage).require
    write(connection, transportPackageBytes)

    expectMessageAck(connection, 1L)
  }

  private def connect()(implicit probe: TestProbe): ActorRef = {
    probe.send(IO(Tcp), Connect(remote))
    probe.expectMsgPF() {
      case Connected(r, _) if r == remote =>
    }

    val connection = probe.sender()
    probe.send(connection, Register(probe.ref))

    connection
  }

  private def connectAndHandshake()(implicit probe: TestProbe): ActorRef = {
    val connection = connect()
    val protoVersion = 1.toByte
    val apiMajorVersion = 1.toByte
    val apiMinorVersion = 1.toByte
    val randomBytes = Random.nextString(10).getBytes

    val randomBytesDigest = DigestUtils.sha1(Array(protoVersion, apiMajorVersion, apiMinorVersion) ++ randomBytes)

    val handshakeBytes = HandshakeCodec.encode(Handshake(protoVersion, apiMajorVersion, apiMinorVersion, BitVector(randomBytes))).require
    write(connection, handshakeBytes)

    val response = probe.expectMsgPF() {
      case Received(bs) =>
        HandshakeCodec.decode(BitVector(bs.toByteBuffer)).require.value
    }

    response should be_==(Handshake(1.toByte, 1.toByte, 1.toByte, BitVector(randomBytesDigest)))
    connection
  }

  private def close(connection: ActorRef)(implicit probe: TestProbe): Unit = {
    probe.send(probe.sender(), Close)
    probe.expectMsg(Closed)
  }

  private def write(connection: ActorRef, bytes: BitVector)(implicit probe: TestProbe): Unit = {
    probe.send(connection, Write(ByteString(bytes.toByteArray)))
  }

  private def receiveMTPackage(connection: ActorRef)(implicit probe: TestProbe): MTPackage = {
    val bss = probe.receiveWhile(1.second, 1.second, 10) {
      case Received(bs) =>
        bs
    }

    val bs = bss.reduce(_ ++ _)

    val tp = TransportPackageCodec.decode(BitVector(bs.asByteBuffer)).require.value

    tp.body must beAnInstanceOf[MTPackage]
    tp.body.asInstanceOf[MTPackage]
  }

  private def expectMessageAck(connection: ActorRef, messageId: Long)(implicit probe: TestProbe): MessageAck = {
    val mtp = receiveMTPackage(connection)
    val expectedAck = MessageAck(Vector(messageId))

    val mb = MessageBoxCodec.decode(mtp.messageBytes).require.value
    mb.body must beAnInstanceOf[MessageAck]

    val ack = mb.asInstanceOf[MessageAck]
    ack must be_==(expectedAck)

    ack
  }

  override def map(fragments: => Fragments) =
    super.map(fragments) ^ step(shutdownSystem) ^ step(closeDb())

  private def shutdownSystem(): Unit =
    TestKit.shutdownActorSystem(system)

  private def closeDb(): Unit =
    ds.close()
}
