package im.actor.server.llectro.results

case class Banner(advertUrl: String, imageUrl: String)

case class Errors(errors: String, status: Option[Int] = None)

case class UserBalance(name: String, balance: BigDecimal)
