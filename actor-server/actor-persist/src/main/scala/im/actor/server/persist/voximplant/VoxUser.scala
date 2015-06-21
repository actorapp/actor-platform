package im.actor.server.persist.voximplant

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class VoxUserTable(tag: Tag) extends Table[models.voximplant.VoxUser](tag, "vox_users") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def voxUserId = column[Long]("vox_user_id")
  def userName = column[String]("user_name")
  def displayName = column[String]("display_name")
  def salt = column[String]("salt")

  def * = (userId, voxUserId, userName, displayName, salt) <> (models.voximplant.VoxUser.tupled, models.voximplant.VoxUser.unapply)
}

object VoxUser {
  val users = TableQuery[VoxUserTable]

  def create(user: models.voximplant.VoxUser) =
    users += user

  def createOrReplace(user: models.voximplant.VoxUser) =
    users.insertOrUpdate(user)

  def findByUserId(userId: Int) =
    users.filter(_.userId === userId).result.headOption
}
