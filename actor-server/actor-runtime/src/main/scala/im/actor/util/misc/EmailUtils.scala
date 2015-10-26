package im.actor.util.misc

object EmailUtils {

  private val testMailRegex = """^.*@{1}acme\d{4}.com""".r

  def isTestEmail(email: String): Boolean = testMailRegex.findFirstIn(email).isDefined

}
