package im.actor.server.api.rpc.service.features

import akka.actor._
import com.google.protobuf.ByteString
import im.actor.api.rpc._
import im.actor.api.rpc.features.FeaturesService
import im.actor.api.rpc.misc.{ ResponseBool, ResponseVoid }
import im.actor.api.rpc.peers.ApiUserOutPeer
import im.actor.server.db.DbExtension
import im.actor.server.model.DeviceFeature
import im.actor.server.persist.DeviceFeatureRepo

import scala.concurrent.{ ExecutionContext, Future }

final class FeaturesServiceImpl(implicit system: ActorSystem) extends FeaturesService {
  override implicit protected val ec: ExecutionContext = system.dispatcher

  private val db = DbExtension(system).db

  override def doHandleEnableFeature(
    featureName: String,
    args:        Option[Array[Byte]],
    clientData:  ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      for {
        _ ← db.run(
          DeviceFeatureRepo.enable(
            DeviceFeature(
              client.authId,
              featureName,
              args.map(ByteString.copyFrom).getOrElse(ByteString.EMPTY)
            )
          )
        )
      } yield Ok(ResponseVoid)
    }

  override def doHandleCheckFeatureEnabled(
    userOutPeer: ApiUserOutPeer,
    featureName: String,
    clientData:  ClientData
  ): Future[HandlerResult[ResponseBool]] =
    authorized(clientData) { client ⇒
      for {
        exists ← db.run(DeviceFeatureRepo.exists(client.authId, featureName))
      } yield Ok(ResponseBool(exists))
    }

  override def doHandleDisableFeature(
    featureName: String,
    clientData:  ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      for {
        _ ← db.run(DeviceFeatureRepo.disable(client.authId, featureName))
      } yield Ok(ResponseVoid)
    }
}