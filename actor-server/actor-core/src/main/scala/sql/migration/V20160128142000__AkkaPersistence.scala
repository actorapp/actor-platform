package sql.migration

import java.sql.Connection
import java.time.Instant
import java.util.Base64

import akka.actor.ActorSystem
import akka.persistence.jdbc.serialization.{ SerializationFacade, Serialized }
import akka.persistence.journal.Tagged
import akka.persistence.{ AtomicWrite, PersistentRepr }
import akka.serialization.{ Serialization, SerializationExtension }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import im.actor.serialization.ActorSerializer
import im.actor.server.CommonSerialization
import im.actor.server.db.DbExtension
import im.actor.server.event.TSEvent
import im.actor.server.group.{ GroupEvent, GroupProcessor }
import im.actor.server.user.{ UserEvent, UserProcessor }
import org.flywaydb.core.api.migration.jdbc.JdbcMigration
import org.joda.time.DateTime
import shardakka.keyvalue.{ RootEvents, ValueCommands, ValueEvents, ValueQueries }

import scala.concurrent._
import scala.concurrent.duration.Duration

final class V20160128142000__AkkaPersistence extends JdbcMigration {

  CommonSerialization.register()
  GroupProcessor.register()
  UserProcessor.register()
  ActorSerializer.register(5201, classOf[RootEvents.KeyCreated])
  ActorSerializer.register(5202, classOf[RootEvents.KeyDeleted])

  ActorSerializer.register(5301, classOf[ValueCommands.Upsert])
  ActorSerializer.register(5302, classOf[ValueCommands.Delete])
  ActorSerializer.register(5303, classOf[ValueCommands.Ack])

  ActorSerializer.register(5401, classOf[ValueQueries.Get])
  ActorSerializer.register(5402, classOf[ValueQueries.GetResponse])

  ActorSerializer.register(5501, classOf[ValueEvents.ValueUpdated])
  ActorSerializer.register(5502, classOf[ValueEvents.ValueDeleted])

  override def migrate(connection: Connection): Unit = {
    implicit val system = DbExtension.system
    implicit val serialization = SerializationExtension(system)

    val seqs = getSeqs(connection)

    val events =
      seqs.flatMap {
        case (persistenceId, seq) ⇒
          getObsoleteEvents(connection, persistenceId, seq)
      }.map {
        case (obsEvent, evTs) ⇒
          val payload =
            obsEvent.payload match {
              case TSEvent(ts, ue: UserEvent) ⇒
                val e = convert(ts, ue)
                Tagged(e, e.tags)
              case TSEvent(ts, ge: GroupEvent) ⇒
                val e = convert(ts, ge)
                Tagged(e, e.tags)
              case other ⇒ other
            }
          (obsEvent.withPayload(payload).withManifest("V1"), evTs)
      }

    batchWrite(connection, events)
  }

  private def batchWrite(
    connection: Connection,
    events:     List[(PersistentRepr, Long)]
  )(implicit system: ActorSystem): Unit = {
    implicit val mat = ActorMaterializer()
    val sql =
      """
        |INSERT INTO persistence_journal (persistence_id, sequence_number, created, tags, message)
        |VALUES (?, ?, ?, ?, ?)
      """.stripMargin

    val flowResult =
      Source(events)
        .map(p ⇒ AtomicWrite(p._1))
        .via(SerializationFacade(system, ",").serialize(serialize = true))
        .map(_.get)
        .map { iter ⇒
          val ps = connection.prepareStatement(sql)
          try {
            for {
              ser ← iter
            } yield {
              ps.setString(1, ser.persistenceId)
              ps.setLong(2, ser.sequenceNr)
              ps.setLong(3, ser.created)
              ps.setString(4, ser.tags.orNull)
              ps.setBytes(5, ser.asInstanceOf[Serialized].serialized)
              ps.addBatch()
            }
            ps.execute()
          } finally {
            ps.close()
          }
        }.runForeach(_ ⇒ ())

    Await.result(flowResult, Duration.Inf)
  }

  private def convert(ts: DateTime, event: GroupEvent): GroupEvent = {
    import im.actor.server.group.GroupEvents._
    val instant = Instant.ofEpochMilli(ts.getMillis)

    event match {
      case e: Created                 ⇒ e.withTs(instant)
      case e: UserInvited             ⇒ e.withTs(instant)
      case e: UserJoined              ⇒ e.withTs(instant)
      case e: UserKicked              ⇒ e.withTs(instant)
      case e: UserLeft                ⇒ e.withTs(instant)
      case e: BotAdded                ⇒ e.withTs(instant)
      case e: AvatarUpdated           ⇒ e.withTs(instant)
      case e: TitleUpdated            ⇒ e.withTs(instant)
      case e: BecamePublic            ⇒ e.withTs(instant)
      case e: AboutUpdated            ⇒ e.withTs(instant)
      case e: TopicUpdated            ⇒ e.withTs(instant)
      case e: UserBecameAdmin         ⇒ e.withTs(instant)
      case e: IntegrationTokenRevoked ⇒ e.withTs(instant)
    }
  }

  private def convert(ts: DateTime, event: UserEvent): UserEvent = {
    import im.actor.server.user.UserEvents._
    val instant = Instant.ofEpochMilli(ts.getMillis)

    event match {
      case e: AuthAdded                 ⇒ e.withTs(instant)
      case e: AuthRemoved               ⇒ e.withTs(instant)
      case e: Created                   ⇒ e.withTs(instant)
      case e: IsAdminUpdated            ⇒ e.withTs(instant)
      case e: PhoneAdded                ⇒ e.withTs(instant)
      case e: EmailAdded                ⇒ e.withTs(instant)
      case e: SocialContactAdded        ⇒ e.withTs(instant)
      case e: CountryCodeChanged        ⇒ e.withTs(instant)
      case e: NameChanged               ⇒ e.withTs(instant)
      case e: Deleted                   ⇒ e.withTs(instant)
      case e: NicknameChanged           ⇒ e.withTs(instant)
      case e: AboutChanged              ⇒ e.withTs(instant)
      case e: AvatarUpdated             ⇒ e.withTs(instant)
      case e: PreferredLanguagesChanged ⇒ e.withTs(instant)
      case e: TimeZoneChanged           ⇒ e.withTs(instant)
      case e: LocalNameChanged          ⇒ e.withTs(instant)
    }
  }

  private def getObsoleteEvents(
    connection:    Connection,
    persistenceId: String,
    seq:           Long
  )(implicit serialization: Serialization): List[(PersistentRepr, Long)] = {
    val stmt = connection.prepareStatement(
      """
        |SELECT persistence_id, sequence_number, marker, message, created FROM journal
        |WHERE persistence_id = ? AND sequence_number > ?
      """.stripMargin
    )

    try {
      stmt.setString(1, persistenceId)
      stmt.setLong(2, seq)
      val rs = stmt.executeQuery()

      var events = List.empty[(PersistentRepr, Long)]

      while (rs.next()) {
        val message = Base64.getDecoder.decode(rs.getString("message"))
        val created = rs.getTimestamp("created")
        events = (serialization.deserialize(message, classOf[PersistentRepr]).get, created.toInstant.toEpochMilli) :: events
      }

      events

    } finally {
      stmt.close()
    }
  }

  private def getPersistenceIds(connection: Connection): List[String] = {
    val stmt = connection.prepareStatement("SELECT DISTINCT persistence_id FROM journal")
    try {
      val rs = stmt.executeQuery()
      var ids = List.empty[String]

      while (rs.next()) {
        ids = rs.getString(1) :: ids
      }

      ids
    } finally {
      stmt.close()
    }
  }

  private def getSeqs(connection: Connection): List[(String, Long)] = {
    val persistenceIds = getPersistenceIds(connection)

    val stmt = connection.prepareStatement(
      """
        |SELECT sequence_number FROM persistence_journal WHERE persistence_id = ?
        |ORDER BY sequence_number DESC LIMIT 1
      """.stripMargin
    )
    try {
      persistenceIds map { id ⇒
        stmt.setString(1, id)
        val rs = stmt.executeQuery()
        val seq =
          if (rs.next())
            rs.getLong(1)
          else
            0L
        (id, seq)
      }
    } finally {
      stmt.close()
    }
  }
}