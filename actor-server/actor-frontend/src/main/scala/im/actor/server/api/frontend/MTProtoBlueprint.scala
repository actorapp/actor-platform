package im.actor.server.api.frontend

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

  val protoVersions: Set[Byte] = Set(1)
  val apiMajorVersions: Set[Byte] = Set(1)

  def apply(connId: String)(implicit sessionRegion: SessionRegion, system: ActorSystem): Flow[ByteString, ByteString, Unit] = {
    val authManager = system.actorOf(AuthorizationManager.props, s"authManager-$connId")
    val authSource = Source(ActorPublisher[MTProto](authManager))

    val sessionClient = system.actorOf(SessionClient.props(sessionRegion), s"sessionClient-$connId")
    val sessionClientSource = Source(ActorPublisher[MTProto](sessionClient))

    val mtprotoFlow = Flow[ByteString]
      .transform(() ⇒ new PackageParseStage)
      .transform(() ⇒ new PackageCheckStage)
      .transform(() ⇒ new PackageHandleStage(protoVersions, apiMajorVersions, authManager, sessionClient))

    val mapRespFlow: Flow[MTProto, ByteString, Unit] = Flow[MTProto]
      .transform(() ⇒ mapResponse(system))

    val completeSink = Sink.onComplete {
      case res ⇒
        res match {
          case Success(_) ⇒
            system.log.debug("Closing connection")
          case Failure(e) ⇒
            system.log.error(e, "Closing connection due to error")
        }

        authManager ! PoisonPill
        sessionClient ! PoisonPill
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

      bcast ~> complete
      bcast ~> mtproto ~> merge
               auth    ~> merge
               session ~> merge ~> mapResp

      // format: ON

      (bcast.in, mapResp.outlet)
    }
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
