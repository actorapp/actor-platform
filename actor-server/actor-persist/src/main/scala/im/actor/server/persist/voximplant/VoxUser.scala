package im.actor.server.persist.voximplant

import im.actor.server.model.voximplant.{ VoxUser ⇒ VoxUserModel }
import slick.driver.PostgresDriver.api._

class VoxUserTable(tag: Tag) extends Table[VoxUserModel](tag, "vox_users") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def voxUserId = column[Long]("vox_user_id")
  def userName = column[String]("user_name")
  def displayName = column[String]("display_name")
  def salt = column[String]("salt")

  def * = (userId, voxUserId, userName, displayName, salt) <> (VoxUserModel.tupled, VoxUserModel.unapply)
}

object VoxUser {
  val users = TableQuery[VoxUserTable]

  def create(user: VoxUserModel) =
    users += user

  def createOrReplace(user: VoxUserModel) =
    users.insertOrUpdate(user)

  def findByUserId(userId: Int) =
    users.filter(_.userId === userId).result.headOption
}
