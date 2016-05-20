package im.actor.server.api.rpc.service

import im.actor.api.rpc._
import im.actor.api.rpc.integrations.ResponseIntegrationToken
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeerType, ApiUserOutPeer }
import im.actor.server._
import im.actor.server.acl.ACLUtils
import im.actor.server.api.http.HttpApiConfig
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.webhooks.IntegrationServiceHelpers.makeUrl
import im.actor.server.api.rpc.service.webhooks.IntegrationsServiceImpl

final class IntegrationsServiceSpec
  extends BaseAppSuite
  with GroupsServiceHelpers
  with ImplicitSessionRegion
  with ImplicitAuthService {
  behavior of "IntegrationsService"

  it should "not allow non group members to get integration token" in t.e1

  it should "respond with empty token to ordinary group member" in t.e2

  it should "not allow ordinary group member to revoke integration token" in t.e3

  it should "allow group admin to get integration token" in t.e4

  it should "allow group admin to revoke integration token" in t.e5

  it should "allow multiple group admins to get integration token" in t.e6

  object t {

    implicit val ec = system.dispatcher

    val groupInviteConfig = GroupInviteConfig("https://actor.im")

    implicit val groupsService = new GroupsServiceImpl(groupInviteConfig)

    private val config = HttpApiConfig("localhost", 9000, "http://actor.im", "/dev/null", None)
    val service = new IntegrationsServiceImpl(config.baseUri)

    val (user1, user1AuthId1, user1AuthSid1, _) = createUser()
    val (user1AuthId2, user1AuthSid2) = createAuthId(user1.id)

    val (user2, user2AuthId, user2AuthSid, _) = createUser()

    val sessionId = createSessionId()
    val clientData1 = ClientData(user1AuthId1, sessionId, Some(AuthData(user1.id, user1AuthSid1, 42)))
    val clientData2 = ClientData(user1AuthId2, sessionId, Some(AuthData(user2.id, user1AuthSid2, 42)))

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(clientData1.authId, user2.id, user2Model.accessSalt)
    val user2OutPeer = ApiUserOutPeer(user2.id, user2AccessHash)

    def e1(): Unit = {
      val outPeer = {
        implicit val clientData = clientData1
        val groupOutPeer = createGroup("Fun group", Set.empty).groupPeer
        ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)
      }
      whenReady(service.handleGetIntegrationToken(outPeer)(clientData2))(_ should matchForbidden)
    }

    def e2(): Unit = {
      val outPeer = {
        implicit val clientData = clientData1
        val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer
        ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)
      }

      whenReady(service.handleGetIntegrationToken(outPeer)(clientData2)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
        inside(resp) {
          case Ok(ResponseIntegrationToken(token, url)) ⇒
            token shouldEqual ""
            url shouldEqual ""
        }
      }
    }

    def e3(): Unit = {
      val outPeer = {
        implicit val clientData = clientData1
        val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer
        ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)
      }

      whenReady(service.handleRevokeIntegrationToken(outPeer)(clientData2)) { resp ⇒
        resp should matchForbidden
      }

      whenReady(service.handleGetIntegrationToken(outPeer)(clientData2)) { resp ⇒
        inside(resp) {
          case Ok(ResponseIntegrationToken(token, url)) ⇒
            token shouldEqual ""
            url shouldEqual ""
        }
      }
    }

    def e4(): Unit = {
      implicit val clientData = clientData1
      val outPeer = {
        val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer
        ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)
      }

      val groupToken = extractToken(outPeer.id)

      whenReady(service.handleGetIntegrationToken(outPeer)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
        inside(resp) {
          case Ok(ResponseIntegrationToken(token, url)) ⇒
            token shouldEqual groupToken
            url shouldEqual makeUrl(config.baseUri, groupToken)
        }
      }
    }

    def e5(): Unit = {
      implicit val clientData = clientData1
      val outPeer = {
        val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer
        ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)
      }

      val newTokenResponse =
        whenReady(service.handleRevokeIntegrationToken(outPeer)) { resp ⇒
          resp should matchPattern {
            case Ok(ResponseIntegrationToken(token, url)) ⇒
          }
          resp.toOption.get
        }

      whenReady(service.handleGetIntegrationToken(outPeer)) { resp ⇒
        inside(resp) {
          case Ok(ResponseIntegrationToken(token, url)) ⇒
            token shouldEqual newTokenResponse.token
            url shouldEqual newTokenResponse.url
        }
      }
    }

    def e6(): Unit = {
      implicit val clientData = clientData1
      val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer
      val outPeer = ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)

      whenReady(groupsService.handleMakeUserAdmin(groupOutPeer, user2OutPeer)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
      }

      val groupToken = extractToken(outPeer.id)

      whenReady(service.handleGetIntegrationToken(outPeer)) { resp ⇒
        resp should matchPattern { case Ok(_) ⇒ }
        inside(resp) {
          case Ok(ResponseIntegrationToken(token, url)) ⇒
            token shouldEqual groupToken
            url shouldEqual makeUrl(config.baseUri, groupToken)
        }
      }

      {
        implicit val clientData = clientData2
        whenReady(service.handleGetIntegrationToken(outPeer)) { resp ⇒
          resp should matchPattern { case Ok(_) ⇒ }
          inside(resp) {
            case Ok(ResponseIntegrationToken(token, url)) ⇒
              token shouldEqual groupToken
              url shouldEqual makeUrl(config.baseUri, groupToken)
          }
        }
      }
    }

  }

}
