package im.actor.server.session

import akka.actor._
import akka.testkit.TestProbe
import com.google.protobuf.ByteString
import im.actor.api.rpc.codecs._
import im.actor.api.rpc.sequence.{ FatSeqUpdate, SeqUpdate, WeakUpdate }
import im.actor.api.rpc.{ Request, RpcRequest, RpcResult }
import im.actor.server
import im.actor.server._
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceConfig, SequenceServiceImpl }
import im.actor.server.api.rpc.{ RpcApiExtension, RpcResultCodec }
import im.actor.server.db.DbExtension
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.session.SessionEnvelope.Payload
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Seconds, Span }
import org.scalatest.{ FlatSpecLike, Matchers }
import scodec.bits.BitVector
import slick.driver.PostgresDriver

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future, blocking }
import scala.util.Random

abstract class BaseSessionSpec(_system: ActorSystem = {
                                 server.ActorSpecification.createSystem()
                               })
  extends server.ActorSuite(_system)
  with FlatSpecLike
  with ScalaFutures
  with Matchers
  with ActorSerializerPrepare
  with ServiceSpecHelpers {

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(10, Seconds))

  protected implicit val ec = system.dispatcher

  protected implicit lazy val db: PostgresDriver.api.Database = {
    DbExtension(_system).db
    DbExtension(_system).clean()
    DbExtension(_system).migrate()
    DbExtension(_system).db
  }

  protected implicit val sessionConfig = SessionConfig.load(system.settings.config.getConfig("session"))

  Session.startRegion(Session.props)

  protected implicit val sessionRegion = Session.startRegionProxy()

  protected val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
  protected implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
  protected implicit val authService = new AuthServiceImpl(new DummyCodeActivation)
  protected val sequenceConfig = SequenceServiceConfig.load().toOption.get
  protected lazy val sequenceService = new SequenceServiceImpl(sequenceConfig)
  protected lazy val messagingService = MessagingServiceImpl()
  protected lazy val contactsService = new ContactsServiceImpl()

  override def beforeAll = {
    RpcApiExtension(system).register(Seq(authService, sequenceService, messagingService, contactsService))
  }

  protected def createAuthId(): Long = {
    val authId = Random.nextLong()
    Await.result(db.run(persist.AuthIdRepo.create(authId, None, None)), 1.second)
    authId
  }

  protected def expectSeqUpdate(authId: Long, sessionId: Long, sendAckAt: Option[Duration] = Some(0.seconds))(implicit probe: TestProbe): SeqUpdate =
    expectUpdateBox(classOf[SeqUpdate], authId, sessionId, sendAckAt)

  protected def expectFatSeqUpdate(authId: Long, sessionId: Long, sendAckAt: Option[Duration] = Some(0.seconds))(implicit probe: TestProbe): FatSeqUpdate =
    expectUpdateBox(classOf[FatSeqUpdate], authId, sessionId, sendAckAt)

  protected def expectWeakUpdate(authId: Long, sessionId: Long)(implicit probe: TestProbe): WeakUpdate =
    expectUpdateBox(classOf[WeakUpdate], authId, sessionId, None)

  protected def expectUpdateBox[T <: im.actor.api.rpc.UpdateBox](clazz: Class[T], authId: Long, sessionId: Long, sendAckAt: Option[Duration])(implicit probe: TestProbe, m: Manifest[T]): T = {
    val mb = expectMessageBox()

    val update = UpdateBoxCodec.decode(mb.body.asInstanceOf[ProtoPush].bodyBytes).require.value

    sendAckAt map { delay ⇒
      Future {
        blocking {
          Thread.sleep(delay.toMillis)
          sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong, MessageAck(Vector(mb.messageId)))
        }
      }
    }

    update shouldBe a[T]

    update.asInstanceOf[T]
  }

  protected def expectRpcResult(authId: Long, sessionId: Long, sendAckAt: Option[Duration] = Some(0.seconds), expectAckFor: Set[Long] = Set.empty)(implicit probe: TestProbe, sessionRegion: SessionRegion): RpcResult = {
    val messages = probe.receiveN(1 + expectAckFor.size, patienceConfig.timeout.totalNanos.nano).toSet

    if (messages.size != expectAckFor.size + 1) {
      fail(s"Expected response and acks for ${expectAckFor.mkString(",")}, got: ${messages.mkString(",")}")
    } else {
      val (rest, ackIds) = messages.foldLeft(Vector.empty[(Long, ProtoMessage)], Set.empty[Long]) {
        case ((rest, ackIds), mbBytes: BitVector) ⇒
          val mb = MessageBoxCodec.decode(mbBytes).require.value

          mb.body match {
            case MessageAck(ids) ⇒ (rest, ackIds ++ ids)
            case body            ⇒ (rest :+ ((mb.messageId, body)), ackIds)
          }
      }

      ackIds shouldEqual expectAckFor

      rest match {
        case Vector((messageId, ProtoRpcResponse(_, rpcResultBytes))) ⇒
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

  protected def expectMessageAck()(implicit probe: TestProbe): MessageAck = {
    val mb = expectMessageBox()
    mb.body shouldBe a[MessageAck]

    val ack = mb.body.asInstanceOf[MessageAck]
    ack
  }

  protected def expectMessageAck(messageId: Long)(implicit probe: TestProbe): MessageAck = {
    val mb = expectMessageBox()
    mb.body shouldBe a[MessageAck]

    val ack = mb.body.asInstanceOf[MessageAck]
    ack.messageIds should ===(Vector(messageId))
    ack
  }

  protected def expectNewSession(authId: Long, sessionId: Long, messageId: Long)(implicit probe: TestProbe, sessionRegion: SessionRegion): NewSession = {
    expectMessageBoxPF() {
      case mb @ MessageBox(_, NewSession(sid, mid)) ⇒
        sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong(), MessageAck(Vector(mb.messageId)))

        val ns = mb.body.asInstanceOf[NewSession]
        ns should ===(NewSession(sessionId, messageId))
        ns
    }
  }

  protected def ignoreNewSession()(implicit probe: TestProbe): Unit = {
    probe.ignoreMsg {
      case body: BitVector ⇒
        MessageBoxCodec.decode(body).require.value.body.isInstanceOf[NewSession]
      case _ ⇒ false
    }
  }

  protected def expectMessageBoxPF[T](hint: String = "")(pf: PartialFunction[MessageBox, T])(implicit probe: TestProbe): T = {
    probe.expectMsgPF(max = patienceConfig.timeout.totalNanos.nano) {
      case body: BitVector ⇒
        val mb = MessageBoxCodec.decode(body).require.value

        assert(pf.isDefinedAt(mb), s"expected: $hint but got $mb")
        pf(mb)
    }
  }

  protected def expectMessageBox()(implicit probe: TestProbe): MessageBox = {
    val packageBody = probe.expectMsgPF(max = patienceConfig.timeout.totalNanos.nano) {
      case body: BitVector ⇒ body
    }

    MessageBoxCodec.decode(packageBody).require.value
  }

  protected def sendMessageBox(authId: Long, sessionId: Long, session: ActorRef, messageId: Long, body: ProtoMessage)(implicit probe: TestProbe) =
    sendEnvelope(authId, sessionId, session, Payload.HandleMessageBox(handleMessageBox(messageId, body)))

  protected def handleMessageBox(messageId: Long, body: ProtoMessage) =
    HandleMessageBox(ByteString.copyFrom(MessageBoxCodec.encode(MessageBox(messageId, body)).require.toByteBuffer))

  protected def sendEnvelope(authId: Long, sessionId: Long, session: ActorRef, payload: Payload)(implicit probe: TestProbe) = {
    session.tell(
      SessionEnvelope(
        authId,
        sessionId
      ).withPayload(payload),
      probe.ref
    )
  }

  protected def sendRequest(authId: Long, sessionId: Long, session: ActorRef, messageId: Long, request: RpcRequest)(implicit probe: TestProbe): Unit = {
    val rqBox = ProtoRpcRequest(RequestCodec.encode(Request(request)).require)
    sendMessageBox(authId, sessionId, session, messageId, rqBox)
  }

  protected def sendRequest(authId: Long, sessionId: Long, session: ActorRef, request: RpcRequest)(implicit probe: TestProbe): Long = {
    val messageId = Random.nextLong()
    sendRequest(authId, sessionId, session, messageId, request)
    messageId
  }
}
