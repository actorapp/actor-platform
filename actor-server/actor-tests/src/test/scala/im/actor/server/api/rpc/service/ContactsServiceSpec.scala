package im.actor.server.api.rpc.service

import im.actor.server.acl.ACLUtils
import im.actor.server.user.{ UserUtils, UserOffice }

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Random

import slick.dbio.DBIO

import im.actor.api.rpc._
import im.actor.api.rpc.contacts.ApiPhoneToImport
import im.actor.api.{ rpc ⇒ api }
import im.actor.server
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server._

class ContactsServiceSpec
  extends BaseAppSuite
  with ImplicitUserRegions
  with ImplicitSessionRegionProxy
  with ImplicitAuthService {
  behavior of "Contacts Service"

  "GetContacts handler" should "respond with isChanged = true and actual users if hash was emptySHA1" in s.getcontacts.changed

  it should "respond with isChanged = false if not changed" in s.getcontacts.notChanged

  it should "respond with isChanged = false for all non deleted contacts" in s.getcontacts.notChangedAfterRemove

  "AddContact handler" should "add contact" in (s.addremove.add())

  "RemoveContact handler" should "remove contact" in (s.addremove.remove)

  "AddContact handler" should "add contact after remove" in (s.addremove.addAfterRemove)

  "ImportContacts handler" should "import contacts starting with 8 in RU" in (s.imprt.ru)

  object s {
    implicit val ec = system.dispatcher

    implicit val sessionRegion = buildSessionRegionProxy()

    implicit val service = new contacts.ContactsServiceImpl

    def addContact(userId: Int, userAccessSalt: String)(implicit clientData: api.ClientData) = {
      Await.result(service.handleAddContact(userId, ACLUtils.userAccessHash(clientData.authId, userId, userAccessSalt)), 3.seconds)
    }

    def removeContact(userId: Int, userAccessSalt: String)(implicit clientData: api.ClientData) = {
      Await.result(service.handleRemoveContact(userId, ACLUtils.userAccessHash(clientData.authId, userId, userAccessSalt)), 3.seconds)
    }

    object getcontacts {
      val (user, authId, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = api.ClientData(authId, sessionId, Some(user.id))

      val userModels = for (i ← 1 to 10) yield {
        val user = createUser()._1.asModel()
        addContact(user.id, user.accessSalt)
        user
      }

      def changed() = {
        val expectedUsers = Await.result(Future.sequence(userModels map { u ⇒
          UserOffice.getApiStruct(u.id, user.id, authId)
        }), 3.seconds)

        whenReady(service.handleGetContacts(service.hashIds(Seq.empty))) { resp ⇒
          resp should matchPattern {
            case Ok(api.contacts.ResponseGetContacts(users, false)) if users.toSet == expectedUsers.toSet ⇒
          }
        }
      }

      def notChanged() = {
        val shuffledIds = Random.shuffle(userModels.map(_.id))
        whenReady(service.handleGetContacts(service.hashIds(shuffledIds))) { resp ⇒
          resp should matchPattern {
            case Ok(api.contacts.ResponseGetContacts(Vector(), true)) ⇒
          }
        }
      }

      def notChangedAfterRemove() = {
        userModels.take(5) foreach { user ⇒
          removeContact(user.id, user.accessSalt)
        }
        val activeContactIds = Random.shuffle(userModels.drop(5).map(_.id))
        whenReady(service.handleGetContacts(service.hashIds(activeContactIds))) { resp ⇒
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
          UserOffice.getApiStruct(user2.id, user.id, authId),
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
            case Ok(api.contacts.ResponseGetContacts(Vector(), true)) ⇒
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
        whenReady(service.handleImportContacts(Vector(ApiPhoneToImport(79031161616L, Some("Kaizer 7"))), Vector.empty)) { resp ⇒
          resp.toOption.get.users.map(_.id) shouldEqual Vector(user2.id)
        }

        whenReady(service.handleImportContacts(Vector(ApiPhoneToImport(89031171717L, Some("Kaizer 8"))), Vector.empty)) { resp ⇒
          resp.toOption.get.users.map(_.id) shouldEqual Vector(user3.id)
        }
      }
    }

  }

}
