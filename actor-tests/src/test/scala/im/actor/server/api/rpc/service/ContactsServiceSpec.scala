package im.actor.server.api.rpc.service

import scala.concurrent._, duration._

import im.actor.api.{ rpc => api }
import im.actor.server.api.util
import im.actor.server.persist
import im.actor.server.push.SeqUpdatesManager

class ContactsServiceSpec extends BaseServiceSpec {
  def is = sequential^s2"""
  ContactsService
    AddContact hamdler should add contact ${s.addremove.add()}
  """

  object s {
    val seqUpdManagerRegion = SeqUpdatesManager.startRegion()

    implicit val db = migrateAndInitDb()
    implicit val service = new contacts.ContactsServiceImpl(seqUpdManagerRegion)
    implicit val authService = buildAuthService()
    implicit val ec = system.dispatcher

    object addremove {

      val authId = createAuthId()
      val phoneNumber = buildPhone()
      val user = createUser(authId, phoneNumber)

      val (user2, _, _) = createUser()
      val user2Model = getUserModel(user2.id)

      implicit val clientData = api.ClientData(authId, Some(user.id))

      def add(firstRun: Boolean = true) = {
        service.handleAddContact(user2.id, util.ACL.userAccessHash(authId, user2.id, user2Model.accessSalt)) must beOkLike {
          case api.misc.ResponseSeq(1001, state) if !state.isEmpty => ok
        }.await(timeout = 4.seconds)
      }
    }
  }
}
