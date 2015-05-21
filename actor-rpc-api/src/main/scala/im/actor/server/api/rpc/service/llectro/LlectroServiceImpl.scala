package im.actor.server.api.rpc.service.llectro

import scala.annotation.tailrec
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import slick.dbio
import slick.dbio.Effect.Read
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.llectro.{ Interest, ResponseGetAdBatters, ResponseGetAvailableInterests, LlectroService }
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.server.persist

class LlectroServiceImpl(implicit db: Database, actorSystem: ActorSystem) extends LlectroService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleNotifyAddView(bannerId: Int, viewDuration: Int, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    Future.successful(Error(CommonErrors.UnsupportedRequest))
  }

  override def jhandleDisableInterests(interests: Vector[Int], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = ???

  override def jhandleEnableInterests(interests: Vector[Int], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = ???

  override def jhandleGetAdBatters(maxBannerWidth: Int, maxBannerHeight: Int, screenDensity: Double, clientData: ClientData): Future[HandlerResult[ResponseGetAdBatters]] = ???

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