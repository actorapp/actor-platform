package im.actor.server.api.rpc.service

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import org.scalatest.Inside._

import im.actor.api.rpc._
import im.actor.api.rpc.pubgroups.ResponseGetPublicGroups
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.pubgroups.PubgroupsServiceImpl
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceConfig, SequenceServiceImpl }
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.peermanagers.{ GroupPeerManager, UserEntity }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager
import im.actor.server.util.ACLUtils.userAccessHash
import im.actor.server.{ ImplicitFileStorageAdapter, BaseAppSuite, MessageParsing }

class PubgroupsServiceSpec extends BaseAppSuite with GroupsServiceHelpers with MessageParsing with ImplicitFileStorageAdapter {
  behavior of "PubgroupsService"

  it should "include number of friends in PubGroup" in t.e1

  it should "list all public groups with descrition" in t.e2

  it should "sort pubgroups by friends count and members count" in t.e3

  it should "show number of members and friends to any non-member" in t.e4

  implicit val sessionRegion = buildSessionRegionProxy()

  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
  implicit val groupPeerManagerRegion = GroupPeerManager.startRegion()

  val awsCredentials = new EnvironmentVariableCredentialsProvider()

  val groupInviteConfig = GroupInviteConfig("http://actor.im")
  val sequenceConfig = SequenceServiceConfig.load().toOption.get

  implicit val privatePeerManagerRegion = UserEntity.startRegion()

  val sequenceService = new SequenceServiceImpl(sequenceConfig)
  val messagingService = messaging.MessagingServiceImpl(mediator)
  implicit val groupService = new GroupsServiceImpl(groupInviteConfig)
  val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
  implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
  implicit val authService = buildAuthService()
  val pubGroupService = new PubgroupsServiceImpl
  val contactService = new ContactsServiceImpl()

  object t {
    val (user1, authId1, _) = createUser()
    val (user2, _, _) = createUser()
    val (user3, _, _) = createUser()
    val (user4, _, _) = createUser()
    val (user5, _, _) = createUser()
    val (user6, _, _) = createUser()
    val (user7, _, _) = createUser()
    val (user8, authId8, _) = createUser()

    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    val descriptions = List("Marvelous group for android developers group", "Group for iOS users", "You know it")

    val androidGroup = createPubGroup("Android group", descriptions(0), Set(user2.id, user4.id)).groupPeer
    val iosGroup = createPubGroup("iOS group", descriptions(1), Set(user2.id, user3.id, user4.id, user5.id, user6.id, user7.id)).groupPeer
    val scalaGroup = createPubGroup("Scala group", descriptions(2), Set(user2.id, user5.id, user6.id, user7.id, user8.id)).groupPeer
    val floodGroup = createPubGroup("Flood group", descriptions(2), Set(user2.id, user3.id, user5.id, user6.id, user7.id)).groupPeer

    def e1() = {
      whenReady(contactService.handleAddContact(user2.id, userAccessHash(clientData.authId, user2.id, getUserModel(user2.id).accessSalt)))(_ ⇒ ())
      whenReady(pubGroupService.handleGetPublicGroups()) { resp ⇒
        inside(resp) {
          case Ok(ResponseGetPublicGroups(groups)) ⇒
            val group = groups.find(_.id == androidGroup.groupId)
            group shouldBe defined
            group.get.friendsCount shouldEqual 1
        }
      }

      whenReady(contactService.handleAddContact(user3.id, userAccessHash(clientData.authId, user3.id, getUserModel(user3.id).accessSalt)))(_ ⇒ ()) //not in group. should not be in friends
      whenReady(contactService.handleAddContact(user4.id, userAccessHash(clientData.authId, user4.id, getUserModel(user4.id).accessSalt)))(_ ⇒ ())

      whenReady(pubGroupService.handleGetPublicGroups()) { resp ⇒
        inside(resp) {
          case Ok(ResponseGetPublicGroups(groups)) ⇒
            val group = groups.find(_.id == androidGroup.groupId)
            group shouldBe defined
            group.get.friendsCount shouldEqual 2
        }
      }
    }

    def e2() = {
      whenReady(pubGroupService.handleGetPublicGroups()) { resp ⇒
        inside(resp) {
          case Ok(ResponseGetPublicGroups(groups)) ⇒
            groups should have length 4
            groups.map(_.description).toSet shouldEqual descriptions.toSet
        }
      }
    }

    def e3() = {
      /**
       * Sorting according number of friends and members
       * ios -     friends = 3; members = 7
       * flood -   friends = 2; members = 6
       * android - friends = 2; members = 3
       * scala -   friends = 1; members = 6
       */
      whenReady(pubGroupService.handleGetPublicGroups()) { resp ⇒
        inside(resp) {
          case Ok(ResponseGetPublicGroups(groups)) ⇒
            groups.map(_.id) shouldEqual List(iosGroup, floodGroup, androidGroup, scalaGroup).map(_.groupId)
        }
      }
    }

    def e4() = {
      implicit val clientData = ClientData(authId8, sessionId, Some(user8.id))
      whenReady(contactService.handleAddContact(user2.id, userAccessHash(clientData.authId, user2.id, getUserModel(user2.id).accessSalt)))(_ ⇒ ())
      whenReady(contactService.handleAddContact(user3.id, userAccessHash(clientData.authId, user3.id, getUserModel(user3.id).accessSalt)))(_ ⇒ ())
      whenReady(contactService.handleAddContact(user4.id, userAccessHash(clientData.authId, user4.id, getUserModel(user4.id).accessSalt)))(_ ⇒ ())

      whenReady(pubGroupService.handleGetPublicGroups()) { resp ⇒
        inside(resp) {
          case Ok(ResponseGetPublicGroups(groups)) ⇒
            groups.find(_.id == floodGroup.groupId) foreach { g ⇒
              g.friendsCount shouldEqual 2
              g.membersCount shouldEqual 6
            }
            //sorting should be the same as in previous example cause we got same contacts
            groups.map(_.id) shouldEqual List(iosGroup, floodGroup, androidGroup, scalaGroup).map(_.groupId)
        }
      }
    }
  }

}
