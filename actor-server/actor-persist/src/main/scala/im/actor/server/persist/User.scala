package im.actor.server.persist

import java.time.{ ZoneOffset, LocalDateTime }

import im.actor.server.db.ActorPostgresDriver.api._

import im.actor.server.models

class UserTable(tag: Tag) extends Table[models.User](tag, "users") {
  import SexColumnType._
  import UserStateColumnType._

  def id = column[Int]("id", O.PrimaryKey)
  def accessSalt = column[String]("access_salt")
  def name = column[String]("name")
  def countryCode = column[String]("country_code")
  def sex = column[models.Sex]("sex")
  def state = column[models.UserState]("state")
  def createdAt = column[LocalDateTime]("created_at")
  def nickname = column[Option[String]]("nickname")
  def about = column[Option[String]]("about")
  def deletedAt = column[Option[LocalDateTime]]("deleted_at")
  def isBot = column[Boolean]("is_bot")
  def external = column[Option[String]]("external")

  def * = (id, accessSalt, name, countryCode, sex, state, createdAt, nickname, about, deletedAt, isBot, external) <> (models.User.tupled, models.User.unapply)
}

object User {
  val users = TableQuery[UserTable]

  def byId(id: Rep[Int]) = users filter (_.id === id)
  def nameById(id: Rep[Int]) = byId(id) map (_.name)

  val byIdC = Compiled(byId _)
  val nameByIdC = Compiled(nameById _)

  def byNickname(nickname: Rep[String]) = users filter (_.nickname === nickname)
  val byNicknameC = Compiled(byNickname _)

  val activeHumanUsers =
    users.filter(u ⇒ u.deletedAt.isEmpty && !u.isBot)

  def create(user: models.User) =
    users += user

  def setCountryCode(userId: Int, countryCode: String) =
    users.filter(_.id === userId).map(_.countryCode).update(countryCode)

  def setDeletedAt(userId: Int) =
    users.filter(_.id === userId).
      map(_.deletedAt).
      update(Some(LocalDateTime.now(ZoneOffset.UTC)))

  def setName(userId: Int, name: String) =
    users.filter(_.id === userId).map(_.name).update(name)

  def allIds = users.map(_.id).result

  def find(id: Int) =
    byIdC(id).result

  def findName(id: Int) =
    nameById(id).result.headOption

  // TODO: #perf will it create prepared statement for each ids length?
  def findSalts(ids: Set[Int]) =
    users.filter(_.id inSet ids).map(u ⇒ (u.id, u.accessSalt)).result

  def findByNickname(nickname: String) =
    byNicknameC(nickname).result.headOption

  def setNickname(userId: Int, nickname: Option[String]) =
    byId(userId).map(_.nickname).update(nickname)

  def setAbout(userId: Int, about: Option[String]) =
    byId(userId).map(_.about).update(about)

  def nicknameExists(nickname: String) =
    users.filter(_.nickname.toLowerCase === nickname.toLowerCase).exists.result

  def findByIds(ids: Set[Int]) =
    users.filter(_.id inSet ids).result

  def findByIdsPaged(ids: Set[Int], number: Int, size: Int) = {
    val offset = (number - 1) * size
    users.
      filter(_.id inSet ids).
      sortBy(_.name).
      drop(offset).
      take(size).
      result
  }

  def activeUsersIds = activeHumanUsers.map(_.id).result

  def page(number: Int, size: Int) = {
    val offset = (number - 1) * size
    activeHumanUsers.
      sortBy(_.name).
      drop(offset).
      take(size)
  }
}
