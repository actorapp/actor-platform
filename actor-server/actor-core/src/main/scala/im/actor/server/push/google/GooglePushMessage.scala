package im.actor.server.push.google

final case class GooglePushMessage(
  to:           String,
  collapse_key: Option[String],
  data:         Option[Map[String, String]],
  time_to_live: Option[Int]
)
