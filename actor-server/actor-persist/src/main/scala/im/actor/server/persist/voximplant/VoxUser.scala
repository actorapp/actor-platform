package im.actor.server.persist.voximplant

import slick.driver.PostgresDriver.api._

import im.actor.server.model

class VoxUserTable(tag: Tag) extends Table[model.voximplant.VoxUser](tag, "vox_users") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def voxUserId = column[Long]("vox_user_id")
  def userName = column[String]("user_name")
  def displayName = column[String]("display_name")
  def salt = column[String]("salt")

  def * = (userId, voxUserId, userName, displayName, salt) <> (model.voximplant.VoxUser.tupled, model.voximplant.VoxUser.unapply)
}

object VoxUser {
  val users = TableQuery[VoxUserTable]

  def create(user: model.voximplant.VoxUser) =
    users += user

  def createOrReplace(user: model.voximplant.VoxUser) =
    users.insertOrUpdate(user)

  def findByUserId(userId: Int) =
    users.filter(_.userId === userId).result.headOption
}
