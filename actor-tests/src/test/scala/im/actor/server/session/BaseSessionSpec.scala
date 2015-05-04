package im.actor.server.session

import scala.concurrent.{ Promise, Future, Await, blocking }
import scala.concurrent.duration._
import scala.util.{ Success, Random }

import akka.actor._
import akka.stream.ActorFlowMaterializer
import akka.testkit.TestProbe
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpecLike, Matchers }

import im.actor.api.rpc.RpcResult
import im.actor.api.rpc.codecs._
import im.actor.api.rpc.sequence.{ SeqUpdate, WeakUpdate }
import im.actor.server.api.ActorSpecHelpers
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.service.sequence.SequenceServiceImpl
import im.actor.server.api.rpc.{ RpcApiService, RpcResultCodec }
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.MTPackage
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.push.WeakUpdatesManager
import im.actor.server.sms.DummyActivationContext
import im.actor.server.social.SocialManager
import im.actor.server.{ KafkaSpec, SqlSpecHelpers, persist }
import im.actor.util.testing._

abstract class BaseSessionSpec(_system: ActorSystem = { ActorSpecification.createSystem() })
  extends ActorSuite(_system) with FlatSpecLike with ScalaFutures with Matchers with SqlSpecHelpers with ActorSpecHelpers with KafkaSpec {

  import SessionMessage._

  implicit val materializer = ActorFlowMaterializer()
  implicit val (ds, db) = migrateAndInitDb()
  implicit val ec = system.dispatcher

  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()

  implicit val sessionConfig = SessionConfig.fromConfig(system.settings.config.getConfig("session"))

  Session.startRegion(Some(Session.props))

  implicit val sessionRegion = Session.startRegionProxy()
  val authService = new AuthServiceImpl(new DummyActivationContext)
  val sequenceService = new SequenceServiceImpl

  system.actorOf(RpcApiService.props(Seq(authService, sequenceService)), "rpcApiService")

  protected def createAuthId(): Long = {
    val authId = Random.nextLong()
    Await.result(db.run(persist.AuthId.create(authId, None, None)), 1.second)
    authId
  }

  protected def expectSeqUpdate(authId: Long, sessionId: Long)(implicit probe: TestProbe): SeqUpdate = {
    UpdateBoxCodec.decode(expectMessageBox(authId, sessionId).body.asInstanceOf[UpdateBox].bodyBytes).require.value.asInstanceOf[SeqUpdate]
  }

  protected def expectWeakUpdate(authId: Long, sessionId: Long)(implicit probe: TestProbe): WeakUpdate = {
    UpdateBoxCodec.decode(expectMessageBox(authId, sessionId).body.asInstanceOf[UpdateBox].bodyBytes).require.value.asInstanceOf[WeakUpdate]
  }

  protected def expectRpcResult(sendAckAt: Option[Duration] = Some(0.seconds))(implicit probe: TestProbe, sessionRegion: SessionRegion): RpcResult = {
    Option(probe.receiveOne(5.seconds)) match {
      case Some(MTPackage(authId, sessionId, mbBytes)) ⇒
        val mb = MessageBoxCodec.decode(mbBytes).require.value
        mb.body match {
          case RpcResponseBox(messageId, rpcResultBytes) ⇒
            sendAckAt map { delay ⇒
              Future {
                blocking {
                  Thread.sleep(delay.toMillis)
                  sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong, MessageAck(Vector(messageId)))
                }
              }
            }

            RpcResultCodec.decode(rpcResultBytes).require.value
          case msg ⇒ throw new Exception(s"Expected RpcResponseBox but got $msg")
        }
      case Some(msg) ⇒ throw new Exception(s"Expected MTPackage but got $msg")
      case None      ⇒ throw new Exception("No rpc response")
    }
  }

  protected def expectMessageAck(authId: Long, sessionId: Long)(implicit probe: TestProbe): MessageAck = {
    val mb = expectMessageBox(authId, sessionId)
    mb.body shouldBe a[MessageAck]

    val ack = mb.body.asInstanceOf[MessageAck]
    ack
  }

  protected def expectMessageAck(authId: Long, sessionId: Long, messageId: Long)(implicit probe: TestProbe): MessageAck = {
    val mb = expectMessageBox(authId, sessionId)
    mb.body shouldBe a[MessageAck]

    val ack = mb.body.asInstanceOf[MessageAck]
    ack.messageIds should ===(Vector(messageId))
    ack
  }

  protected def expectNewSession(authId: Long, sessionId: Long, messageId: Long)(implicit probe: TestProbe, sessionRegion: SessionRegion): NewSession = {
    expectMessageBoxPF(authId, sessionId) {
      case mb @ MessageBox(_, NewSession(sid, mid)) if sid == sessionId && mid == messageId ⇒
        sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong(), MessageAck(Vector(mb.messageId)))

        val ns = mb.body.asInstanceOf[NewSession]
        ns should ===(NewSession(sessionId, messageId))
        ns
    }
  }

  protected def ignoreNewSession(authId: Long, sessionId: Long)(implicit probe: TestProbe): Unit = {
    probe.ignoreMsg {
      case MTPackage(aid, sid, body) if aid == authId && sid == sessionId ⇒
        MessageBoxCodec.decode(body).require.value.body.isInstanceOf[NewSession]
      case _ ⇒ false
    }
  }

  protected def expectMessageBoxPF[T](authId: Long, sessionId: Long, hint: String = "")(pf: PartialFunction[MessageBox, T])(implicit probe: TestProbe): T = {
    probe.expectMsgPF() {
      case MTPackage(aid, sid, body) if aid == authId && sid == sessionId ⇒
        val mb = MessageBoxCodec.decode(body).require.value

        assert(pf.isDefinedAt(mb), s"expected: ${hint} but got ${mb}")
        pf(mb)
    }
  }

  protected def expectMessageBox(authId: Long, sessionId: Long)(implicit probe: TestProbe): MessageBox = {
    val packageBody = probe.expectMsgPF() {
      case MTPackage(aid, sid, body) if aid == authId && sid == sessionId ⇒ body
    }

    MessageBoxCodec.decode(packageBody).require.value
  }

  protected def sendMessageBox(authId: Long, sessionId: Long, session: ActorRef, messageId: Long, body: ProtoMessage)(implicit probe: TestProbe) =
    sendEnvelope(authId, sessionId, session, HandleMessageBox(MessageBoxCodec.encode(MessageBox(messageId, body)).require.toByteArray))

  protected def sendEnvelope(authId: Long, sessionId: Long, session: ActorRef, msg: SessionMessage)(implicit probe: TestProbe) = {
    session.tell(
      Envelope(
        authId,
        sessionId,
        msg
      ),
      probe.ref
    )
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.awaitTermination()
    closeDb()
  }

  private def closeDb(): Unit = {
    ds.close()
  }

}