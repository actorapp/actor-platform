package im.actor.server.persist.presences

import org.joda.time.DateTime
import slick.dbio.Effect.{ Read, Write }
import slick.profile.{ SqlAction, FixedSqlAction }

import im.actor.server.model.presences.{ UserPresence ⇒ UserPresenceModel }
import slick.driver.PostgresDriver.api._
import com.github.tototoshi.slick.PostgresJodaSupport._

final class UserPresenceTable(tag: Tag) extends Table[UserPresenceModel](tag, "user_presences") {
  def userId = column[Int]("user_id", O.PrimaryKey)

  def authId = column[Long]("auth_id", O.PrimaryKey)

  def lastSeenAt = column[Option[DateTime]]("last_seen_at")

  def * = (userId, authId, lastSeenAt) <> (UserPresenceModel.tupled, UserPresenceModel.unapply)
}

object UserPresenceRepo {
  val presences = TableQuery[UserPresenceTable]

  def createOrUpdate(userPresence: UserPresenceModel): FixedSqlAction[Int, NoStream, Write] =
    presences.insertOrUpdate(userPresence)

  private def last(userId: Rep[Int]) =
    presences
      .filter(_.userId === userId)
      .distinctOn(_.userId)
      .sortBy(_.lastSeenAt.desc)
      .take(1)

  private val lastC = Compiled(last _)

  def find(userId: Int): SqlAction[Option[UserPresenceModel], NoStream, Read] = lastC(userId).result.headOption
}
