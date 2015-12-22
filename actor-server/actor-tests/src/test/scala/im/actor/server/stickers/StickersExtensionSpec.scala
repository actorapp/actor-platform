package im.actor.server.stickers

import cats.data.Xor
import im.actor.server.sticker.{ Sticker, StickerImage }
import im.actor.server.user.UserExtension
import im.actor.server.{ ImplicitSessionRegion, ImplicitAuthService, BaseAppSuite }
import im.actor.util.misc.IdUtils

class StickersExtensionSpec
  extends BaseAppSuite
  with ImplicitSessionRegion
  with ImplicitAuthService {

  behavior of "Stickers extension"

  it should "create sticker pack" in e1

  it should "return error when operating on unexisting packs" in e4

  it should "not allow to toggle default for non admin user" in e2

  it should "toggle default for admin user" in e3

  it should "add and remove stickers from sticker pack that user owns" in e5

  it should "not allow user to view and modify alien packs" in e6

  it should "store actual size of sticker" in e7

  private val stickersExt = StickersExtension(system)
  private val userExt = UserExtension(system)

  def e1() = {
    val (user, _, _, _) = createUser()

    val packId = whenReady(stickersExt.createPack(user.id, isDefault = false))(identity)

    whenReady(stickersExt.isOwner(user.id, packId)) { _ shouldEqual true }

    whenReady(stickersExt.getStickerPacks(user.id)) { packs ⇒
      packs should have length 1
      val pack = packs.head
      pack.isDefault shouldEqual false
      pack.id shouldEqual packId
      pack.ownerUserId shouldEqual user.id
    }
  }

  def e2() = {
    val (user, _, _, _) = createUser()

    val packId = whenReady(stickersExt.createPack(user.id, isDefault = false))(identity)

    whenReady(stickersExt.makeStickerPackDefault(user.id, packId)) { resp ⇒
      inside(resp) {
        case Xor.Left(StickerErrors.NotAdmin) ⇒
      }
    }
    whenReady(stickersExt.unmakeStickerPackDefault(user.id, packId)) { resp ⇒
      inside(resp) {
        case Xor.Left(StickerErrors.NotAdmin) ⇒
      }
    }
  }

  def e3() = {
    val (user, _, _, _) = createUser()
    whenReady(userExt.updateIsAdmin(user.id, isAdmin = true))(identity)

    val packId = whenReady(stickersExt.createPack(user.id, isDefault = false))(identity)

    whenReady(stickersExt.unmakeStickerPackDefault(user.id, packId)) { resp ⇒
      inside(resp) {
        case Xor.Left(StickerErrors.AlreadyNotDefault) ⇒
      }
    }

    whenReady(stickersExt.makeStickerPackDefault(user.id, packId)) { _ shouldEqual Xor.Right(()) }

    whenReady(stickersExt.getStickerPacks(user.id)) { packs ⇒
      packs should have length 1
      packs.head.isDefault shouldEqual true
    }

    whenReady(stickersExt.makeStickerPackDefault(user.id, packId)) { resp ⇒
      inside(resp) {
        case Xor.Left(StickerErrors.AlreadyDefault) ⇒
      }
    }

    whenReady(stickersExt.unmakeStickerPackDefault(user.id, packId)) { _ shouldEqual Xor.Right(()) }
  }

  def e4() = {
    val (user, _, _, _) = createUser()
    whenReady(userExt.updateIsAdmin(user.id, isAdmin = true))(identity)

    val someId = IdUtils.nextIntId()

    whenReady(stickersExt.getStickers(user.id, someId)) { resp ⇒
      inside(resp) {
        case Xor.Left(StickerErrors.NotFound) ⇒
      }
    }

    whenReady(stickersExt.deleteSticker(user.id, someId, someId)) { resp ⇒
      inside(resp) {
        case Xor.Left(StickerErrors.NotFound) ⇒
      }
    }

    whenReady(stickersExt.makeStickerPackDefault(user.id, someId)) { resp ⇒
      inside(resp) {
        case Xor.Left(StickerErrors.NotFound) ⇒
      }
    }

    whenReady(stickersExt.unmakeStickerPackDefault(user.id, someId)) { resp ⇒
      inside(resp) {
        case Xor.Left(StickerErrors.NotFound) ⇒
      }
    }
  }

  def e5() = {
    val (user, _, _, _) = createUser()

    val packId = whenReady(stickersExt.createPack(user.id, isDefault = false))(identity)

    whenReady(addDummy(user.id, packId)) { _ shouldEqual Xor.Right(()) }
    whenReady(addDummy(user.id, packId)) { _ shouldEqual Xor.Right(()) }
    whenReady(addDummy(user.id, packId)) { _ shouldEqual Xor.Right(()) }
    whenReady(addDummy(user.id, packId)) { _ shouldEqual Xor.Right(()) }
    whenReady(addDummy(user.id, packId)) { _ shouldEqual Xor.Right(()) }

    val stickerIds = whenReady(stickersExt.getStickers(user.id, packId)) { stickersResp ⇒
      stickersResp should matchPattern {
        case Xor.Right(_) ⇒
      }
      val stickers = stickersResp.toOption.get

      stickers should have length 5

      stickers map { sticker ⇒
        sticker.packId shouldEqual packId
        sticker.id
      }
    }

    stickerIds foreach { id ⇒
      whenReady(stickersExt.deleteSticker(user.id, packId, id)) { _ shouldEqual Xor.Right(()) }
    }

    whenReady(stickersExt.getStickers(user.id, packId)) { stickersResp ⇒
      stickersResp should matchPattern {
        case Xor.Right(_) ⇒
      }
      val stickers = stickersResp.toOption.get
      stickers shouldBe empty
    }
  }

  def e6() = {
    val (ownerUser, _, _, _) = createUser()
    val (otherUser, _, _, _) = createUser()

    val packId = whenReady(stickersExt.createPack(ownerUser.id, isDefault = false))(identity)

    whenReady(addDummy(ownerUser.id, packId)) { _ shouldEqual Xor.Right(()) }
    whenReady(addDummy(ownerUser.id, packId)) { _ shouldEqual Xor.Right(()) }
    whenReady(addDummy(ownerUser.id, packId)) { _ shouldEqual Xor.Right(()) }

    val stickerId = whenReady(stickersExt.getStickers(ownerUser.id, packId)) { stickersResp ⇒
      stickersResp should matchPattern {
        case Xor.Right(_) ⇒
      }
      val stickers = stickersResp.toOption.get
      stickers should have length 3
      stickers.head.id
    }

    whenReady(stickersExt.isOwner(otherUser.id, packId)) { _ shouldEqual false }

    whenReady(stickersExt.getStickers(otherUser.id, packId)) { resp ⇒
      inside(resp) {
        case Xor.Left(StickerErrors.NotOwner) ⇒
      }
    }

    whenReady(stickersExt.deleteSticker(otherUser.id, packId, stickerId)) { resp ⇒
      inside(resp) {
        case Xor.Left(StickerErrors.NotFound) ⇒
      }
    }
  }

  def e7() = {
    val (user, _, _, _) = createUser()

    val packId = whenReady(stickersExt.createPack(user.id, isDefault = false))(identity)

    whenReady(stickersExt.addSticker(
      user.id,
      packId,
      None,
      Sticker(
        Some(
          StickerImage(width = 128, height = 140)
        ),
        Some(StickerImage(width = 220, height = 256)),
        Some(StickerImage(width = 512, height = 512))
      )
    )) { _ shouldEqual Xor.Right(()) }

    whenReady(stickersExt.getStickers(user.id, packId)) { stickersResp ⇒
      stickersResp should matchPattern {
        case Xor.Right(_) ⇒
      }
      val stickers = stickersResp.toOption.get

      stickers should have length 1

      val sticker = stickers.head
      sticker.image128Width shouldEqual 128
      sticker.image128Height shouldEqual 140

      sticker.image256Width shouldEqual Some(220)
      sticker.image256Height shouldEqual Some(256)

      sticker.image512Width shouldEqual Some(512)
      sticker.image512Height shouldEqual Some(512)
    }
  }

  private def addDummy(userId: Int, packId: Int) =
    stickersExt.addSticker(userId, packId, None, Sticker(Some(StickerImage()), None, None))

}
