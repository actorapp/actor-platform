package im.actor.server.api.rpc.service.groups.v2

import cats.data.Xor
import im.actor.api.rpc.groups.{ ApiGroupType, UpdateGroupInviteObsolete }
import im.actor.api.rpc.messaging.UpdateMessage
import im.actor.api.rpc.peers.{ ApiGroupOutPeer, ApiUserOutPeer }
import im.actor.api.rpc.{ AuthData, ClientData, Ok, PeersImplicits, RpcError, RpcResponse }
import im.actor.api.rpc.sequence.ApiUpdateOptimization
import im.actor.server._
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.group.{ GroupExtension, GroupServiceMessages, GroupType }
import im.actor.server.sequence.SeqState
import im.actor.server.user.UserExtension
import im.actor.util.ThreadLocalSecureRandom

import scala.concurrent.Future

class GroupV2GroupSpec extends BaseAppSuite
  //    with GroupsServiceHelpers
  with MessageParsing
  //    with MessagingSpecHelpers
  with ImplicitSequenceService
  with ImplicitAuthService
  with ImplicitMessagingService
  with ImplicitSessionRegion
  with SeqUpdateMatchers
  with PeersImplicits {

  behavior of "Groups V2 API"

  it should "create group and return correct members list" in create

  it should "allow ONLY group members to invite other users" in invite

  //  it should "allow only group admin to change about" in changeAbout

  //  it should "allow only group admin to revoke integration token" in revokeToken

  // join
  // invite
  // kick
  // leave
  // change actions by admin
  // change actions by non-admin

  val optimizations = Vector(ApiUpdateOptimization.GROUPS_V2, ApiUpdateOptimization.STRIP_ENTITIES)

  val groupInviteConfig = GroupInviteConfig("http://actor.im")
  val groupExt = GroupExtension(system)

  val groupService = new GroupsServiceImpl(groupInviteConfig)

  val userExt = UserExtension(system)

  def create(): Unit = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val sessionId = createSessionId()

    implicit val clientData = ClientData(aliceAuthId, sessionId, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val aliceOutPeer = getUserOutPeer(alice.id, aliceAuthId)

    val members = 1 to 20 map { i ⇒
      val (user, authId, authSid, _) = createUser()
      getUserOutPeer(user.id, aliceAuthId)
    }

    val title = "V2 first group"
    val randomId = nextRandomId()
    val createResp = response(groupService.handleCreateGroup(
      randomId,
      title = title,
      users = members,
      groupType = None,
      optimizations = optimizations
    ))

    // members checks
    createResp.users shouldBe empty // strip entities optimization
    createResp.userPeers should not be empty
    createResp.userPeers should have length 21 // 20 invited users + 1 creator
    createResp.group.membersCount shouldEqual Some(21)
    createResp.userPeers should contain theSameElementsAs (members :+ aliceOutPeer)

    // group properties checks
    createResp.group.groupType shouldEqual Some(ApiGroupType.GROUP)
    createResp.group.title shouldEqual title
    createResp.group.isHidden shouldEqual Some(false)

    // membership checks
    createResp.group.isMember shouldEqual Some(true)
    createResp.group.isAdmin shouldEqual Some(true)
    createResp.group.creatorUserId shouldEqual alice.id

    // Group V2 updates
    expectUpdate(classOf[UpdateMessage]) { upd ⇒
      upd.randomId shouldEqual randomId
      upd.message shouldEqual GroupServiceMessages.groupCreated
    }

    // There should be no Group V1 updates
    expectNoUpdate(emptyState, classOf[UpdateGroupInviteObsolete])
  }

  def invite() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()
    val (carol, _, _, _) = createUser()

    val sessionId = createSessionId()

    val aliceClientData = ClientData(aliceAuthId, sessionId, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobClientData = ClientData(bobAuthId, sessionId, Some(AuthData(bob.id, bobAuthSid, 42)))

    val randomId = nextRandomId()
    val (seqState, groupOutPeer) = {
      implicit val cd = aliceClientData
      val createResp = response(groupService.handleCreateGroup(
        randomId,
        title = "Invite check group",
        users = Vector.empty,
        groupType = None,
        optimizations = optimizations
      ))
      val group = createResp.group
      group.membersCount shouldEqual Some(1)
      group.members should have length 1
      group.members map (_.userId) shouldEqual Vector(alice.id)

      mkSeqState(createResp.seq, createResp.state) → ApiGroupOutPeer(group.id, group.accessHash)
    }

    // Don't allow non-member to invite other users
    {
      implicit val cd = bobClientData
      whenReady(groupService.handleInviteUser(
        groupOutPeer,
        nextRandomId(),
        getUserOutPeer(carol.id, bobAuthId),
        optimizations
      )) { _ should matchForbidden }
    }

    // Allow group member to invite other users
    {
      implicit val cd = aliceClientData
      val randomId = nextRandomId()
      whenReady(groupService.handleInviteUser(
        groupOutPeer,
        randomId,
        getUserOutPeer(bob.id, aliceAuthId),
        optimizations
      )) { resp ⇒

      }

    }

  }

  private def response[A <: RpcResponse](result: Future[RpcError Xor A]): A = {
    val r = result.futureValue
    r should matchPattern {
      case Ok(_) ⇒
    }
    r.getOrElse(fail("Rpc response was not OK"))
  }

  private def nextRandomId() = ThreadLocalSecureRandom.current().nextLong()

}
