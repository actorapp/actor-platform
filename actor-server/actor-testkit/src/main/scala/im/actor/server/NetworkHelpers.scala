package im.actor.server

import java.net.ServerSocket

object NetworkHelpers {
  def randomPort(): Int = {
    val socket = new ServerSocket(0)
    try {
      socket.setReuseAddress(true)
      socket.getLocalPort
    } finally { socket.close() }
  }
}
