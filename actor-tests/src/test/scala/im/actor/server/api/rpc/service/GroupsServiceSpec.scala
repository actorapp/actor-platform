package im.actor.server.api.rpc.service

import scala.util.Random

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager

import im.actor.api.rpc._
import im.actor.api.rpc.groups._
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.UserOutPeer
import im.actor.server.api.rpc.service.groups.GroupsServiceImpl
import im.actor.server.api.util.ACL
import im.actor.server.persist
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.push.{ WeakUpdatesManager, SeqUpdatesManager }
import im.actor.server.social.SocialManager

class GroupsServiceSpec extends BaseServiceSuite with GroupsServiceHelpers {
  behavior of "GroupsService"

  it should "send invites on group creation" in e1

  it should "send updates on group invite" in e2

  it should "send updates ot title change" in e3

  implicit val sessionRegion = buildSessionRegionProxy()

  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()

  val bucketName = "actor-uploads-test"
  val awsCredentials = new EnvironmentVariableCredentialsProvider()
  implicit val transferManager = new TransferManager(awsCredentials)

  implicit val service = new GroupsServiceImpl(bucketName)
  implicit val authService = buildAuthService()
  implicit val ec = system.dispatcher

  def e1() = {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()

    val sessionId = createSessionId()

    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    whenReady(db.run(persist.sequence.SeqUpdate.find(authId2).head)) { s ⇒
      s.header should ===(UpdateGroupInvite.header)
    }

    whenReady(db.run(persist.GroupUser.findUserIds(groupOutPeer.groupId))) { userIds ⇒
      userIds.toSet should ===(Set(user1.id, user2.id))
    }
  }

  def e2() = {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACL.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = UserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    whenReady(service.handleInviteUser(groupOutPeer, Random.nextLong(), user2OutPeer)) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseSeqDate(1001, _, _)) ⇒
      }
    }

    whenReady(db.run(persist.sequence.SeqUpdate.find(authId2))) { updates ⇒
      updates.map(_.header) should ===(
        Seq(
          UpdateGroupMembersUpdate.header,
          UpdateGroupAvatarChanged.header,
          UpdateGroupTitleChanged.header,
          UpdateGroupInvite.header
        )
      )
    }

    whenReady(db.run(persist.sequence.SeqUpdate.find(authId1).head)) { update ⇒
      update.header should ===(UpdateGroupUserAdded.header)
    }
  }

  def e3() = {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    whenReady(service.handleEditGroupTitle(groupOutPeer, Random.nextLong(), "Very fun group")) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseSeqDate(1001, _, _)) ⇒
      }
    }

    whenReady(db.run(persist.sequence.SeqUpdate.find(authId1))) { updates ⇒
      updates.head.header should ===(UpdateGroupTitleChanged.header)
    }

    whenReady(db.run(persist.sequence.SeqUpdate.find(authId2))) { updates ⇒
      updates.head.header should ===(UpdateGroupTitleChanged.header)
    }
  }
}
