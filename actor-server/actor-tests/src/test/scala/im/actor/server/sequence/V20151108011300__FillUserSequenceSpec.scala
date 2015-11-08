package im.actor.server.sequence

import im.actor.api.rpc.contacts.UpdateContactsAdded
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.{ SeqUpdate, UpdateMapping }
import im.actor.server.{ BaseAppSuite, ImplicitAuthService, ImplicitSessionRegion }
import slick.dbio.DBIO
import slick.jdbc.GetResult
import sql.migration.V20151108011300__FillUserSequence

final class V20151108011300__FillUserSequenceSpec extends BaseAppSuite with ImplicitAuthService with ImplicitSessionRegion {

  import V20151108011300__FillUserSequence._

  it should "properly migrate updates from authId with max sequence" in maxSeq

  implicit val getSeqUpdate = GetResult[SeqUpdate](r ⇒ SeqUpdate(
    userId = r.nextInt(),
    seq = r.nextInt(),
    timestamp = r.nextLong(),
    mapping = Some(UpdateMapping.parseFrom(r.nextBytes()))
  ))

  def maxSeq() = {
    val (user, authId1, _, _) = createUser()
    val (authId2, _) = createAuthId(user.id)
    val (authId3, _) = createAuthId(user.id)

    val seq1 = buildObsSeq(authId1, (BulkSize * 1.5).toInt)
    val seq2 = buildObsSeq(authId2, BulkSize + 50)
    val seq3 = buildObsSeq(authId3, (BulkSize / 2).toInt)

    whenReady(db.run(DBIO.sequence((seq1 ++ seq2 ++ seq3) map {
      case Obsolete(authId, timestamp, seq, header, data, userIds, groupIds) ⇒
        sql"""INSERT INTO seq_updates_ngen (auth_id, timestamp, seq, header, serialized_data, user_ids_str, group_ids_str)
              VALUES ($authId, $timestamp, $seq, $header, $data, $userIds, $groupIds)
           """.asUpdate
    })))(identity)

    new V20151108011300__FillUserSequence().migrate(db.source.createConnection())

    checkValidSeq(user.id, seq1)
  }

  private def checkValidSeq(userId: Int, obsSeq: Seq[Obsolete]): Unit = {
    db.run(for (seqs ← sql"""SELECT * FROM user_sequence""".as[SeqUpdate]) yield {
      assert(seqs.size.toInt == obsSeq.size.toInt, "wrong sequence size")

      seqs.zipWithIndex foreach {
        case (SeqUpdate(`userId`, seq, timestamp, Some(mappingBytes)), index) ⇒
          assert(index == seq, "seq is broken")
          val seqUpd =
            UpdateMapping
              .parseFrom(mappingBytes.toByteArray)
              .getDefault

          assert(seqUpd.header == UpdateContactsAdded.header, "wrong header")

          val upd = UpdateContactsAdded.parseFrom(seqUpd.body.toByteArray).right.get
          assert(upd == UpdateContactsAdded(Vector(index)), "wrong update")

          assert(seqUpd.userIds == Seq(index), "wrong userIds")
          assert(seqUpd.groupIds == Seq(index, index), "wrong groupIds")
        case (wrong, _) ⇒ fail(s"Wrong seqUpdate $wrong")
      }
    })
  }

  private def buildObsSeq(authId: Long, seq: Int) =
    for (i ← 1 to seq) yield {
      val upd = UpdateContactsAdded(Vector(i))
      Obsolete(authId, i.toLong + 100, i, upd.header, upd.toByteArray, s"$i", s"$i,$i")
    }
}