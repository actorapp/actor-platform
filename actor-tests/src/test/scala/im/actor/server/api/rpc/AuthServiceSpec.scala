package im.actor.server.api.rpc

import im.actor.api.rpc._
import im.actor.api.rpc.auth._
import im.actor.api.rpc.misc._
import im.actor.server.SqlSpecHelpers
import im.actor.util.testing._

import scala.concurrent._, duration._

import org.specs2._

class AuthServiceSpec extends ActorSpecification with SqlSpecHelpers {

  def is = s2"""
  AuthService should
    log in on ${service.e1}
  """

  object service extends auth.AuthServiceImpl {
    override implicit val ec: ExecutionContext = system.dispatcher

    val db = migrateAndInitDb()

    val authId = 1L
    val phoneNumber = 79991112233L

    def e1 = {
      handleSendAuthCode(authId, None, phoneNumber, 1, "apiKey") must beLike[HandlerResult[ResponseSendAuthCode]] {
        case Ok(ResponseSendAuthCode(_, false), Vector()) => ok
      }.await
    }
  }
}
