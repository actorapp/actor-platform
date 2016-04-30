package im.actor.server.notify

import java.nio.file.Files

import im.actor.env.ActorEnv
import im.actor.server.email.Content

import scala.util.Try

final case class NotificationTemplate(private val templatePath: String) {
  private val emailTemplate: Option[String] =
    Try(new String(Files.readAllBytes(ActorEnv.getAbsolutePath(templatePath)))).toOption
  private val compatTemplate = "You have $$UNREAD_COUNT$$ unread messages in $$DIALOG_COUNT$$ dialogs."

  def render(name: String, unreadCount: Int, dialogCount: Int): Content = {
    def render(text: String) =
      text
        .replace("$$NAME$$", name)
        .replace("$$UNREAD_COUNT$$", unreadCount.toString)
        .replace("$$DIALOG_COUNT$$", dialogCount.toString)
    val compatText = render(compatTemplate)
    emailTemplate match {
      case Some(template) ⇒ Content(Some(render(template)), Some(compatText))
      case None           ⇒ Content(None, Some(compatText))
    }
  }
}