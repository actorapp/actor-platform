package im.actor.server.user

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.server.{ KeyValueMappings, model, persist }
import shardakka.ShardakkaExtension
import slick.dbio.DBIO

import scala.concurrent.{ ExecutionContext, Future }

object ContactsUtils {

  def deleteContact(ownerUserId: Int, userId: Int)(implicit ec: ExecutionContext, timeout: Timeout, system: ActorSystem): DBIO[Int] =
    for {
      result ← persist.contact.UserContactRepo.delete(ownerUserId, userId)
    } yield result
}
