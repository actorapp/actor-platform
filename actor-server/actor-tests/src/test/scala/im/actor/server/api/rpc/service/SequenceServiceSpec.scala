package im.actor.server.api.rpc.service

import scala.concurrent.Await
import scala.concurrent.duration._

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import com.google.protobuf.CodedInputStream
import com.typesafe.config.ConfigFactory
import slick.dbio.DBIO

import im.actor.api.rpc._
import im.actor.api.rpc.contacts.UpdateContactsAdded
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.sequence.ResponseGetDifference
import im.actor.server.BaseAppSuite
import im.actor.server.api.rpc.service.auth.AuthConfig
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.peermanagers.{ GroupPeerManager, PrivatePeerManager }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.push.SeqUpdatesManager
import im.actor.server.push.{ WeakUpdatesManager, SeqUpdatesManager }
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

  implicit val service = new sequence.SequenceServiceImpl
  implicit val msgService = messaging.MessagingServiceImpl(mediator)
  val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
  implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
  implicit val authSmsConfig = AuthConfig.fromConfig(system.settings.config.getConfig("auth"))
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

    val actions = for (i ← 0 to 202) yield {
      val update = UpdateContactsAdded(Vector(i, 0, 0))
      val (userIds, groupIds) = updateRefs(update)
      persistAndPushUpdate(authId, update.header, update.toByteArray, userIds, groupIds, None, None, isFat = false)
    }

    Await.result(db.run(DBIO.sequence(actions)), 10.seconds)

    val (seq1, state1) = whenReady(service.handleGetDifference(0, Array.empty)) { res ⇒
      res should matchPattern {
        case Ok(ResponseGetDifference(seq, state, users, updates, true, groups)) if updates.length == 100 ⇒
      }

      val diff = res.toOption.get

      for (i ← 0 to 99) {
        val data = diff.updates(i).update
        val in = CodedInputStream.newInstance(data)
        UpdateContactsAdded.parseFrom(in) shouldEqual Right(UpdateContactsAdded(Vector(i, 0, 0)))
      }

      diff.seq shouldEqual 1099

      (diff.seq, diff.state)
    }

    val (seq2, state2) = whenReady(service.handleGetDifference(seq1, state1)) { res ⇒
      res should matchPattern {
        case Ok(ResponseGetDifference(seq, state, users, updates, true, groups)) if updates.length == 100 ⇒
      }

      val diff = res.toOption.get

      for (i ← 0 to 99) {
        val data = diff.updates(i).update
        val in = CodedInputStream.newInstance(data)
        UpdateContactsAdded.parseFrom(in) shouldEqual Right(UpdateContactsAdded(Vector(100 + i, 0, 0)))
      }

      diff.seq shouldEqual 1199

      (diff.seq, diff.state)
    }

    val (seq3, state3) = whenReady(service.handleGetDifference(seq2, state2)) { res ⇒
      res should matchPattern {
        case Ok(ResponseGetDifference(seq, state, users, updates, false, groups)) if updates.length == 3 ⇒
      }

      val diff = res.toOption.get

      for (i ← 0 to 2) {
        val data = diff.updates(i).update
        val in = CodedInputStream.newInstance(data)
        UpdateContactsAdded.parseFrom(in) shouldEqual Right(UpdateContactsAdded(Vector(200 + i, 0, 0)))
      }

      diff.seq shouldEqual 1202

      (diff.seq, diff.state)
    }

    whenReady(service.handleGetDifference(seq2, state2)) { res ⇒
      res should matchPattern {
        case Ok(ResponseGetDifference(seq, state, users, updates, false, groups)) if updates.length == 3 ⇒
      }
    }

    whenReady(service.handleGetDifference(seq3, state3)) { res ⇒
      res should matchPattern {
        case Ok(ResponseGetDifference(seq, state, users, updates, false, groups)) if updates.isEmpty ⇒
      }

      val diff = res.toOption.get

      diff.seq shouldEqual seq3

      diff.state shouldEqual state3
    }

    whenReady(service.handleGetState()) { res ⇒
      val state = res.toOption.get
      state.seq shouldEqual seq3
    }

    Thread.sleep(5000)

    whenReady(service.handleGetDifference(seq3, state3)) { res ⇒
      val diff = res.toOption.get

      diff.needMore shouldEqual false
      diff.seq shouldEqual 1999
      diff.state shouldEqual state3
    }
  }
}
