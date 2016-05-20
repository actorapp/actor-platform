package im.actor.server.api.rpc.service

import im.actor.api.rpc._
import im.actor.api.rpc.search._
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server._
import im.actor.server.api.rpc.service.search.SearchServiceImpl

final class SearchServiceSpec
  extends BaseAppSuite
  with ServiceSpecHelpers
  with MessagingSpecHelpers
  with ContactsSpecHelpers
  with GroupsServiceHelpers
  with ImplicitAuthService
  with ImplicitSessionRegion {
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

    implicit val clientData = ClientData(authId1, 1, Some(AuthData(user1.id, authSid1, 42)))
    addContact(user2.id)

    whenReady(searchService.handlePeerSearch(Vector(
      ApiSearchPeerTypeCondition(ApiSearchPeerType.Contacts)
    ), Vector.empty)) { resp ⇒
      inside(resp) {
        case Ok(ResponsePeerSearch(results, users, groups, _, _)) ⇒
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

    implicit val clientData = ClientData(authId1, 1, Some(AuthData(user1.id, authSid1, 42)))
    createGroup("Hell yeah", Set.empty)

    whenReady(searchService.handlePeerSearch(Vector(
      ApiSearchPeerTypeCondition(ApiSearchPeerType.Groups)
    ), Vector.empty)) { resp ⇒
      inside(resp) {
        case Ok(ResponsePeerSearch(result, users, groups, _, _)) ⇒
          groups.length shouldBe 1
          val group = groups.head
          group.title shouldBe "Hell yeah"
      }
    }

    whenReady(searchService.handlePeerSearch(Vector(
      ApiSearchPeerTypeCondition(ApiSearchPeerType.Groups),
      ApiSearchPieceText("zz")
    ), Vector.empty)) { resp ⇒
      inside(resp) {
        case Ok(ResponsePeerSearch(result, users, groups, _, _)) ⇒
          groups shouldBe empty
      }
    }

    whenReady(searchService.handlePeerSearch(Vector(
      ApiSearchPeerTypeCondition(ApiSearchPeerType.Groups),
      ApiSearchPieceText("ell")
    ), Vector.empty)) { resp ⇒
      inside(resp) {
        case Ok(ResponsePeerSearch(result, users, groups, _, _)) ⇒
          groups should not be empty
      }
    }
  }
}