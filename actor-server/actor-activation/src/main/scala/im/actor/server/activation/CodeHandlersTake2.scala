package im.actor.server.activation

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.server.activation.Activation.{EmailCode, SmsCode, Code}
import im.actor.server.db.DbExtension
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.persist.{UserRepo, UserPhoneRepo}

import scala.concurrent.Future

//this is attempt to avoid blocking calls in code matching function
trait CodeHandler2 {
  def isDefinedAt(code: Code): Future[Boolean]
  def apply(x: Code): Future[CodeFailure Xor Unit]
  def orElse(that: CodeHandler2): CodeHandler2 = new OrElse(this, that)

  class OrElse(c1: CodeHandler2, c2: CodeHandler2) extends CodeHandler2 {
    override def isDefinedAt(code: Code): Future[Boolean] =
      for {
        isDefinedC1 <- c1.isDefinedAt(code)
        isDefinedC2 <- c2.isDefinedAt(code)
      } yield isDefinedC1 || isDefinedC2

    override def apply(c: Code): Future[Xor[CodeFailure, Unit]] =
      for {
        isDefined <- c1.isDefinedAt(c)
        result <- if (isDefined) c1.apply(c) else c2.apply(c)
      } yield result

    override def orElse(that: CodeHandler2) = {
      new OrElse(c1, c2 orElse that)
    }
  }
}

class BotHandler(config: BotConfig)(implicit system: ActorSystem) extends CodeHandler2 {
  private val db = DbExtension(system).db

  override def isDefinedAt(code: Code): Future[Boolean] = code match {
    case SmsCode(phone, _) => isOnline(phone)
    case EmailCode(email, _) =>   isOnline(email)
  }

  override def apply(x: Code): Future[Xor[CodeFailure, Unit]] = ??? //same as Take1

  private def isOnline(phone: Long) = db.run {
    for {
      optPhone <- UserPhoneRepo.findByPhoneNumber(phone).headOption
      exists <- optPhone map { phone => UserRepo.find(phone.userId) map (_.isDefined) } getOrElse DBIO.successful(false)
    //check that user online
    } yield exists
  }

  private def isOnline(email: String): Future[Boolean] = ???
}

object Main {

  //how to chain them?
  val example1 = new BotHandler(null)
  val example2 = new BotHandler(null)

  val resulting = example1 orElse example2

  resulting(SmsCode(123L, "123123"))

}
