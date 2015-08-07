package im.actor.server.util

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

import im.actor.api.rpc.auth.AuthService
import im.actor.api.rpc.users.{ ContactRecord, ContactType, User }
import im.actor.server._
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }

class UserUtilsSpec
  extends BaseAppSuite
  with ImplicitSessionRegionProxy
  with ImplicitUserRegions
  with ImplicitAuthService {

  import UserUtils._

  it should "generate proper User struct" in e1

  val userTups = Seq(createUser(), createUser()) map {
    case (struct, authId, phone) ⇒
      (Await.result(db.run(persist.User.find(struct.id) map (_.head)), 5.seconds), authId, phone)
  }

  val userIds = userTups map (_._1.id) toSet

  def e1() = {
    val (requestingUser, requestingAuthId, _) = createUser()

    val expectedStructs = userTups map {
      case (user, authId, phone) ⇒
        User(
          user.id,
          ACLUtils.userAccessHash(requestingAuthId, user),
          user.name,
          None,
          None,
          Some(phone),
          None,
          Some(false),
          Vector(ContactRecord(ContactType.Phone, None, Some(phone), Some("Mobile phone"), None)),
          None,
          None
        )
    }

    whenReady(db.run(getUserStructs(userIds, requestingUser.id, requestingAuthId))) { structs ⇒
      structs shouldEqual expectedStructs
    }

    whenReady(db.run(getUserStructsPar(userIds, requestingUser.id, requestingAuthId))) { structs ⇒
      structs shouldEqual expectedStructs
    }
  }
}
