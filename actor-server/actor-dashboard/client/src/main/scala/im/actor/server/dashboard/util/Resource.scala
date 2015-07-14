package im.actor.server.dashboard.util

abstract class Resource(baseUri: String) {
  def makeUrl(uri: String): String = baseUri + uri
}