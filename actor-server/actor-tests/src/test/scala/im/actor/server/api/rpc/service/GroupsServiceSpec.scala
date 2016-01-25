package im.actor.server.api.rpc.service

import im.actor.api.rpc._
import im.actor.api.rpc.counters.UpdateCountersChanged
import im.actor.api.rpc.groups._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeerType, ApiUserOutPeer }
import im.actor.server._
import im.actor.server.acl.ACLUtils
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupRpcErrors, GroupsServiceImpl }
import im.actor.server.group.GroupServiceMessages
import im.actor.server.model.PeerType
import im.actor.server.persist.HistoryMessageRepo
import slick.dbio.DBIO

import scala.util.Random

final class GroupsServiceSpec
  extends BaseAppSuite
  with GroupsServiceHelpers
  with MessageParsing
  with ImplicitSequenceService
  with ImplicitAuthService
  with ImplicitSessionRegion
  with SeqUpdateMatchers
  with PeersImplicits {
  behavior of "GroupsService"

  it should "send invites on group creation" in sendInvitesOnCreate

  it should "send updates on group invite" in sendUpdatesOnInvite

  it should "send updates ot title change" in sendUpdatesOnTitleChange

  it should "persist service messages in history" in e4

  it should "generate invite url for group member" in e5

  it should "not generate invite url for group non members" in e6

  it should "revoke invite token and generate new token for group member" in e7

  it should "allow user to join group by correct invite link and send correct updates" in e8

  it should "not allow group member to join group by invite link" in e9

  it should "send updates on user join" in e10

  it should "send UserInvited and UserJoined on user's first MessageRead" in e11

  it should "receive userJoined once" in userJoinedOnce

  it should "not allow to create group with empty name" in e13

  "Creator of group" should "be groupAdminColor" in e14

  "MakeUserAdmin" should "allow group member to become admin" in e15

  it should "forbid to perform action by non-admin" in e16

  it should "return error when user is already admin" in e17

  "EditGroupAbout" should "allow group admin to change 'about'" in e18

  it should "forbid to change 'about' by non-admin" in e19

  it should "set 'about' to empty when None comes" in e20

  it should "forbid to set invalid 'about' field (empty, or longer than 255 characters)" in e21

  "EditGroupTopic" should "allow any group member to change topic" in e22

  it should "forbid to set invalid topic (empty, or longer than 255 characters)" in e23

  it should "set topic to empty when None comes" in e24

  "Leave group" should "mark messages read in left user dialog" in e25

  "Kick user" should "mark messages read in kicked user dialog" in e26

  "Kick user" should "mark messages read in public group" in markReadOnKickInPublic

  "Kicked user" should "not be able to write to group" in e27

  val groupInviteConfig = GroupInviteConfig("http://actor.im")

  implicit val messagingService = messaging.MessagingServiceImpl()
  implicit val service = new GroupsServiceImpl(groupInviteConfig)

  def sendInvitesOnCreate() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, _, _, _) = createUser()

    val sessionId = createSessionId()

    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    expectUpdate(classOf[UpdateChatGroupsChanged])(identity)
    expectUpdate(classOf[UpdateGroupUserInvited])(identity)
    expectUpdate(classOf[UpdateGroupInvite])(identity)

    whenReady(db.run(persist.GroupUserRepo.findUserIds(groupOutPeer.groupId))) { userIds ⇒
      userIds.toSet shouldEqual Set(user1.id, user2.id)
    }
  }

  def sendUpdatesOnInvite() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))
    val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData1.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    {
      implicit val clientData = clientData1
      val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

      whenReady(service.handleInviteUser(groupOutPeer, Random.nextLong(), user2OutPeer)) { resp ⇒
        resp should matchPattern {
          case Ok(ResponseSeqDate(3, _, _)) ⇒
        }
      }
      expectUpdate(classOf[UpdateGroupUserInvited])(identity)
      expectUpdate(classOf[UpdateGroupInvite])(identity)
      expectUpdate(classOf[UpdateChatGroupsChanged])(identity)
    }

    {
      implicit val clientData = clientData2
      expectUpdate(classOf[UpdateGroupInvite])(identity)
      //UpdateChatGroupsChanged will come after creation of dialog
    }

  }

  def sendUpdatesOnTitleChange() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))
    val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2)))

    {
      implicit val clientData = clientData1

      val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

      whenReady(service.handleEditGroupTitle(groupOutPeer, Random.nextLong(), "Very fun group")) { resp ⇒
        resp should matchPattern {
          case Ok(ResponseSeqDate(4, _, _)) ⇒
        }
      }
      expectUpdate(classOf[UpdateChatGroupsChanged])(identity)
      expectUpdate(classOf[UpdateGroupUserInvited])(identity)
      expectUpdate(classOf[UpdateGroupInvite])(identity)
      expectUpdate(classOf[UpdateGroupTitleChanged])(identity)
    }

    {
      implicit val clientData = clientData2
      expectUpdate(classOf[UpdateGroupInvite])(identity)
      expectUpdate(classOf[UpdateGroupTitleChanged])(identity)
      //UpdateChatGroupsChanged will come after creation of dialog
    }

  }

  def e4() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    whenReady(db.run(persist.HistoryMessageRepo.find(user1.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
      serviceMessages should have length 1
      serviceMessages
        .map { e ⇒ parseMessage(e.messageContentData) } shouldEqual
        Vector(Right(GroupServiceMessages.groupCreated))

    }

    whenReady(service.handleInviteUser(groupOutPeer, Random.nextLong(), user2OutPeer)) { resp ⇒
      resp should matchPattern { case Ok(_) ⇒ }
      whenReady(db.run(persist.HistoryMessageRepo.find(user1.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 2
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) } shouldEqual
          Vector(
            Right(GroupServiceMessages.userInvited(user2.id)),
            Right(GroupServiceMessages.groupCreated)
          )
      }
      whenReady(db.run(persist.HistoryMessageRepo.find(user2.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 1
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) } shouldEqual
          Vector(Right(GroupServiceMessages.userInvited(user2.id)))
      }
    }

    //TODO: is it ok to remove avatar of group without avatar
    whenReady(service.handleRemoveGroupAvatar(groupOutPeer, Random.nextLong())) { resp ⇒
      resp should matchPattern { case Ok(_) ⇒ }

      Thread.sleep(500)

      whenReady(db.run(persist.HistoryMessageRepo.find(user1.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 3
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) } shouldEqual
          Vector(
            Right(GroupServiceMessages.changedAvatar(None)),
            Right(GroupServiceMessages.userInvited(user2.id)),
            Right(GroupServiceMessages.groupCreated)
          )
      }
      whenReady(db.run(persist.HistoryMessageRepo.find(user2.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 2
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) } shouldEqual
          Vector(
            Right(GroupServiceMessages.changedAvatar(None)),
            Right(GroupServiceMessages.userInvited(user2.id))
          )
      }
    }

    whenReady(service.handleEditGroupTitle(groupOutPeer, Random.nextLong(), "Not fun group")) { resp ⇒
      resp should matchPattern { case Ok(_) ⇒ }
      whenReady(db.run(persist.HistoryMessageRepo.find(user1.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 4
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) }.head shouldEqual Right(GroupServiceMessages.changedTitle("Not fun group"))
      }
    }

    whenReady(service.handleLeaveGroup(groupOutPeer, Random.nextLong())(ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2))))) { resp ⇒
      resp should matchPattern { case Ok(_) ⇒ }
      whenReady(db.run(persist.HistoryMessageRepo.find(user1.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 5
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) }.head shouldEqual Right(GroupServiceMessages.userLeft(user2.id))
      }
    }

    whenReady(service.handleInviteUser(groupOutPeer, Random.nextLong(), user2OutPeer)) { resp ⇒
      resp should matchPattern { case Ok(_) ⇒ }
      whenReady(db.run(persist.HistoryMessageRepo.find(user1.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 6
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) }.head shouldEqual Right(GroupServiceMessages.userInvited(user2.id))
      }
    }

    whenReady(service.handleKickUser(groupOutPeer, Random.nextLong(), user2OutPeer)) { resp ⇒
      resp should matchPattern { case Ok(_) ⇒ }
      whenReady(db.run(persist.HistoryMessageRepo.find(user1.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 7
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) }.head shouldEqual Right(GroupServiceMessages.userKicked(user2.id))
      }
    }

  }

  def e5() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    {
      implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))
      var expUrl: String = ""
      whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
        inside(resp) {
          case Ok(ResponseInviteUrl(url)) ⇒
            url should startWith(groupInviteConfig.baseUrl)
            expUrl = url
        }
      }
      whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
        inside(resp) {
          case Ok(ResponseInviteUrl(url)) ⇒
            url should startWith(groupInviteConfig.baseUrl)
            url shouldEqual expUrl
        }
      }
    }

    {
      implicit val clientData = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2)))
      var expUrl: String = ""
      whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
        inside(resp) {
          case Ok(ResponseInviteUrl(url)) ⇒
            url should startWith(groupInviteConfig.baseUrl)
            expUrl = url
        }
      }
      whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
        inside(resp) {
          case Ok(ResponseInviteUrl(url)) ⇒
            url should startWith(groupInviteConfig.baseUrl)
            url shouldEqual expUrl
        }
      }
    }

    val findTokens =
      for {
        tokens ← DBIO.sequence(List(
          persist.GroupInviteTokenRepo.find(groupOutPeer.groupId, user1.id),
          persist.GroupInviteTokenRepo.find(groupOutPeer.groupId, user2.id)
        ))
      } yield tokens.flatten
    whenReady(db.run(findTokens)) { tokens ⇒
      tokens should have length 2
      tokens.foreach(_.groupId shouldEqual groupOutPeer.groupId)
      tokens.map(_.creatorId) should contain allOf (user1.id, user2.id)
    }
  }

  def e6() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    {
      implicit val clientData = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2)))
      whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
        resp should matchNotAuthorized
      }
    }

  }

  def e7() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    var expUrl: String = ""
    whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
      inside(resp) {
        case Ok(ResponseInviteUrl(url)) ⇒
          url should startWith(groupInviteConfig.baseUrl)
          expUrl = url
      }
    }
    whenReady(service.handleRevokeInviteUrl(groupOutPeer)) { resp ⇒
      inside(resp) {
        case Ok(ResponseInviteUrl(url)) ⇒
          url should startWith(groupInviteConfig.baseUrl)
          url should not equal expUrl
      }
    }

    whenReady(db.run(persist.GroupInviteTokenRepo.find(groupOutPeer.groupId, user1.id))) { tokens ⇒
      tokens should have length 1
    }

  }

  def e8() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Invite Fun group", Set.empty).groupPeer

    whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
      inside(resp) {
        case Ok(ResponseInviteUrl(url)) ⇒
          url should startWith(groupInviteConfig.baseUrl)

          {
            implicit val clientData = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2)))
            whenReady(service.handleJoinGroup(url)) { resp ⇒
              resp should matchPattern {
                case Ok(ResponseJoinGroup(_, _, _, _, _, _)) ⇒
              }
            }
          }
      }
    }
    whenReady(db.run(persist.GroupUserRepo.findUserIds(groupOutPeer.groupId))) { userIds ⇒
      userIds should have length 2
      userIds should contain allOf (user1.id, user2.id)
    }
  }

  def e9() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    implicit val clientData = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
      inside(resp) {
        case Ok(ResponseInviteUrl(url)) ⇒
          url should startWith(groupInviteConfig.baseUrl)

          {
            implicit val clientData = ClientData(authId2, createSessionId(), Some(AuthData(user2.id, authSid2)))
            val outPeer = ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)
            whenReady(messagingService.handleMessageRead(outPeer, System.currentTimeMillis()))(_ ⇒ ())

            whenReady(service.handleJoinGroup(url)) { resp ⇒
              inside(resp) {
                case Error(err) ⇒ err shouldEqual GroupRpcErrors.UserAlreadyInvited
              }
            }
          }
      }
    }
  }

  def e10() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))
    val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData1.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val createGroupResponse = createGroup("Invite Fun group", Set.empty)

    val groupOutPeer = createGroupResponse.groupPeer

    whenReady(service.jhandleGetGroupInviteUrl(groupOutPeer, clientData1)) { resp ⇒
      inside(resp) {
        case Ok(ResponseInviteUrl(url)) ⇒
          url should startWith(groupInviteConfig.baseUrl)

          whenReady(service.jhandleJoinGroup(url, clientData2))(_ ⇒ ())

          expectUpdate(createGroupResponse.seq, classOf[UpdateMessage]) { upd ⇒
            upd.message shouldEqual GroupServiceMessages.userJoined
          }
          expectUpdate(createGroupResponse.seq, classOf[UpdateCountersChanged])(identity)
      }
    }
    whenReady(db.run(persist.GroupUserRepo.findUserIds(groupOutPeer.groupId))) { userIds ⇒
      userIds should have length 2
      userIds should contain allOf (user1.id, user2.id)
    }
  }

  def e11() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))
    val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData1.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = {
      implicit val clientData = clientData1
      val groupOutPeer = createGroup("Invite Fun group", Set.empty).groupPeer

      whenReady(service.handleInviteUser(groupOutPeer, Random.nextLong, user2OutPeer)) { _ ⇒ }

      groupOutPeer
    }

    {
      implicit val clientData = clientData2
      // send it twice to ensure that ServiceMessage isn't sent twice
      whenReady(messagingService.handleMessageRead(ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash), System.currentTimeMillis))(identity)
      whenReady(messagingService.handleMessageRead(ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash), System.currentTimeMillis))(identity)
    }

    {
      implicit val clientData = clientData1
      expectUpdate(classOf[UpdateGroupInvite])(identity)
      expectUpdate(classOf[UpdateGroupUserInvited])(identity)
      expectUpdate(classOf[UpdateMessageRead])(identity)
      expectUpdate(classOf[UpdateCountersChanged])(identity)
      expectUpdate(classOf[UpdateMessage])(identity)
    }
  }

  def userJoinedOnce() = {

    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))
    val clientData2 = ClientData(authId2, createSessionId(), Some(AuthData(user2.id, authSid2)))

    val groupOutPeer = {
      implicit val clientData = clientData1
      createGroup("Fun group", Set.empty).groupPeer
    }

    val url = whenReady(service.jhandleGetGroupInviteUrl(groupOutPeer, clientData1)) { _.toOption.get.url }

    whenReady(service.jhandleJoinGroup(url, clientData2)) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseJoinGroup(_, _, _, _, _, _)) ⇒
      }
    }

    val peer = ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)

    whenReady(messagingService.jhandleSendMessage(peer, 22324L, ApiTextMessage("hello", Vector.empty, None), clientData1)) { _ ⇒ }

    whenReady(messagingService.jhandleMessageRead(peer, System.currentTimeMillis, clientData2)) { _ ⇒ }

    {
      implicit val clientData = clientData1
      expectUpdate(classOf[UpdateChatGroupsChanged])(identity)
      expectUpdate(classOf[UpdateGroupInvite])(identity)
      expectUpdate(classOf[UpdateMessageSent])(identity)
      expectUpdate(classOf[UpdateMessage])(identity)
      expectUpdate(classOf[UpdateCountersChanged])(identity)
      expectUpdate(classOf[UpdateMessageRead])(identity)
    }
  }

  def e13() = {
    val (user1, authId1, authSid1, _) = createUser()

    implicit val clientData = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))

    whenReady(service.handleCreateGroupObsolete(1L, "", Vector.empty)) { resp ⇒
      inside(resp) {
        case Error(GroupRpcErrors.WrongGroupTitle) ⇒
      }
    }
  }

  def e14() = {
    val (user1, authId1, authSid1, _) = createUser()
    implicit val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    whenReady(db.run(persist.GroupUserRepo.find(groupOutPeer.groupId, user1.id))) { groupUser ⇒
      groupUser shouldBe defined
      groupUser.get.isAdmin shouldEqual true
    }
  }

  def e15() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    implicit val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData1.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    whenReady(service.handleMakeUserAdmin(groupOutPeer, user2OutPeer)) { resp ⇒
      inside(resp) {
        case Ok(ResponseMakeUserAdmin(members, _, _)) ⇒
          members.find(_.userId == user2.id) foreach (_.isAdmin shouldEqual Some(true))
      }
    }
  }

  def e16() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()
    val (user3, authId3, authSid3, _) = createUser()

    val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))
    val clientData2 = ClientData(authId2, createSessionId(), Some(AuthData(user2.id, authSid2)))

    val user3Model = getUserModel(user3.id)
    val user3AccessHash = ACLUtils.userAccessHash(clientData2.authId, user3.id, user3Model.accessSalt)
    val user3OutPeer = ApiUserOutPeer(user3.id, user3AccessHash)

    val groupOutPeer = {
      implicit val clientData = clientData1
      createGroup("Fun group", Set(user2.id)).groupPeer
    }

    whenReady(service.jhandleMakeUserAdmin(groupOutPeer, user3OutPeer, clientData2)) { resp ⇒
      resp shouldEqual Error(CommonErrors.forbidden("Only admin can perform this action."))
    }
  }

  def e17() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    implicit val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData1.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    whenReady(service.handleMakeUserAdmin(groupOutPeer, user2OutPeer)) { resp ⇒
      resp should matchPattern {
        case Ok(_: ResponseMakeUserAdmin) ⇒
      }
    }

    whenReady(service.handleMakeUserAdmin(groupOutPeer, user2OutPeer)) { resp ⇒
      resp shouldEqual Error(GroupRpcErrors.UserAlreadyAdmin)
    }
  }

  def e18() = {
    val (user1, authId1, authSid1, _) = createUser()

    implicit val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    val about = Some("It is group for fun")
    whenReady(service.handleEditGroupAbout(groupOutPeer, 1L, about)) { resp ⇒
      resp should matchPattern {
        case Ok(_: ResponseSeqDate) ⇒
      }
    }

    whenReady(db.run(persist.GroupRepo.find(groupOutPeer.groupId))) { group ⇒
      group.get.about shouldEqual about
    }
  }

  def e19() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))
    val clientData2 = ClientData(authId2, createSessionId(), Some(AuthData(user2.id, authSid2)))

    val groupOutPeer = {
      implicit val clientData = clientData1
      createGroup("Fun group", Set(user2.id)).groupPeer
    }

    whenReady(service.jhandleEditGroupAbout(groupOutPeer, 1L, Some("It is group for fun"), clientData2)) { resp ⇒
      resp shouldEqual Error(CommonErrors.forbidden("Only admin can perform this action."))
    }
  }

  def e20() = {
    val (user1, authId1, authSid1, _) = createUser()

    implicit val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    whenReady(service.handleEditGroupAbout(groupOutPeer, 1L, None)) { resp ⇒
      resp should matchPattern {
        case Ok(_: ResponseSeqDate) ⇒
      }
    }

    whenReady(db.run(persist.GroupRepo.find(groupOutPeer.groupId))) { group ⇒
      group.get.about shouldEqual None
    }
  }

  def e21() = {
    val (user1, authId1, authSid1, _) = createUser()

    implicit val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    val longAbout = 1 to 300 map (e ⇒ ".") mkString ""
    whenReady(service.handleEditGroupAbout(groupOutPeer, 1L, Some(longAbout))) { resp ⇒
      resp shouldEqual Error(GroupRpcErrors.AboutTooLong)
    }

    val emptyAbout = ""
    whenReady(service.handleEditGroupAbout(groupOutPeer, 1L, Some(emptyAbout))) { resp ⇒
      resp shouldEqual Error(GroupRpcErrors.AboutTooLong)
    }

    whenReady(db.run(persist.GroupRepo.find(groupOutPeer.groupId))) { group ⇒
      group.get.about shouldEqual None
    }
  }

  def e22() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))
    val clientData2 = ClientData(authId2, createSessionId(), Some(AuthData(user2.id, authSid2)))

    val groupOutPeer = {
      implicit val cd = clientData1
      createGroup("Fun group", Set(user2.id)).groupPeer
    }

    val topic1 = Some("Fun stufff")
    whenReady(service.jhandleEditGroupTopic(groupOutPeer, 1L, topic1, clientData1)) { resp ⇒
      resp should matchPattern {
        case Ok(_: ResponseSeqDate) ⇒
      }
    }

    val topic2 = Some("Fun stuff. Typo!")
    whenReady(service.jhandleEditGroupTopic(groupOutPeer, 1L, topic2, clientData2)) { resp ⇒
      resp should matchPattern {
        case Ok(_: ResponseSeqDate) ⇒
      }
    }

    whenReady(db.run(persist.GroupRepo.find(groupOutPeer.groupId))) { group ⇒
      group.get.topic shouldEqual topic2
    }

  }

  def e23() = {
    val (user1, authId1, authSid1, _) = createUser()

    implicit val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    val longTopic = 1 to 300 map (e ⇒ ".") mkString ""
    whenReady(service.handleEditGroupTopic(groupOutPeer, 1L, Some(longTopic))) { resp ⇒
      resp shouldEqual Error(GroupRpcErrors.TopicTooLong)
    }

    val emptyTopic = ""
    whenReady(service.handleEditGroupTopic(groupOutPeer, 1L, Some(emptyTopic))) { resp ⇒
      resp shouldEqual Error(GroupRpcErrors.TopicTooLong)
    }

    whenReady(db.run(persist.GroupRepo.find(groupOutPeer.groupId))) { group ⇒
      group.get.topic shouldEqual None
    }

  }

  def e24() = {
    val (user1, authId1, authSid1, _) = createUser()

    implicit val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    whenReady(service.handleEditGroupTopic(groupOutPeer, 1L, None)) { resp ⇒
      resp should matchPattern {
        case Ok(_: ResponseSeqDate) ⇒
      }
    }

    whenReady(db.run(persist.GroupRepo.find(groupOutPeer.groupId))) { group ⇒
      group.get.topic shouldEqual None
    }

  }

  def e25() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1)))
    val clientData2 = ClientData(authId2, createSessionId(), Some(AuthData(user2.id, authSid2)))

    val groupOutPeer = {
      implicit val clientData = clientData1
      createGroup("Fun group", Set(user2.id)).groupPeer
    }
    val outPeer = ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)

    for (_ ← 1 to 6) {
      implicit val clientData = clientData1
      whenReady(messagingService.handleSendMessage(outPeer, Random.nextLong(), ApiTextMessage("hello", Vector.empty, None))) { _ ⇒ }
    }

    {
      implicit val clientData = clientData2

      whenReady(messagingService.handleLoadDialogs(Long.MaxValue, 100)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.unreadCount > 6 shouldEqual true
      }

      whenReady(service.handleLeaveGroup(groupOutPeer, Random.nextLong())) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
      }

      whenReady(messagingService.handleLoadDialogs(Long.MaxValue, 100)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.unreadCount shouldEqual 0
      }
    }

    for (_ ← 1 to 6) {
      implicit val clientData = clientData1
      whenReady(messagingService.handleSendMessage(outPeer, Random.nextLong(), ApiTextMessage("bye left user", Vector.empty, None))) { _ ⇒ }
    }

    {
      implicit val clientData = clientData2

      whenReady(messagingService.handleLoadDialogs(Long.MaxValue, 100)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.unreadCount shouldEqual 0
      }
    }

  }

  def e26() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()

    val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))
    val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(authId1, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = {
      implicit val clientData = clientData1
      createGroup("Fun group", Set(user2.id)).groupPeer
    }
    val outPeer = ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)

    for (_ ← 1 to 6) {
      implicit val clientData = clientData1
      whenReady(messagingService.handleSendMessage(outPeer, Random.nextLong(), ApiTextMessage("hello", Vector.empty, None))) { _ ⇒ }
    }

    {
      implicit val clientData = clientData2
      whenReady(messagingService.handleLoadDialogs(Long.MaxValue, 100)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.unreadCount > 6 shouldEqual true
      }
    }

    {
      implicit val clientData = clientData1
      whenReady(service.handleKickUser(groupOutPeer, Random.nextLong(), user2OutPeer)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
      }
    }

    {
      implicit val clientData = clientData2
      whenReady(messagingService.handleLoadDialogs(Long.MaxValue, 100)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.unreadCount shouldEqual 0
      }
    }

    for (_ ← 1 to 6) {
      implicit val clientData = clientData1
      whenReady(messagingService.handleSendMessage(outPeer, Random.nextLong(), ApiTextMessage("bye kicked user", Vector.empty, None))) { _ ⇒ }
    }

    {
      implicit val clientData = clientData2

      whenReady(messagingService.handleLoadDialogs(Long.MaxValue, 100)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.unreadCount shouldEqual 0
      }
    }

  }

  def markReadOnKickInPublic() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()

    val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))
    val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(authId1, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = {
      implicit val clientData = clientData1
      createPubGroup("Public group", "desc", Set(user2.id)).groupPeer
    }
    val outPeer = ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)

    for (_ ← 1 to 6) {
      implicit val clientData = clientData1
      whenReady(messagingService.handleSendMessage(outPeer, Random.nextLong(), ApiTextMessage("hello public", Vector.empty, None))) { _ ⇒ }
    }

    Thread.sleep(2000)

    {
      implicit val clientData = clientData2
      whenReady(messagingService.handleLoadDialogs(Long.MaxValue, 100)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.unreadCount shouldBe 6
      }
    }

    {
      implicit val clientData = clientData1
      whenReady(service.handleKickUser(groupOutPeer, Random.nextLong(), user2OutPeer)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
      }
    }

    {
      implicit val clientData = clientData2
      whenReady(messagingService.handleLoadDialogs(Long.MaxValue, 100)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.unreadCount shouldEqual 0
      }
    }

    for (_ ← 1 to 6) {
      implicit val clientData = clientData1
      whenReady(messagingService.handleSendMessage(outPeer, Random.nextLong(), ApiTextMessage("bye kicked user", Vector.empty, None))) { _ ⇒ }
    }

    {
      implicit val clientData = clientData2

      whenReady(messagingService.handleLoadDialogs(Long.MaxValue, 100)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.unreadCount shouldEqual 0
      }
    }

  }

  def e27() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()

    val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1)))
    val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(authId1, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = {
      implicit val clientData = clientData1
      createGroup("Fun group", Set(user2.id)).groupPeer
    }
    val outPeer = ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)

    for (_ ← 1 to 6) {
      implicit val clientData = clientData2
      sendMessageToGroup(groupOutPeer.groupId, ApiTextMessage("hello", Vector.empty, None))
    }

    {
      implicit val clientData = clientData1
      whenReady(service.handleKickUser(groupOutPeer, Random.nextLong(), user2OutPeer)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
      }
    }

    val user1Seq = whenReady(sequenceService.jhandleGetState(Vector.empty, clientData1))(_.toOption.get.seq)
    val user2Seq = whenReady(sequenceService.jhandleGetState(Vector.empty, clientData2))(_.toOption.get.seq)

    {
      implicit val clientData = clientData2

      val randomId = Random.nextLong()
      whenReady(messagingService.handleSendMessage(outPeer, randomId, ApiTextMessage("WTF? am i kicked?!!?!?!?!?!?!?!?!??!?!?!", Vector.empty, None))) { resp ⇒
        inside(resp) {
          case Error(err) ⇒ err.code shouldEqual 403
        }

        expectNoUpdate(user2Seq, classOf[UpdateMessageSent])

        whenReady(db.run(HistoryMessageRepo.find(user2.id, outPeer.asModel, Set(randomId)))) { ms ⇒
          ms shouldBe empty
        }
        whenReady(db.run(HistoryMessageRepo.find(user1.id, outPeer.asModel, Set(randomId)))) { ms ⇒
          ms shouldBe empty
        }

      }
    }
    {
      implicit val clientData = clientData1

      expectNoUpdate(user1Seq, classOf[UpdateMessage])
      expectNoUpdate(user1Seq, classOf[UpdateCountersChanged])
    }
  }
}
