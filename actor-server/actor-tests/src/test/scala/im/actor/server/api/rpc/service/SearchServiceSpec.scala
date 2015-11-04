package im.actor.server.api.rpc.service

import im.actor.api.rpc._
import im.actor.api.rpc.search._
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server._

final class SearchServiceSpec
  extends BaseAppSuite
  with ServiceSpecHelpers
  with MessagingSpecHelpers
  with ContactsSpecHelpers
  with GroupsServiceHelpers
  with ImplicitAuthService
  with ImplicitSessionRegionProxy {
  behavior of "PeerSearch"
  it should "search private peers" in privat
  it should "search groups" in groups

  implicit val msgService = MessagingServiceImpl()
  implicit val groupsService = new GroupsServiceImpl(GroupInviteConfig(""))
  override val contactsService = new ContactsServiceImpl
  val searchService = new SearchServiceImpl

  def privat() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, _, _, _) = createUser()

    implicit val clientData = ClientData(authId1, 1, Some(AuthData(user1.id, authSid1)))
    addContact(user2.id)

    whenReady(searchService.handlePeerSearch(Vector(
      ApiSearchPeerTypeCondition(ApiSearchPeerType.Contacts)
    ))) { resp ⇒
      inside(resp) {
        case Ok(ResponsePeerSearch(results, users, groups)) ⇒
          groups shouldBe empty
          users.map(_.id) shouldBe Seq(user2.id)
          results.length shouldBe 1
          val result = results.head

          result.title shouldBe user2.name
      }
    }
  }

  def groups() = {
    val (user1, authId1, authSid1, _) = createUser()

    implicit val clientData = ClientData(authId1, 1, Some(AuthData(user1.id, authSid1)))
    createGroup("Hell yeah", Set.empty)

    whenReady(searchService.handlePeerSearch(Vector(
      ApiSearchPeerTypeCondition(ApiSearchPeerType.Groups)
    ))) { resp ⇒
      inside(resp) {
        case Ok(ResponsePeerSearch(result, users, groups)) ⇒
          groups.length shouldBe 1
          val group = groups.head
          group.title shouldBe "Hell yeah"
      }
    }

    whenReady(searchService.handlePeerSearch(Vector(
      ApiSearchPeerTypeCondition(ApiSearchPeerType.Groups),
      ApiSearchPieceText("zz")
    ))) { resp ⇒
      inside(resp) {
        case Ok(ResponsePeerSearch(result, users, groups)) ⇒
          groups shouldBe empty
      }
    }

    whenReady(searchService.handlePeerSearch(Vector(
      ApiSearchPeerTypeCondition(ApiSearchPeerType.Groups),
      ApiSearchPieceText("ell")
    ))) { resp ⇒
      inside(resp) {
        case Ok(ResponsePeerSearch(result, users, groups)) ⇒
          groups should not be empty
      }
    }
  }
}