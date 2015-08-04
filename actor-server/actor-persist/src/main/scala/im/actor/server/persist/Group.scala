package im.actor.server.persist

import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.server.models

class FullGroupTable(tag: Tag) extends Table[models.FullGroup](tag, "groups") {
  def id = column[Int]("id", O.PrimaryKey)

  def creatorUserId = column[Int]("creator_user_id")

  def accessHash = column[Long]("access_hash")

  def title = column[String]("title")

  def isPublic = column[Boolean]("is_public")

  def createdAt = column[DateTime]("created_at")

  def about = column[Option[String]]("about")

  def topic = column[Option[String]]("topic")

  def titleChangerUserId = column[Int]("title_changer_user_id")

  def titleChangedAt = column[DateTime]("title_changed_at")

  def titleChangeRandomId = column[Long]("title_change_random_id")

  def avatarChangerUserId = column[Int]("avatar_changer_user_id")

  def avatarChangedAt = column[DateTime]("avatar_changed_at")

  def avatarChangeRandomId = column[Long]("avatar_change_random_id")

  def * =
    (
      id,
      creatorUserId,
      accessHash,
      title,
      isPublic,
      createdAt,
      about,
      topic,
      titleChangerUserId,
      titleChangedAt,
      titleChangeRandomId,
      avatarChangerUserId,
      avatarChangedAt,
      avatarChangeRandomId
    ) <> (models.FullGroup.tupled, models.FullGroup.unapply)

  def asGroup = (id, creatorUserId, accessHash, title, isPublic, createdAt, about, topic) <> ((models.Group.apply _).tupled, models.Group.unapply)
}

object Group {
  val groups = TableQuery[FullGroupTable]
  val groupsC = Compiled(groups)

  def byId(id: Rep[Int]) = groups filter (_.id === id)
  def groupById(id: Rep[Int]) = byId(id) map (_.asGroup)
  def titleById(id: Rep[Int]) = byId(id) map (_.title)

  val byIdC = Compiled(byId _)
  val groupByIdC = Compiled(groupById _)
  val titleByIdC = Compiled(titleById _)

  val allIds = groups.map(_.id).result

  def create(group: models.Group, randomId: Long) = {
    groups += models.FullGroup(
      id = group.id,
      creatorUserId = group.creatorUserId,
      accessHash = group.accessHash,
      title = group.title,
      isPublic = group.isPublic,
      createdAt = group.createdAt,
      about = group.about,
      topic = group.topic,
      titleChangerUserId = group.creatorUserId,
      titleChangedAt = group.createdAt,
      titleChangeRandomId = randomId,
      avatarChangerUserId = group.creatorUserId,
      avatarChangedAt = group.createdAt,
      avatarChangeRandomId = randomId
    )
  }

  def findPublic =
    groups.filter(_.isPublic === true).map(_.asGroup).result

  def find(id: Int) =
    groupByIdC(id).result.headOption

  def findTitle(id: Int) =
    titleByIdC(id).result.headOption

  def findFull(id: Int) =
    byIdC(id).result.headOption

  def updateTitle(id: Int, title: String, changerUserId: Int, randomId: Long, date: DateTime) =
    byIdC.applied(id)
      .map(g ⇒ (g.title, g.titleChangerUserId, g.titleChangedAt, g.titleChangeRandomId))
      .update((title, changerUserId, date, randomId))

  def updateTopic(id: Int, topic: Option[String]) =
    byIdC.applied(id).map(_.topic).update(topic)

  def updateAbout(id: Int, about: Option[String]) =
    byIdC.applied(id).map(_.about).update(about)

  def makePublic(id: Int) = byIdC.applied(id).map(_.isPublic).update(true)
}
