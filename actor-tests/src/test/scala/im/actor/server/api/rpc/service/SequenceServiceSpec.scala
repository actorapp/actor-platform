package im.actor.server.api.rpc.service

import scala.concurrent.Await
import scala.concurrent.duration._

import slick.dbio.DBIO

import im.actor.api.rpc._
import im.actor.api.rpc.contacts.UpdateContactsAdded
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.sequence.ResponseGetDifference
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.push.{ WeakUpdatesManager, SeqUpdatesManager }
import im.actor.server.social.SocialManager

class SequenceServiceSpec extends BaseServiceSuite {

  behavior of "Sequence service"

  it should "get state" in e1
  it should "get difference" in e2

  implicit val sessionRegion = buildSessionRegionProxy()
  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()

  implicit val service = new sequence.SequenceServiceImpl
  implicit val msgService = new messaging.MessagingServiceImpl
  implicit val authService = buildAuthService()
  implicit val ec = system.dispatcher

  import SeqUpdatesManager._

  def e1() = {
    val (user, authId, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId, sessionId, Some(user.id))

    whenReady(service.handleGetState()) { res ⇒
      res should matchPattern { case Ok(ResponseSeq(999, _)) ⇒ }
    }
  }

  def e2() = {
    val (user, authId, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId, sessionId, Some(user.id))

    val update = UpdateContactsAdded(Vector(1, 2, 3))
    val (userIds, groupIds) = updateRefs(update)

    val actions = for (a ← 1 to 203) yield {
      persistAndPushUpdate(authId, update.header, update.toByteArray, userIds, groupIds, None, None)
    }

    Await.result(db.run(DBIO.sequence(actions)), 10.seconds)

    val (seq1, state1) = whenReady(service.handleGetDifference(0, Array.empty)) { res ⇒
      res should matchPattern {
        case Ok(ResponseGetDifference(seq, state, users, updates, true, groups, phones, emails)) if updates.length == 100 ⇒
      }

      val diff = res.toOption.get

      (diff.seq, diff.state)
    }

    val (seq2, state2) = whenReady(service.handleGetDifference(seq1, state1)) { res ⇒
      res should matchPattern {
        case Ok(ResponseGetDifference(seq, state, users, updates, true, groups, phones, emails)) if updates.length == 100 ⇒
      }

      val diff = res.toOption.get

      (diff.seq, diff.state)
    }

    val (seq3, state3) = whenReady(service.handleGetDifference(seq2, state2)) { res ⇒
      res should matchPattern {
        case Ok(ResponseGetDifference(seq, state, users, updates, false, groups, phones, emails)) if updates.length == 3 ⇒
      }

      val diff = res.toOption.get

      (diff.seq, diff.state)
    }

    whenReady(service.handleGetDifference(seq2, state2)) { res ⇒
      res should matchPattern {
        case Ok(ResponseGetDifference(seq, state, users, updates, false, groups, phones, emails)) if updates.length == 3 ⇒
      }
    }

    whenReady(service.handleGetDifference(seq3, state3)) { res ⇒
      res should matchPattern {
        case Ok(ResponseGetDifference(seq, state, users, updates, false, groups, phones, emails)) if updates.isEmpty ⇒
      }

      val diff = res.toOption.get

      diff.seq shouldEqual seq3

      diff.state shouldEqual state3
    }
  }
}