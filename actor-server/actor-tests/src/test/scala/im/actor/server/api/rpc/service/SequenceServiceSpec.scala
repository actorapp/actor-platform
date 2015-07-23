package im.actor.server.api.rpc.service

import im.actor.server.group.{ GroupOfficeRegion, GroupOffice }
import im.actor.server.user.{ UserOfficeRegion, UserOffice }

import scala.concurrent.Await
import scala.concurrent.duration._

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import com.google.protobuf.CodedInputStream
import com.typesafe.config.ConfigFactory
import org.scalatest.Inside._
import slick.dbio.DBIO

import im.actor.api.rpc._
import im.actor.api.rpc.groups.UpdateGroupUserLeave
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.sequence.{ DifferenceUpdate, ResponseGetDifference }
import im.actor.api.rpc.users.UpdateUserNameChanged
import im.actor.server.{ ActorSpecification, BaseAppSuite }
import im.actor.server.api.rpc.service.sequence.SequenceServiceConfig
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.presences.PresenceManager
import im.actor.server.push.SeqUpdatesManager
import im.actor.server.social.SocialManager

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
  implicit val privatePeerManagerRegion = UserOfficeRegion.start()
  implicit val groupPeerManagerRegion = GroupOfficeRegion.start()

  val bucketName = "actor-uploads-test"
  val awsCredentials = new EnvironmentVariableCredentialsProvider()
  implicit val transferManager = new TransferManager(awsCredentials)
  val config = SequenceServiceConfig.load().get //20 kB by default

  implicit val service = new sequence.SequenceServiceImpl(config)
  implicit val msgService = messaging.MessagingServiceImpl(mediator)
  val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
  implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
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

    def withError(maxUpdateSize: Long) = {
      val example = UpdateUserNameChanged(1000, "Looooooooooooooooooooooooooooooong name")
      maxUpdateSize * (1 + 4.toDouble / example.getSerializedSize)
    }

    //serialized update size is: 40 bytes for body + 4 bytes for header, 44 bytes total
    //with max update size of 20 KiB 1281 updates should split into three parts
    val actions = for (i ← 1000 to 2280) yield {
      val update = UpdateUserNameChanged(i, "Looooooooooooooooooooooooooooooong name")
      val (userIds, groupIds) = updateRefs(update)
      persistAndPushUpdate(authId, update.header, update.toByteArray, userIds, groupIds, None, None, isFat = false)
    }
    var totalUpdates: Seq[DifferenceUpdate] = Seq.empty

    Await.result(db.run(DBIO.sequence(actions)), 10.seconds)

    val (seq1, state1) = whenReady(service.handleGetDifference(0, Array.empty)) { res ⇒
      val diff = res.toOption.get
      inside(res) {
        case Ok(ResponseGetDifference(seq, state, users, updates, needMore, groups)) ⇒
          (updates.map(_.toByteArray.length).sum <= withError(config.maxUpdateSizeInBytes)) shouldEqual true
          needMore shouldEqual true
          totalUpdates ++= updates
          diff.seq shouldEqual 999 + updates.length
      }
      (diff.seq, diff.state)
    }

    val (seq2, state2) = whenReady(service.handleGetDifference(seq1, state1)) { res ⇒
      val diff = res.toOption.get
      inside(res) {
        case Ok(ResponseGetDifference(seq, state, users, updates, needMore, groups)) ⇒
          (updates.map(_.toByteArray.length).sum <= withError(config.maxUpdateSizeInBytes)) shouldEqual true
          needMore shouldEqual true
          totalUpdates ++= updates
          diff.seq shouldEqual seq1 + updates.length
      }
      (diff.seq, diff.state)
    }

    val finalSeq = whenReady(service.handleGetDifference(seq2, state2)) { res ⇒
      val diff = res.toOption.get
      inside(res) {
        case Ok(ResponseGetDifference(seq, state, users, updates, needMore, groups)) ⇒
          (updates.map(_.toByteArray.length).sum <= withError(config.maxUpdateSizeInBytes)) shouldEqual true
          needMore shouldEqual false
          totalUpdates ++= updates
          diff.seq shouldEqual seq2 + updates.length
      }
      diff.seq
    }

    for (i ← 1000 to 2280) {
      val data = totalUpdates(i - 1000)
      val in = CodedInputStream.newInstance(data.update)
      UpdateUserNameChanged.parseFrom(in) should matchPattern {
        case Right(UpdateUserNameChanged(`i`, _)) ⇒
      }
    }

    finalSeq shouldEqual 2280
    totalUpdates.length shouldEqual 1281

  }
}
