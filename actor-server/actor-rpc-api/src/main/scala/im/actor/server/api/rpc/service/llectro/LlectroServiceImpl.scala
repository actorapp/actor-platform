package im.actor.server.api.rpc.service.llectro

import java.util.UUID

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import slick.dbio
import slick.dbio.Effect.Read
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.llectro._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.server.llectro.Llectro
import im.actor.server.persist
import im.actor.server.util.UserUtils

object Errors {
  val LlectroNotInitialized = RpcError(400, "LLECTRO_USER_NOT_INITIALIZED", "", false, None)
}

class LlectroServiceImpl(llectro: Llectro)(implicit db: Database, actorSystem: ActorSystem) extends LlectroService {
  import UserUtils._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleInitLlectro(screenWidth: Int, screenHeight: Int, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action =
      requireAuth(clientData) map { implicit client ⇒
        for (_ ← createLlectroUser(screenWidth, screenHeight))
          yield Ok(ResponseVoid)
      }

    db.run(toDBIOAction(action))
  }

  override def jhandleNotifyBannerClick(bannerId: Int, clientData: im.actor.api.rpc.ClientData): Future[HandlerResult[ResponseVoid]] =
    Future.successful(Error(CommonErrors.UnsupportedRequest))

  override def jhandleNotifyBannerView(bannerId: Int, viewDuration: Int, clientData: im.actor.api.rpc.ClientData): Future[HandlerResult[ResponseVoid]] =
    Future.successful(Error(CommonErrors.UnsupportedRequest))

  override def jhandleDisableInterests(interests: Vector[Int], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action =
      requireAuth(clientData).map { client ⇒
        persist.llectro.LlectroUser.findByUserId(client.userId).flatMap {
          case Some(user) ⇒
            for (result ← llectro.deleteInterest(user, interests)) yield result.collectFirst {
              case Left(e) ⇒ throw new Exception(e.errors)
            }.getOrElse(Ok(ResponseVoid))
          case None ⇒ DBIO.successful(Error(Errors.LlectroNotInitialized))
        }
      }
    db.run(toDBIOAction(action))
  }

  override def jhandleEnableInterests(interests: Vector[Int], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action =
      requireAuth(clientData).map { client ⇒
        persist.llectro.LlectroUser.findByUserId(client.userId).flatMap {
          case Some(user) ⇒ for (_ ← llectro.addInterests(user, interests)) yield Ok(ResponseVoid)
          case None       ⇒ DBIO.successful(Error(Errors.LlectroNotInitialized))
        }
      }
    db.run(toDBIOAction(action))
  }

  override def jhandleGetAvailableInterests(clientData: ClientData): Future[HandlerResult[ResponseGetAvailableInterests]] = {
    val authorizedAction = requireAuth(clientData) map { client ⇒
      for {
        enabledIds ← persist.llectro.UserInterest.findIdsByUserId(client.userId)
        rootInterests ← persist.llectro.Interest.find(1, 1)
      } yield {
        val tree = rootInterests.toVector map { interest ⇒
          Interest(interest.id, interest.name, Vector.empty, enabledIds.contains(interest.id))
        }

        Ok(ResponseGetAvailableInterests(tree))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  def jhandleGetBalance(clientData: ClientData): Future[HandlerResult[ResponseGetBalance]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      persist.llectro.LlectroUser.findByUserId(client.userId).flatMap {
        case Some(user) ⇒ for {
          userBalance ← DBIO.from(llectro.getUserBalance(user.uuid))
          balance = f"${userBalance.balance}%1.3f"
        } yield Ok(ResponseGetBalance(balance))
        case None ⇒ DBIO.successful(Error(Errors.LlectroNotInitialized))
      }
    }
    db.run(toDBIOAction(authorizedAction))
  }

  private def getInterestsTree(level: Int, parentId: Int): dbio.DBIOAction[Vector[Interest], NoStream, Read with Effect] = {
    persist.llectro.Interest.find(level, parentId) flatMap { interests ⇒
      DBIO.sequence(interests.toVector map { interest ⇒
        for {
          children ← getInterestsTree(interest.level + 1, interest.id)
        } yield Interest(interest.id, interest.name, children, false)
      })
    }
  }

  private def createLlectroUser(screenWidth: Int, screenHeight: Int)(implicit clientData: AuthorizedClientData): DBIO[UUID] = {
    persist.llectro.LlectroUser.findByUserId(clientData.userId) flatMap {
      case Some(user) ⇒
        for {
          _ ← llectro.createDevice(clientData.authId, screenWidth, screenHeight)
        } yield user.uuid
      case None ⇒
        for {
          user ← getClientUserUnsafe
          llectroUser ← llectro.createUserAndDevice(user.id, user.name, clientData.authId, screenWidth, screenHeight)
        } yield llectroUser.uuid
    }
  }
}