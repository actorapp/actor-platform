package im.actor.server.persist.messaging

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.{ DialogId, ReactionEvent }

final class ReactionEventTable(tag: Tag) extends Table[ReactionEvent](tag, "reactions") {
  def dialogType = column[Int]("dialog_type", O.PrimaryKey)

  def dialogId = column[String]("dialog_id", O.PrimaryKey)

  def randomId = column[Long]("random_id", O.PrimaryKey)

  def code = column[String]("code", O.PrimaryKey)

  def userId = column[Int]("user_id", O.PrimaryKey)

  def * = (dialogType, dialogId, randomId, code, userId) <> ((applyReaction _).tupled, unapplyReaction)

  private def applyReaction(
    dialogType: Int,
    dialogId:   String,
    randomId:   Long,
    code:       String,
    userId:     Int
  ) =
    ReactionEvent(
      dialogType = dialogType,
      dialogId = dialogId,
      randomId = randomId,
      code = code,
      userId = userId
    )

  private def unapplyReaction(reaction: ReactionEvent) = reaction match {
    case ReactionEvent(dialogType, dialogId, randomId, code, userId) ⇒
      Some((dialogType, dialogId, randomId, code, userId))
    case _ ⇒ throw new RuntimeException("Reaction with an empty peer")
  }

}

object ReactionEventRepo {
  val reactions = TableQuery[ReactionEventTable]

  def byDialogId(typ: Rep[Int], id: Rep[String]) = reactions filter (r ⇒ r.dialogType === typ && r.dialogId === id)

  val byRandomId = Compiled { (dialogType: Rep[Int], dialogId: Rep[String], randomId: Rep[Long]) ⇒
    byDialogId(dialogType, dialogId) filter (_.randomId === randomId)
  }

  val byPK = Compiled { (peerType: Rep[Int], peerId: Rep[String], randomId: Rep[Long], code: Rep[String], userId: Rep[Int]) ⇒
    byDialogId(peerType, peerId).filter(r ⇒ r.randomId === randomId && r.code === code && r.userId === userId)
  }

  def create(dialogId: DialogId, randomId: Long, code: String, userId: Int) =
    (reactions += ReactionEvent(dialogId.typ.value, dialogId.id, randomId, code, userId)).asTry

  def delete(dialogId: DialogId, randomId: Long, code: String, userId: Int) =
    byPK((dialogId.typ.value, dialogId.id, randomId, code, userId)).delete

  def fetch(dialogId: DialogId, randomId: Long) =
    byRandomId((dialogId.typ.value, dialogId.id, randomId)).result

  def fetch(dialogId: DialogId, randomIds: Set[Long]) =
    byDialogId(dialogId.typ.value, dialogId.id).filter(_.randomId.inSet(randomIds)).result
}