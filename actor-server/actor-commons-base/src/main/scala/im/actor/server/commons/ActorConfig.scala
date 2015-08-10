package im.actor.server.commons

import com.typesafe.config.{ ConfigFactory, Config }

object ActorConfig {
  def load(): Config = {
    ConfigFactory.parseString(
      s"""
        |akka {
        |  actor {
        |    provider: "akka.cluster.ClusterActorRefProvider"
        |  }
        |
        |  remote {
        |    netty.tcp {
        |      hostname: "127.0.0.1"
        |      port: 2553
        |    }
        |  }
        |
        |  cluster {
        |    seed-nodes: [ "akka.tcp://actor-server@127.0.0.1:2553" ]
        |  }
        |
        |  persistence {
        |    journal.plugin: "jdbc-journal"
        |    snapshot-store.plugin: "jdbc-snapshot-store"
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
      .resolve()
  }
}
