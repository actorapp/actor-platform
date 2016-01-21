package im.actor.server.sequence

import im.actor.api.rpc.contacts.UpdateContactsAdded
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.{ SeqUpdate, UpdateMapping }
import im.actor.server.{ BaseAppSuite, ImplicitAuthService, ImplicitSessionRegion }
import slick.dbio.DBIO
import slick.jdbc.GetResult
import sql.migration.V20151108011300__FillUserSequence

import scala.concurrent.Await
import scala.concurrent.duration._

final class V20151108011300__FillUserSequenceSpec extends BaseAppSuite with ImplicitAuthService with ImplicitSessionRegion {

  import V20151108011300__FillUserSequence._

  it should "properly migrate updates from authId with max sequence" in maxSeq
  it should "migrate new updates on second run" in secondRun

  implicit val getSeqUpdate = GetResult[New](r ⇒ New(
    userId = r.nextInt(),
    seq = r.nextInt(),
    timestamp = r.nextLong(),
    mapping = UpdateMapping.parseFrom(r.nextBytes()).toByteArray
  ))

  def maxSeq() = {
    val (user1, authId1, _, _) = createUser()
    val (authId2, _) = createAuthId(user1.id)
    val (authId3, _) = createAuthId(user1.id)

    createUser()

    fillObsolete(authId1, (BulkSize * 1.5).toInt)
    fillObsolete(authId2, BulkSize + 50)
    fillObsolete(authId3, BulkSize / 2)

    new V20151108011300__FillUserSequence().migrate()

    checkValidSeq(user1.id, authId1)
  }

  def secondRun() = {
    val (user1, authId1, _, _) = createUser()
    val (authId2, _) = createAuthId(user1.id)
    val (authId3, _) = createAuthId(user1.id)

    val seq1 = fillObsolete(authId1, (BulkSize * 1.5).toInt)
    fillObsolete(authId2, BulkSize + 50)
    fillObsolete(authId3, BulkSize / 2)

    new V20151108011300__FillUserSequence().migrate()

    fillObsolete(authId1, 10, seq1.size + 1)

    new V20151108011300__FillUserSequence().migrate()

    checkValidSeq(user1.id, authId1)
  }

  private def fillObsolete(authId: Long, seq: Int, startFrom: Int = 1): Seq[Obsolete] = {
    val obss = buildObsSeq(authId, seq, startFrom)

    whenReady(db.run(DBIO.sequence(obss map {
      case Obsolete(authId, timestamp, seq, header, data, userIds, groupIds) ⇒
        sql"""INSERT INTO seq_updates_ngen (auth_id, timestamp, seq, header, serialized_data, user_ids_str, group_ids_str)
              VALUES ($authId, $timestamp, $seq, $header, $data, $userIds, $groupIds)
           """.asUpdate
    })))(identity)

    obss
  }

  private def checkValidSeq(userId: Int, oldestAuthId: Long): Unit = {
    Await.result(db.run(for {
      seqs ← sql"""SELECT * FROM user_sequence WHERE user_id = $userId ORDER BY seq ASC""".as[New]
      obsSeq ← sql"""SELECT seq FROM seq_updates_ngen WHERE auth_id = $oldestAuthId ORDER BY timestamp DESC LIMIT 1"""
        .as[Int].headOption.map(_.getOrElse(0))
    } yield {
      assert(seqs.size.toInt == obsSeq.toInt, "wrong sequence size")

      seqs.zipWithIndex foreach {
        case (New(`userId`, seq, timestamp, mappingBytes), index) ⇒
          assert(index + 1 == seq, "seq is broken")
          val seqUpd =
            UpdateMapping
              .parseFrom(mappingBytes)
              .getDefault

          assert(seqUpd.header == UpdateContactsAdded.header, "wrong header")

          val upd = UpdateContactsAdded.parseFrom(seqUpd.body.toByteArray).right.get
          assert(upd == UpdateContactsAdded(Vector(index + 1)), "wrong update")

          assert(seqUpd.userIds == Seq(index + 1), "wrong userIds")
          assert(seqUpd.groupIds == Seq(index + 1, index + 1), "wrong groupIds")
        case (wrong, _) ⇒ fail(s"Wrong seqUpdate $wrong")
      }
    }), patienceConfig.timeout.totalNanos.nanos)
  }

  private def buildObsSeq(authId: Long, seq: Int, startFrom: Int = 1) =
    for (i ← startFrom to seq) yield {
      val upd = UpdateContactsAdded(Vector(i))
      Obsolete(authId, i.toLong + 100, i, upd.header, upd.toByteArray, s"$i", s"$i,$i")
    }
}