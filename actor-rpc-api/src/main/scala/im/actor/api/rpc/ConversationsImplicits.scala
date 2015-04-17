package im.actor.api

import com.google.protobuf.CodedInputStream
import org.joda.time.DateTime

import im.actor.api.rpc._, conversations._
import im.actor.api.rpc.messaging.{ Message, MessageContent }
import im.actor.server.models

trait ConversationsImplicits {
  implicit class ExtHistoryMessageModel(model: models.HistoryMessage) {
    def asStruct(lastReceivedAt: DateTime, lastReadAt: DateTime) = {
      val in = CodedInputStream.newInstance(model.messageContentData)
      MessageContent.parseFrom(in) match {
        case Right(messageContent) =>
          val state = if (model.userId == model.senderUserId) {
            if (model.date.getMillis < lastReadAt.getMillis) {
              Some(MessageState.Read)
            } else if (model.date.getMillis < lastReceivedAt.getMillis) {
              Some(MessageState.Received)
            } else {
              Some(MessageState.Sent)
            }
          } else {
            None // for incoming
          }

          HistoryMessage(senderUserId = model.senderUserId, randomId = model.randomId, date = model.date.getMillis, message = messageContent, state = state)
        case Left(e) => throw new Exception(s"Failed to parse message content $e")
      }
    }
  }
}
