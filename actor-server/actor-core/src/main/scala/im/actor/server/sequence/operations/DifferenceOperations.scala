package im.actor.server.sequence.operations

import im.actor.api.rpc.sequence.UpdateEmptyUpdate
import im.actor.server.model.{ SeqUpdate, SerializedUpdate, UpdateMapping }
import im.actor.server.persist.sequence.UserSequenceRepo
import im.actor.server.sequence.{ CommonState, Difference, SeqUpdatesExtension }
import slick.dbio._

import scala.annotation.tailrec
import scala.collection.immutable
import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait DifferenceOperations { this: SeqUpdatesExtension ⇒

  private val DiffStep = 100L

  private type ReduceKey = String
  private object DiffAcc {
    def empty(commonSeq: Int) = DiffAcc(commonSeq, 0, immutable.TreeMap.empty, Map.empty)
  }

  /**
   * Accumulator to store updates from difference.
   *
   * @param commonSeq Sequence common for all user's authorizations.
   *                  Must be highest sequence number from all updates in `DiffAcc`
   * @param seqDelta `AuthId` specific delta, that will be added to old sequence.
   *                 Must be incremented with every non-empty update for current `authId`.
   * @param generic Sorted map from common seq to serialized update.
   *                Generic updates, are those, which won't be reduced(don't have reduce key).
   * @param reduced Map from reduce key to pair of common seq - serialized update.
   *                Not all updates in sequence with same reduce key should go to difference,
   *                only update with highest common seq should go in difference.
   */
  private case class DiffAcc(
    commonSeq: Int,
    seqDelta:  Int,
    generic:   immutable.SortedMap[Int, SerializedUpdate],
    reduced:   Map[ReduceKey, (Int, SerializedUpdate)]
  ) {
    def nonEmpty = generic.nonEmpty || reduced.nonEmpty

    def toVector = (generic ++ reduced.values).values.toVector
  }

  def getDifference(
    userId:         Int,
    clientSeq:      Int,
    state:          Array[Byte],
    authId:         Long,
    authSid:        Int,
    maxSizeInBytes: Long
  ): Future[Difference] = {
    def run(commonSeq: Int, acc: DiffAcc, currentSize: Long): DBIO[(DiffAcc, Boolean)] = {
      UserSequenceRepo.fetchAfterSeq(userId, commonSeq, DiffStep) flatMap { updates ⇒
        if (updates.isEmpty) {
          DBIO.successful(acc → false)
        } else {
          val (newAcc, newSize, allFit) = append(updates.toList, currentSize, maxSizeInBytes, acc, authId, authSid)
          if (allFit)
            run(newAcc.commonSeq, newAcc, newSize)
          else
            DBIO.successful(newAcc → true)
        }
      }
    }

    val commonSeq = CommonState.validate(state) match {
      case Success(CommonState(_, 0)) ⇒
        log.debug("Got old client with seq: {}", clientSeq)
        clientSeq
      case Success(CommonState(_, seq)) ⇒ seq
      case Failure(_) ⇒
        log.debug("Failed to parse CommonState, using seq: {}", clientSeq)
        clientSeq
    }

    for {
      (acc, needMore) ← db.run(run(commonSeq, DiffAcc.empty(commonSeq), 0L))
    } yield Difference(
      updates = acc.toVector,
      seq = clientSeq + acc.seqDelta,
      commonState = commonState(acc.commonSeq).toByteArray,
      needMore = needMore
    )
  }

  private def append(
    updates:            List[SeqUpdate],
    currentSizeInBytes: Long,
    maxSizeInBytes:     Long,
    updateAcc:          DiffAcc,
    authId:             Long,
    authSid:            Int
  ): (DiffAcc, Long, Boolean) = {
    @tailrec
    def run(updatesLeft: List[SeqUpdate], acc: DiffAcc, currSize: Long): (DiffAcc, Long, Boolean) = {
      updatesLeft match {
        case h :: t ⇒
          val upd = getUpdate(h.getMapping, authId, authSid)
          // TODO: don't count already reduced updates. How to make it?
          val newSize = currSize + (if (upd.header == UpdateEmptyUpdate.header) 0 else upd.body.size())
          if (newSize > maxSizeInBytes && acc.nonEmpty) {
            (acc, currSize, false)
          } else {
            val newAcc = if (upd.header == UpdateEmptyUpdate.header) {
              acc.copy(commonSeq = h.commonSeq)
            } else {
              h.reduceKey map (_.value) match {
                case None ⇒ acc.copy(
                  seqDelta = acc.seqDelta + 1,
                  commonSeq = h.commonSeq,
                  generic = acc.generic + (h.commonSeq → upd)
                )
                case Some(reduceKey) ⇒ acc.copy(
                  seqDelta = acc.seqDelta + 1,
                  commonSeq = h.commonSeq,
                  reduced = acc.reduced + (reduceKey → (h.commonSeq → upd))
                )
              }
            }
            run(t, newAcc, newSize)
          }
        case Nil ⇒ (acc, currSize, true)
      }
    }
    run(updates, updateAcc, currentSizeInBytes)
  }

  // authSid comes for compatibility with older clients
  private def getUpdate(mapping: UpdateMapping, authId: Long, authSid: Int): SerializedUpdate =
    mapping.custom.getOrElse(
      authId,
      mapping.customObsolete.getOrElse(
        authSid,
        mapping.getDefault
      )
    )

}
