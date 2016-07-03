package im.actor.server.api.rpc.service.auth

import im.actor.api.rpc._
import im.actor.api.rpc.auth.{ ResponseAuth, ResponseStartUsernameAuth }
import im.actor.server.acl.ACLUtils
import im.actor.server.persist.UserPasswordRepo
import im.actor.server.user.UserExtension
import im.actor.server.{ BaseAppSuite, ImplicitAuthService, ImplicitSessionRegion }

final class PasswordAuthSpec extends BaseAppSuite with ImplicitSessionRegion with ImplicitAuthService {
  it should "auth by password" in authByPassword

  val userExt = UserExtension(system)

  def authByPassword() = {
    val (user, authId, _, _) = createUser()
    whenReady(userExt.changeNickname(user.id, authId, Some("manickname")))(identity)
    val (hash, salt) = ACLUtils.hashPassword("ma password")
    whenReady(db.run(UserPasswordRepo.createOrReplace(user.id, hash, salt)))(identity)

    implicit val clientData = ClientData(authId, 1, None)

    whenReady(authService.handleStartUsernameAuth("manickname", 0, "", Array.empty, "specs", None, Vector.empty)) { resp ⇒
      inside(resp) {
        case Ok(ResponseStartUsernameAuth(txHash, true)) ⇒
          whenReady(authService.handleValidatePassword(txHash, "ma password")) { resp ⇒
            inside(resp) {
              case Ok(ResponseAuth(_, _)) ⇒
            }
          }
      }
    }
  }
}
