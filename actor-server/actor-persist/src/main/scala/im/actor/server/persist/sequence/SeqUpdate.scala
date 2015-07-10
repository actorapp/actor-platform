package im.actor.server.persist.sequence

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class SeqUpdateTable(tag: Tag) extends Table[models.sequence.SeqUpdate](tag, "seq_updates_ngen") {
  def authId = column[Long]("auth_id", O.PrimaryKey)

  def timestamp = column[Long]("timestamp")

  def seq = column[Int]("seq")

  def header = column[Int]("header")

  def serializedData = column[Array[Byte]]("serialized_data")

  def userIds = column[String]("user_ids_str")

  def groupIds = column[String]("group_ids_str")

  def * = (authId, timestamp, seq, header, serializedData, userIds, groupIds) <> ((toModel _).tupled, fromModel)

  private def toModel(authId: Long, timestamp: Long, seq: Int, header: Int, serializedData: Array[Byte], userIdsStr: String, groupIdsStr: String): models.sequence.SeqUpdate = {
    models.sequence.SeqUpdate(authId, timestamp, seq, header, serializedData, toIntSet(userIdsStr), toIntSet(groupIdsStr))
  }

  private def fromModel(update: models.sequence.SeqUpdate) =
    Some((update.authId, update.timestamp, update.seq, update.header, update.serializedData, update.userIds.mkString(","), update.groupIds.mkString(",")))

  private def toIntSet(str: String): Set[Int] = {
    if (str.isEmpty) {
      Set.empty
    } else {
      str.split(',').map(x ⇒ x.toInt).toSet
    }
  }
}

object SeqUpdate {
  val updates = TableQuery[SeqUpdateTable]

  def create(update: models.sequence.SeqUpdate) = {
    updates += update
  }

  def createBulk(newUpdates: Seq[models.sequence.SeqUpdate]) = {
    updates ++= newUpdates
  }

  def findLast(authId: Long) =
    updates.filter(_.authId === authId).sortBy(_.timestamp.desc).take(1).result.headOption

  def find(authId: Long) =
    updates.filter(_.authId === authId).sortBy(_.timestamp.desc).result

  def findAfter(authId: Long, timestamp: Long, limit: Int) =
    updates.filter(u ⇒ u.authId === authId && u.timestamp > timestamp).sortBy(_.timestamp.asc).take(limit).result
}
