package im.actor.server.session

import akka.contrib.pattern.DistributedPubSubExtension
import im.actor.server

import scala.concurrent.{ Promise, Future, Await, blocking }
import scala.concurrent.duration._
import scala.util.{ Success, Random }

import akka.actor._
import akka.stream.ActorMaterializer
import akka.testkit.TestProbe
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpecLike, Matchers }

import im.actor.api.rpc.RpcResult
import im.actor.api.rpc.codecs._
import im.actor.api.rpc.sequence.{ SeqUpdate, WeakUpdate }
import im.actor.server.activation.internal.DummyCodeActivation
import im.actor.server.api.ActorSpecHelpers
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceConfig, SequenceServiceImpl }
import im.actor.server.api.rpc.{ RpcApiService, RpcResultCodec }
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.MTPackage
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.push.WeakUpdatesManager
import im.actor.server.social.SocialManager
import im.actor.server.user.UserOffice
import im.actor.server.{ KafkaSpec, SqlSpecHelpers, persist }

abstract class BaseSessionSpec(_system: ActorSystem = { server.ActorSpecification.createSystem() })
  extends server.ActorSuite(_system) with FlatSpecLike with ScalaFutures with Matchers with SqlSpecHelpers with ActorSpecHelpers {

  import SessionMessage._

  implicit val materializer = ActorMaterializer()
  implicit val (ds, db) = migrateAndInitDb()
  implicit val ec = system.dispatcher

  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()
  implicit val userOfficeRegion = UserOffice.startRegion()

  val mediator = DistributedPubSubExtension(_system).mediator

  implicit val sessionConfig = SessionConfig.load(system.settings.config.getConfig("session"))

  Session.startRegion(Some(Session.props(mediator)))

  implicit val sessionRegion = Session.startRegionProxy()

  val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
  implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
  val authService = new AuthServiceImpl(new DummyCodeActivation, mediator)
  val sequenceConfig = SequenceServiceConfig.load.toOption.get
  val sequenceService = new SequenceServiceImpl(sequenceConfig)

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
