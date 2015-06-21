package im.actor.server.session

trait MessageIdHelper {
  var lastMessageId: Long = 0

  protected def nextMessageId(): Long = {
    lastMessageId += 1
    lastMessageId
  }
}
