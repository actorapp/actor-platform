package im.actor.server.session

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future, blocking }
import scala.util.Random

import akka.actor._
import akka.contrib.pattern.DistributedPubSubExtension
import akka.stream.ActorMaterializer
import akka.testkit.TestProbe
import akka.util.Timeout
import com.google.protobuf.ByteString
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Seconds, Span }
import org.scalatest.{ FlatSpecLike, Matchers }
import slick.driver.PostgresDriver

import im.actor.api.rpc.RpcResult
import im.actor.api.rpc.codecs._
import im.actor.api.rpc.sequence.{ SeqUpdate, WeakUpdate }
import im.actor.server
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceConfig, SequenceServiceImpl }
import im.actor.server.api.rpc.{ RpcApiService, RpcResultCodec }
import im.actor.server.db.DbExtension
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.MTPackage
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.sequence.WeakUpdatesManager
import im.actor.server.session.SessionEnvelope.Payload
import im.actor.server._

abstract class BaseSessionSpec(_system: ActorSystem = {
                                 server.ActorSpecification.createSystem()
                               })
  extends server.ActorSuite(_system)
  with FlatSpecLike
  with ScalaFutures
  with Matchers
  with ImplicitUserRegions
  with ActorSerializerPrepare {

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(30, Seconds))

  protected implicit val timeout = Timeout(10.seconds)

  protected implicit val materializer = ActorMaterializer()
  protected implicit val ec = system.dispatcher

  protected implicit val db: PostgresDriver.api.Database = DbExtension(_system).db
  DbExtension(_system).clean()
  DbExtension(_system).migrate()

  protected implicit val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
  protected implicit val presenceManagerRegion = PresenceManager.startRegion()
  protected implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()

  protected val mediator = DistributedPubSubExtension(system).mediator

  protected implicit val sessionConfig = SessionConfig.load(system.settings.config.getConfig("session"))

  Session.startRegion(Some(Session.props(mediator)))

  protected implicit val sessionRegion = Session.startRegionProxy()

  protected val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
  protected implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
  protected val authService = new AuthServiceImpl(new DummyCodeActivation, mediator)
  protected val sequenceConfig = SequenceServiceConfig.load.toOption.get
  protected val sequenceService = new SequenceServiceImpl(sequenceConfig)

  system.actorOf(RpcApiService.props(Seq(authService, sequenceService)), "rpcApiService")

  protected def createAuthId(): Long = {
    val authId = Random.nextLong()
    Await.result(db.run(persist.AuthId.create(authId, None, None)), 1.second)
    authId
  }

  protected def expectSeqUpdate(authId: Long, sessionId: Long, sendAckAt: Option[Duration] = Some(0.seconds))(implicit probe: TestProbe): SeqUpdate = {
    val mb = expectMessageBox(authId, sessionId)

    val update = UpdateBoxCodec.decode(mb.body.asInstanceOf[UpdateBox].bodyBytes).require.value.asInstanceOf[SeqUpdate]

    sendAckAt map { delay ⇒
      Future {
        blocking {
          Thread.sleep(delay.toMillis)
          sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong, MessageAck(Vector(mb.messageId)))
        }
      }
    }

    update
  }

  protected def expectWeakUpdate(authId: Long, sessionId: Long)(implicit probe: TestProbe): WeakUpdate = {
    UpdateBoxCodec.decode(expectMessageBox(authId, sessionId).body.asInstanceOf[UpdateBox].bodyBytes).require.value.asInstanceOf[WeakUpdate]
  }

  protected def expectRpcResult(sendAckAt: Option[Duration] = Some(0.seconds), expectAckFor: Set[Long] = Set.empty)(implicit probe: TestProbe, sessionRegion: SessionRegion): RpcResult = {
    val messages = probe.receiveN(1 + expectAckFor.size).toSet

    if (messages.size != expectAckFor.size + 1) {
      fail(s"Expected response and acks for ${expectAckFor.mkString(",")}, got: ${messages.mkString(",")}")
    } else {
      val (rest, ackIds) = messages.foldLeft(Vector.empty[(Long, Long, Long, ProtoMessage)], Set.empty[Long]) {
        case ((rest, ackIds), MTPackage(authId, sessionId, mbBytes)) ⇒
          val mb = MessageBoxCodec.decode(mbBytes).require.value

          mb.body match {
            case MessageAck(ids) ⇒ (rest, ackIds ++ ids)
            case body            ⇒ (rest :+ ((authId, sessionId, mb.messageId, body)), ackIds)
          }
      }

      ackIds shouldEqual expectAckFor

      rest match {
        case Vector((authId, sessionId, messageId, RpcResponseBox(_, rpcResultBytes))) ⇒
          sendAckAt map { delay ⇒
            Future {
              blocking {
                Thread.sleep(delay.toMillis)
                sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong, MessageAck(Vector(messageId)))
              }
            }
          }

          RpcResultCodec.decode(rpcResultBytes).require.value
        case unexpected ⇒ throw new Exception(s"Expected RpcResponseBox but got $unexpected")
      }
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
    sendEnvelope(authId, sessionId, session, Payload.HandleMessageBox(HandleMessageBox(ByteString.copyFrom(MessageBoxCodec.encode(MessageBox(messageId, body)).require.toByteBuffer))))

  protected def sendEnvelope(authId: Long, sessionId: Long, session: ActorRef, payload: Payload)(implicit probe: TestProbe) = {
    session.tell(
      SessionEnvelope(
        authId,
        sessionId
      ).withPayload(payload),
      probe.ref
    )
  }
}
