package im.actor.server.enrich

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.files.ApiFastThumb
import im.actor.api.rpc.messaging.{ ApiDocumentExPhoto, ApiDocumentMessage, ApiTextMessage, UpdateMessageContentChanged }
import im.actor.api.rpc.peers.ApiPeerType
import im.actor.api.rpc.{ AuthData, ClientData, peers }
import im.actor.server._
import im.actor.server.acl.ACLUtils
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging
import im.actor.server.social.SocialManager

import scala.util.Random

class RichMessageWorkerSpec
  extends BaseAppSuite
  with GroupsServiceHelpers
  with MessageParsing
  with ImplicitSessionRegion
  with ImplicitSequenceService
  with SeqUpdateMatchers
  with ImplicitAuthService {

  behavior of "Rich message updater"

  it should "change text message to document message when image url in private chat" in t.privat.changeMessagePrivate()

  it should "change text message to document message when image url in group chat" in t.group.changeMessageGroup()

  it should "not change message without image url in private chat" in t.privat.dontChangePrivate()

  it should "not change message without image url in group chat" in t.group.dontChangeGroup()

  object t {

    val ThumbMinSize = 90
    implicit val ec = system.dispatcher

    implicit val socialManagerRegion = SocialManager.startRegion()

    val groupInviteConfig = GroupInviteConfig("http://actor.im")

    implicit val service = messaging.MessagingServiceImpl()
    implicit val groupsService = new GroupsServiceImpl(groupInviteConfig)

    RichMessageWorker.startWorker(RichMessageConfig(5 * 1024 * 1024))

    object privat {
      val (user1, authId, authSid, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user1.id, authSid, 42)))

      val (user2, _, _, _) = createUser()
      val user2Model = getUserModel(user2.id)
      val user2AccessHash = ACLUtils.userAccessHash(authId, user2.id, user2Model.accessSalt)
      val user2Peer = peers.ApiOutPeer(ApiPeerType.Private, user2.id, user2AccessHash)

      def dontChangePrivate() = {

        val resp1 = whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage(NonImages.mixedText, Vector.empty, None), None, None))(_.toOption.get)
        expectNoUpdate(resp1.seq, classOf[UpdateMessageContentChanged])

        val resp2 = whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage(NonImages.plainText, Vector.empty, None), None, None))(_.toOption.get)
        expectNoUpdate(resp2.seq, classOf[UpdateMessageContentChanged])

        val resp3 = whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage(NonImages.nonImageUrl, Vector.empty, None), None, None))(_.toOption.get)
        expectNoUpdate(resp2.seq, classOf[UpdateMessageContentChanged])

      }

      def changeMessagePrivate() = {

        {
          val image = Images.noNameHttp
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          val resp = whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage(image.url, Vector.empty, None), None, None))(_.toOption.get)

          expectUpdate(resp.seq, classOf[UpdateMessageContentChanged]) { update ⇒
            update.message should matchPattern {
              case ApiDocumentMessage(_, _, image.contentLength, _, image.mimeType, Some(ApiFastThumb(`thumbW`, `thumbH`, _)), Some(ApiDocumentExPhoto(image.w, image.h))) ⇒
            }
          }
        }

        {
          val image = Images.withNameHttp
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          val imageName = image.fileName.get
          val resp = whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage(image.url, Vector.empty, None), None, None))(_.toOption.get)

          expectUpdate(resp.seq, classOf[UpdateMessageContentChanged]) { update ⇒
            update.message should matchPattern {
              case ApiDocumentMessage(_, _, image.contentLength, `imageName`, image.mimeType, Some(ApiFastThumb(`thumbW`, `thumbH`, _)), Some(ApiDocumentExPhoto(image.w, image.h))) ⇒
            }
          }
        }

        {
          val image = Images.noNameHttps
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          val resp = whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage(image.url, Vector.empty, None), None, None))(_.toOption.get)

          expectUpdate(resp.seq, classOf[UpdateMessageContentChanged]) { update ⇒
            update.message should matchPattern {
              case ApiDocumentMessage(_, _, image.contentLength, _, image.mimeType, Some(ApiFastThumb(`thumbW`, `thumbH`, _)), Some(ApiDocumentExPhoto(image.w, image.h))) ⇒
            }
          }
        }
      }
    }

    object group {
      val (user1, authId1, authSid1, _) = createUser()
      val (user2, authId2, _, _) = createUser()
      val (user3, authId3, _, _) = createUser()

      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))

      val groupOutPeer = createGroup("Test group", Set(user2.id, user3.id)).groupPeer

      def dontChangeGroup() = {

        val resp1 = whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), ApiTextMessage(NonImages.mixedText, Vector.empty, None), None, None))(_.toOption.get)
        expectNoUpdate(resp1.seq, classOf[UpdateMessageContentChanged])

        val resp2 = whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), ApiTextMessage(NonImages.plainText, Vector.empty, None), None, None))(_.toOption.get)
        expectNoUpdate(resp2.seq, classOf[UpdateMessageContentChanged])

        val resp3 = whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), ApiTextMessage(NonImages.nonImageUrl, Vector.empty, None), None, None))(_.toOption.get)
        expectNoUpdate(resp3.seq, classOf[UpdateMessageContentChanged])
      }

      def changeMessageGroup() = {

        {
          val image = Images.noNameHttp
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          val resp = whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), ApiTextMessage(image.url, Vector.empty, None), None, None))(_.toOption.get)

          expectUpdate(resp.seq, classOf[UpdateMessageContentChanged]) { update ⇒
            update.message should matchPattern {
              case ApiDocumentMessage(_, _, image.contentLength, _, image.mimeType, Some(ApiFastThumb(`thumbW`, `thumbH`, _)), Some(ApiDocumentExPhoto(image.w, image.h))) ⇒
            }
          }
        }

        {
          val image = Images.withNameHttp
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          val imageName = image.fileName.get
          val resp = whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), ApiTextMessage(image.url, Vector.empty, None), None, None))(_.toOption.get)

          expectUpdate(resp.seq, classOf[UpdateMessageContentChanged]) { update ⇒
            update.message should matchPattern {
              case ApiDocumentMessage(_, _, image.contentLength, `imageName`, image.mimeType, Some(ApiFastThumb(`thumbW`, `thumbH`, _)), Some(ApiDocumentExPhoto(image.w, image.h))) ⇒
            }
          }
        }

        {
          val image = Images.noNameHttps
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          val resp = whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), ApiTextMessage(image.url, Vector.empty, None), None, None))(_.toOption.get)

          expectUpdate(resp.seq, classOf[UpdateMessageContentChanged]) { update ⇒
            update.message should matchPattern {
              case ApiDocumentMessage(_, _, image.contentLength, _, image.mimeType, Some(ApiFastThumb(`thumbW`, `thumbH`, _)), Some(ApiDocumentExPhoto(image.w, image.h))) ⇒
            }
          }
        }
      }
    }

  }

}
