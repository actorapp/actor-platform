package im.actor.server.dashboard

import im.actor.server.dashboard.controllers.Users
import im.actor.server.util.{ ACL, IdUtils }
import im.actor.server.{ SqlSpecHelpers, models, persist }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, FlatSpec, Matchers }
import play.api.http.HeaderNames
import play.api.libs.iteratee.Input
import play.api.libs.json.{ JsArray, JsValue, Json }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import slick.driver.PostgresDriver

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.forkjoin.ThreadLocalRandom

class UsersSpec
  extends FlatSpec
  with SqlSpecHelpers
  with ScalaFutures
  with Matchers
  with BeforeAndAfterAll {

  behavior of "Users controller"

  "get user" should "not authorize" in s.notAuthorized()

  "get user" should "not find user" in s.notFound()

  "get user" should "find single user" in s.singleUser()

  "get user" should "find right user by id from multiple users" in s.byUserId()

  "get user" should "find find user with phone" in s.userWithPhone()

  "delete user" should "mark user as deleted at current date" in s.deleteUser()

  "update user" should "change name to one given in request body" in s.updateUserName()

  "create user" should "create new user with given name and phone" in s.createUser()

  val (ds, database: PostgresDriver.backend.DatabaseDef) = migrateAndInitDb()
  val rnd = ThreadLocalRandom.current()

  object s {

    class TestController extends Users {
      override def db = database
    }

    val token = "secret"
    val email = "hs@gmail.com"
    val manager = models.Manager(1, "Homer", "Simpson", "sm.actor.im", token, email)
    val authorizedGET = FakeRequest(GET, s"/users?auth-token=$token&email=$email")
    val authorizedDELETE = FakeRequest(DELETE, s"/users?auth-token=$token&email=$email")
    val authorizedPUT = FakeRequest("GET", s"/users?auth-token=$token&email=$email")
    val authorizedPOST = FakeRequest("POST", s"/users?auth-token=$token&email=$email")

    def notAuthorized() = {
      val result = new TestController().get(22).apply(FakeRequest())
      status(result) shouldEqual 401
    }

    def notFound() = {
      whenReady(database.run(persist.Manager.create(manager))) { _ ⇒
        val result = new TestController().get(22).apply(authorizedGET)
        status(result) shouldEqual 404
        (contentAsJson(result) \ "message").as[String] shouldEqual "No such user found"
      }
    }

    def singleUser() = {
      val user = genUser()
      whenReady(database.run(persist.User.create(user))) { _ ⇒
        val result = new TestController().get(user.id).apply(authorizedGET)
        status(result) shouldEqual 200
        val userResult: JsValue = contentAsJson(result)
        (userResult \ "id").as[Int] shouldEqual user.id
        (userResult \ "name").as[String] shouldEqual user.name
        (userResult \ "sex").as[Int] shouldEqual 1
        (userResult \ "phones").as[JsArray].value shouldBe empty
      }
    }

    def userWithPhone() = {
      val user = genUser()
      val phone = genPhone(user.id, 75552223311L)
      whenReady(database.run {
        for {
          _ ← persist.User.create(user)
          _ ← persist.UserPhone.create(phone)
        } yield ()
      }) { _ ⇒
        val result = new TestController().get(user.id).apply(authorizedGET)
        status(result) shouldEqual 200
        val userResult: JsValue = contentAsJson(result)
        (userResult \ "id").as[Int] shouldEqual user.id
        (userResult \ "name").as[String] shouldEqual user.name
        (userResult \ "sex").as[Int] shouldEqual 1
        (userResult \ "phones").as[JsArray].value should have length 1
      }
    }

    def byUserId() = {
      whenReady(database.run {
        for {
          _ ← persist.User.create(genUser())
          user = genUser()
          _ ← persist.User.create(user)
        } yield user
      }) { user ⇒
        val result = new TestController().get(user.id).apply(authorizedGET)
        status(result) shouldEqual 200
        val userResult: JsValue = contentAsJson(result)
        (userResult \ "id").as[Int] shouldEqual user.id
        (userResult \ "name").as[String] shouldEqual user.name
        (userResult \ "sex").as[Int] shouldEqual 1
      }
    }

    def deleteUser() = {
      val user = genUser()
      whenReady(database.run(persist.User.create(user))) { _ ⇒
        val delete = new TestController().delete(user.id).apply(authorizedDELETE)
        status(delete) shouldEqual 202
        whenReady(database.run(persist.User.find(user.id).headOption)) { optUser ⇒
          optUser.map {
            _.deletedAt shouldBe defined
          } getOrElse fail
        }
      }
    }

    def updateUserName() = {
      val user = genUser()
      whenReady(database.run(persist.User.create(user))) { _ ⇒

        //ugly workaroung for this bug https://revoltingcode.wordpress.com/2013/10/27/play-framework-2-controller-testing-with-json-body-parser/
        val update = authorizedPUT.
          withHeaders(
            HeaderNames.CONTENT_TYPE → "application/json",
            HeaderNames.ACCEPT_LANGUAGE → "RU"
          )
        val result = new TestController().update(user.id).apply(update).
          feed(Input.El(Json.toJson(Map("name" → "George Bush")).toString().getBytes)).
          flatMap(_.run)

        status(result) shouldEqual 202
        whenReady(database.run(persist.User.find(user.id).headOption)) { optUser ⇒
          optUser.map {
            _.name shouldEqual "George Bush"
          } getOrElse fail
        }
      }
    }

    def createUser() = {
      val user = genUser()
      val phone = genPhone(user.id, 75552223312L)

      //ugly workaroung for this bug https://revoltingcode.wordpress.com/2013/10/27/play-framework-2-controller-testing-with-json-body-parser/
      val request = authorizedPOST.
        withHeaders(
          HeaderNames.CONTENT_TYPE → "application/json",
          HeaderNames.ACCEPT_LANGUAGE → "RU"
        )
      val body = Json.toJson(Map("name" → user.name, "phone" → phone.number.toString)).toString()
      val result = new TestController().create().apply(request).feed(Input.El(body.getBytes)).flatMap(_.run)
      status(result) shouldEqual 201

      val userId = (contentAsJson(result) \ "id").as[Int]

      whenReady(database.run {
        for {
          user ← persist.User.find(userId).headOption
          phone ← persist.UserPhone.findByUserId(userId)
        } yield (user, phone)
      }) { up ⇒
        val (uptUser, phones) = up
        uptUser shouldBe defined
        uptUser.map { u ⇒
          u.name shouldEqual user.name
          u.sex shouldEqual user.sex
          u.state shouldEqual user.state
          u.countryCode shouldEqual "RU" //from Accept language header
          u.deletedAt shouldEqual user.deletedAt

        } getOrElse fail
        phones should have length 1
        phones.map {
          _.number shouldEqual phone.number
        }
      }
    }
  }

  def genUser(): models.User = models.User(IdUtils.nextIntId(rnd), ACL.nextAccessSalt(rnd), "Henry Ford", "US", models.NoSex, models.UserState.Registered)

  def genPhone(userId: Int, phone: Long) = models.UserPhone(IdUtils.nextIntId(rnd), userId, ACL.nextAccessSalt(rnd), phone, "Mobile phone")

  override def afterAll(): Unit = {
    super.afterAll()
    database.ioExecutionContext
    database.close()
    ds.close()
  }

}
