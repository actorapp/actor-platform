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

  val DiffStep = 100L

  val updatesC = Compiled(updates)

  def afterTimestamp(authId: Rep[Long], timestamp: Rep[Long], limit: ConstColumn[Long]) =
    updates.filter(u ⇒ u.authId === authId && u.timestamp > timestamp).sortBy(_.timestamp.asc).take(limit)
  def last(authId: Rep[Long]) =
    updates.filter(_.authId === authId).sortBy(_.timestamp.desc).take(1)
  def byAuthId(authId: Rep[Long]) =
    updates.filter(_.authId === authId).sortBy(_.timestamp.desc)

  val afterTimestampC = Compiled(afterTimestamp _)
  val lastC = Compiled(last _)
  val byAuthIdC = Compiled(byAuthId _)

  def create(update: models.sequence.SeqUpdate) =
    updatesC += update

  def createBulk(newUpdates: Seq[models.sequence.SeqUpdate]) =
    updatesC ++= newUpdates

  def findLast(authId: Long) =
    lastC(authId).result.headOption

  def find(authId: Long) =
    byAuthIdC(authId).result

  def findAfter(authId: Long, timestamp: Long) =
    afterTimestampC((authId, timestamp, DiffStep)).result
}
