package im.actor.server.persist.ilectro

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class UserInterestTable(tag: Tag) extends Table[models.ilectro.UserInterest](tag, "users_interests") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def interestId = column[Int]("interest_id", O.PrimaryKey)

  def * = (userId, interestId) <> (models.ilectro.UserInterest.tupled, models.ilectro.UserInterest.unapply)
}

object UserInterest {
  val usersInterests = TableQuery[UserInterestTable]
}
