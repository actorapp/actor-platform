package im.actor.server.webhooks

import scala.concurrent.ExecutionContext
import scala.concurrent.forkjoin.ThreadLocalRandom

import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.ClientData
import im.actor.api.rpc.messaging.{ Message, MessagingService, TextMessage }
import im.actor.api.rpc.peers.{ OutPeer, PeerType }
import im.actor.server.persist

class WebhookHandler(service: MessagingService)(implicit db: Database, ec: ExecutionContext) {

  def send(content: Content, token: String) = {
    val message: Message = content match {
      case Text(text)    ⇒ TextMessage(text, Vector.empty, None)
      case Document(url) ⇒ throw new NotImplementedError()
      case Image(url)    ⇒ throw new NotImplementedError()
    }

    db.run {
      for {
        optBot ← persist.GroupBot.findByToken(token)
        userAuth ← optBot.map { bot ⇒
          for {
            optGroup ← persist.Group.find(bot.groupId).headOption
            authIds ← persist.AuthId.findByUserId(bot.userId)

            authId ← (optGroup, authIds) match {
              case (None, _) ⇒ DBIO.successful(None)
              case (Some(group), auth +: _) ⇒
                val rnd = ThreadLocalRandom.current()
                val clientData = ClientData(auth.id, rnd.nextLong(), Some(bot.userId))
                DBIO.from(service.jhandleSendMessage(OutPeer(PeerType.Group, group.id, group.accessHash), rnd.nextLong(), message, clientData))

              case (Some(group), Seq()) ⇒
                val rnd = ThreadLocalRandom.current()
                val authId = rnd.nextLong()
                val clientData = ClientData(authId, rnd.nextLong(), Some(bot.userId))
                for {
                  _ ← persist.AuthId.create(authId, Some(bot.userId), None)
                  _ ← DBIO.from(service.jhandleSendMessage(OutPeer(PeerType.Group, group.id, group.accessHash), ThreadLocalRandom.current().nextLong(), message, clientData))
                } yield ()
            }
          } yield ()
        }.getOrElse(DBIO.successful(None))
      } yield ()
    }
  }

}
