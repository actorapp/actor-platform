package im.actor.server.enrich

import im.actor.api.rpc.files.ApiFastThumb
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.{ AuthData, ClientData }
import im.actor.server._
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging
import im.actor.server.social.SocialManager

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

      def dontChangePrivate() = {

        val state1 = {
          val resp = sendMessageToUser(user2.id, NonImages.mixedText)._2
          mkSeqState(resp.seq, resp.state)
        }
        expectNoUpdate(state1, classOf[UpdateMessageContentChanged])

        val state2 = {
          val resp = sendMessageToUser(user2.id, NonImages.plainText)._2
          mkSeqState(resp.seq, resp.state)
        }
        expectNoUpdate(state2, classOf[UpdateMessageContentChanged])

        val state3 = {
          val resp = sendMessageToUser(user2.id, NonImages.nonImageUrl)._2
          mkSeqState(resp.seq, resp.state)
        }
        expectNoUpdate(state3, classOf[UpdateMessageContentChanged])

      }

      def changeMessagePrivate() = {

        {
          val image = Images.noNameHttp
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)

          val state = {
            val resp = sendMessageToUser(user2.id, image.url)._2
            mkSeqState(resp.seq, resp.state)
          }

          expectUpdate(state, classOf[UpdateMessageContentChanged]) { update ⇒
            update.message should matchPattern {
              case ApiDocumentMessage(_, _, image.contentLength, _, image.mimeType, Some(ApiFastThumb(`thumbW`, `thumbH`, _)), Some(ApiDocumentExPhoto(image.w, image.h))) ⇒
            }
          }
        }

        {
          val image = Images.withNameHttp
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          val imageName = image.fileName.get

          val state = {
            val resp = sendMessageToUser(user2.id, image.url)._2
            mkSeqState(resp.seq, resp.state)
          }

          expectUpdate(state, classOf[UpdateMessageContentChanged]) { update ⇒
            update.message should matchPattern {
              case ApiDocumentMessage(_, _, image.contentLength, `imageName`, image.mimeType, Some(ApiFastThumb(`thumbW`, `thumbH`, _)), Some(ApiDocumentExPhoto(image.w, image.h))) ⇒
            }
          }
        }

        {
          val image = Images.noNameHttps
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)

          val state = {
            val resp = sendMessageToUser(user2.id, image.url)._2
            mkSeqState(resp.seq, resp.state)
          }

          expectUpdate(state, classOf[UpdateMessageContentChanged]) { update ⇒
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

      val groupId = createGroup("Test group", Set(user2.id, user3.id)).groupPeer.groupId

      def dontChangeGroup() = {

        val state1 = {
          val resp = sendMessageToGroup(groupId, textMessage(NonImages.mixedText))._2
          mkSeqState(resp.seq, resp.state)
        }
        expectNoUpdate(state1, classOf[UpdateMessageContentChanged])

        val state2 = {
          val resp = sendMessageToGroup(groupId, textMessage(NonImages.plainText))._2
          mkSeqState(resp.seq, resp.state)
        }
        expectNoUpdate(state2, classOf[UpdateMessageContentChanged])

        val state3 = {
          val resp = sendMessageToGroup(groupId, textMessage(NonImages.nonImageUrl))._2
          mkSeqState(resp.seq, resp.state)
        }
        expectNoUpdate(state3, classOf[UpdateMessageContentChanged])
      }

      def changeMessageGroup() = {

        {
          val image = Images.noNameHttp
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)

          val state = {
            val resp = sendMessageToGroup(groupId, textMessage(image.url))._2
            mkSeqState(resp.seq, resp.state)
          }

          expectUpdate(state, classOf[UpdateMessageContentChanged]) { update ⇒
            update.message should matchPattern {
              case ApiDocumentMessage(_, _, image.contentLength, _, image.mimeType, Some(ApiFastThumb(`thumbW`, `thumbH`, _)), Some(ApiDocumentExPhoto(image.w, image.h))) ⇒
            }
          }
        }

        {
          val image = Images.withNameHttp
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          val imageName = image.fileName.get

          val state = {
            val resp = sendMessageToGroup(groupId, textMessage(image.url))._2
            mkSeqState(resp.seq, resp.state)
          }

          expectUpdate(state, classOf[UpdateMessageContentChanged]) { update ⇒
            update.message should matchPattern {
              case ApiDocumentMessage(_, _, image.contentLength, `imageName`, image.mimeType, Some(ApiFastThumb(`thumbW`, `thumbH`, _)), Some(ApiDocumentExPhoto(image.w, image.h))) ⇒
            }
          }
        }

        {
          val image = Images.noNameHttps
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)

          val state = {
            val resp = sendMessageToGroup(groupId, textMessage(image.url))._2
            mkSeqState(resp.seq, resp.state)
          }

          expectUpdate(state, classOf[UpdateMessageContentChanged]) { update ⇒
            update.message should matchPattern {
              case ApiDocumentMessage(_, _, image.contentLength, _, image.mimeType, Some(ApiFastThumb(`thumbW`, `thumbH`, _)), Some(ApiDocumentExPhoto(image.w, image.h))) ⇒
            }
          }
        }
      }
    }

  }

}
