package utils

import im.actor.server.models
import org.apache.commons.codec.digest.DigestUtils
import play.api.libs.json.Reads
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import scala.concurrent.forkjoin.ThreadLocalRandom

object JsonConstructors {

  val length:Reads[String] = minLength[String](1) keepAnd maxLength[String](255)

  private val rnd = ThreadLocalRandom.current()

  def makeManager(name:String, lastName:String, domain: String, email:String): models.Manager = {
    val id = rnd.nextInt(Int.MaxValue) + 1
    val authToken = DigestUtils.sha256(name+lastName)
    models.Manager(id, name, lastName, domain, new String(authToken), email)
  }

}
