package im.actor.server.persist

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.model.GroupInviteToken
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

final class GroupInviteTokenTable(tag: Tag) extends Table[GroupInviteToken](tag, "group_invite_tokens") {
  def groupId = column[Int]("group_id", O.PrimaryKey)
  def creatorId = column[Int]("creator_id", O.PrimaryKey)
  def token = column[String]("token", O.PrimaryKey)
  def revokedAt = column[Option[DateTime]]("revoked_at")

  def * = (groupId, creatorId, token, revokedAt) <> (GroupInviteToken.tupled, GroupInviteToken.unapply)
}

//TODO: replace with key-value
object GroupInviteTokenRepo {
  private val groupInviteTokens = TableQuery[GroupInviteTokenTable]
  private val activeTokens = groupInviteTokens.filter(_.revokedAt.isEmpty)

  @deprecated("use key-value style", "2016-07-07")
  def create(token: GroupInviteToken) =
    groupInviteTokens += token

  @deprecated("use key-value style", "2016-07-07")
  def find(groupId: Int, userId: Int) =
    activeTokens.filter(t ⇒ t.groupId === groupId && t.creatorId === userId).result

  @deprecated("use key-value style", "2016-07-07")
  def findByToken(token: String) =
    activeTokens.filter(_.token === token).result.headOption

  @deprecated("use key-value style", "2016-07-07")
  def revoke(groupId: Int, userId: Int) =
    activeTokens.filter(t ⇒ t.groupId === groupId && t.creatorId === userId).map(_.revokedAt).update(Some(DateTime.now))

}
