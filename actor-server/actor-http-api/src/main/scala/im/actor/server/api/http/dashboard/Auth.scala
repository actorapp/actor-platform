package im.actor.server.api.http.dashboard

import java.time.{ LocalDateTime, ZoneOffset }

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.forkjoin.ThreadLocalRandom

import akka.http.scaladsl.model.{ StatusCode, StatusCodes }
import cats.data.Xor
import slick.driver.PostgresDriver.api._

import im.actor.server.api.http.dashboard.DBIOResult._
import im.actor.server.util.ACLUtils.accessToken
import im.actor.server.{ models, persist }
import im.actor.server.email._

object Auth {

  //not authorized action
  def start(email: String)(implicit db: Database, ec: ExecutionContext, emailSender: EmailSender): Future[ErrorResult Xor StatusCode] = {
    val action = for {
      userEmail ← fromDBIOOption(StatusCodes.BadRequest → DashboardError("no such account"))(persist.UserEmail.find(email))
      existing ← fromDBIO(persist.DashboardSession.findByUserId(userEmail.userId))
      _ ← fromDBIO(existing map { s ⇒ persist.DashboardSession.markInactive(s.id) } getOrElse DBIO.successful(0))
      session = createDashboardSession(userEmail.userId)
      _ ← fromFuture(emailSender.send(Message(email, "Actor dashboard passcode",
        s"${session.passcode} is your actor dashboard one-time passcode")))
    } yield StatusCodes.Accepted
    db.run(action.value)
  }

  //not authorized action
  def login(email: String, passcode: String)(implicit db: Database, ec: ExecutionContext): Future[ErrorResult Xor SuccessResult[AuthToken]] = {
    val action = for {
      userEmail ← fromDBIOOption(StatusCodes.BadRequest → DashboardError("no such account"))(persist.UserEmail.find(email))
      session ← fromDBIOOption(StatusCodes.BadRequest → DashboardError("Session was not initiated. Initiate session first!"))(persist.DashboardSession.findByUserId(userEmail.userId))
      _ ← fromBoolean(StatusCodes.Unauthorized → DashboardError("Wrong passcode provided"))(session.passcode == passcode)
      _ ← fromDBIO(persist.DashboardSession.markActive(session.id))
    } yield StatusCodes.OK → AuthToken(session.authToken)
    db.run(action.value)
  }

  def logout(email: String, token: String)(implicit db: Database, ec: ExecutionContext): Future[ErrorResult Xor StatusCode] = {
    val action = for {
      userEmail ← fromDBIOOption(StatusCodes.BadRequest → DashboardError("no such account"))(persist.UserEmail.find(email))
      session ← fromDBIOOption(StatusCodes.BadRequest → DashboardError("Can't log out since user was not logged in"))(persist.DashboardSession.findByUserId(userEmail.userId))
      _ ← fromBoolean(StatusCodes.Forbidden → DashboardError("Wrong auth token!"))(token == session.authToken)
      _ ← fromDBIO(persist.DashboardSession.markInactive(session.id))
    } yield StatusCodes.OK
    db.run(action.value)
  }

  private def genCode() = ThreadLocalRandom.current.nextLong().toString.dropWhile(c ⇒ c == '0' || c == '-').take(6)

  private def createDashboardSession(userId: Int) = {
    val rng = ThreadLocalRandom.current()
    models.DashboardSession(rng.nextLong(), userId, genCode(), accessToken(rng), isActive = false, LocalDateTime.now(ZoneOffset.UTC))
  }

}
