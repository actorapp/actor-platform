package im.actor.server.util

import scala.language.postfixOps

import im.actor.api.rpc.auth.AuthService
import im.actor.api.rpc.users.User
import im.actor.server.{ ImplicitRegions, BaseAppSuite }

class UserUtilsSpec extends BaseAppSuite with ImplicitRegions {

  import UserUtils._

  it should "generate proper User struct" in e1

  implicit val authService: AuthService = buildAuthService()

  val (users, authIds, phones) =
    Seq(createUser(), createUser()).foldLeft(Seq.empty[User], Seq.empty[Long], Seq.empty[Long]) {
      case ((uacc, aacc, pacc), (u, a, p)) ⇒
        (uacc :+ u, aacc :+ a, pacc :+ p)
    }

  val userIds = users map (_.id) toSet

  def e1() = {
    val (requestingUser, requestingAuthId, _) = createUser()

    val structs = userStructs(userIds, requestingUser.id, requestingAuthId)
  }
}