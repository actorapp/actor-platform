package im.actor.server.api.rpc.service

import im.actor.api.rpc._
import im.actor.api.rpc.contacts.ApiPhoneToImport
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.users.ApiSex
import im.actor.api.{ rpc ⇒ api }
import im.actor.server._
import im.actor.server.acl.ACLUtils
import im.actor.server.api.rpc.service.contacts.ContactsRpcErrors
import im.actor.server.user.UserExtension
import im.actor.util.misc.IdUtils

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Random

final class ContactsServiceSpec
  extends BaseAppSuite
  with ImplicitSessionRegion
  with ImplicitAuthService {
  behavior of "Contacts Service"

  "GetContacts handler" should "respond with isChanged = true and actual users if hash was emptySHA1" in s.getcontacts.changed

  it should "respond with isChanged = false if not changed" in s.getcontacts.notChanged

  it should "respond with isChanged = false for all non deleted contacts" in s.getcontacts.notChangedAfterRemove

  "AddContact handler" should "add contact" in s.addremove.add()

  it should "not add self to contacts" in s.addremove.cantAddSelf()

  "RemoveContact handler" should "remove contact" in s.addremove.remove

  "AddContact handler" should "add contact after remove" in s.addremove.addAfterRemove

  "ImportContacts handler" should "import contacts starting with 8 in RU" in s.imprt.ru

  "AddContact handler" should "add contact without a phone" in s.addremove.addWithoutPhone()

  object s {
    implicit val ec = system.dispatcher

    implicit val service = new contacts.ContactsServiceImpl

    private implicit val userExt = UserExtension(system)

    def addContact(userId: Int, userAccessSalt: String)(implicit clientData: api.ClientData) = {
      Await.result(service.handleAddContact(userId, ACLUtils.userAccessHash(clientData.authId, userId, userAccessSalt)), 5.seconds)
    }

    def removeContact(userId: Int, userAccessSalt: String)(implicit clientData: api.ClientData) = {
      Await.result(service.handleRemoveContact(userId, ACLUtils.userAccessHash(clientData.authId, userId, userAccessSalt)), 5.seconds)
    }

    object getcontacts {
      val (user, authId, authSid, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = api.ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

      val userModels = for (i ← 1 to 10) yield {
        val user = createUser()._1.asModel()
        addContact(user.id, user.accessSalt)
        user
      }

      def changed() = {
        val expectedUsers = Await.result(Future.sequence(userModels map { u ⇒
          UserExtension(system).getApiStruct(u.id, user.id, authId)
        }), 3.seconds)

        whenReady(service.handleGetContacts(service.hashIds(Seq.empty), Vector.empty)) { resp ⇒
          resp should matchPattern {
            case Ok(api.contacts.ResponseGetContacts(users, false, _)) if users.toSet == expectedUsers.toSet ⇒
          }
        }
      }

      def notChanged() = {
        val shuffledIds = Random.shuffle(userModels.map(_.id))
        whenReady(service.handleGetContacts(service.hashIds(shuffledIds), Vector.empty)) { resp ⇒
          resp should matchPattern {
            case Ok(api.contacts.ResponseGetContacts(Vector(), true, _)) ⇒
          }
        }
      }

      def notChangedAfterRemove() = {
        userModels.take(5) foreach { user ⇒
          removeContact(user.id, user.accessSalt)
        }
        val activeContactIds = Random.shuffle(userModels.drop(5).map(_.id))
        whenReady(service.handleGetContacts(service.hashIds(activeContactIds), Vector.empty)) { resp ⇒
          resp should matchPattern {
            case Ok(api.contacts.ResponseGetContacts(Vector(), true, _)) ⇒
          }
        }
      }
    }

    object addremove {
      val authId = createAuthId()
      val sessionId = createSessionId()
      val phoneNumber = buildPhone()
      val (user, authSid) = createUser(authId, phoneNumber)

      val (user2, _, _, _) = createUser()
      val user2AccessHash = ACLUtils.userAccessHash(authId, user2.id, getUserModel(user2.id).accessSalt)

      implicit val clientData = api.ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

      def add(firstRun: Boolean = true, expectedUpdSeq: Int = 1) = {
        whenReady(service.handleAddContact(user2.id, user2AccessHash)) { resp ⇒
          resp should matchPattern {
            case Ok(ResponseSeq(seq, state)) if seq == expectedUpdSeq ⇒
          }
        }

        val ExpectedUsers = Vector(Await.result(
          UserExtension(system).getApiStruct(user2.id, user.id, authId),
          3.seconds
        ))

        whenReady(service.handleGetContacts(service.hashIds(Seq.empty), Vector.empty)) { resp ⇒
          resp should matchPattern {
            case Ok(api.contacts.ResponseGetContacts(ExpectedUsers, false, _)) ⇒
          }
        }
      }

      def cantAddSelf() = {
        val userAccessHash = ACLUtils.userAccessHash(authId, user.id, getUserModel(user.id).accessSalt)
        whenReady(service.handleAddContact(user.id, userAccessHash)) { resp ⇒
          inside(resp) {
            case Error(ContactsRpcErrors.CantAddSelf) ⇒
          }
        }
        whenReady(service.handleGetContacts(service.hashIds(Seq.empty), Vector.empty)) { resp ⇒
          inside(resp) {
            case Ok(api.contacts.ResponseGetContacts(users, false, _)) ⇒ users should not contain user
          }
        }
      }

      def addWithoutPhone() = {
        val userId = IdUtils.nextIntId()
        whenReady(userExt.create(userId, ACLUtils.nextAccessSalt(), Some("nickname"), "Name", "us", ApiSex.Unknown, isBot = true))(identity)
        whenReady(userExt.getAccessHash(userId, clientData.authId)) { accessSalt ⇒
          whenReady(service.handleAddContact(userId, accessSalt)) { rsp ⇒
            inside(rsp) {
              case Ok(ResponseSeq(_, _)) ⇒
            }
          }
        }
      }

      def remove() = {
        whenReady(service.handleRemoveContact(user2.id, user2AccessHash)) { resp ⇒
          resp should matchPattern {
            case Ok(ResponseSeq(3, state)) ⇒
          }
        }

        whenReady(service.handleGetContacts(service.hashIds(Seq.empty), Vector.empty)) { resp ⇒
          resp should matchPattern {
            case Ok(api.contacts.ResponseGetContacts(Vector(), true, _)) ⇒
          }
        }
      }

      def addAfterRemove() = add(firstRun = false, expectedUpdSeq = 4)
    }

    object imprt {
      val authId = createAuthId()
      val sessionId = createSessionId()
      val (user, authSid) = createUser(authId, 79031151515L)

      val (user2, _) = createUser(79031161616L)

      val (user3, _) = createUser(79031171717L)

      implicit val clientData = api.ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

      def ru() = {
        whenReady(service.handleImportContacts(Vector(ApiPhoneToImport(79031161616L, Some("Kaizer 7"))), Vector.empty, Vector.empty)) { resp ⇒
          resp.toOption.get.users.map(_.id) shouldEqual Vector(user2.id)
        }

        whenReady(service.handleImportContacts(Vector(ApiPhoneToImport(89031171717L, Some("Kaizer 8"))), Vector.empty, Vector.empty)) { resp ⇒
          resp.toOption.get.users.map(_.id) shouldEqual Vector(user3.id)
        }
      }
    }

  }

}
