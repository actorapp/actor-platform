package im.actor.server.api.rpc.service

import scala.concurrent._
import scala.concurrent.duration._

import slick.dbio.DBIO

import im.actor.api.rpc.contacts.PhoneToImport
import im.actor.api.{ rpc ⇒ api }, api._
import im.actor.server
import im.actor.server.BaseAppSuite
import im.actor.server.api.util
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManagerRegion, PresenceManager }
import im.actor.server.push.{ WeakUpdatesManager, SeqUpdatesManager }
import im.actor.server.social.SocialManager
import im.actor.server.user.UserOffice
import im.actor.server.util.{ UserUtils, ACLUtils }

class ContactsServiceSpec extends BaseAppSuite {
  behavior of "Contacts Service"

  "GetContacts handler" should "respond with isChanged = true and actual users if hash was emptySHA1" in s.getcontacts.changed

  it should "respond with isChanged = false if not changed" in s.getcontacts.notChanged

  "AddContact handler" should "add contact" in (s.addremove.add())

  "RemoveContact handler" should "remove contact" in (s.addremove.remove)

  "AddContact handler" should "add contact after remove" in (s.addremove.addAfterRemove)

  "ImportContacts handler" should "import contacts starting with 8 in RU" in (s.imprt.ru)

  object s {
    implicit val ec = system.dispatcher

    implicit val sessionRegion = buildSessionRegionProxy()

    implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
    implicit val socialManagerRegion = SocialManager.startRegion()
    implicit val userOfficeRegion = UserOffice.startRegion()

    implicit val service = new contacts.ContactsServiceImpl
    val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
    implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
    implicit val authService = buildAuthService()

    def addContact(userId: Int, userAccessSalt: String)(implicit clientData: api.ClientData) = {
      Await.result(service.handleAddContact(userId, ACLUtils.userAccessHash(clientData.authId, userId, userAccessSalt)), 3.seconds)
    }

    object getcontacts {
      val (user, authId, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = api.ClientData(authId, sessionId, Some(user.id))

      val userModels = for (i ← 1 to 3) yield {
        val user = createUser()._1.asModel()
        addContact(user.id, user.accessSalt)
        user
      }

      def changed() = {
        val expectedUsers = Await.result(db.run(DBIO.sequence(userModels map { user ⇒
          UserUtils.userStruct(user, None, clientData.authId)
        })), 3.seconds)

        whenReady(service.handleGetContacts(service.hashIds(Seq.empty))) { resp ⇒
          resp should matchPattern {
            case Ok(api.contacts.ResponseGetContacts(users, false)) if users == expectedUsers.toVector ⇒
          }
        }
      }

      def notChanged() = {
        whenReady(service.handleGetContacts(service.hashIds(userModels.map(_.id)))) { resp ⇒
          resp should matchPattern {
            case Ok(api.contacts.ResponseGetContacts(Vector(), true)) ⇒
          }
        }
      }
    }

    object addremove {
      val authId = createAuthId()
      val sessionId = createSessionId()
      val phoneNumber = buildPhone()
      val user = createUser(authId, phoneNumber)

      val (user2, _, _) = createUser()
      val user2Model = getUserModel(user2.id)
      val user2AccessHash = ACLUtils.userAccessHash(authId, user2.id, user2Model.accessSalt)

      implicit val clientData = api.ClientData(authId, sessionId, Some(user.id))

      def add(firstRun: Boolean = true, expectedUpdSeq: Int = 1000) = {
        whenReady(service.handleAddContact(user2.id, user2AccessHash)) { resp ⇒
          resp should matchPattern {
            case Ok(api.misc.ResponseSeq(seq, state)) if seq == expectedUpdSeq ⇒
          }
        }

        val expectedUsers = Vector(Await.result(
          db.run(server.util.UserUtils.userStruct(user2Model, None, clientData.authId)),
          3.seconds
        ))

        whenReady(service.handleGetContacts(service.hashIds(Seq.empty))) { resp ⇒
          resp should matchPattern {
            case Ok(api.contacts.ResponseGetContacts(expectedUsers, false)) ⇒
          }
        }
      }

      def remove() = {
        whenReady(service.handleRemoveContact(user2.id, user2AccessHash)) { resp ⇒
          resp should matchPattern {
            case Ok(api.misc.ResponseSeq(1002, state)) ⇒
          }
        }

        whenReady(service.handleGetContacts(service.hashIds(Seq.empty))) { resp ⇒
          resp should matchPattern {
            case Ok(api.contacts.ResponseGetContacts(Vector(), false)) ⇒
          }
        }
      }

      def addAfterRemove() = add(firstRun = false, expectedUpdSeq = 1003)
    }

    object imprt {
      val authId = createAuthId()
      val sessionId = createSessionId()
      val user = createUser(authId, 79031151515L)

      val user2 = createUser(79031161616L)

      val user3 = createUser(79031171717L)

      implicit val clientData = api.ClientData(authId, sessionId, Some(user.id))

      def ru() = {
        whenReady(service.handleImportContacts(Vector(PhoneToImport(79031161616L, Some("Kaizer 7"))), Vector.empty)) { resp ⇒
          resp.toOption.get.users.map(_.id) shouldEqual Vector(user2.id)
        }

        whenReady(service.handleImportContacts(Vector(PhoneToImport(89031171717L, Some("Kaizer 8"))), Vector.empty)) { resp ⇒
          resp.toOption.get.users.map(_.id) shouldEqual Vector(user3.id)
        }
      }
    }
  }

}
