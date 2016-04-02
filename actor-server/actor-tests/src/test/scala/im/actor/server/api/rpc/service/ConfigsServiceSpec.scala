package im.actor.server.api.rpc.service

import im.actor.api.rpc._
import im.actor.api.rpc.configs._
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.server._
import im.actor.server.api.rpc.service.configs.ConfigsServiceImpl

class ConfigsServiceSpec
  extends BaseAppSuite
  with ImplicitSessionRegion
  with ImplicitAuthService {
  behavior of "Configs Service"

  it should "save parameter even if it already exists" in e1()

  it should "get parameters" in e2()

  val service = new ConfigsServiceImpl

  val (user, authId, authSid, _) = createUser()
  val sessionId = createSessionId()

  implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

  def e1() = {
    whenReady(service.handleEditParameter("par1", Some("val1"))) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseSeq(_, _)) ⇒
      }
    }

    whenReady(service.handleEditParameter("par1", Some("val2"))) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseSeq(_, _)) ⇒
      }
    }
  }

  def e2() = {
    whenReady(service.handleGetParameters()) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseGetParameters(Vector(ApiParameter("par1", "val2")))) ⇒
      }
    }
  }
}
