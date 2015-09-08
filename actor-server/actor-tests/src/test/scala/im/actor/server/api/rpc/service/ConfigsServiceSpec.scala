package im.actor.server.api.rpc.service

import im.actor.api.rpc._
import im.actor.api.rpc.configs._
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.server._
import im.actor.server.api.rpc.service.configs.ConfigsServiceImpl

class ConfigsServiceSpec
  extends BaseAppSuite
  with ImplicitUserRegions
  with ImplicitSessionRegionProxy
  with ImplicitAuthService {
  behavior of "Configs Service"

  it should "save parameter even if it already exists" in e1()

  it should "get parameters" in e2()

  val service = new ConfigsServiceImpl

  val (user, _, _) = createUser()
  val authId = createAuthId()
  val sessionId = createSessionId()

  implicit val clientData = ClientData(authId, sessionId, Some(user.id))

  def e1() = {
    whenReady(service.handleEditParameter("par1", "val1")) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseSeq(_, _)) ⇒
      }
    }

    whenReady(service.handleEditParameter("par1", "val2")) { resp ⇒
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
