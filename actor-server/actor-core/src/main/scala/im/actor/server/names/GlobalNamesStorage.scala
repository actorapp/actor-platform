package im.actor.server.names

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.server.db.DbExtension
import im.actor.server.persist.UserRepo
import im.actor.storage.SimpleStorage
import slick.dbio._

import scala.concurrent.Future

/**
 * Stores mapping "Global name" -> "Global name owner(group/user)"
 * global name: String
 * global name owner: GlobalNameOwner
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

  def getUserOwnerId(name: String): Future[Option[Int]] =
    getOwner(name) map (_.collect {
      case GlobalNameOwner(OwnerType.User, userId) ⇒ userId
    })

  def getGroupOwnerId(name: String): Future[Option[Int]] =
    getOwner(name) map (_.collect {
      case GlobalNameOwner(OwnerType.Group, groupId) ⇒ groupId
    })

  /**
   * Compatible with storing nicknames in `im.actor.server.persist.UserRepo`
   */
  def exists(name: String): Future[Boolean] = {
    val existsInKV = conn.run(GlobalNamesStorage.get(name)) map (_.isDefined)

    existsInKV flatMap {
      case true  ⇒ FastFuture.successful(true)
      case false ⇒ db.run(UserRepo.nicknameExists(name))
    }
  }

  /**
   * `oldGlobalName` = None,            `newGlobalName` = Some("name") - insert new name
   * `oldGlobalName` = Some("oldName"), `newGlobalName` = Some("name") - update existing name
   * `oldGlobalName` = Some("oldName"), `newGlobalName` = None         - delete existing name
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
      GlobalNamesStorage.get(name)
    ) map { optBytes ⇒
        optBytes map GlobalNameOwner.parseFrom
      }
    optOwner flatMap {
      case o @ Some(_) ⇒ FastFuture.successful(o)
      case None        ⇒ db.run(UserRepo.findByNickname(name)) map (_.map(u ⇒ GlobalNameOwner(OwnerType.User, u.id)))
    }
  }

  private def upsert(name: String, owner: GlobalNameOwner): Future[Unit] =
    conn.run(
      GlobalNamesStorage.upsert(name, owner.toByteArray)
    ) map (_ ⇒ ())

  /**
   * Compatible with storing nicknames in `im.actor.server.persist.UserRepo`
   */
  private def delete(name: String): Future[Unit] = {
    val kvDelete = conn.run(GlobalNamesStorage.delete(name))

    kvDelete flatMap { count ⇒
      if (count > 0) {
        db.run {
          for {
            optUser ← UserRepo.findByNickname(name)
            _ ← optUser match {
              case Some(u) ⇒ UserRepo.setNickname(u.id, None)
              case None    ⇒ DBIO.successful(0)
            }
          } yield ()
        }
      } else {
        FastFuture.successful(())
      }
    }
  }
}
