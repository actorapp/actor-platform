package im.actor.server.names

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import com.github.ghik.silencer.silent
import im.actor.server.db.DbExtension
import im.actor.server.persist.UserRepo
import im.actor.storage.SimpleStorage
import slick.dbio._

import scala.concurrent.Future

/**
 * Stores mapping "Normalized global name" -> "Global name owner(group/user)"
 * normalized global name: String
 * global name owner: im.actor.server.names.GlobalNameOwner
 */
private object GlobalNamesStorage extends SimpleStorage("global_names")

/**
 * Storage that keeps compatibility between
 * storing nicknames in `im.actor.server.persist.UserRepo`
 * and storing group and user names in new `im.actor.storage.SimpleStorage` storage
 */
final class GlobalNamesStorageKeyValueStorage(implicit system: ActorSystem) {
  import system.dispatcher

  private val (db, conn) = {
    val ext = DbExtension(system)
    (ext.db, ext.connector)
  }

  def getUserId(name: String): Future[Option[Int]] =
    getOwner(name) map (_.collect {
      case GlobalNameOwner(OwnerType.User, userId) ⇒ userId
    })

  /**
   * Search groups(id -> global name) by global name prefix
   * Looks only in GlobalNamesStorage
   */
  def groupIdsByPrefix(namePrefix: String): Future[IndexedSeq[(Int, String)]] = {
    conn.run(GlobalNamesStorage.getByPrefix(normalized(namePrefix))) map { searchResults ⇒
      searchResults flatMap {
        case (fullName, bytes) ⇒
          Some(GlobalNameOwner.parseFrom(bytes)) filter (_.ownerType.isGroup) map (o ⇒ o.ownerId → fullName)
      }
    }
  }

  /**
   * Search users(id -> global name) by global name prefix
   * Looks in both GlobalNamesStorage and UserRepo(compatibility mode)
   */
  def userIdsByPrefix(namePrefix: String): Future[IndexedSeq[(Int, String)]] = {
    val kvSearch = conn.run(GlobalNamesStorage.getByPrefix(normalized(namePrefix))) map { searchResults ⇒
      searchResults flatMap {
        case (fullName, bytes) ⇒
          Some(GlobalNameOwner.parseFrom(bytes)) filter (_.ownerType.isUser) map (o ⇒ o.ownerId → fullName)
      }
    }
    val compatSearch = db.run(UserRepo.findByNicknamePrefix(namePrefix): @silent) map { users ⇒
      users flatMap { user ⇒
        user.nickname map (user.id → _)
      }
    }
    for {
      kv ← kvSearch
      compat ← compatSearch
    } yield kv ++ compat
  }

  def getGroupId(name: String): Future[Option[Int]] =
    getOwner(name) map (_.collect {
      case GlobalNameOwner(OwnerType.Group, groupId) ⇒ groupId
    })

  /**
   * Compatible with storing nicknames in `im.actor.server.persist.UserRepo`
   */
  def exists(name: String): Future[Boolean] = {
    val existsInKV = conn.run(GlobalNamesStorage.get(normalized(name))) map (_.isDefined)

    existsInKV flatMap {
      case true  ⇒ FastFuture.successful(true)
      case false ⇒ db.run(UserRepo.nicknameExists(name): @silent)
    }
  }

  /**
   * `oldGlobalName` = None,            `newGlobalName` = Some("name") - insert new name
   * `oldGlobalName` = Some("oldName"), `newGlobalName` = Some("name") - update existing name
   * `oldGlobalName` = Some("oldName"), `newGlobalName` = None         - delete existing name
   * `oldGlobalName` = None,            `newGlobalName` = None         - does nothing
   */
  def updateOrRemove(oldGlobalName: Option[String], newGlobalName: Option[String], owner: GlobalNameOwner): Future[Unit] = {
    val deleteFu = (oldGlobalName map delete) getOrElse FastFuture.successful(())
    val upsertFu = (newGlobalName map (n ⇒ upsert(n, owner))) getOrElse FastFuture.successful(())
    for {
      _ ← deleteFu
      _ ← upsertFu
    } yield ()
  }

  /**
   * Compatible with storing nicknames in `im.actor.server.persist.UserRepo`
   */
  private def getOwner(name: String): Future[Option[GlobalNameOwner]] = {
    val optOwner = conn.run(
      GlobalNamesStorage.get(normalized(name))
    ) map { _ map GlobalNameOwner.parseFrom }

    optOwner flatMap {
      case o @ Some(_) ⇒ FastFuture.successful(o)
      case None        ⇒ db.run(UserRepo.findByNickname(name): @silent) map (_.map(u ⇒ GlobalNameOwner(OwnerType.User, u.id)))
    }
  }

  private def upsert(name: String, owner: GlobalNameOwner): Future[Unit] =
    conn.run(
      GlobalNamesStorage.upsert(normalized(name), owner.toByteArray)
    ) map (_ ⇒ ())

  /**
   * Compatible with storing nicknames in `im.actor.server.persist.UserRepo`
   */
  private def delete(name: String): Future[Unit] = {
    val kvDelete = conn.run(GlobalNamesStorage.delete(normalized(name)))

    kvDelete flatMap { count ⇒
      if (count == 0) {
        db.run {
          for {
            optUser ← UserRepo.findByNickname(name): @silent
            _ ← optUser match {
              case Some(u) ⇒ UserRepo.setNickname(u.id, None): @silent
              case None    ⇒ DBIO.successful(0)
            }
          } yield ()
        }
      } else {
        FastFuture.successful(())
      }
    }
  }

  private def normalized(name: String) = name.toLowerCase

}
