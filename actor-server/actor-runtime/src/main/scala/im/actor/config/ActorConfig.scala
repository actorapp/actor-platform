package im.actor.config

import com.typesafe.config.{ Config, ConfigFactory }

object ActorConfig {
  def load(): Config = {
    ConfigFactory.parseString(
      """
        |akka {
        |  actor {
        |    provider: "akka.cluster.ClusterActorRefProvider"
        |  }
        |}
        |
        |jdbc-connection {
        |  jndiPath: "/"
        |  dataSourceName: "DefaultDataSource"
        |}
      """.stripMargin
    )
      .withFallback(ConfigFactory.load())
      .withFallback(ConfigFactory.parseString(
        """
          |akka {
          |  persistence {
          |    journal.plugin: "jdbc-journal"
          |    snapshot-store.plugin: "jdbc-snapshot-store"
          |  }
          |}
        """.stripMargin
      ))
      .resolve()
  }
}
