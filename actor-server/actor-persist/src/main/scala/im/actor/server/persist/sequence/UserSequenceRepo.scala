package im.actor.server.persist.sequence

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.{ UpdateMapping, SeqUpdate }

private[sequence] final class UserSequenceTable(tag: Tag) extends Table[SeqUpdate](tag, "user_sequence") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def seq = column[Int]("seq", O.PrimaryKey)
  def timestamp = column[Long]("timestamp")
  def mapping = column[Array[Byte]]("mapping")

  def * = (userId, seq, timestamp, mapping) <> (applySeqUpdate.tupled, unapplySeqUpdate)

  private def applySeqUpdate: (Int, Int, Long, Array[Byte]) ⇒ SeqUpdate = {
    (userId, seq, timestamp, mapping) ⇒ SeqUpdate(userId, seq, timestamp, Some(UpdateMapping.parseFrom(mapping)))
  }

  private def unapplySeqUpdate: SeqUpdate ⇒ Option[(Int, Int, Long, Array[Byte])] = {
    seqUpdate ⇒ Some((seqUpdate.userId, seqUpdate.seq, seqUpdate.timestamp, seqUpdate.mapping.map(_.toByteArray).getOrElse(Array.empty)))
  }
}

object UserSequenceRepo {
  private val sequence = TableQuery[UserSequenceTable]

  private val sequenceC = Compiled(sequence)

  private def byUser(userId: Rep[Int]) = sequence.filter(_.userId === userId)

  private def byUserAfterSeq(userId: Rep[Int], seq: Rep[Int], limit: ConstColumn[Long]) = byUser(userId).filter(_.seq > seq).sortBy(_.seq.asc).take(limit)

  private val userSequence = Compiled(byUserAfterSeq _)

  private val userSequenceSeq = Compiled {
    byUser _ andThen (_.sortBy(_.seq.desc).map(_.seq).take(1))
  }

  def create(updates: Seq[SeqUpdate]) = (sequenceC ++= updates).transactionally

  def create(update: SeqUpdate) = sequenceC += update

  def fetchSeq(userId: Int) = userSequenceSeq(userId).result.headOption

  def fetchAfterSeq(userId: Int, seq: Int, limit: Long) =
    userSequence((userId, seq, limit)).result
}
