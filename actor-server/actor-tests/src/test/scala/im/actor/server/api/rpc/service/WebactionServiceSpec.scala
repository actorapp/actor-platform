package im.actor.server.api.rpc.service

import im.actor.api.rpc.collections.{ ApiInt32Value, ApiMapValue, ApiMapValueItem, ApiStringValue }
import im.actor.api.rpc.webactions.{ ResponseCompleteWebaction, ResponseInitWebaction }
import im.actor.api.rpc.{ AuthData, ClientData, Error, Ok }
import im.actor.server.api.rpc.service.webactions.{ WebactionsErrors, WebactionsKeyValues, WebactionsServiceImpl }
import im.actor.server.webactions.CorrectWebaction
import im.actor.server.{ BaseAppSuite, ImplicitAuthService, ImplicitSessionRegion }
import org.scalatest.Inside._

class WebactionServiceSpec
  extends BaseAppSuite
  with ImplicitSessionRegion
  with ImplicitAuthService {

  behavior of "WebactionService"

  "Init webaction" should "response with WEBACTION_NOT_FOUND when there is no webaction with such name" in t.e1

  it should "response with FAILED_TO_CREATE_WEBACTION when web action cannot be instantiated" in t.e2

  it should "response with correct uri and regex when web action exists" in t.e3

  "Complete webaction" should "response with WRONG_WEBACTION_HASH to wrong action hash" in t.e4

  it should "response with ApiMapValue to correct action hash and remove action hash from key value" in t.e5

  val service = new WebactionsServiceImpl()

  object t {
    val (user, userAuthId, userAuthSid, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(userAuthId, sessionId, Some(AuthData(user.id, userAuthSid, 42)))
    private val kv = WebactionsKeyValues.actionHashUserKV()

    def e1(): Unit = {
      whenReady(service.handleInitWebaction("foo", emptyParams)) { resp ⇒
        inside(resp) {
          case Error(WebactionsErrors.WebactionNotFound) ⇒
        }
      }
    }

    def e2(): Unit = {
      whenReady(service.handleInitWebaction("wrong", emptyParams)) { resp ⇒
        inside(resp) {
          case Error(WebactionsErrors.FailedToCreateWebaction) ⇒
        }
      }
    }

    def e3(): Unit = {
      val actionName = "correct"
      whenReady(service.handleInitWebaction(actionName, emptyParams)) { resp ⇒
        inside(resp) {
          case Ok(ResponseInitWebaction(uri, reg, hash)) ⇒
            uri shouldEqual CorrectWebaction.uri
            reg shouldEqual CorrectWebaction.regex
            whenReady(kv.get(hash)) { optAction ⇒
              optAction shouldBe defined
              val action = optAction.get
              actionName shouldEqual action
            }
        }
      }
    }

    def e4(): Unit = {
      whenReady(service.handleInitWebaction("correct", emptyParams)) { resp ⇒
        inside(resp) {
          case Ok(ResponseInitWebaction(uri, reg, hash)) ⇒
        }
      }
      whenReady(service.handleCompleteWebaction("wrong_hash", CorrectWebaction.completeUri)) { resp ⇒
        inside(resp) {
          case Error(WebactionsErrors.WrongActionHash) ⇒
        }
      }
    }

    def e5(): Unit = {
      val actionHash = whenReady(service.handleInitWebaction("correct", emptyParams)) { resp ⇒
        resp.toOption.get.actionHash
      }
      whenReady(service.handleCompleteWebaction(actionHash, CorrectWebaction.completeUri)) { resp ⇒
        inside(resp) {
          case Ok(ResponseCompleteWebaction(map)) ⇒
            map.items should have length 2
            map.items should contain theSameElementsAs Vector(
              ApiMapValueItem("userId", ApiInt32Value(user.id)),
              ApiMapValueItem("url", ApiStringValue(CorrectWebaction.completeUri.reverse))
            )
        }
      }
      whenReady(kv.get(actionHash)) { optAction ⇒
        optAction should not be defined
      }
    }

    private val emptyParams = ApiMapValue(Vector())
  }

}
