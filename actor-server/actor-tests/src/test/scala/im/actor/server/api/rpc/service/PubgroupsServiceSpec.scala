package im.actor.server.api.rpc.service

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import org.scalatest.Inside._

import im.actor.api.rpc._
import im.actor.api.rpc.pubgroups.ResponseGetPublicGroups
import im.actor.server.api.rpc.service.auth.AuthSmsConfig
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.pubgroups.PubgroupsServiceImpl
import im.actor.server.api.rpc.service.sequence.SequenceServiceImpl
import im.actor.server.oauth.{ GmailProvider, OAuth2GmailConfig }
import im.actor.server.peermanagers.{ GroupPeerManager, PrivatePeerManager }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager
import im.actor.server.util.ACLUtils
import im.actor.server.{ BaseAppSuite, MessageParsing }

class PubgroupsServiceSpec extends BaseAppSuite with GroupsServiceHelpers with MessageParsing {
  behavior of "PubgroupsService"

  it should "include number of friends in PubGroup" in e1

  it should "list all public groups" in e2

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

  implicit val privatePeerManagerRegion = PrivatePeerManager.startRegion()

  val sequenceService = new SequenceServiceImpl
  val messagingService = messaging.MessagingServiceImpl(mediator)
  implicit val groupService = new GroupsServiceImpl(bucketName, groupInviteConfig)
  val oauth2GmailConfig = OAuth2GmailConfig.fromConfig(system.settings.config.getConfig("oauth.v2.gmail"))
  implicit val oauth2Service = new GmailProvider(oauth2GmailConfig)
  implicit val authSmsConfig = AuthSmsConfig.fromConfig(system.settings.config.getConfig("auth"))
  implicit val authService = buildAuthService()
  val pubGroupService = new PubgroupsServiceImpl
  val contactService = new ContactsServiceImpl()

  def e1() = {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()
    val (user3, _, _) = createUser()
    val (user4, _, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    createPubGroup("Android group", Set(user2.id, user4.id)).groupPeer

    whenReady(contactService.handleAddContact(
      user2.id,
      ACLUtils.userAccessHash(clientData.authId, user2.id, getUserModel(user2.id).accessSalt)
    ))(_ ⇒ ())
    whenReady(pubGroupService.handleGetPublicGroups()) { resp ⇒
      inside(resp) {
        case Ok(ResponseGetPublicGroups(groups)) ⇒
          groups should have length 1
          groups.head.friendsCount shouldEqual 1
      }
    }

    whenReady(contactService.handleAddContact(
      user3.id,
      ACLUtils.userAccessHash(clientData.authId, user3.id, getUserModel(user3.id).accessSalt)
    ))(_ ⇒ ()) //not in group. should not be in friends
    whenReady(contactService.handleAddContact(
      user4.id,
      ACLUtils.userAccessHash(clientData.authId, user4.id, getUserModel(user4.id).accessSalt)
    ))(_ ⇒ ())

    whenReady(pubGroupService.handleGetPublicGroups()) { resp ⇒
      inside(resp) {
        case Ok(ResponseGetPublicGroups(groups)) ⇒
          groups should have length 1
          groups.head.friendsCount shouldEqual 2
      }
    }
  }

  def e2() = {
    val (user1, authId1, _) = createUser()
    val (user2, _, _) = createUser()
    val (user3, _, _) = createUser()
    val (user4, _, _) = createUser()

    val sessionId = createSessionId()

    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    createPubGroup("Android group", Set(user2.id, user4.id)).groupPeer
    createPubGroup("Scala group", Set(user2.id, user3.id)).groupPeer

    whenReady(pubGroupService.handleGetPublicGroups()) { resp ⇒
      inside(resp) {
        case Ok(ResponseGetPublicGroups(groups)) ⇒
          groups should have length 3 //2 here, 1 from previous spec
      }
    }
  }

}
