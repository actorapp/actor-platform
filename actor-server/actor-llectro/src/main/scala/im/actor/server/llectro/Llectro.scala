package im.actor.server.llectro

import java.util.UUID

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import slick.dbio
import slick.dbio.Effect.{ Read, Transactional, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.FixedSqlAction

import im.actor.server.llectro.results.{ UserBalance, Banner, Errors }
import im.actor.server.models.llectro.{ LlectroUser, Interest }
import im.actor.server.{ models, persist }

class Llectro(implicit system: ActorSystem) {
  private implicit val ec = system.dispatcher
  private implicit val meterializer = ActorMaterializer()
  private implicit val http = Http()

  private implicit val config = LlectroConfig(ConfigFactory.load().getConfig("llectro"))

  val users = new Users()
  private val lists = new Lists()

  def createUserAndDevice(userId: Int, name: String, authId: Long, screenWidth: Int, screenHeight: Int): dbio.DBIOAction[LlectroUser, NoStream, Write with Effect with Transactional] = {
    val llectroUser = models.llectro.LlectroUser(userId, UUID.randomUUID(), name)

    (for {
      _ ← persist.llectro.LlectroUser.create(llectroUser)
      _ ← createDevice(authId, screenWidth, screenHeight)
      createResult ← DBIO.from(users.create(llectroUser))
    } yield {
      if (createResult.isLeft)
        throw new Exception(s"Failed to create user ${createResult}")

      llectroUser
    }).transactionally
  }

  def createDevice(authId: Long, screenWidth: Int, screenHeight: Int): FixedSqlAction[Int, NoStream, Write] = {
    val llectroDevice = models.llectro.LlectroDevice(authId, screenWidth, screenHeight)

    persist.llectro.LlectroDevice.create(llectroDevice)
  }

  def deleteInterest(user: LlectroUser, interests: Vector[Int]): dbio.DBIOAction[Vector[Either[Errors, Unit]], NoStream, Write with Effect with Transactional] = {
    DBIO.sequence(
      for {
        result ← interests.map { interest ⇒
          (for {
            _ ← persist.llectro.UserInterest.delete(user.userId, interest)
            resp ← DBIO.from(users.deleteInterest(user.uuid, interest))
          } yield resp).transactionally
        }
      } yield result
    )
  }

  def addInterests(user: LlectroUser, interests: Vector[Int]): dbio.DBIOAction[Right[Errors, Unit], NoStream, Read with Write with Effect with Transactional] = {
    (for {
      _ ← DBIO.sequence(interests.map(id ⇒ persist.llectro.UserInterest.createIfNotExists(user.userId, id)))
      resp ← DBIO.from(users.addInterests(user.uuid, interests.toList)) flatMap {
        case Left(e)      ⇒ DBIO.failed(new Exception(e.errors))
        case r @ Right(_) ⇒ DBIO.successful(r)
      }
    } yield resp).transactionally
  }

  def getBanners(userUuid: UUID, screenWidth: Int, screenHeight: Int): Future[Banner] = {
    users.getBanners(userUuid, screenWidth, screenHeight) map {
      case Right(banners) ⇒ banners
      case Left(e)        ⇒ throw new Exception(s"Failed to get banners: $e")
    }
  }

  def getUserBalance(userUuid: UUID): Future[UserBalance] = {
    users.getBalance(userUuid) map {
      case Right(userBalance) ⇒ userBalance
      case Left(e)            ⇒ throw new Exception(s"Failed to get balance: $e")
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
      persist.llectro.Interest.createOrUpdate(interests)
    } map (_.sum)
  }
}
