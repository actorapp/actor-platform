package sql.migration

import java.sql.Connection
import java.util.Base64

import akka.actor.ActorSystem
import akka.persistence.PersistentRepr
import akka.serialization.SerializationExtension
import im.actor.server.CommonSerialization
import im.actor.server.group.GroupProcessor
import im.actor.server.user.UserProcessor
import org.flywaydb.core.api.migration.jdbc.JdbcMigration

import scala.concurrent.Await
import scala.concurrent.duration.Duration

final class V20160128142000__AkkaPersistence extends JdbcMigration {

  CommonSerialization.register()
  GroupProcessor.register()
  UserProcessor.register()

  override def migrate(connection: Connection): Unit = {
    val system = ActorSystem("migration")
    val serialization = SerializationExtension(system)

    val stmt = connection.prepareStatement("SELECT persistence_id, sequence_number, marker, message, created FROM journal LIMIT 10")
    val rs = stmt.executeQuery()

    while (rs.next()) {
      val persistenceId = rs.getString("persistence_id")
      println(s"=== ${persistenceId}")
      val sequenceNumber = rs.getLong("sequence_number")
      val marker = rs.getString("marker")
      val message = Base64.getDecoder.decode(rs.getString("message"))
      val created = rs.getTimestamp("created")
      val repr = serialization.deserialize(message, classOf[PersistentRepr])
      println(repr.get)
    }

    system.terminate()
    Await.result(system.whenTerminated, Duration.Inf)
    //throw new RuntimeException("xaxaxa")

  }
}