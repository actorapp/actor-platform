package im.actor.api

import im.actor.api.rpc._, messaging._

trait MessagingImplicits {
  implicit class ExtMessage(m: Message) {
    def toMessageContent: MessageContent = MessageContent(m.header, m)
  }
}
