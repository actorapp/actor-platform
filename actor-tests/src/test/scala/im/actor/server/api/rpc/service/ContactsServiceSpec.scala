package im.actor.server.api.rpc.service

import scala.concurrent._
import scala.concurrent.duration._

import slick.dbio.DBIO

import im.actor.api.{ rpc => api }
import im.actor.server.api.util
import im.actor.server.push.SeqUpdatesManager

class ContactsServiceSpec extends BaseServiceSpec {
  def is = sequential ^ s2"""
  GetContacts handler should
    respond with isChanged = true and actual users if hash was emptySHA1 ${s.getcontacts.changed}
    respond with isChanged = false if not changed ${s.getcontacts.notChanged}
  ContactsService
    AddContact handler should add contact ${s.addremove.add()}
    remove contact ${s.addremove.remove}
    add after remove ${s.addremove.addAfterRemove}
  """

  object s {
    val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
    val rpcApiService = buildRpcApiService()
    val sessionRegion = buildSessionRegion(rpcApiService)

    implicit val service = new contacts.ContactsServiceImpl(seqUpdManagerRegion)
    implicit val authService = buildAuthService(sessionRegion)
    implicit val ec = system.dispatcher

    def addContact(userId: Int, userAccessSalt: String)(implicit clientData: api.ClientData) = {
      Await.result(service.handleAddContact(userId, util.ACL.userAccessHash(clientData.authId, userId, userAccessSalt)), 3.seconds)
    }

    object getcontacts {
      val (user, authId, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = api.ClientData(authId, sessionId, Some(user.id))

      val userModels = for (i <- 1 to 3) yield {
        val user = createUser()._1.asModel()
        addContact(user.id, user.accessSalt)
        user
      }

      def changed = {
        val expectedUsers = Await.result(db.run(DBIO.sequence(userModels map { user =>
          util.User.struct(user, None, clientData.authId)
        })), 3.seconds)

        service.handleGetContacts(service.hashIds(Seq.empty)) must beOk(
          api.contacts.ResponseGetContacts(expectedUsers.toVector, false)
        ).await
      }

      def notChanged = {
        service.handleGetContacts(service.hashIds(userModels.map(_.id))) must beOk(
          api.contacts.ResponseGetContacts(Vector.empty, true)
        ).await
      }
    }

    object addremove {
      val authId = createAuthId()
      val sessionId = createSessionId()
      val phoneNumber = buildPhone()
      val user = createUser(authId, phoneNumber)

      val (user2, _, _) = createUser()
      val user2Model = getUserModel(user2.id)
      val user2AccessHash = util.ACL.userAccessHash(authId, user2.id, user2Model.accessSalt)

      implicit val clientData = api.ClientData(authId, sessionId, Some(user.id))

      def add(firstRun: Boolean = true, expectedUpdSeq: Int = 1000) = {
        service.handleAddContact(user2.id, user2AccessHash) must beOkLike {
          case api.misc.ResponseSeq(seq, state) if seq == expectedUpdSeq => ok
        }.await

        val expectedUsers = Vector(Await.result(
          db.run(util.User.struct(user2Model, None, clientData.authId)),
          3.seconds
        ))

        service.handleGetContacts(service.hashIds(Seq.empty)) must beOk(
          api.contacts.ResponseGetContacts(expectedUsers, false)
        ).await
      }

      def remove = {
        service.handleRemoveContact(user2.id, user2AccessHash) must beOkLike {
          case api.misc.ResponseSeq(1002, state) => ok
        }.await

        service.handleGetContacts(service.hashIds(Seq.empty)) must beOk(
          api.contacts.ResponseGetContacts(Vector.empty, false)
        ).await
      }

      def addAfterRemove = add(firstRun = false, expectedUpdSeq = 1003)
    }

  }

}
