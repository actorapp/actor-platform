package im.actor.server.api.rpc.service

import im.actor.api.{ rpc => api }
import im.actor.server.api.util
import im.actor.server.persist
import im.actor.server.SqlSpecHelpers
import im.actor.util.testing._

import scala.concurrent._, duration._

class EncryptionServiceSpec extends ActorSpecification with SqlSpecHelpers with ServiceSpecHelpers with HandlerMatchers {
  def is = s2"""
  EncryptionService
    GetPublicKeys handler should return public keys ${s.e1}
  """

  object s {
    implicit val service = new encryption.EncryptionServiceImpl {
      override implicit val ec = system.dispatcher
      override implicit val actorSystem = system

      val db = migrateAndInitDb()
    }

    implicit val authService = buildAuthService(system, service.db)

    implicit val ec = system.dispatcher

    val authId = createAuthId(service.db)
    val phoneNumber = buildPhone()

    val user = createUser(authId, phoneNumber)

    val authId2 = createAuthId(service.db)
    val phoneNumber2 = buildPhone()

    val user2 = createUser(authId2, phoneNumber2)

    implicit val clientData = api.ClientData(authId, Some(user.id))

    def e1 = {
      val user2Model = Await.result(service.db.run(persist.User.find(user2.id).head), 1.second)
      val user2pk = Await.result(service.db.run(persist.UserPublicKey.find(user2.id, authId2).head), 1.second)

      service.handleGetPublicKeys(
        keys = Vector(
          api.encryption.PublicKeyRequest(
            user2.id,
            util.ACL.userAccessHash(authId, user2Model),
            user2.keyHashes.head
          )
        )
      ) must beOkLike {
        case api.encryption.ResponseGetPublicKeys(Vector(
          pk @ api.encryption.PublicKey(user2.id, user2pk.hash, _)
        )) if pk.key.sameElements(user2pk.data) => ok
      }.await
    }
  }
}
