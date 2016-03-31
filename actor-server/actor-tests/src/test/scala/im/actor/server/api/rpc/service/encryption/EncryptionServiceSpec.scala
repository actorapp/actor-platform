package im.actor.server.api.rpc.service.encryption

import im.actor.api.rpc.encryption._
import im.actor.api.rpc._
import im.actor.server.{ ImplicitSessionRegion, ImplicitAuthService, BaseAppSuite }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

final class EncryptionServiceSpec extends BaseAppSuite with ImplicitAuthService with ImplicitSessionRegion {
  it should "create key group and load keys" in keyGroup
  it should "create and load ephermal keys" in ephermalKeys
  "SendEncryptedPackage" should "ignore ignored keys" in ignoredKeys

  lazy val service = new EncryptionServiceImpl

  def keyGroup() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val aliceClientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobClientData = ClientData(bobAuthId, 1, Some(AuthData(bob.id, bobAuthSid, 42)))

    val supportedEncryptions = Vector("sup1", "sup2", "sup3")
    val identityKey = ApiEncryptionKey(1L, "idalg", Some(Array[Byte](1, 2, 3)), Some(Array[Byte](1)))
    val keys = Vector(ApiEncryptionKey(2L, "keyalg", Some(Array[Byte](3, 4, 5)), Some(Array[Byte](3))))
    val signatures = Vector(
      ApiEncryptionKeySignature(2L, "signalg", Array[Byte](4)),
      ApiEncryptionKeySignature(2L, "signalg", Array[Byte](5)),
      ApiEncryptionKeySignature(4L, "signalg", Array[Byte](5))
    )

    val keyGroupId = {
      implicit val clientData = aliceClientData

      whenReady(service.handleCreateNewKeyGroup(
        identityKey = identityKey,
        supportedEncryptions = supportedEncryptions,
        keys = keys,
        signatures = signatures
      ))(_.toOption.get.keyGroupId)
    }

    {
      implicit val clientData = bobClientData
      whenReady(service.handleLoadPublicKeyGroups(getUserOutPeer(alice.id, bobAuthId))) { resp ⇒
        inside(resp) {
          case Ok(ResponsePublicKeyGroups(Vector(kg))) ⇒
            kg.keyGroupId shouldBe keyGroupId
            kg.keys.map(_.keyId) shouldBe keys.map(_.keyId)
            kg.signatures.map(_.keyId) shouldBe signatures.map(_.keyId)
            kg.supportedEncryption shouldBe supportedEncryptions
        }
      }

      whenReady(service.handleLoadPublicKey(getUserOutPeer(alice.id, bobAuthId), keyGroupId, Vector(keys.head.keyId))) { resp ⇒
        inside(resp) {
          case Ok(ResponsePublicKeys(
            ks,
            signs
            )) ⇒
            ks.map(_.keyId) shouldBe keys.map(_.keyId)
            signs.map(_.keyId) shouldBe signatures.take(2).map(_.keyId)
        }
      }
    }
  }

  def ephermalKeys() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val aliceClientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobClientData = ClientData(bobAuthId, 1, Some(AuthData(bob.id, bobAuthSid, 42)))

    val supportedEncryptions = Vector("sup1", "sup2", "sup3")
    val identityKey = ApiEncryptionKey(1L, "idalg", Some(Array[Byte](1, 2, 3)), Some(Array[Byte](1)))
    val keys = Vector(ApiEncryptionKey(2L, "keyalg", Some(Array[Byte](3, 4, 5)), Some(Array[Byte](3))))
    val signatures = Vector(
      ApiEncryptionKeySignature(2L, "signalg", Array[Byte](4)),
      ApiEncryptionKeySignature(2L, "signalg", Array[Byte](5)),
      ApiEncryptionKeySignature(4L, "signalg", Array[Byte](5))
    )

    val ephKeys = Vector(
      ApiEncryptionKey(5L, "ephkeyalg", Some(Array[Byte](8, 9, 10)), Some(Array[Byte](3))),
      ApiEncryptionKey(6L, "ephkeyalg", Some(Array[Byte](8, 9, 10)), Some(Array[Byte](3)))
    )
    val ephSignatures = Vector(
      ApiEncryptionKeySignature(5L, "ephsignalg", Array[Byte](40)),
      ApiEncryptionKeySignature(5L, "ephsignalg", Array[Byte](41)),
      ApiEncryptionKeySignature(6L, "ephsignalg", Array[Byte](60))
    )

    val keyGroupId = {
      implicit val clientData = aliceClientData

      val keyGroupId = whenReady(service.handleCreateNewKeyGroup(
        identityKey = identityKey,
        supportedEncryptions = supportedEncryptions,
        keys = keys,
        signatures = signatures
      ))(_.toOption.get.keyGroupId)

      whenReady(service.handleUploadPreKey(
        keyGroupId,
        ephKeys,
        ephSignatures
      ))(identity)

      keyGroupId
    }

    {
      implicit val clientData = bobClientData

      whenReady(service.handleLoadPrePublicKeys(
        getUserOutPeer(alice.id, bobAuthId),
        keyGroupId
      )) { resp ⇒
        inside(resp) {
          case Ok(ResponsePublicKeys(Vector(k), sigs)) ⇒
            ephKeys.map(_.keyId) should contain(k.keyId)
            sigs.map(_.keyId).distinct shouldBe Vector(k.keyId)
        }
      }

      whenReady(service.handleLoadPublicKey(
        getUserOutPeer(alice.id, bobAuthId),
        keyGroupId,
        Vector(ephKeys.head.keyId)
      )) { resp ⇒
        inside(resp) {
          case Ok(ResponsePublicKeys(Vector(k), signs)) ⇒
            k.keyId shouldBe ephKeys.head.keyId
            signs.map(_.keyId) shouldBe ephSignatures.take(2).map(_.keyId)
        }
      }
    }
  }

  def ignoredKeys() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val aliceClientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobClientData = ClientData(bobAuthId, 1, Some(AuthData(bob.id, bobAuthSid, 42)))

    val (aliceKeyGroupId1, aliceKeys1) = createKeyGroup()(aliceClientData)
    val (aliceKeyGroupId2, _) = createKeyGroup()(aliceClientData)
    val (bobKeyGroupId, _) = createKeyGroup()(bobClientData)

    {
      implicit val clientData = bobClientData

      whenReady(service.handleSendEncryptedPackage(
        randomId = Random.nextLong(),
        destPeers = Vector(getUserOutPeer(alice.id, bobAuthId)),
        ignoredKeyGroups = Vector(ApiKeyGroupId(alice.id, 1), ApiKeyGroupId(alice.id, aliceKeyGroupId2)),
        encryptedBox = ApiEncryptedBox(
          keys = aliceKeys1 map (key ⇒ ApiEncyptedBoxKey(alice.id, aliceKeyGroupId1, key.keyAlg, key.keyMaterial.get)),
          algType = "",
          encPackage = Array(),
          senderKeyGroupId = bobKeyGroupId,
          Vector.empty
        )
      )) { resp ⇒
        inside(resp) {
          case Ok(resp: ResponseSendEncryptedPackage) ⇒
            resp.missedKeyGroups shouldBe empty
            resp.obsoleteKeyGroups shouldBe empty
        }
      }
    }
  }

  private def createKeyGroup()(implicit clientData: ClientData) = {
    val supportedEncryptions = Vector("sup1")
    val identityKey = ApiEncryptionKey(Random.nextLong(), "idalg", Some(Array[Byte](1, 2, 3)), Some(Array[Byte](1)))
    val keys = Vector(ApiEncryptionKey(Random.nextLong(), "keyalg", Some(Array[Byte](3, 4, 5)), Some(Array[Byte](3))))
    val signatures = Vector(
      ApiEncryptionKeySignature(keys.head.keyId, "signalg", Array[Byte](4))
    )
    val keyGroupId = Await.result(service.handleCreateNewKeyGroup(
      identityKey = identityKey,
      supportedEncryptions = supportedEncryptions,
      keys = keys,
      signatures = signatures
    ), 5.seconds).toOption.get.keyGroupId
    (keyGroupId, keys)
  }
}