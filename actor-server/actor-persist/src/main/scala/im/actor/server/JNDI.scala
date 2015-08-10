package im.actor.server

import javax.naming._
import javax.naming.Context._

/**
 * JNDI Helpers.
 */
object JNDI {

  private val IN_MEMORY_JNDI = "tyrex.naming.MemoryContextFactory"
  private val IN_MEMORY_URL = "/"

  /**
   * An in memory JNDI implementation.
   */
  lazy val initialContext = {

    val env = new java.util.Hashtable[String, String]

    env.put(INITIAL_CONTEXT_FACTORY, IN_MEMORY_JNDI)

    env.put(PROVIDER_URL, IN_MEMORY_URL)

    new InitialContext(env)

  }

}
