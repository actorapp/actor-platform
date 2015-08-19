package im.actor.server.user

import scala.concurrent.{ Future, ExecutionContext }

import akka.actor.{ ActorRef, ActorSystem }
import akka.contrib.pattern.DistributedPubSubExtension
import akka.pattern.ask
import akka.util.Timeout

import im.actor.server.db.ActorPostgresDriver.api._

import im.actor.server.db.DbExtension
import im.actor.server.sequence.{ SeqUpdatesExtension, SeqUpdatesManager }
import im.actor.server.{ models, persist ⇒ p }

trait AuthEvent

object AuthEvents {
  case object AuthIdInvalidated
}

trait AuthCommands {
  self: Queries ⇒

  import UserCommands._
  import akka.contrib.pattern.DistributedPubSubMediator._

  def authIdTopic(authId: Long): String = s"auth.events.${authId}"

  def auth(userId: Int, authId: Long)(
    implicit
    userOfficeRegion: UserProcessorRegion,
    timeout:          Timeout,
    ec:               ExecutionContext
  ): Future[NewAuthAck] = {
    (userOfficeRegion.ref ? NewAuth(userId, authId)).mapTo[NewAuthAck]
  }

  def removeAuth(userId: Int, authId: Long)(
    implicit
    userOfficeRegion: UserProcessorRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[RemoveAuthAck] = (userOfficeRegion.ref ? RemoveAuth(userId, authId)).mapTo[RemoveAuthAck]

  def logoutByAppleToken(token: Array[Byte])(implicit ec: ExecutionContext, system: ActorSystem, timeout: Timeout, db: Database, userProcessorRegion: UserProcessorRegion): Future[Unit] = {
    db.run(p.push.ApplePushCredentials.findByToken(token)) flatMap { creds ⇒
      Future.sequence(creds map (c ⇒ logout(c.authId))) map (_ ⇒ ())
    }
  }

  def logout(authId: Long)(implicit ec: ExecutionContext, system: ActorSystem, timeout: Timeout, db: Database, userProcessorRegion: UserProcessorRegion): Future[Unit] = {
    db.run(p.AuthSession.findByAuthId(authId)) flatMap {
      case Some(session) ⇒ logout(session)
      case None          ⇒ throw new Exception("Can't find auth session to logout")
    }
  }

  def logout(session: models.AuthSession)(implicit ec: ExecutionContext, system: ActorSystem, timeout: Timeout, db: Database, userProcessorRegion: UserProcessorRegion): Future[Unit] = {
    system.log.warning(s"Terminating AuthSession ${session.id} of user ${session.userId} and authId ${session.authId}")

    implicit val seqExt = SeqUpdatesExtension(system)
    val mediator = DistributedPubSubExtension(system).mediator

    for {
      _ ← removeAuth(session.userId, session.authId)
      _ ← db.run(p.AuthSession.delete(session.userId, session.id))
      _ = SeqUpdatesManager.deletePushCredentials(session.authId)
    } yield {
      publishAuthIdInvalidated(mediator, session.authId)
    }
  }

  private def publishAuthIdInvalidated(mediator: ActorRef, authId: Long): Unit = {
    mediator ! Publish(authIdTopic(authId), AuthEvents.AuthIdInvalidated)
  }
}
