package im.actor.server.api.rpc.service

import scala.concurrent._
import scala.concurrent.duration._

import im.actor.api.{ rpc => api }
import im.actor.server.api.util
import im.actor.server.persist
import im.actor.server.presences.PresenceManager
import im.actor.server.push.{ WeakUpdatesManager, SeqUpdatesManager }

class EncryptionServiceSpec extends BaseServiceSpec {
  def is = s2"""
  EncryptionService
    GetPublicKeys handler should return public keys ${s.e1}
  """

  object s {
    val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
    val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
    val presenceManagerRegion = PresenceManager.startRegion()
    val rpcApiService = buildRpcApiService()
    val sessionRegion = buildSessionRegion(rpcApiService, seqUpdManagerRegion, weakUpdManagerRegion, presenceManagerRegion)

    implicit val service = new encryption.EncryptionServiceImpl
    implicit val authService = buildAuthService(sessionRegion)
    implicit val ec = system.dispatcher

    val authId = createAuthId()
    val sessionId = createSessionId()
    val phoneNumber = buildPhone()

    val user = createUser(authId, phoneNumber)

    val authId2 = createAuthId()
    val phoneNumber2 = buildPhone()

    val user2 = createUser(authId2, phoneNumber2)

    implicit val clientData = api.ClientData(authId, sessionId, Some(user.id))

    def e1 = {
      val user2Model = Await.result(db.run(persist.User.find(user2.id).head), 1.second)
      val user2pk = Await.result(db.run(persist.UserPublicKey.findByUserId(user2.id).head), 1.second)

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
