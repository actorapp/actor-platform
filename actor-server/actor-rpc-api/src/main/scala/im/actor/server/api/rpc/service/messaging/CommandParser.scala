package im.actor.server.api.rpc.service.messaging

trait CommandParser {

  private val commandPattern = """^\/[a-zA-Z0-9_-]{1,255}(\s|$)""".r

  def parseCommand(content: String): Option[(String, Option[String])] = {
    val trimmed = content.trim
    commandPattern.findFirstIn(trimmed) map { slashCommand ⇒
      val command = (slashCommand substring 1).trim
      val text = Some(trimmed.replace(slashCommand, "").trim).filter(_.nonEmpty)
      (command, text)
    }
  }

}