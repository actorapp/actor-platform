package im.actor.server.persist.sequence

import com.eaio.uuid.UUID
import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.server.models

class SeqUpdateTable(tag: Tag) extends Table[models.sequence.SeqUpdate](tag, "seq_updates_ngen") {
  import RefColumnType._

  def authId = column[Long]("auth_id", O.PrimaryKey)
  def ref = column[models.sequence.Ref]("ref")
  def seq = column[Int]("seq")
  def date = column[DateTime]("date")
  def header = column[Int]("header")
  def serializedData = column[Array[Byte]]("serialized_data")

  def * = (authId, ref, seq, date, header, serializedData) <> (
    (models.sequence.SeqUpdate.apply _: (Long, models.sequence.Ref, Int, DateTime, Int, Array[Byte]) => models.sequence.SeqUpdate).tupled,
    models.sequence.SeqUpdate.unapply)
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
    updates.filter(_.authId === authId).sortBy(_.seq.desc).result
}
