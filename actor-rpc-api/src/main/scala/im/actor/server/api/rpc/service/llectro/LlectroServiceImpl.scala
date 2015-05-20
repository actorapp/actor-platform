package im.actor.server.api.rpc.service.llectro

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.llectro.{ ResponseGetAdBatters, ResponseGetAvailableInterests, LlectroService }
import im.actor.api.rpc.misc.ResponseVoid

class LlectroServiceImpl(implicit db: Database, actorSystem: ActorSystem) extends LlectroService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleNotifyAddView(bannerId: Int, viewDuration: Int, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = ???

  override def jhandleDisableInterests(interests: Vector[Int], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = ???

  override def jhandleEnableInterests(interests: Vector[Int], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = ???

  override def jhandleGetAdBatters(maxBannerWidth: Int, maxBannerHeight: Int, screenDensity: Double, clientData: ClientData): Future[HandlerResult[ResponseGetAdBatters]] = ???

  override def jhandleGetAvailableInterests(clientData: ClientData): Future[HandlerResult[ResponseGetAvailableInterests]] = ???
}