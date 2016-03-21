package im.actor.server.model

/**
 * @param event event fields, separated by semicolon(;)
 */
final case class ClientStats(id: Long, userId: Int, authId: Long, eventType: String, event: String)