package im.actor.server.persist

import java.time.{ ZoneOffset, LocalDateTime }

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.{ Sex, UserState, User }
import im.actor.util.misc.PhoneNumberUtils

import scala.concurrent.ExecutionContext

final class UserTable(tag: Tag) extends Table[User](tag, "users") {
  import SexColumnType._
  import UserStateColumnType._

  def id = column[Int]("id", O.PrimaryKey)
  def accessSalt = column[String]("access_salt")
  def name = column[String]("name")
  def countryCode = column[String]("country_code")
  def sex = column[Sex]("sex")
  def state = column[UserState]("state")
  def createdAt = column[LocalDateTime]("created_at")
  def nickname = column[Option[String]]("nickname")
  def about = column[Option[String]]("about")
  def deletedAt = column[Option[LocalDateTime]]("deleted_at")
  def isBot = column[Boolean]("is_bot")
  def external = column[Option[String]]("external")

  def * = (id, accessSalt, name, countryCode, sex, state, createdAt, nickname, about, deletedAt, isBot, external) <> (User.tupled, User.unapply)
}

object UserRepo {
  val users = TableQuery[UserTable]

  def byId(id: Rep[Int]) = users filter (_.id === id)
  def nameById(id: Rep[Int]) = byId(id) map (_.name)

  val byIdC = Compiled(byId _)
  val nameByIdC = Compiled(nameById _)

  def byNickname(nickname: Rep[String]) = users filter (_.nickname.toLowerCase === nickname.toLowerCase)
  def idsByNickname(nickname: Rep[String]) = byNickname(nickname).map(_.id)

  val byNicknameC = Compiled(byNickname _)
  val idsByNicknameC = Compiled(idsByNickname _)

  def byPhone(phone: Rep[Long]) = (for {
    phones ← UserPhoneRepo.phones.filter(_.number === phone)
    users ← users if users.id === phones.userId
  } yield users).take(1)
  def idByPhone(phone: Rep[Long]) = byPhone(phone) map (_.id)

  val idByPhoneC = Compiled(idByPhone _)

  def idsByEmail(email: Rep[String]) =
    for {
      emails ← UserEmailRepo.emails filter (_.email.toLowerCase === email.toLowerCase)
      users ← users filter (_.id === emails.userId) map (_.id)
    } yield users
  val idsByEmailC = Compiled(idsByEmail _)

  val activeHumanUsers =
    users.filter(u ⇒ u.deletedAt.isEmpty && !u.isBot)

  def create(user: User) =
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

  def all = users.result

  def fetchPeople = activeHumanUsers.result

  def find(id: Int) =
    byIdC(id).result.headOption

  def findName(id: Int) =
    nameById(id).result.headOption

  // TODO: #perf will it create prepared statement for each ids length?
  def findSalts(ids: Set[Int]) =
    users.filter(_.id inSet ids).map(u ⇒ (u.id, u.accessSalt)).result

  def findByNickname(query: String) = {
    val nickname =
      if (query.startsWith("@")) query.drop(1) else query
    byNicknameC(nickname).result.headOption
  }

  def findIdsByNickname(nickname: String) =
    idsByNicknameC(nickname).result.headOption

  def findIdsByEmail(email: String) =
    idsByEmailC(email).result.headOption

  def findIds(query: String)(implicit ec: ExecutionContext) =
    for {
      e ← idsByEmailC(query).result
      n ← idsByNicknameC(query).result
      p ← PhoneNumberUtils.normalizeStr(query)
        .headOption
        .map(idByPhoneC(_).result)
        .getOrElse(DBIO.successful(Nil))
    } yield e ++ n ++ p

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

  def isDeleted(userId: Int): DBIO[Boolean] =
    byIdC.applied(userId).filter(_.deletedAt.nonEmpty).exists.result
}
