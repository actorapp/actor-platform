package im.actor.server.office

import im.actor.api.rpc.messaging._

import scala.concurrent.ExecutionContext

trait PeerProcessor[State <: ProcessorState, Event <: AnyRef] extends Processor[State, Event] {

  private implicit val ec: ExecutionContext = context.dispatcher

  protected def getPushText(message: Message, clientName: String, outUser: Int): String = {
    message match {
      case TextMessage(text, _, _) ⇒
        formatAuthored(clientName, text)
      case dm: DocumentMessage ⇒
        dm.ext match {
          case Some(_: DocumentExPhoto) ⇒
            formatAuthored(clientName, "Photo")
          case Some(_: DocumentExVideo) ⇒
            formatAuthored(clientName, "Video")
          case _ ⇒
            formatAuthored(clientName, dm.name)
        }
      case unsupported ⇒ ""
    }
  }

  private def formatAuthored(authorName: String, message: String): String = s"${authorName}: ${message}"
}