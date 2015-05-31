package im.actor.server.api.rpc.service

import scala.util.Random

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import org.scalatest.Inside._
import slick.dbio.DBIO

import im.actor.api.rpc._
import im.actor.api.rpc.groups._
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.UserOutPeer
import im.actor.server.api.rpc.service.groups.{ GroupErrors, GroupInviteConfig, GroupsServiceImpl, ServiceMessages }
import im.actor.server.api.rpc.service.messaging.GroupPeerManager
import im.actor.server.api.rpc.service.sequence.SequenceServiceImpl
import im.actor.server.models.Peer
import im.actor.server.{ MessageParsing, persist }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager
import im.actor.server.util.ACLUtils

class GroupsServiceSpec extends BaseServiceSuite with GroupsServiceHelpers with MessageParsing {
  behavior of "GroupsService"

  it should "send invites on group creation" in e1

  it should "send updates on group invite" in e2

  it should "send updates ot title change" in e3

  it should "persist service messages in history" in e4

  it should "generate invite url for group member" in e5

  it should "not generate invite url for group non members" in e6

  it should "revoke invite token and generate new token for group member" in e7

  it should "allow user to join group by correct invite link and send correct updates" in e8

  it should "not allow group member to join group by invite link" in e9

  it should "send updates on user join" in e10

  implicit val sessionRegion = buildSessionRegionProxy()

  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
  implicit val groupPeerManagerRegion = GroupPeerManager.startRegion()

  val bucketName = "actor-uploads-test"
  val awsCredentials = new EnvironmentVariableCredentialsProvider()
  implicit val transferManager = new TransferManager(awsCredentials)
  val groupInviteConfig = GroupInviteConfig("http://actor.im")

  val sequenceService = new SequenceServiceImpl
  implicit val service = new GroupsServiceImpl(bucketName, groupInviteConfig)
  implicit val authService = buildAuthService()
  implicit val ec = system.dispatcher

  def e1() = {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()

    val sessionId = createSessionId()

    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    whenReady(db.run(persist.sequence.SeqUpdate.findLast(authId2))) { uOpt ⇒
      val u = uOpt.get
      u.header should ===(UpdateGroupInvite.header)
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
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
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

  def e4() = {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = UserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    whenReady(db.run(persist.HistoryMessage.find(user1.id, Peer.group(groupOutPeer.groupId)))) { serviceMessages ⇒
      serviceMessages should have length 1
      serviceMessages
        .map { e ⇒ parseMessage(e.messageContentData) } shouldEqual
        Vector(Right(ServiceMessages.groupCreated))

    }

    whenReady(service.handleInviteUser(groupOutPeer, Random.nextLong(), user2OutPeer)) { resp ⇒
      resp should matchPattern { case Ok(_) ⇒ }
      whenReady(db.run(persist.HistoryMessage.find(user1.id, Peer.group(groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 2
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) } shouldEqual
          Vector(
            Right(ServiceMessages.userInvited(user2.id)),
            Right(ServiceMessages.groupCreated)
          )
      }
      whenReady(db.run(persist.HistoryMessage.find(user2.id, Peer.group(groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 1
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) } shouldEqual
          Vector(Right(ServiceMessages.userInvited(user2.id)))
      }
    }

    //TODO: is it ok to remove avatar of group without avatar
    whenReady(service.handleRemoveGroupAvatar(groupOutPeer, Random.nextLong())) { resp ⇒
      resp should matchPattern { case Ok(_) ⇒ }
      whenReady(db.run(persist.HistoryMessage.find(user1.id, Peer.group(groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 3
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) } shouldEqual
          Vector(
            Right(ServiceMessages.changedAvatar(None)),
            Right(ServiceMessages.userInvited(user2.id)),
            Right(ServiceMessages.groupCreated)
          )
      }
      whenReady(db.run(persist.HistoryMessage.find(user2.id, Peer.group(groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 2
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) } shouldEqual
          Vector(
            Right(ServiceMessages.changedAvatar(None)),
            Right(ServiceMessages.userInvited(user2.id))
          )
      }
    }

    whenReady(service.handleEditGroupTitle(groupOutPeer, Random.nextLong(), "Not fun group")) { resp ⇒
      resp should matchPattern { case Ok(_) ⇒ }
      whenReady(db.run(persist.HistoryMessage.find(user1.id, Peer.group(groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 4
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) }.head shouldEqual Right(ServiceMessages.changedTitle("Not fun group"))
      }
    }

    whenReady(service.handleLeaveGroup(groupOutPeer, Random.nextLong())(ClientData(authId2, sessionId, Some(user2.id)))) { resp ⇒
      resp should matchPattern { case Ok(_) ⇒ }
      whenReady(db.run(persist.HistoryMessage.find(user1.id, Peer.group(groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 5
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) }.head shouldEqual Right(ServiceMessages.userLeft(user2.id))
      }
    }

    whenReady(service.handleInviteUser(groupOutPeer, Random.nextLong(), user2OutPeer)) { resp ⇒
      resp should matchPattern { case Ok(_) ⇒ }
      whenReady(db.run(persist.HistoryMessage.find(user1.id, Peer.group(groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 6
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) }.head shouldEqual Right(ServiceMessages.userInvited(user2.id))
      }
    }

    whenReady(service.handleKickUser(groupOutPeer, Random.nextLong(), user2OutPeer)) { resp ⇒
      resp should matchPattern { case Ok(_) ⇒ }
      whenReady(db.run(persist.HistoryMessage.find(user1.id, Peer.group(groupOutPeer.groupId)))) { serviceMessages ⇒
        serviceMessages should have length 7
        serviceMessages.map { e ⇒ parseMessage(e.messageContentData) }.head shouldEqual Right(ServiceMessages.userKicked(user2.id))
      }
    }

  }

  def e5() = {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = UserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    {
      implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))
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
      implicit val clientData = ClientData(authId2, sessionId, Some(user2.id))
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
          persist.GroupInviteToken.find(groupOutPeer.groupId, user1.id),
          persist.GroupInviteToken.find(groupOutPeer.groupId, user2.id)
        ))
      } yield tokens.flatten
    whenReady(db.run(findTokens)) { tokens ⇒
      tokens should have length 2
      tokens.foreach(_.groupId shouldEqual groupOutPeer.groupId)
      tokens.map(_.creatorId) should contain allOf (user1.id, user2.id)
    }
  }

  def e6() = {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = UserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer

    {
      implicit val clientData = ClientData(authId2, sessionId, Some(user2.id))
      whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
        resp shouldEqual Error(CommonErrors.UserNotAuthorized)
      }
    }

  }

  def e7() = {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = UserOutPeer(user2.id, user2AccessHash)

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

    whenReady(db.run(persist.GroupInviteToken.find(groupOutPeer.groupId, user1.id))) { tokens ⇒
      tokens should have length 1
    }

  }

  def e8() = {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = UserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Invite Fun group", Set.empty).groupPeer

    whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
      inside(resp) {
        case Ok(ResponseInviteUrl(url)) ⇒
          url should startWith(groupInviteConfig.baseUrl)

          {
            implicit val clientData = ClientData(authId2, sessionId, Some(user2.id))
            whenReady(service.handleJoinGroup(url)) { resp ⇒
              resp should matchPattern {
                case Ok(ResponseJoinGroup(_, _, _, _)) ⇒
              }
            }
          }
      }
    }
    whenReady(db.run(persist.GroupUser.findUserIds(groupOutPeer.groupId))) { userIds ⇒
      userIds should have length 2
      userIds should contain allOf (user1.id, user2.id)
    }
  }

  def e9() = {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = UserOutPeer(user2.id, user2AccessHash)

    val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

    whenReady(service.handleGetGroupInviteUrl(groupOutPeer)) { resp ⇒
      inside(resp) {
        case Ok(ResponseInviteUrl(url)) ⇒
          url should startWith(groupInviteConfig.baseUrl)

          {
            implicit val clientData = ClientData(authId2, sessionId, Some(user2.id))
            whenReady(service.handleJoinGroup(url)) { resp ⇒
              inside(resp) {
                case Error(err) ⇒ err shouldEqual GroupErrors.UserAlreadyInvited
              }
            }
          }
      }
    }
  }

  def e10() = {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData1 = ClientData(authId1, sessionId, Some(user1.id))
    val clientData2 = ClientData(authId2, sessionId, Some(user2.id))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData1.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = UserOutPeer(user2.id, user2AccessHash)

    val createGroupResponse = createGroup("Invite Fun group", Set.empty)

    val groupOutPeer = createGroupResponse.groupPeer

    whenReady(service.jhandleGetGroupInviteUrl(groupOutPeer, clientData1)) { resp ⇒
      inside(resp) {
        case Ok(ResponseInviteUrl(url)) ⇒
          url should startWith(groupInviteConfig.baseUrl)

          whenReady(service.jhandleJoinGroup(url, clientData2))(_ ⇒ ())

          whenReady(sequenceService.jhandleGetDifference(createGroupResponse.seq, createGroupResponse.state, clientData1)) { diff ⇒
            val resp = diff.toOption.get

            val updates = resp.updates
            updates should have length 1

            val update = UpdateMessage.parseFrom(CodedInputStream.newInstance(updates.head.update)).right.toOption.get
            update.message shouldEqual ServiceMessages.userJoined
          }

          //TODO: find out how it should look like.
//          whenReady(sequenceService.jhandleGetDifference(0, Array.empty, clientData2)) { diff ⇒
//            val resp = diff.toOption.get
//
//            val updates = resp.updates
//            updates should have length 1
//
//            val update = UpdateMessageSent.parseFrom(CodedInputStream.newInstance(updates.head.update)) //.left.toOption.get
//          }
      }
    }
    whenReady(db.run(persist.GroupUser.findUserIds(groupOutPeer.groupId))) { userIds ⇒
      userIds should have length 2
      userIds should contain allOf (user1.id, user2.id)
    }
  }

}
