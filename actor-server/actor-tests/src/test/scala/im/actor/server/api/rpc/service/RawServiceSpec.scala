package im.actor.server.api.rpc.service

import akka.actor.ActorSystem
import akka.util.Timeout
import cats.data.Xor
import im.actor.api.rpc._
import im.actor.api.rpc.collections._
import im.actor.rpc.raw.RawApiService
import im.actor.server.api.rpc.RawApiExtension
import im.actor.server.api.rpc.service.raw.RawServiceImpl
import im.actor.server.{ BaseAppSuite, ImplicitAuthService, ImplicitSessionRegion }

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.duration._
import scalaz.\/-

class RawServiceSpec
  extends BaseAppSuite
  with ImplicitAuthService
  with ImplicitSessionRegion {

  behavior of "raw api service"

  it should "respond with Unsupported Request, when there is no such service" in e1

  it should "respond with error to invalid request" in e2

  it should "respond with result to valid request" in e3

  it should "detect dynamically registered services" in e4

  val service = new RawServiceImpl()

  RawApiExtension(system).register("dictionary", new DictionaryService(system))

  val (user, authId, authSid, _) = createUser()
  implicit val clientData: ClientData = ClientData(authId, createSessionId(), Some(AuthData(user.id, authSid)))

  def e1() = {
    whenReady(service.handleRawRequest("maps", "getGeo", None)) { resp ⇒
      inside(resp) {
        case Error(err) ⇒ err shouldEqual CommonErrors.UnsupportedRequest
      }
    }
  }

  def e2() = {
    whenReady(service.handleRawRequest("dictionary", "getWord", Some(ApiInt32Value(22)))) { resp ⇒
      inside(resp) {
        case Error(err) ⇒ err shouldEqual ServiceErrors.InvalidParams
      }
    }

    //not right cause we should pass params as map, not as string
    whenReady(service.handleRawRequest("dictionary", "getWord", Some(ApiStringValue("culture")))) { resp ⇒
      inside(resp) {
        case Error(err) ⇒ err shouldEqual ServiceErrors.InvalidParams
      }
    }
  }

  def e3() = {
    whenReady(service.handleRawRequest("dictionary", "getWord", Some(ApiMapValue(Vector(ApiMapValueItem("word", ApiStringValue("culture"))))))) { resp ⇒
      inside(resp) {
        case \/-(rawValue) ⇒ inside(rawValue.result) {
          case ApiMapValue(items) ⇒
            items should have length 1
            items.head shouldEqual ApiMapValueItem("meaning", ApiStringValue(DictionaryMeanings.Culture))
        }
      }
    }

    whenReady(service.handleRawRequest("dictionary", "getWord", Some(ApiMapValue(Vector(ApiMapValueItem("word", ApiStringValue("UNKNOWN"))))))) { resp ⇒
      inside(resp) {
        case \/-(rawValue) ⇒ inside(rawValue.result) {
          case ApiMapValue(items) ⇒
            items shouldBe empty
        }
      }
    }
  }

  def e4() = {
    whenReady(service.handleRawRequest("echo", "makeEcho", Some(ApiMapValue(Vector(ApiMapValueItem("query", ApiStringValue("Hello"))))))) { resp ⇒
      inside(resp) {
        case Error(err) ⇒ err shouldEqual CommonErrors.UnsupportedRequest
      }
    }

    RawApiExtension(system).register("echo", new EchoService(system))

    whenReady(service.handleRawRequest("echo", "makeEcho", Some(ApiMapValue(Vector(ApiMapValueItem("query", ApiStringValue("Hello"))))))) { resp ⇒
      inside(resp) {
        case \/-(rawValue) ⇒ inside(rawValue.result) {
          case ApiMapValue(items) ⇒
            items should have length 1
            items.head shouldEqual ApiMapValueItem("echo", ApiStringValue("Hello you back!"))
        }
      }
    }
  }

  //===================================Dictionary service

  private object DictionaryMeanings {
    val Culture = "Anthropology. the sum total of ways of living built up by a group of human beings and transmitted from one generation to another."
    val Science = "Knowledge, as of facts or principles; knowledge gained by systematic study."
    val Software = "Computers. the programs used to direct the operation of a computer, as well as documentation giving instructions on how to use them."
  }

  /**
   * Example raw api service that stores and retrieves words from dictionary.
   */
  private final class DictionaryService(system: ActorSystem) extends RawApiService(system) {
    import DictionaryMeanings._
    import ServiceErrors._
    import im.actor.api.rpc.FutureResultRpcCats._

    implicit val timeout = Timeout(20.seconds)
    private val kv = TrieMap.empty[String, String]

    kv.put("culture", Culture)
    kv.put("science", Science)
    kv.put("software", Software)

    override def handleRequests: Handler = implicit client ⇒ params ⇒ {
      case "getWord" ⇒ getWord(params)
      //      case "putWord" => putWord()
    }

    def getWord(optParams: Option[ApiRawValue])(implicit client: AuthorizedClientData): Response = {
      val ps = optParams flatMap {
        case ApiMapValue(items) ⇒ items collectFirst { case ApiMapValueItem("word", ApiStringValue(str)) ⇒ str }
        case _                  ⇒ None
      }
      (for {
        key ← fromOption(InvalidParams)(ps)
        optValue ← point(kv.get(key))
        result = optValue map { e ⇒ Vector(ApiMapValueItem("meaning", ApiStringValue(e))) } getOrElse Vector.empty
      } yield ApiMapValue(result)).value
    }
  }

  //===================================Echo service

  private final class EchoService(system: ActorSystem) extends RawApiService(system) {
    import ServiceErrors._

    override def handleRequests: Handler = implicit client ⇒ params ⇒ {
      case "makeEcho" ⇒ echo(params)
    }

    def echo(params: Option[ApiRawValue]): Response = {
      val resp = extractStringFromMap(params, "query") map { q ⇒
        Xor.right(ApiMapValue(Vector(ApiMapValueItem("echo", ApiStringValue(s"$q you back!")))))
      } getOrElse Xor.left(InvalidParams)
      Future.successful(resp)
    }
  }

  private def extractStringFromMap(optParams: Option[ApiRawValue], key: String): Option[String] =
    optParams flatMap {
      case ApiMapValue(items) ⇒ items collectFirst { case ApiMapValueItem(`key`, ApiStringValue(str)) ⇒ str }
      case _                  ⇒ None
    }

  private object ServiceErrors {
    val InvalidParams = RpcError(400, "INVALID_PARAMS", "", canTryAgain = true, None)
  }

}
