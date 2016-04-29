package im.actor.server.email

import akka.actor._

sealed trait EmailExtension extends Extension

final class EmailExtensionImpl(system: ActorSystem) extends EmailExtension {
  import system.dispatcher
  private val config = EmailConfig.load.get
  val sender: EmailSender = new SmtpEmailSender(config)
}

object EmailExtension extends ExtensionId[EmailExtensionImpl] with ExtensionIdProvider {
  override def lookup = EmailExtension

  override def createExtension(system: ExtendedActorSystem) = new EmailExtensionImpl(system)
}
