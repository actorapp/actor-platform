package im.actor.server.oauth

object OAuth2ProvidersDomains {
  private val google = List("gmail.com", "googlemail.com")
  private val mailRu = List() //example

  private val domains = google ++ mailRu

  def supportsOAuth2(email: String): Boolean = domains exists email.contains
}
