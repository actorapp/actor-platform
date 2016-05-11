package im.actor.server.frontend

import java.net.InetAddress

import akka.stream.FlowShape
import kamon.metric.instrument.{ MinMaxCounter, Histogram }

import scala.util.{ Failure, Success }

import akka.actor._
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl._
import akka.util.ByteString

import im.actor.server.mtproto.codecs.transport._
import im.actor.server.mtproto.transport._
import im.actor.server.session.SessionRegion

object MTProtoBlueprint {

  import akka.stream.stage._

  type MTProtoFlow = Flow[ByteString, ByteString, akka.NotUsed]

  val protoVersions: Set[Byte] = Set(1, 2, 3)
  val apiMajorVersions: Set[Byte] = Set(1)

  def apply(connId: String, connTimeHist: Histogram, connCountMM: MinMaxCounter, serverKeys: Seq[ServerKey], remoteAddr: InetAddress)(implicit sessionRegion: SessionRegion, system: ActorSystem): MTProtoFlow = {
    val sessionClient = system.actorOf(SessionClient.props(sessionRegion, remoteAddr), s"sessionClient_${connId}")
    val authManager = system.actorOf(AuthorizationManager.props(serverKeys, sessionClient), s"authManager-$connId")
    val authSource = Source.fromPublisher(ActorPublisher[MTProto](authManager))

    val sessionClientSource = Source.fromPublisher(ActorPublisher[MTProto](sessionClient))

    val mtprotoFlow = Flow.fromGraph(new PackageParseStage())
      .transform(() ⇒ new PackageCheckStage)
      .via(new PackageHandleStage(protoVersions, apiMajorVersions, authManager, sessionClient))

    val mapRespFlow: Flow[MTProto, ByteString, akka.NotUsed] = Flow[MTProto]
      .transform(() ⇒ mapResponse(system))

    val connStartTime = System.currentTimeMillis()
    connCountMM.increment()

    val completeSink = Sink onComplete {
      case res ⇒
        res match {
          case Success(_) ⇒
            system.log.debug("Closing connection {}", connId)
          case Failure(e) ⇒
            system.log.debug("Closing connection {} due to error: {}", connId, e)
        }

        connTimeHist.record(System.currentTimeMillis() - connStartTime)
        connCountMM.decrement()
        authManager ! PoisonPill
        sessionClient ! PoisonPill
    }

    Flow.fromGraph(GraphDSL.create() { implicit builder ⇒
      import GraphDSL.Implicits._

      val bcast = builder.add(Broadcast[ByteString](2))
      val merge = builder.add(Merge[MTProto](3))

      val mtproto = builder.add(mtprotoFlow)
      val auth = builder.add(authSource)
      val session = builder.add(sessionClientSource)
      val mapResp = builder.add(mapRespFlow)
      val complete = builder.add(completeSink)

      // format: OFF

      bcast ~> complete
      bcast ~> mtproto ~> merge
               auth    ~> merge
               session ~> merge ~> mapResp

      // format: ON

      FlowShape(bcast.in, mapResp.outlet)
    })
  }

  def mapResponse(system: ActorSystem) = new PushStage[MTProto, ByteString] {
    private[this] var packageIndex: Int = -1

    override def onPush(elem: MTProto, ctx: Context[ByteString]) = {
      packageIndex += 1
      val pkg = TransportPackage(packageIndex, elem)

      val resBits = TransportPackageCodec.encode(pkg).require
      val res = ByteString(resBits.toByteBuffer)

      elem match {
        case _: Drop ⇒
          ctx.pushAndFinish(res)
        case _ ⇒
          ctx.push(res)
      }
    }
  }
}
