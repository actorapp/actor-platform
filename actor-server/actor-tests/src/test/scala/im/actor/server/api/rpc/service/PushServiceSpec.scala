package im.actor.server.api.rpc.service

import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.{ AuthData, ClientData, Ok }
import im.actor.server._
import im.actor.server.api.rpc.service.push.PushServiceImpl

class PushServiceSpec extends BaseAppSuite
  with ImplicitSessionRegion
  with ImplicitAuthService {
  behavior of "PushService"

  it should "replace google push credentials when other exists for current authId(sequntial)" in changeForAuthIdSeq

  it should "replace google push credentials when other exists for current authId(parallel)" in changeForAuthIdPar

  val pushService = new PushServiceImpl

  def changeForAuthIdSeq(): Unit = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(aliceAuthId, sessionId, Some(AuthData(alice.id, aliceAuthSid, 42)))

    //registered once
    whenReady(pushService.handleRegisterGooglePush(22L, "oneTokenValue")) { resp ⇒
      resp should matchPattern {
        case Ok(_) ⇒
      }
    }

    whenReady(pushService.handleRegisterGooglePush(22L, "otherTokenValue")) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseVoid) ⇒
      }
    }
  }

  def changeForAuthIdPar(): Unit = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(aliceAuthId, sessionId, Some(AuthData(alice.id, aliceAuthSid, 42)))

    val createFu = (1 to 5000).par map { i ⇒
      pushService.handleRegisterGooglePush(22L, s"tokenNumber${i}")
    }

    createFu foreach { resp ⇒
      resp.futureValue should matchPattern {
        case Ok(ResponseVoid) ⇒
      }
    }
  }

}
