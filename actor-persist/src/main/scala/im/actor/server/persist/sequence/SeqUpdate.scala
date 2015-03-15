package im.actor.server.persist.sequence

import com.eaio.uuid.UUID
import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.models
import org.joda.time.DateTime

import scala.language.{ implicitConversions, postfixOps }

import slick.driver.PostgresDriver.api._

class SeqUpdateTable(tag: Tag) extends Table[models.sequence.SeqUpdate](tag, "seq_updates_ngen") {
  import RefColumnType._

  def authId = column[Long]("auth_id", O.PrimaryKey)
  def ref = column[models.sequence.Ref]("id")
  def date = column[DateTime]("date")
  def header = column[Int]("header")
  def serializedData = column[Array[Byte]]("serialized_data")

  def * = (authId, ref, date, header, serializedData) <> (
    models.sequence.SeqUpdate.apply _ tupled,
    models.sequence.SeqUpdate.unapply)
}

object SeqUpdate {
  val updates = TableQuery[SeqUpdateTable]
}
