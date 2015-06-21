package im.actor.server.persist.llectro

import scala.concurrent.ExecutionContext

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class UserInterestTable(tag: Tag) extends Table[models.llectro.UserInterest](tag, "llectro_users_interests") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def interestId = column[Int]("interest_id", O.PrimaryKey)

  def * = (userId, interestId) <> (models.llectro.UserInterest.tupled, models.llectro.UserInterest.unapply)
}

object UserInterest {
  val interests = TableQuery[UserInterestTable]

  def createIfNotExists(userId: Int, interestId: Int)(implicit ec: ExecutionContext) =
    interests.filter(i ⇒ i.userId === userId && i.interestId === interestId).result.headOption flatMap {
      case Some(_) ⇒ DBIO.successful(0)
      case None    ⇒ interests += models.llectro.UserInterest(userId, interestId)
    }

  def findIdsByUserId(userId: Int) =
    interests.filter(_.userId === userId).map(_.interestId).result

  def delete(userId: Int, interestId: Int) =
    interests.filter(ui ⇒ ui.interestId === interestId && ui.userId === userId).delete
}
