package im.actor.server.api.http.dashboard

import java.time.{ ZoneOffset, LocalDateTime }

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.forkjoin.ThreadLocalRandom

import akka.http.scaladsl.model.{ StatusCode, StatusCodes }
import cats.data.Xor
import slick.dbio.DBIO

import im.actor.server.api.http.dashboard.DBIOResult._
import im.actor.server.util.ACLUtils
import im.actor.server.util.IdUtils._
import im.actor.server.{ models, persist }
import slick.driver.PostgresDriver.api._

object Users {
  def get(id: Int)(implicit db: Database, ec: ExecutionContext): Future[ErrorResult Xor SuccessResult[CompleteUser]] = {
    val action = for {
      user ← fromDBIOOption(StatusCodes.NotFound → DashboardError("No such user found"))(persist.User.find(id).headOption)
      phones ← fromDBIO(persist.UserPhone.findByUserId(id))
      emails ← fromDBIO(persist.UserEmail.findByUserId(id))
    } yield StatusCodes.OK → CompleteUser(user, phones, emails)
    db.run(action.value)
  }

  def delete(id: Int)(implicit db: Database, ec: ExecutionContext): Future[ErrorResult Xor StatusCode] = {
    val action = fromDBIO(for (_ ← persist.User.setDeletedAt(id)) yield StatusCodes.Accepted)
    db.run(action.value)
  }

  def create(form: UserForm)(implicit db: Database, ec: ExecutionContext): Future[ErrorResult Xor SuccessResult[CreatedUser]] = {
    val user = createUser(form.userName)
    val action = for {
      _ ← (form.phone, form.email) match {
        case (Some(phone), Some(email)) ⇒
          for {
            _ ← fromDBIOBoolean(StatusCodes.NotAcceptable → DashboardError(s"User with phone $phone already exists"))(persist.UserPhone.exists(phone).map(!_))
            _ ← fromDBIOBoolean(StatusCodes.NotAcceptable → DashboardError(s"User with email $email already exists"))(persist.UserEmail.exists(email).map(!_))
            rnd = ThreadLocalRandom.current()
            userPhone = models.UserPhone(nextIntId(rnd), user.id, ACLUtils.nextAccessSalt(rnd), phone, "Mobile phone")
            userEmail = models.UserEmail(nextIntId(rnd), user.id, ACLUtils.nextAccessSalt(rnd), email, "Email")

            _ ← fromDBIO(persist.User.create(user))
            _ ← fromDBIO(persist.UserPhone.create(userPhone))
            _ ← fromDBIO(persist.UserEmail.create(userEmail))
          } yield ()
        case (None, Some(email)) ⇒
          for {
            _ ← fromDBIOBoolean(StatusCodes.NotAcceptable → DashboardError(s"User with email $email already exists"))(persist.UserEmail.exists(email).map(!_))
            rnd = ThreadLocalRandom.current()
            userEmail = models.UserEmail(nextIntId(rnd), user.id, ACLUtils.nextAccessSalt(rnd), email, "Email")

            _ ← fromDBIO(persist.User.create(user))
            _ ← fromDBIO(persist.UserEmail.create(userEmail))
          } yield ()
        case (Some(phone), None) ⇒
          for {
            _ ← fromDBIOBoolean(StatusCodes.NotAcceptable → DashboardError(s"User with phone $phone already exists"))(persist.UserPhone.exists(phone).map(!_))
            rnd = ThreadLocalRandom.current()
            userPhone = models.UserPhone(nextIntId(rnd), user.id, ACLUtils.nextAccessSalt(rnd), phone, "Mobile phone")

            _ ← fromDBIO(persist.User.create(user))
            _ ← fromDBIO(persist.UserPhone.create(userPhone))
          } yield ()
        case (None, None) ⇒ point(())
      }
    } yield StatusCodes.Created → CreatedUser(user.id)
    db.run(action.value)
  }

  def list(page: Int, perPage: Int)(implicit db: Database, ec: ExecutionContext): Future[Seq[CompleteUser]] = {
    val action = for {
      users ← persist.User.page(page, perPage)
      completeUsers ← DBIO.sequence(users map { user ⇒
        for {
          userPhones ← persist.UserPhone.findByUserId(user.id)
          userEmails ← persist.UserEmail.findByUserId(user.id)
        } yield CompleteUser(user, userPhones, userEmails)
      })
    } yield completeUsers
    db.run(action)
  }

  def update(id: Int, updateForm: UpdateForm)(implicit db: Database, ec: ExecutionContext): Future[StatusCode] = {
    val action = for {
      _ ← persist.User.setName(id, updateForm.userName)
    } yield StatusCodes.Accepted
    db.run(action)
  }

  private def createUser(userName: String): models.User = {
    val rnd = ThreadLocalRandom.current()
    models.User(
      id = nextIntId(rnd),
      accessSalt = ACLUtils.nextAccessSalt(rnd),
      name = userName,
      countryCode = "RU",
      sex = models.NoSex,
      state = models.UserState.Registered,
      createdAt = LocalDateTime.now(ZoneOffset.UTC)
    )
  }
}
