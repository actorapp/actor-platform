package im.actor.server.persist

import java.time.Instant

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.{ FullGroup, Group }

final class FullGroupTable(tag: Tag) extends Table[FullGroup](tag, "groups") {
  def id = column[Int]("id", O.PrimaryKey)

  def creatorUserId = column[Int]("creator_user_id")

  def accessHash = column[Long]("access_hash")

  def title = column[String]("title")

  def isPublic = column[Boolean]("is_public")

  def createdAt = column[Instant]("created_at")

  def about = column[Option[String]]("about")

  def topic = column[Option[String]]("topic")

  def titleChangerUserId = column[Int]("title_changer_user_id")

  def titleChangedAt = column[Instant]("title_changed_at")

  def titleChangeRandomId = column[Long]("title_change_random_id")

  def avatarChangerUserId = column[Int]("avatar_changer_user_id")

  def avatarChangedAt = column[Instant]("avatar_changed_at")

  def avatarChangeRandomId = column[Long]("avatar_change_random_id")

  def isHidden = column[Boolean]("is_hidden")

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
      avatarChangeRandomId,
      isHidden
    ) <> (FullGroup.tupled, FullGroup.unapply)

  def asGroup = (id, creatorUserId, accessHash, title, isPublic, createdAt, about, topic) <> ((Group.apply _).tupled, Group.unapply)
}

object GroupRepo {
  val groups = TableQuery[FullGroupTable]
  val groupsC = Compiled(groups)

  def byId(id: Rep[Int]) = groups filter (_.id === id)
  def groupById(id: Rep[Int]) = byId(id) map (_.asGroup)
  def titleById(id: Rep[Int]) = byId(id) map (_.title)

  val byIdC = Compiled(byId _)
  val groupByIdC = Compiled(groupById _)
  val titleByIdC = Compiled(titleById _)

  val allIds = groups.map(_.id)

  @deprecated("Duplication of event-sourced groups logic", "2016-06-05")
  def create(group: Group, randomId: Long, isHidden: Boolean) = {
    groups += FullGroup(
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
      avatarChangeRandomId = randomId,
      isHidden = isHidden
    )
  }

  @deprecated("Public groups are deprecated in Group V2 API", "2016-06-05")
  def findPublic =
    groups.filter(_.isPublic === true).map(_.asGroup).result

  @deprecated("Replace with some sort of key-value maybe?", "2016-06-05")
  def findAllIds = allIds.result

  @deprecated("Remove, only used in tests", "2016-06-05")
  def find(id: Int) =
    groupByIdC(id).result.headOption

  @deprecated("Compatibility with old group API", "2016-06-05")
  def findFull(id: Int) =
    byIdC(id).result.headOption

  @deprecated("Duplication of event-sourced groups logic", "2016-06-05")
  def updateTitle(id: Int, title: String, changerUserId: Int, randomId: Long, date: Instant) =
    byIdC.applied(id)
      .map(g ⇒ (g.title, g.titleChangerUserId, g.titleChangedAt, g.titleChangeRandomId))
      .update((title, changerUserId, date, randomId))

  @deprecated("Duplication of event-sourced groups logic", "2016-06-05")
  def updateTopic(id: Int, topic: Option[String]) =
    byIdC.applied(id).map(_.topic).update(topic)

  @deprecated("Duplication of event-sourced groups logic", "2016-06-05")
  def updateAbout(id: Int, about: Option[String]) =
    byIdC.applied(id).map(_.about).update(about)

  @deprecated("Migrations only", "2016-06-05")
  def makeHidden(id: Int) = byIdC.applied(id).map(_.isHidden).update(true)
}
