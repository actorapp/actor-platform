package im.actor.server.api.rpc.service.llectro

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import slick.dbio
import slick.dbio.Effect.Read
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.ilectro._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.server.ilectro.ILectro
import im.actor.server.persist

object Errors {
  val IlectroNotInitialized = RpcError(400, "ILECTRO_USER_NOT_INITIALIZED", "", false, None)
}

class IlectroServiceImpl(ilectro: ILectro)(implicit db: Database, actorSystem: ActorSystem) extends IlectroService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleNotifyAddView(bannerId: Int, viewDuration: Int, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    Future.successful(Error(CommonErrors.UnsupportedRequest))
  }

  override def jhandleDisableInterests(interests: Vector[Int], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action =
      requireAuth(clientData).map { client ⇒
        persist.ilectro.ILectroUser.findByUserId(client.userId).flatMap {
          case Some(user) ⇒
            for (result ← ilectro.deleteInterest(user, interests)) yield result.collectFirst {
              case Left(e) ⇒ throw new Exception(e.errors)
            }.getOrElse(Ok(ResponseVoid))
          case None ⇒ DBIO.successful(Error(Errors.IlectroNotInitialized))
        }
      }
    db.run(toDBIOAction(action))
  }

  override def jhandleEnableInterests(interests: Vector[Int], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action =
      requireAuth(clientData).map { client ⇒
        persist.ilectro.ILectroUser.findByUserId(client.userId).flatMap {
          case Some(user) ⇒ for (_ ← ilectro.addInterests(user, interests)) yield Ok(ResponseVoid)
          case None       ⇒ DBIO.successful(Error(Errors.IlectroNotInitialized))
        }
      }
    db.run(toDBIOAction(action))
  }

  override def jhandleGetAdBanners(maxBannerWidth: Int, maxBannerHeight: Int, screenDensity: Double, clientData: ClientData): Future[HandlerResult[ResponseGetAdBanners]] = {
    val authorizedAction = requireAuth(clientData) map { client ⇒
      persist.ilectro.ILectroUser.findByUserId(client.userId) flatMap {
        case Some(user) ⇒
          for (banner ← DBIO.from(ilectro.getBanners(user.uuid))) yield {
            Ok(ResponseGetAdBanners(Vector(Banner(0, 0, 0, banner.imageUrl, banner.advertUrl, 0))))
          }
        case None ⇒ DBIO.successful(Error(Errors.IlectroNotInitialized))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleGetAvailableInterests(clientData: ClientData): Future[HandlerResult[ResponseGetAvailableInterests]] = {
    val authorizedAction = requireAuth(clientData) map { client ⇒
      for (tree ← getInterestsTree(0, 0))
        yield Ok(ResponseGetAvailableInterests(tree))
    }

    db.run(toDBIOAction(authorizedAction))
  }

  private def getInterestsTree(level: Int, parentId: Int): dbio.DBIOAction[Vector[Interest], NoStream, Read with Effect] = {
    persist.ilectro.Interest.find(level, parentId) flatMap { interests ⇒
      DBIO.sequence(interests.toVector map { interest ⇒
        for {
          children ← getInterestsTree(interest.level + 1, interest.id)
        } yield Interest(interest.id, interest.name, children, false)
      })
    }
  }
}