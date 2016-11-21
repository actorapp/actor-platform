package im.actor.server.cli

import java.security.SecureRandom

import akka.http.scaladsl.util.FastFuture
import better.files._
import im.actor.crypto.Curve25519

import scala.concurrent.Future

final class SecurityHandlers {
  private val random = new SecureRandom()

  def createKey(path: String): Future[Unit] = {
    val pubPath = path + ".pub"
    val privatePath = path + ".private"

    val pubFile = File(pubPath)
    val privateFile = File(privatePath)

    if (pubFile.exists) {
      println(s"File $pubPath already exists!")
      FastFuture.successful(())
    } else if (privateFile.exists) {
      println(s"File $privatePath already exists!")
      FastFuture.successful(())
    } else {
      val randomBytes = new Array[Byte](32)
      random.nextBytes(randomBytes)

      val pair = Curve25519.keyGen(randomBytes)
      pubFile.write(pair.getPublicKey)
      privateFile.write(pair.getPrivateKey)

      println(
        s"""
           |Created $pubFile and $privateFile
           |You need to add the following to your server.conf:
           |
           |
           |modules: {
           |  # ... other modules
           |  security {
           |  # ... other settings
           |    server-keys: [
           |    # ... other server keys
           |      {
           |        public: "$pubPath"
           |        private: "$privatePath"
           |      }
           |    ]
           |  }
           |}
           |""".stripMargin
      )
      FastFuture.successful(())
    }
  }
}
