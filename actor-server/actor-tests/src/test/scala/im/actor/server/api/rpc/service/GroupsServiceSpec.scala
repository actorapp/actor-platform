package im.actor.server.api.rpc.service

import im.actor.api.rpc._
import im.actor.api.rpc.counters.UpdateCountersChanged
import im.actor.api.rpc.groups._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeer, ApiPeerType, ApiUserOutPeer }
import im.actor.server._
import im.actor.server.acl.ACLUtils
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupRpcErrors, GroupsServiceImpl }
import im.actor.server.group.{ GroupExtension, GroupServiceMessages }
import im.actor.server.model.PeerType
import im.actor.server.persist.HistoryMessageRepo
import slick.dbio.DBIO

import scala.util.Random

final class GroupsServiceSpec
  extends BaseAppSuite
  with GroupsServiceHelpers
  with MessageParsing
  with MessagingSpecHelpers
  with ImplicitSequenceService
  with ImplicitAuthService
  with ImplicitMessagingService
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

  it should "send UpdateChatGroupsChanged to all group members on group creation" in updateChatGroupsChanged

  "Creator of group" should "be group admin" in e14

  "MakeUserAdmin" should "allow group member to become admin" in e15

  it should "forbid to perform action by non-admin" in e16

  it should "return error when user is already admin" in e17

  "EditGroupAbout" should "allow group admin to change 'about'" in e18

  it should "forbid to change 'about' by non-admin" in e19

  it should "set 'about' to empty when None comes" in e20

  it should "forbid to set 'about' field longer than 255 characters" in e21

  "EditGroupTopic" should "allow any group member to change topic" in e22

  it should "forbid to set topic longer than 255 characters" in e23

  it should "set topic to empty when None comes" in e24

  "Left user" should "have zero counter in given group" in e25

  "Kicked user" should "have zero counter in given group" in e26

  it should "not be able to write to group" in kickedCantWrite

  val groupInviteConfig = GroupInviteConfig("http://actor.im")
  val groupExt = GroupExtension(system)

  implicit val service = new GroupsServiceImpl(groupInviteConfig)

  def sendInvitesOnCreate() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, _, _, _) = createUser()

    val sessionId = createSessionId()

    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    expectUpdate(classOf[UpdateChatGroupsChanged])(identity)
    expectUpdate(classOf[UpdateGroupUserInvitedObsolete])(identity)
    expectUpdate(classOf[UpdateGroupInviteObsolete])(identity)

    val groupId = groupOutPeer.groupId
    groupExt.isMember(groupId, user1.id).futureValue shouldEqual true
    groupExt.isMember(groupId, user2.id).futureValue shouldEqual true
  }

  def sendUpdatesOnInvite() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
    val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData1.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    {
      implicit val clientData = clientData1
      val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

      whenReady(service.handleInviteUser(groupOutPeer, Random.nextLong(), user2OutPeer, Vector.empty)) { resp ⇒
        resp should matchPattern {
          case Ok(ResponseSeqDate(4, _, _)) ⇒
        }
      }
      expectUpdate(classOf[UpdateGroupUserInvitedObsolete])(identity)
      expectUpdate(classOf[UpdateGroupInviteObsolete])(identity)
      expectUpdate(classOf[UpdateChatGroupsChanged])(identity)

      // produced by create in group v2
      expectNoUpdate(emptyState, classOf[UpdateGroupMemberChanged])
      expectNoUpdate(emptyState, classOf[UpdateGroupAboutChanged])
      expectNoUpdate(emptyState, classOf[UpdateGroupAvatarChanged])
      expectNoUpdate(emptyState, classOf[UpdateGroupTopicChanged])
      expectNoUpdate(emptyState, classOf[UpdateGroupTitleChanged])
      expectNoUpdate(emptyState, classOf[UpdateGroupOwnerChanged])

      // produced by invite in group v2
      expectNoUpdate(emptyState, classOf[UpdateGroupMembersUpdated])

      // service messages from create/invite in group v2
      expectNoUpdate(emptyState, classOf[UpdateMessage])
    }

    {
      implicit val clientData = clientData2
      expectUpdate(classOf[UpdateGroupInviteObsolete])(identity)
      //UpdateChatGroupsChanged will come after creation of dialog
    }

  }

  def sendUpdatesOnTitleChange() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
    val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))

    {
      implicit val clientData = clientData1

      val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

      whenReady(service.handleEditGroupTitle(groupOutPeer, Random.nextLong(), "Very fun group", Vector.empty)) { resp ⇒
        resp should matchPattern {
          case Ok(ResponseSeqDate(5, _, _)) ⇒
        }
      }
      expectUpdate(classOf[UpdateChatGroupsChanged])(identity)
      expectUpdate(classOf[UpdateGroupUserInvitedObsolete])(identity)
      expectUpdate(classOf[UpdateGroupInviteObsolete])(identity)
      expectUpdate(classOf[UpdateGroupTitleChangedObsolete])(identity)

      // produced by create in group v2
      expectNoUpdate(emptyState, classOf[UpdateGroupMemberChanged])
      expectNoUpdate(emptyState, classOf[UpdateGroupAboutChanged])
      expectNoUpdate(emptyState, classOf[UpdateGroupAvatarChanged])
      expectNoUpdate(emptyState, classOf[UpdateGroupTopicChanged])
      expectNoUpdate(emptyState, classOf[UpdateGroupTitleChanged])
      expectNoUpdate(emptyState, classOf[UpdateGroupOwnerChanged])

      // produced by chnage group title group v2
      //TODO: it won't work this way
      expectNoUpdate(emptyState, classOf[UpdateGroupTitleChanged])

      // service messages from create/change title in group v2
      expectNoUpdate(emptyState, classOf[UpdateMessage])
    }

    {
      implicit val clientData = clientData2
      expectUpdate(classOf[UpdateGroupInviteObsolete])(identity)
      expectUpdate(classOf[UpdateGroupTitleChangedObsolete])(identity)
      //UpdateChatGroupsChanged will come after creation of dialog
    }

  }

  //TODO: write this spec in more readable way
  def e4() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    val clientDataUser1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientDataUser1.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = {
      implicit val cd = clientDataUser1
      createGroup("Fun group", Set.empty).groupPeer
    }

    {
      implicit val cd = clientDataUser1

      whenReady(db.run(persist.HistoryMessageRepo.find(user1.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 1
        serviceMessages
          .map { e ⇒ parseMessage(e.messageContentData) } shouldEqual
          Vector(Right(GroupServiceMessages.groupCreated))

      }

      whenReady(service.handleInviteUser(groupOutPeer, Random.nextLong(), user2OutPeer, Vector.empty)) { resp ⇒
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
      whenReady(service.handleRemoveGroupAvatar(groupOutPeer, Random.nextLong(), Vector.empty)) { resp ⇒
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

      whenReady(service.handleEditGroupTitle(groupOutPeer, Random.nextLong(), "Not fun group", Vector.empty)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
        whenReady(db.run(persist.HistoryMessageRepo.find(user1.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
          serviceMessages should have length 4
          serviceMessages.map { e ⇒ parseMessage(e.messageContentData) }.head shouldEqual Right(GroupServiceMessages.changedTitle("Not fun group"))
        }
      }

    }

    {
      implicit val cd = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))
      whenReady(service.handleLeaveGroup(groupOutPeer, Random.nextLong(), Vector.empty)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
        whenReady(db.run(persist.HistoryMessageRepo.find(user1.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
          serviceMessages should have length 6
          val msgs = serviceMessages.map(e ⇒ parseMessage(e.messageContentData))
          msgs(0) shouldEqual Right(GroupServiceMessages.userLeft)
          msgs(1) shouldEqual Right(GroupServiceMessages.userJoined)
        }
      }
    }

    {
      implicit val cd = clientDataUser1
      whenReady(service.handleInviteUser(groupOutPeer, Random.nextLong(), user2OutPeer, Vector.empty)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
        whenReady(db.run(persist.HistoryMessageRepo.find(user1.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
          serviceMessages should have length 7
          serviceMessages.map { e ⇒ parseMessage(e.messageContentData) }.head shouldEqual Right(GroupServiceMessages.userInvited(user2.id))
        }
      }

      whenReady(service.handleKickUser(groupOutPeer, Random.nextLong(), user2OutPeer, Vector.empty)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
        whenReady(db.run(persist.HistoryMessageRepo.find(user1.id, model.Peer(PeerType.Group, groupOutPeer.groupId)))) { serviceMessages ⇒
          serviceMessages should have length 8
          serviceMessages.map { e ⇒ parseMessage(e.messageContentData) }.head shouldEqual Right(GroupServiceMessages.userKicked(user2.id))
        }
      }
    }

  }

  def e5() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    {
      implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
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
      implicit val clientData = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))
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
    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    {
      implicit val clientData = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))
      whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
        resp should matchForbidden
      }
    }

  }

  def e7() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))

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
    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Invite Fun group", Set.empty).groupPeer

    whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
      inside(resp) {
        case Ok(ResponseInviteUrl(url)) ⇒
          url should startWith(groupInviteConfig.baseUrl)

          {
            implicit val clientData = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))
            whenReady(service.handleJoinGroup(url, Vector.empty)) { resp ⇒
              resp should matchPattern {
                case Ok(ResponseJoinGroup(_, _, _, _, _, _, _)) ⇒
              }
            }
          }
      }
    }
    val memberIds = groupExt.getMemberIds(groupOutPeer.groupId).futureValue._1
    memberIds should have length 2
    memberIds should contain allOf (user1.id, user2.id)
  }

  def e9() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    implicit val clientData = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1, 42)))

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    val url = whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
      inside(resp) {
        case Ok(ResponseInviteUrl(url)) ⇒
          url should startWith(groupInviteConfig.baseUrl)
      }
      resp.toOption.get.url
    }

    {
      implicit val clientData = ClientData(authId2, createSessionId(), Some(AuthData(user2.id, authSid2, 42)))
      whenReady(msgService.handleMessageRead(groupOutPeer.asOutPeer, System.currentTimeMillis()))(identity)

      whenReady(service.handleJoinGroup(url, Vector.empty)) { resp ⇒
        inside(resp) {
          case Error(err) ⇒ err shouldEqual GroupRpcErrors.AlreadyJoined
        }
      }
    }
  }

  def e10() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
    val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData1.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val createGroupResponse = createGroup("Invite Fun group", Set.empty)

    val groupOutPeer = createGroupResponse.groupPeer

    whenReady(service.handleGetGroupInviteUrl(groupOutPeer)(clientData1)) { resp ⇒
      inside(resp) {
        case Ok(ResponseInviteUrl(url)) ⇒
          url should startWith(groupInviteConfig.baseUrl)

          whenReady(service.handleJoinGroup(url, Vector.empty)(clientData2))(_ ⇒ ())

          val state = mkSeqState(createGroupResponse.seq, createGroupResponse.state)
          expectUpdate(state, classOf[UpdateMessage]) { upd ⇒
            upd.message shouldEqual GroupServiceMessages.userJoined
          }
          expectUpdate(state, classOf[UpdateCountersChanged])(identity)
      }
    }
    val memberIds = groupExt.getMemberIds(groupOutPeer.groupId).futureValue._1
    memberIds should have length 2
    memberIds should contain allOf (user1.id, user2.id)
  }

  def e11() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
    val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData1.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = {
      implicit val clientData = clientData1
      val groupOutPeer = createGroup("Invite Fun group", Set.empty).groupPeer

      whenReady(service.handleInviteUser(groupOutPeer, Random.nextLong, user2OutPeer, Vector.empty)) { _ ⇒ }

      sendMessageToGroup(groupOutPeer.groupId, ApiTextMessage("This is message to initialize group dialog", Vector.empty, None))

      groupOutPeer
    }

    {
      implicit val clientData = clientData2
      // send it twice to ensure that ServiceMessage isn't sent twice
      whenReady(msgService.handleMessageRead(ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash), System.currentTimeMillis))(identity)
      whenReady(msgService.handleMessageRead(ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash), System.currentTimeMillis))(identity)
    }

    {
      implicit val clientData = clientData1
      expectUpdate(classOf[UpdateGroupInviteObsolete])(identity)
      expectUpdate(classOf[UpdateGroupUserInvitedObsolete])(identity)
      expectUpdate(classOf[UpdateMessageRead])(identity)
      expectUpdate(classOf[UpdateCountersChanged])(identity)
      expectUpdate(classOf[UpdateMessage])(identity)
    }
  }

  def userJoinedOnce() = {

    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1, 42)))
    val clientData2 = ClientData(authId2, createSessionId(), Some(AuthData(user2.id, authSid2, 42)))

    val groupOutPeer = {
      implicit val clientData = clientData1
      createGroup("Fun group", Set.empty).groupPeer
    }

    val url = whenReady(service.handleGetGroupInviteUrl(groupOutPeer)(clientData1)) { _.toOption.get.url }

    whenReady(service.handleJoinGroup(url, Vector.empty)(clientData2)) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseJoinGroup(_, _, _, _, _, _, _)) ⇒
      }
    }

    val peer = ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)

    whenReady(msgService.handleSendMessage(peer, 22324L, ApiTextMessage("hello", Vector.empty, None), None, None)(clientData1)) { _ ⇒ }

    whenReady(msgService.handleMessageRead(peer, System.currentTimeMillis)(clientData2)) { _ ⇒ }

    {
      implicit val clientData = clientData1
      expectUpdate(classOf[UpdateChatGroupsChanged])(identity)
      expectUpdate(classOf[UpdateGroupInviteObsolete])(identity)
      expectUpdate(classOf[UpdateMessageSent])(identity)
      expectUpdate(classOf[UpdateMessage])(identity)
      expectUpdate(classOf[UpdateCountersChanged])(identity)
      expectUpdate(classOf[UpdateMessageRead])(identity)
    }
  }

  def e13() = {
    val (user1, authId1, authSid1, _) = createUser()

    implicit val clientData = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1, 42)))

    whenReady(service.handleCreateGroupObsolete(1L, "", Vector.empty)) { resp ⇒
      inside(resp) {
        case Error(GroupRpcErrors.InvalidTitle) ⇒
      }
    }
  }

  def updateChatGroupsChanged() = {
    val sessionId = createSessionId()

    val users = for (i ← 1 to 5) yield createUser()
    val userIds = (users map (_._1.id)).toSet

    val groupPeer = {
      val (user, authId, authSid, _) = users.head
      implicit val cd = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))
      createGroup("Fun group", userIds).groupPeer
    }

    users foreach {
      case (user, authId, authSid, _) ⇒
        implicit val cd = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))
        expectUpdate(classOf[UpdateChatGroupsChanged]) { u ⇒
          val groupDialogs = u.dialogs.find(_.key == "groups")
          groupDialogs shouldBe defined
          val dialogsShort = groupDialogs.get.dialogs
          dialogsShort should have length 1
          dialogsShort.head.peer shouldEqual ApiPeer(ApiPeerType.Group, groupPeer.groupId)
        }
    }
  }

  def e14() = {
    val (user1, authId1, authSid1, _) = createUser()
    implicit val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1, 42)))

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    val members = groupExt.getApiFullStruct(groupOutPeer.groupId, user1.id).futureValue.members
    val optCreator = members.find(_.userId == user1.id)
    optCreator shouldBe defined
    optCreator.get.isAdmin shouldEqual Some(true)
  }

  def e15() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    implicit val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1, 42)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData1.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    whenReady(service.handleMakeUserAdminObsolete(groupOutPeer, user2OutPeer)) { resp ⇒
      inside(resp) {
        case Ok(ResponseMakeUserAdminObsolete(members, _, _)) ⇒
          members.find(_.userId == user2.id) foreach (_.isAdmin shouldEqual Some(true))
      }
    }
  }

  def e16() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()
    val (user3, authId3, authSid3, _) = createUser()

    val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1, 42)))
    val clientData2 = ClientData(authId2, createSessionId(), Some(AuthData(user2.id, authSid2, 42)))

    val user3Model = getUserModel(user3.id)
    val user3AccessHash = ACLUtils.userAccessHash(clientData2.authId, user3.id, user3Model.accessSalt)
    val user3OutPeer = ApiUserOutPeer(user3.id, user3AccessHash)

    val groupOutPeer = {
      implicit val clientData = clientData1
      createGroup("Fun group", Set(user2.id)).groupPeer
    }

    whenReady(service.handleMakeUserAdmin(groupOutPeer, user3OutPeer)(clientData2)) { resp ⇒
      resp shouldEqual Error(CommonRpcErrors.forbidden("Only admin can perform this action."))
    }
  }

  def e17() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    implicit val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1, 42)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData1.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    whenReady(service.handleMakeUserAdminObsolete(groupOutPeer, user2OutPeer)) { resp ⇒
      resp should matchPattern {
        case Ok(_: ResponseMakeUserAdminObsolete) ⇒
      }
    }

    whenReady(service.handleMakeUserAdmin(groupOutPeer, user2OutPeer)) { resp ⇒
      resp shouldEqual Error(GroupRpcErrors.UserAlreadyAdmin)
    }
  }

  def e18() = {
    val (user, authId, authSid, _) = createUser()

    implicit val cd = ClientData(authId, createSessionId(), Some(AuthData(user.id, authSid, 42)))

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    val about = Some("It is group for fun")
    whenReady(service.handleEditGroupAbout(groupOutPeer, 1L, about, Vector.empty)) { resp ⇒
      resp should matchPattern {
        case Ok(_: ResponseSeqDate) ⇒
      }
    }

    val groupAbout = groupExt.getApiFullStruct(groupOutPeer.groupId, user.id).futureValue.about
    groupAbout shouldEqual about
  }

  def e19() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1, 42)))
    val clientData2 = ClientData(authId2, createSessionId(), Some(AuthData(user2.id, authSid2, 42)))

    val groupOutPeer = {
      implicit val clientData = clientData1
      createGroup("Fun group", Set(user2.id)).groupPeer
    }

    whenReady(service.handleEditGroupAbout(groupOutPeer, 1L, Some("It is group for fun"), Vector.empty)(clientData2)) { resp ⇒
      resp shouldEqual Error(CommonRpcErrors.forbidden("Only admin can perform this action."))
    }
  }

  def e20() = {
    val (user, authId, authSid, _) = createUser()

    implicit val cd = ClientData(authId, createSessionId(), Some(AuthData(user.id, authSid, 42)))

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    whenReady(service.handleEditGroupAbout(groupOutPeer, 1L, None, Vector.empty)) { resp ⇒
      resp should matchPattern {
        case Ok(_: ResponseSeqDate) ⇒
      }
    }

    val groupAbout = groupExt.getApiFullStruct(groupOutPeer.groupId, user.id).futureValue.about
    groupAbout shouldEqual None
  }

  def e21() = {
    val (user, authId, authSid, _) = createUser()

    implicit val clientData1 = ClientData(authId, createSessionId(), Some(AuthData(user.id, authSid, 42)))

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    val longAbout = 1 to 300 map (e ⇒ ".") mkString ""
    whenReady(service.handleEditGroupAbout(groupOutPeer, 1L, Some(longAbout), Vector.empty)) { resp ⇒
      resp shouldEqual Error(GroupRpcErrors.AboutTooLong)
    }

    val groupAbout = groupExt.getApiFullStruct(groupOutPeer.groupId, user.id).futureValue.about
    groupAbout shouldEqual None
  }

  def e22() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1, 42)))
    val clientData2 = ClientData(authId2, createSessionId(), Some(AuthData(user2.id, authSid2, 42)))

    val groupOutPeer = {
      implicit val cd = clientData1
      createGroup("Fun group", Set(user2.id)).groupPeer
    }

    val topic1 = Some("Fun stufff")
    whenReady(service.handleEditGroupTopic(groupOutPeer, 1L, topic1, Vector.empty)(clientData1)) { resp ⇒
      resp should matchPattern {
        case Ok(_: ResponseSeqDate) ⇒
      }
    }

    val topic2 = Some("Fun stuff. Typo!")
    whenReady(service.handleEditGroupTopic(groupOutPeer, 2L, topic2, Vector.empty)(clientData2)) { resp ⇒
      resp should matchPattern {
        case Ok(_: ResponseSeqDate) ⇒
      }
    }

    val groupTopic = groupExt.getApiStruct(groupOutPeer.groupId, user1.id).futureValue.theme
    groupTopic shouldEqual topic2
  }

  def e23() = {
    val (user, authId, authSid, _) = createUser()

    implicit val clientData1 = ClientData(authId, createSessionId(), Some(AuthData(user.id, authSid, 42)))

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    val longTopic = 1 to 300 map (e ⇒ ".") mkString ""
    whenReady(service.handleEditGroupTopic(groupOutPeer, 1L, Some(longTopic), Vector.empty)) { resp ⇒
      resp shouldEqual Error(GroupRpcErrors.TopicTooLong)
    }

    val groupTopic = groupExt.getApiFullStruct(groupOutPeer.groupId, user.id).futureValue.theme
    groupTopic shouldEqual None
  }

  def e24() = {
    val (user, authId, authSid, _) = createUser()

    implicit val clientData1 = ClientData(authId, createSessionId(), Some(AuthData(user.id, authSid, 42)))

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    whenReady(service.handleEditGroupTopic(groupOutPeer, 1L, None, Vector.empty)) { resp ⇒
      resp should matchPattern {
        case Ok(_: ResponseSeqDate) ⇒
      }
    }

    val groupTopic = groupExt.getApiFullStruct(groupOutPeer.groupId, user.id).futureValue.theme
    groupTopic shouldEqual None
  }

  def e25() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val clientData1 = ClientData(authId1, createSessionId(), Some(AuthData(user1.id, authSid1, 42)))
    val clientData2 = ClientData(authId2, createSessionId(), Some(AuthData(user2.id, authSid2, 42)))

    val groupOutPeer = {
      implicit val clientData = clientData1
      createGroup("Fun group", Set(user2.id)).groupPeer
    }

    for (_ ← 1 to 6) {
      implicit val cd = clientData1
      sendMessageToGroup(groupOutPeer.groupId, textMessage("hello"))
    }

    {
      implicit val cd = clientData2

      whenReady(msgService.handleLoadDialogs(Long.MaxValue, 100, Vector.empty)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.peer.id shouldEqual groupOutPeer.groupId
        dialog.unreadCount should be > 6
      }

      whenReady(service.handleLeaveGroup(groupOutPeer, Random.nextLong(), Vector.empty)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
      }

      whenReady(msgService.handleLoadDialogs(Long.MaxValue, 100, Vector.empty)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.peer.id shouldEqual groupOutPeer.groupId
        dialog.unreadCount shouldEqual 0
      }
    }

    for (_ ← 1 to 6) {
      implicit val cd = clientData1
      sendMessageToGroup(groupOutPeer.groupId, textMessage("bye left user"))
    }

    {
      implicit val cd = clientData2

      whenReady(msgService.handleLoadDialogs(Long.MaxValue, 100, Vector.empty)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.peer.id shouldEqual groupOutPeer.groupId
        dialog.unreadCount shouldEqual 0
      }
    }

  }

  def e26() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()

    val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
    val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))

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
      whenReady(msgService.handleSendMessage(outPeer, Random.nextLong(), ApiTextMessage("hello", Vector.empty, None), None, None)) { _ ⇒ }
    }

    {
      implicit val clientData = clientData2
      whenReady(msgService.handleLoadDialogs(Long.MaxValue, 100, Vector.empty)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.unreadCount > 6 shouldEqual true
      }
    }

    {
      implicit val clientData = clientData1
      whenReady(service.handleKickUser(groupOutPeer, Random.nextLong(), user2OutPeer, Vector.empty)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
      }
    }

    {
      implicit val clientData = clientData2
      whenReady(msgService.handleLoadDialogs(Long.MaxValue, 100, Vector.empty)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.unreadCount shouldEqual 0
      }
    }

    for (_ ← 1 to 6) {
      implicit val clientData = clientData1
      whenReady(msgService.handleSendMessage(outPeer, Random.nextLong(), ApiTextMessage("bye kicked user", Vector.empty, None), None, None)) { _ ⇒ }
    }

    {
      implicit val clientData = clientData2

      whenReady(msgService.handleLoadDialogs(Long.MaxValue, 100, Vector.empty)) { resp ⇒
        val dialog = resp.toOption.get.dialogs.head
        dialog.unreadCount shouldEqual 0
      }
    }

  }

  def kickedCantWrite() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()

    val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
    val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))

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
      whenReady(service.handleKickUser(groupOutPeer, Random.nextLong(), user2OutPeer, Vector.empty)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
      }
    }

    val user1State = getCurrentState(clientData1)
    val user2State = getCurrentState(clientData2)

    {
      implicit val clientData = clientData2

      val randomId = Random.nextLong()
      whenReady(msgService.handleSendMessage(outPeer, randomId, ApiTextMessage("WTF? am i kicked?!!?!?!?!?!?!?!?!??!?!?!", Vector.empty, None), None, None)) { resp ⇒
        inside(resp) {
          case Error(err) ⇒ err.code shouldEqual 403
        }

        expectNoUpdate(user2State, classOf[UpdateMessageSent])

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

      expectNoUpdate(user1State, classOf[UpdateMessage])
      expectNoUpdate(user1State, classOf[UpdateCountersChanged])
    }
  }
}
