package im.actor.server.enrich

import im.actor.server.group.GroupOffice
import im.actor.server.user.UserOffice

import scala.util.Random

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import slick.dbio.{ DBIO, DBIOAction, Effect, NoStream }

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.files.FastThumb
import im.actor.api.rpc.messaging.{ DocumentExPhoto, DocumentMessage, TextMessage }
import im.actor.api.rpc.peers.PeerType
import im.actor.api.rpc.{ ClientData, peers }
import im.actor.server._
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.{ GroupsServiceHelpers, messaging }
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager
import im.actor.server.util.ACLUtils

class RichMessageWorkerSpec extends BaseAppSuite with GroupsServiceHelpers with MessageParsing with ImplicitFileStorageAdapter {

  behavior of "Rich message updater"

  it should "change text message to document message when image url in private chat" in t.privat.changeMessagePrivate()

  it should "change text message to document message when image url in group chat" in t.group.changeMessageGroup()

  it should "not change message without image url in private chat" in t.privat.dontChangePrivate()

  it should "not change message without image url in group chat" in t.group.dontChangeGroup()

  val awsCredentials = new EnvironmentVariableCredentialsProvider()

  object t {

    val ThumbMinSize = 90
    implicit val ec = system.dispatcher

    implicit val sessionRegion = buildSessionRegionProxy()
    implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
    implicit val socialManagerRegion = SocialManager.startRegion()
    implicit val presenceManagerRegion = PresenceManager.startRegion()
    implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
    implicit val privatePeerManagerRegion = UserOffice.startRegion()
    implicit val groupPeerManagerRegion = GroupOffice.startRegion()

    val groupInviteConfig = GroupInviteConfig("http://actor.im")

    implicit val service = messaging.MessagingServiceImpl(mediator)
    implicit val groupsService = new GroupsServiceImpl(groupInviteConfig)
    val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
    implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
    implicit val authService = buildAuthService()

    RichMessageWorker.startWorker(RichMessageConfig(5 * 1024 * 1024), mediator)

    def sleepSome = futureSleep(4000)

    def withCleanup(cleanupAction: DBIOAction[Unit, NoStream, Effect.Write])(block: ⇒ Unit) = whenReady(db.run(cleanupAction))(_ ⇒ block)

    object privat {
      val (user1, authId, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(user1.id))

      val (user2, _, _) = createUser()
      val user2Model = getUserModel(user2.id)
      val user2AccessHash = ACLUtils.userAccessHash(authId, user2.id, user2Model.accessSalt)
      val user2Peer = peers.OutPeer(PeerType.Private, user2.id, user2AccessHash)

      val selectMessages =
        for {
          messages ← DBIO.sequence(List(
            persist.HistoryMessage.find(user1.id, models.Peer.privat(user2.id)),
            persist.HistoryMessage.find(user2.id, models.Peer.privat(user1.id))
          ))
        } yield messages.flatMap(identity)

      val deleteMessages =
        for {
          _ ← DBIO.sequence(List(
            persist.HistoryMessage.deleteAll(user1.id, models.Peer.privat(user2.id)),
            persist.HistoryMessage.deleteAll(user2.id, models.Peer.privat(user1.id))
          ))
        } yield ()

      def dontChangePrivate() = {

        withCleanup(deleteMessages) {
          whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage(NonImages.mixedText, Vector.empty, None)).flatMap(_ ⇒ sleepSome))(_ ⇒ ())

          whenReady(db.run(selectMessages)) { messages ⇒
            messages should have length 2
            messages
              .map(e ⇒ parseMessage(e.messageContentData))
              .foreach(_ should matchPattern {
                case Right(TextMessage(NonImages.mixedText, _, _)) ⇒
              })
          }
        }

        withCleanup(deleteMessages) {
          whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage(NonImages.plainText, Vector.empty, None)).flatMap(_ ⇒ sleepSome))(_ ⇒ ())

          whenReady(db.run(selectMessages)) { messages ⇒
            messages should have length 2
            messages
              .map(e ⇒ parseMessage(e.messageContentData))
              .foreach(_ should matchPattern {
                case Right(TextMessage(NonImages.plainText, _, _)) ⇒
              })
          }
        }

        withCleanup(deleteMessages) {
          whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage(NonImages.nonImageUrl, Vector.empty, None)).flatMap(_ ⇒ sleepSome))(_ ⇒ ())

          whenReady(db.run(selectMessages)) { messages ⇒
            messages should have length 2
            messages
              .map(e ⇒ parseMessage(e.messageContentData))
              .foreach(_ should matchPattern {
                case Right(TextMessage(NonImages.nonImageUrl, _, _)) ⇒
              })
          }
        }
      }

      def changeMessagePrivate() = {
        withCleanup(deleteMessages) {
          val image = Images.noNameHttp
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage(image.url, Vector.empty, None)).flatMap(_ ⇒ sleepSome))(_ ⇒ ())

          whenReady(db.run(selectMessages)) { messages ⇒
            messages should have length 2
            messages
              .map(e ⇒ parseMessage(e.messageContentData))
              .foreach(_ should matchPattern {
                case Right(DocumentMessage(_, _, image.contentLength, _, image.mimeType, Some(FastThumb(`thumbW`, `thumbH`, _)), Some(DocumentExPhoto(image.w, image.h)))) ⇒
              })
          }
        }

        withCleanup(deleteMessages) {
          val image = Images.withNameHttp
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          val imageName = image.fileName.get
          whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage(image.url, Vector.empty, None)).flatMap(_ ⇒ sleepSome))(_ ⇒ ())

          whenReady(db.run(selectMessages)) { messages ⇒
            messages should have length 2
            messages
              .map(e ⇒ parseMessage(e.messageContentData))
              .foreach(_ should matchPattern {
                case Right(DocumentMessage(_, _, image.contentLength, `imageName`, image.mimeType, Some(FastThumb(`thumbW`, `thumbH`, _)), Some(DocumentExPhoto(image.w, image.h)))) ⇒
              })
          }
        }

        withCleanup(deleteMessages) {
          val image = Images.noNameHttps
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage(image.url, Vector.empty, None)).flatMap(_ ⇒ sleepSome))(_ ⇒ ())

          whenReady(db.run(selectMessages)) { messages ⇒
            messages should have length 2
            messages
              .map(e ⇒ parseMessage(e.messageContentData))
              .foreach(_ should matchPattern {
                case Right(DocumentMessage(_, _, image.contentLength, _, image.mimeType, Some(FastThumb(`thumbW`, `thumbH`, _)), Some(DocumentExPhoto(image.w, image.h)))) ⇒
              })
          }
        }
      }
    }

    object group {
      val (user1, authId1, _) = createUser()
      val (user2, authId2, _) = createUser()
      val (user3, authId3, _) = createUser()

      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

      val groupOutPeer = createGroup("Test group", Set(user2.id, user3.id)).groupPeer

      val selectMessages =
        for {
          messages ← DBIO.sequence(List(
            persist.HistoryMessage.find(user1.id, models.Peer.group(groupOutPeer.groupId)),
            persist.HistoryMessage.find(user2.id, models.Peer.group(groupOutPeer.groupId)),
            persist.HistoryMessage.find(user3.id, models.Peer.group(groupOutPeer.groupId))
          ))
        } yield messages.flatMap(identity)

      val deleteMessages =
        for {
          _ ← DBIO.sequence(List(
            persist.HistoryMessage.deleteAll(user1.id, models.Peer.group(groupOutPeer.groupId)),
            persist.HistoryMessage.deleteAll(user2.id, models.Peer.group(groupOutPeer.groupId)),
            persist.HistoryMessage.deleteAll(user3.id, models.Peer.group(groupOutPeer.groupId))
          ))
        } yield ()

      def dontChangeGroup() = {
        withCleanup(deleteMessages) {
          whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage(NonImages.mixedText, Vector.empty, None)).flatMap(_ ⇒ sleepSome))(_ ⇒ ())

          whenReady(db.run(selectMessages)) { messages ⇒
            messages should have length 3
            messages
              .map(e ⇒ parseMessage(e.messageContentData))
              .foreach(_ should matchPattern {
                case Right(TextMessage(NonImages.mixedText, _, _)) ⇒
              })
          }
        }

        withCleanup(deleteMessages) {
          whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage(NonImages.plainText, Vector.empty, None)).flatMap(_ ⇒ sleepSome))(_ ⇒ ())

          whenReady(db.run(selectMessages)) { messages ⇒
            messages should have length 3
            messages
              .map(e ⇒ parseMessage(e.messageContentData))
              .foreach(_ should matchPattern {
                case Right(TextMessage(NonImages.plainText, _, _)) ⇒
              })
          }
        }

        withCleanup(deleteMessages) {
          whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage(NonImages.nonImageUrl, Vector.empty, None)).flatMap(_ ⇒ futureSleep(5000)))(_ ⇒ ())

          whenReady(db.run(selectMessages)) { messages ⇒
            messages should have length 3
            messages
              .map(e ⇒ parseMessage(e.messageContentData))
              .foreach(_ should matchPattern {
                case Right(TextMessage(NonImages.nonImageUrl, _, _)) ⇒
              })
          }
        }
      }

      def changeMessageGroup() = {
        withCleanup(deleteMessages) {
          val image = Images.noNameHttp
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage(image.url, Vector.empty, None)).flatMap(_ ⇒ futureSleep(5000)))(_ ⇒ ())

          whenReady(db.run(selectMessages)) { messages ⇒
            messages should have length 3
            messages
              .map(e ⇒ parseMessage(e.messageContentData))
              .foreach(_ should matchPattern {
                case Right(DocumentMessage(_, _, image.contentLength, _, image.mimeType, Some(FastThumb(`thumbW`, `thumbH`, _)), Some(DocumentExPhoto(image.w, image.h)))) ⇒
              })
          }
        }

        withCleanup(deleteMessages) {
          val image = Images.withNameHttp
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          val imageName = image.fileName.get
          whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage(image.url, Vector.empty, None)).flatMap(_ ⇒ futureSleep(5000)))(_ ⇒ ())

          whenReady(db.run(selectMessages)) { messages ⇒
            messages should have length 3
            messages
              .map(e ⇒ parseMessage(e.messageContentData))
              .foreach(_ should matchPattern {
                case Right(DocumentMessage(_, _, image.contentLength, `imageName`, image.mimeType, Some(FastThumb(`thumbW`, `thumbH`, _)), Some(DocumentExPhoto(image.w, image.h)))) ⇒
              })
          }
        }

        withCleanup(deleteMessages) {
          val image = Images.noNameHttps
          val (thumbW, thumbH) = image.getThumbWH(ThumbMinSize)
          whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage(image.url, Vector.empty, None)).flatMap(_ ⇒ futureSleep(5000)))(_ ⇒ ())

          whenReady(db.run(selectMessages)) { messages ⇒
            messages should have length 3
            messages
              .map(e ⇒ parseMessage(e.messageContentData))
              .foreach(_ should matchPattern {
                case Right(DocumentMessage(_, _, image.contentLength, _, image.mimeType, Some(FastThumb(`thumbW`, `thumbH`, _)), Some(DocumentExPhoto(image.w, image.h)))) ⇒
              })
          }
        }
      }
    }

  }

}
