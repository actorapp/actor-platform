package im.actor.server.ilectro.results

case class Banner(advertUrl: String, imageUrl: String)

case class Errors(errors: String, status: Option[Int] = None)
