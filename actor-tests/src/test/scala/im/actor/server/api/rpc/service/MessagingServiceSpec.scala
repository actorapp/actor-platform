package im.actor.server.api.rpc.service

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc._
import im.actor.api.rpc.files.FileLocation
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.{ PeerType, UserOutPeer }
import im.actor.server.api.rpc.service.groups.GroupsServiceImpl
import im.actor.server.persist
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager
import im.actor.server.util.ACLUtils

class MessagingServiceSpec extends BaseServiceSuite with GroupsServiceHelpers {
  behavior of "MessagingService"

  "Messaging" should "send messages" in s.privat.sendMessage

  it should "send group messages" in s.group.sendMessage

  it should "not send messages when user is not in group" in s.group.restrictAlienUser

  object s {
    implicit val sessionRegion = buildSessionRegionProxy()
    implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
    implicit val socialManagerRegion = SocialManager.startRegion()
    implicit val presenceManagerRegion = PresenceManager.startRegion()
    implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()

    val bucketName = "actor-uploads-test"
    val awsCredentials = new EnvironmentVariableCredentialsProvider()
    implicit val transferManager = new TransferManager(awsCredentials)

    implicit val service = new messaging.MessagingServiceImpl
    implicit val groupsService = new GroupsServiceImpl(bucketName)
    implicit val authService = buildAuthService()
    implicit val ec = system.dispatcher

    object privat {
      val (user, authId, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(user.id))

      val (user2, _, _) = createUser()
      val user2Model = getUserModel(user2.id)
      val user2AccessHash = ACLUtils.userAccessHash(authId, user2.id, user2Model.accessSalt)
      val user2Peer = peers.OutPeer(PeerType.Private, user2.id, user2AccessHash)

      def sendMessage() = {
        whenReady(service.handleSendMessage(user2Peer, 1L, TextMessage("Hi Shiva", None))) { resp ⇒
          resp should matchPattern {
            case Ok(ResponseSeqDate(1000, _, _)) ⇒
          }
        }
      }
    }

    object group {
      val (user1, authId1, _) = createUser()
      val (user2, authId2, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

      val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

      def sendMessage() = {
        whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, 2L, TextMessage("Hi again", None))) { resp ⇒
          resp should matchPattern {
            case Ok(ResponseSeqDate(1001, _, _)) ⇒
          }
        }

        whenReady(db.run(persist.sequence.SeqUpdate.find(authId2).head)) { u ⇒
          u.header should ===(UpdateMessage.header)
        }
      }

      def restrictAlienUser() = {
        val (alien, authIdAlien, _) = createUser()

        val alienClientData = ClientData(authId1, sessionId, Some(alien.id))

        whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, 3L, TextMessage("Hi again", None))(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

        whenReady(groupsService.handleEditGroupTitle(groupOutPeer, 4L, "Loosers")(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

        val (user3, authId3, _) = createUser()
        val user3OutPeer = UserOutPeer(user3.id, 11)

        whenReady(groupsService.handleInviteUser(groupOutPeer, 4L, user3OutPeer)(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

        val fileLocation = FileLocation(1L, 1L)
        whenReady(groupsService.handleEditGroupAvatar(groupOutPeer, 5L, fileLocation)(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

        whenReady(groupsService.handleRemoveGroupAvatar(groupOutPeer, 5L)(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

        whenReady(groupsService.handleLeaveGroup(groupOutPeer, 5L)(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

      }
    }
  }
}
