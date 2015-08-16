package im.actor.server

import javax.naming.Context._

import tyrex.naming.MemoryContextFactory

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

    System.setProperty(INITIAL_CONTEXT_FACTORY, IN_MEMORY_JNDI)
    env.put(INITIAL_CONTEXT_FACTORY, IN_MEMORY_JNDI)

    System.setProperty(PROVIDER_URL, IN_MEMORY_URL)
    env.put(PROVIDER_URL, IN_MEMORY_URL)

    new MemoryContextFactory().getInitialContext(env)
  }

}
