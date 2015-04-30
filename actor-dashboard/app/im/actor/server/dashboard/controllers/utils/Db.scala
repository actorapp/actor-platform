package im.actor.server.dashboard.controllers.utils

import com.typesafe.config.ConfigFactory

import im.actor.server.db.DbInit

object Db extends DbInit {

  val db = init

  private def init = {
    val config = ConfigFactory.load()
    val serverConfig = config.getConfig("actor-server")
    val sqlConfig = serverConfig.getConfig("persist.sql")

    val ds = initDs(sqlConfig)
    initDb(ds)
  }

}
