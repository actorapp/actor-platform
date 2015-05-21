package im.actor.server.persist

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class UserInterestTable(tag: Tag) extends Table[models.UserInterest](tag, "users_interests") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def interestId = column[Int]("interest_id", O.PrimaryKey)

  def * = (userId, interestId) <> (models.UserInterest.tupled, models.UserInterest.unapply)
}

object UserInterest {
  val usersInterests = TableQuery[UserInterestTable]
}
