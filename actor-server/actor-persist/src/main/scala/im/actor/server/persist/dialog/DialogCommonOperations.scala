package im.actor.server.persist.dialog

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.{ DialogCommon, Peer }
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

object DialogCommonRepo {
  val dialogCommon = TableQuery[DialogCommonTable]

  private def byPK(dialogId: Rep[String]) = {
    dialogCommon.filter(_.dialogId === dialogId)
  }

  val byPKC = Compiled(byPK _)
}

trait DialogCommonOperations extends DialogId {
  import DialogCommonRepo._

  def findCommon(userId: Option[Int], peer: Peer): DBIO[Option[DialogCommon]] =
    byPKC.applied(getDialogId(userId, peer)).result.headOption

  def commonExists(dialogId: String) =
    byPKC.applied(dialogId).exists.result

  def updateLastMessageDatePrivate(userId: Int, peer: Peer, lastMessageDate: DateTime)(implicit ec: ExecutionContext) = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(_.lastMessageDate).update(lastMessageDate)
  }

  def updateLastMessageDateGroup(peer: Peer, lastMessageDate: DateTime)(implicit ec: ExecutionContext) = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer))
      .map(_.lastMessageDate)
      .update(lastMessageDate)
  }

  def updateLastReceivedAtPrivate(userId: Int, peer: Peer, lastReceivedAt: DateTime)(implicit ec: ExecutionContext) = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(_.lastReceivedAt).update(lastReceivedAt)
  }

  def updateLastReceivedAtGroup(peer: Peer, lastReceivedAt: DateTime)(implicit ec: ExecutionContext) = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer)).map(_.lastReceivedAt).update(lastReceivedAt)
  }

  def updateLastReadAtPrivate(userId: Int, peer: Peer, lastReadAt: DateTime)(implicit ec: ExecutionContext) = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(_.lastReadAt).update(lastReadAt)
  }

  def updateLastReadAtGroup(peer: Peer, lastReadAt: DateTime)(implicit ec: ExecutionContext) = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer)).map(_.lastReadAt).update(lastReadAt)
  }

  def requirePrivate(peer: Peer) = require(peer.`type`.isPrivate, "It should be private peer")

  def requireGroup(peer: Peer) = require(peer.`type`.isGroup, "It should be group peer")
}
