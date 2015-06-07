package im.actor.server.api.frontend

import akka.actor._
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl._
import akka.util.{ ByteString, Timeout }
import slick.driver.PostgresDriver.api.Database

import im.actor.server.mtproto.codecs.transport._
import im.actor.server.mtproto.transport._
import im.actor.server.session.SessionRegion

object MTProto {

  import akka.stream.stage._

  val protoVersions: Set[Byte] = Set(1)
  val apiMajorVersions: Set[Byte] = Set(1)
  val mapParallelism = 4 // TODO: #perf tune up and make it configurable

  def flow(connId: String, maxBufferSize: Int, sessionRegion: SessionRegion)(implicit db: Database, system: ActorSystem, timeout: Timeout) = {
    val authManager = system.actorOf(AuthorizationManager.props(db), s"authManager-${connId}")
    val authSource = Source(ActorPublisher[MTProto](authManager))

    val sessionClient = system.actorOf(SessionClient.props(sessionRegion), s"sessionClient-${connId}")
    val sessionClientSource = Source(ActorPublisher[MTProto](sessionClient))

    val mtprotoFlow: Flow[ByteString, MTProto, Unit] = Flow[ByteString]
      .transform(() ⇒ new PackageParseStage)
      .transform(() ⇒ new PackageCheckStage)
      .transform(() ⇒ new PackageHandleStage(protoVersions, apiMajorVersions, authManager, sessionClient))
      .mapAsync(mapParallelism)(identity)

    val mapRespFlow: Flow[MTProto, ByteString, Unit] = Flow[MTProto]
      .transform(() ⇒ mapResponse(system))

    val completeSink = Sink.onComplete {
      case x ⇒
        system.log.debug("Completing {}", x)
    }

    Flow() { implicit builder ⇒
      import FlowGraph.Implicits._

      val bcast = builder.add(Broadcast[ByteString](2))
      val merge = builder.add(Merge[MTProto](3))

      val mtproto = builder.add(mtprotoFlow)
      val auth = builder.add(authSource)
      val session = builder.add(sessionClientSource)
      val mapResp = builder.add(mapRespFlow)
      val complete = builder.add(completeSink)

      // format: OFF

      mtproto ~> merge
      auth    ~> merge
      session ~> merge ~> mapResp ~> bcast ~> complete

      // format: ON

      (mtproto.inlet, bcast.out(1))
    }
  }

  def parsePackage(maxBufferSize: Int)(implicit system: ActorSystem) = {}

  def mapResponse(system: ActorSystem) = new PushPullStage[MTProto, ByteString] {
    private[this] var packageIndex: Int = -1

    override def onPush(elem: MTProto, ctx: Context[ByteString]) = {
      packageIndex += 1
      val pkg = TransportPackage(packageIndex, elem)
      // system.log.debug("Sending TransportPackage {}", pkg)

      val resBits = TransportPackageCodec.encode(pkg).require
      val res = ByteString(resBits.toByteBuffer)
      // system.log.debug("Sending bytes {}", resBits)

      elem match {
        case _: Drop ⇒
          ctx.pushAndFinish(res)
        case _ ⇒
          ctx.push(res)
      }
    }

    override def onPull(ctx: Context[ByteString]) = ctx.pull()
  }
}
