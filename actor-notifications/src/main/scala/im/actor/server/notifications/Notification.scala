package im.actor.server.notifications

case class Notification(userId: Int, data: Map[Option[String], Int])
