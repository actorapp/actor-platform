package im.actor.server.persist.sequence

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class SeqUpdateTable(tag: Tag) extends Table[models.sequence.SeqUpdate](tag, "seq_updates_ngen") {
  def authId = column[Long]("auth_id", O.PrimaryKey)

  def timestamp = column[Long]("timestamp")

  def seq = column[Int]("seq")

  def header = column[Int]("header")

  def serializedData = column[Array[Byte]]("serialized_data")

  def * = (authId, timestamp, seq, header, serializedData) <>(models.sequence.SeqUpdate.tupled, models.sequence.SeqUpdate.unapply)
}

object SeqUpdate {
  val updates = TableQuery[SeqUpdateTable]

  def create(update: models.sequence.SeqUpdate) = {
    updates += update
  }

  def createBulk(newUpdates: Seq[models.sequence.SeqUpdate]) = {
    updates ++= newUpdates
  }

  def find(authId: Long) =
    updates.filter(_.authId === authId).sortBy(_.timestamp.desc).result
}
