package im.actor.server.ilectro

import java.util.UUID

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory
import slick.dbio
import slick.dbio.Effect.{ Read, Transactional, Write }
import slick.driver.PostgresDriver.api._

import im.actor.server.ilectro.results.{ Banner, Errors }
import im.actor.server.models.ilectro.{ ILectroUser, Interest }
import im.actor.server.{ models, persist }

class ILectro(implicit system: ActorSystem) {
  private implicit val ec = system.dispatcher
  private implicit val meterializer = ActorFlowMaterializer()
  private implicit val http = Http()

  private implicit val config = ILectroConfig(ConfigFactory.load().getConfig("actor-server.ilectro"))

  val users = new Users()
  private val lists = new Lists()

  def createUser(userId: Int, name: String): dbio.DBIOAction[ILectroUser, NoStream, Write with Effect with Transactional] = {
    val ilectroUser = models.ilectro.ILectroUser(userId, UUID.randomUUID(), name)

    (for {
      _ ← persist.ilectro.ILectroUser.create(ilectroUser)
      createResult ← DBIO.from(users.create(ilectroUser))
    } yield {
      if (createResult.isLeft)
        throw new Exception(s"Failed to create user ${createResult}")

      ilectroUser
    }).transactionally
  }

  def deleteInterest(user: ILectroUser, interests: Vector[Int]): dbio.DBIOAction[Vector[Either[Errors, Unit]], NoStream, Write with Effect with Transactional] = {
    DBIO.sequence(
      for {
        result ← interests.map { interest ⇒
          (for {
            _ ← persist.ilectro.UserInterest.delete(user.userId, interest)
            resp ← DBIO.from(users.deleteInterest(user.uuid, interest))
          } yield resp).transactionally
        }
      } yield result
    )
  }

  def addInterests(user: ILectroUser, interests: Vector[Int]): dbio.DBIOAction[Right[Errors, Unit], NoStream, Read with Write with Effect with Transactional] = {
    (for {
      _ ← DBIO.sequence(interests.map(id ⇒ persist.ilectro.UserInterest.createIfNotExists(user.userId, id)))
      resp ← DBIO.from(users.addInterests(user.uuid, interests.toList)) flatMap {
        case Left(e)      ⇒ DBIO.failed(new Exception(e.errors))
        case r @ Right(_) ⇒ DBIO.successful(r)
      }
    } yield resp).transactionally
  }

  def getBanners(userUuid: UUID): Future[Banner] = {
    users.getBanners(userUuid) map {
      case Right(banners) ⇒ banners
      case Left(e)        ⇒ throw new Exception(s"Failed to get banners: $e")
    }
  }

  def getAndPersistInterests()(implicit db: Database): Future[Int] = {
    lists.getInterests() flatMap {
      case Right(interests) ⇒
        persistInterests(interests)
      case Left(e) ⇒
        throw new Exception(s"Failed to load interests: ${e}")
    }
  }

  def persistInterests(interests: List[Interest])(implicit db: Database): Future[Int] = {
    db.run {
      persist.ilectro.Interest.createOrUpdate(interests)
    } map (_.sum)
  }
}
