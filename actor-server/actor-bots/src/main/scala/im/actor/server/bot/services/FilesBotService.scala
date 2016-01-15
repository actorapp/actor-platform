package im.actor.server.bot.services

import akka.actor.ActorSystem
import im.actor.bots.BotMessages._
import im.actor.concurrent.FutureResultCats
import im.actor.server.acl.ACLUtils
import im.actor.server.bot.{ ApiToBotConversions, BotServiceBase }
import im.actor.server.db.DbExtension
import im.actor.server.file.FileStorageExtension
import im.actor.server.persist.FileRepo

private[bot] object FilesBotErrors {
  val LocationInvalid = BotError(400, "LOCATION_INVALID")
  val Forbidden = BotError(403, "FORBIDDEN")
  val DownloadFailed = BotError(500, "DOWNLOAD_FAILED")
  val FileTooBig = BotError(400, "FILE_TOO_BIG")
}

private[bot] final class FilesBotService(_system: ActorSystem) extends BotServiceBase(_system) with FutureResultCats[BotError] with ApiToBotConversions {

  import FilesBotErrors._

  private implicit val system: ActorSystem = _system
  import system.dispatcher

  private val fsAdapter = FileStorageExtension(system).fsAdapter
  private val db = DbExtension(system).db

  private val MaxSize = 1024 * 1024 * 5 //5Mb

  override def handlers: Handlers = {
    case DownloadFile(location) ⇒ downloadFile(location).toWeak
  }

  private def downloadFile(location: FileLocation) = RequestHandler[DownloadFile, DownloadFile#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          f ← fromFutureOption(LocationInvalid)(db.run(FileRepo.find(location.fileId)))
          _ ← fromBoolean(Forbidden)(location.accessHash == ACLUtils.fileAccessHash(f.id, f.accessSalt))
          _ ← fromBoolean(FileTooBig)(f.size <= MaxSize)
          data ← fromFutureOption(DownloadFailed)(fsAdapter.downloadFileF(f.id))
        } yield ResponseDownloadFile(data)).value
      }
  }

}
