package im.actor.server.commons

import com.typesafe.config.{ ConfigFactory, Config }

object ActorConfig {
  def load(): Config = {
    // to supress "possible missing interpolator" warning
    // TODO: consider smarter way
    val s = "$"

    ConfigFactory.parseString(
      s"""
        |akka {
        |  actor {
        |    provider: "akka.cluster.ClusterActorRefProvider"
        |
        |    kryo {
        |      idstrategy = "explicit"
        |      kryo-custom-serializer-init = "im.actor.server.commons.serialization.KryoInit"
        |    }
        |  }
        |
        |  remote {
        |    netty.tcp {
        |      hostname = "127.0.0.1"
        |      port = 2553
        |    }
        |  }
        |
        |  cluster {
        |    seed-nodes = [ "akka.tcp://actor-server@127.0.0.1:2553" ]
        |  }
        |
        |  persistence {
        |    journal.plugin = "jdbc-journal"
        |    snapshot-store.plugin = "jdbc-snapshot-store"
        |  }
        |}
        |
        |jdbc-connection {
        |  username: $s{services.postgresql.user}
        |  password: $s{services.postgresql.password}
        |  url: "jdbc:postgresql://"$s{services.postgresql.host}":"$s{services.postgresql.port}"/"$s{services.postgresql.db}
        |}
      """.stripMargin
    )
      .withFallback(ConfigFactory.load())
      .resolve()
  }
}
