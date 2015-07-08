package im.actor.server.api.rpc.service

import scala.concurrent.Await
import scala.concurrent.duration._

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import com.typesafe.config.ConfigFactory
import org.scalatest.Inside._
import slick.dbio.DBIO

import im.actor.api.rpc._
import im.actor.api.rpc.groups.UpdateGroupUserLeave
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.sequence.ResponseGetDifference
import im.actor.server.BaseAppSuite
import im.actor.server.api.rpc.service.auth.AuthConfig
import im.actor.server.api.rpc.service.sequence.SequenceServiceConfig
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.peermanagers.{ GroupPeerManager, PrivatePeerManager }
import im.actor.server.presences.PresenceManager
import im.actor.server.push.SeqUpdatesManager
import im.actor.server.social.SocialManager
import im.actor.util.testing.ActorSpecification

class SequenceServiceSpec extends BaseAppSuite({
  ActorSpecification.createSystem(
    ConfigFactory.parseString(
      """
        push.seq-updates-manager.receive-timeout = 3 seconds
      """
    )
  )
}) {

  behavior of "Sequence service"

  it should "get state" in e1
  it should "get difference" in e2

  implicit val sessionRegion = buildSessionRegionProxy()
  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()
  implicit val privatePeerManagerRegion = PrivatePeerManager.startRegion()
  implicit val groupPeerManagerRegion = GroupPeerManager.startRegion()

  val bucketName = "actor-uploads-test"
  val awsCredentials = new EnvironmentVariableCredentialsProvider()
  implicit val transferManager = new TransferManager(awsCredentials)
  val config = SequenceServiceConfig.load().get //20 kB by default

  implicit val service = new sequence.SequenceServiceImpl(config)
  implicit val msgService = messaging.MessagingServiceImpl(mediator)
  val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
  implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
  implicit val authSmsConfig = AuthConfig.load.get
  implicit val authService = buildAuthService()

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

    //serialized update size is: 32 bytes for body + 4 bytes for header, 36 bytes total
    //with max update size of 20 KiB 1281 updates should split into three parts of length 568 + 568 + 145
    val actions = for (i ← 0 to 1280) yield {
      val update = UpdateGroupUserLeave(Int.MaxValue, Int.MaxValue, Long.MaxValue, Long.MaxValue)
      val (userIds, groupIds) = updateRefs(update)
      persistAndPushUpdate(authId, update.header, update.toByteArray, userIds, groupIds, None, None, isFat = false)
    }
    var totalUpdates: Int = 0

    Await.result(db.run(DBIO.sequence(actions)), 10.seconds)

    val (seq1, state1) = whenReady(service.handleGetDifference(0, Array.empty)) { res ⇒
      val diff = res.toOption.get
      inside(res) {
        case Ok(ResponseGetDifference(seq, state, users, updates, needMore, groups)) ⇒
          (updates.map(_.toByteArray.length).sum <= config.maxUpdateSizeInBytes) shouldEqual true
          needMore shouldEqual true
          totalUpdates += updates.length
          diff.seq shouldEqual 999 + updates.length
      }
      (diff.seq, diff.state)
    }

    val (seq2, state2) = whenReady(service.handleGetDifference(seq1, state1)) { res ⇒
      val diff = res.toOption.get
      inside(res) {
        case Ok(ResponseGetDifference(seq, state, users, updates, needMore, groups)) ⇒
          (updates.map(_.toByteArray.length).sum <= config.maxUpdateSizeInBytes) shouldEqual true
          needMore shouldEqual true
          totalUpdates += updates.length
          diff.seq shouldEqual seq1 + updates.length
      }
      (diff.seq, diff.state)
    }

    val finalSeq = whenReady(service.handleGetDifference(seq2, state2)) { res ⇒
      val diff = res.toOption.get
      inside(res) {
        case Ok(ResponseGetDifference(seq, state, users, updates, needMore, groups)) ⇒
          (updates.map(_.toByteArray.length).sum <= config.maxUpdateSizeInBytes) shouldEqual true
          needMore shouldEqual false
          totalUpdates += updates.length
          diff.seq shouldEqual seq2 + updates.length
      }
      diff.seq
    }
    finalSeq shouldEqual 2280
    totalUpdates shouldEqual 1281
  }
}
