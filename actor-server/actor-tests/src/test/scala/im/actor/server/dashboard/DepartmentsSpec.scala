//package im.actor.server.dashboard
//
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.forkjoin.ThreadLocalRandom
//
//import com.github.tminglei.slickpg.LTree
//import play.api.http.HeaderNames
//import play.api.libs.iteratee.Input
//import play.api.libs.json.{ JsArray, Json }
//import play.api.test.FakeRequest
//import play.api.test.Helpers._
//
//import im.actor.server.dashboard.controllers.Departments
//import im.actor.server.util.IdUtils
//import im.actor.server.{ models, persist }
//
//class DepartmentsSpec extends BasicDashboardSpec {
//
//  behavior of "Departments controller"
//
//  "request" should "not authorize" in s.notAuthorized()
//
//  "create dept" should "create new dept with given name and struct" in s.createDept()
//
//  "create dept" should "not create dept when request with invalid json body comes" in s.createDeptInvaidJson()
//
//  "update dept" should "change name to given one" in s.updateDept()
//
//  "update dept" should "not update dept when request with invalid json body comes" in s.updateDeptInvalidJson()
//
//  "delete dept" should "mark dept as deleted at current date" in s.deleteDept()
//
//  "get dept" should "return given dept with all subdepts" in s.getDept()
//
//  case class Dept(name: String, struct: String)
//
//  object Depts {
//    val root = Dept("root", "1")
//    val main = Dept("Main dept", "1.1")
//    val subDept = Dept("First sub dept", "1.1.1")
//  }
//
//  object s {
//
//    val token = "secret"
//
//    def authorized(method: String) = FakeRequest(method, s"/users?auth-token=$token")
//
//    val authorizedGET = authorized(GET)
//    val authorizedDELETE = authorized(DELETE)
//    val authorizedPUT = authorized(PUT)
//    val authorizedPOST = authorized(POST)
//
//    val manager = models.Manager(1, "Homer", "Simpson", "sm.actor.im", token, "hs@gmail.com")
//
//    class TestController extends Departments {
//      override val db = database
//    }
//
//    val deptsController = new TestController()
//
//    def notAuthorized() = {
//      val result = deptsController.get("1.1")(FakeRequest())
//      status(result) shouldEqual 401
//    }
//
//    def createDept() = {
//      whenReady(database.run(persist.Manager.create(manager))) { _ ⇒
//        val dept = makeDept(Depts.root)
//        //ugly workaround for this bug https://revoltingcode.wordpress.com/2013/10/27/play-framework-2-controller-testing-with-json-body-parser/
//        val request = authorizedPOST.withHeaders(HeaderNames.CONTENT_TYPE → "application/json")
//        val body = Json.toJson(Map("title" → dept.name, "struct" → dept.struct.toString)).toString()
//        val result = deptsController.create()(request).feed(Input.El(body.getBytes)).flatMap(_.run)
//
//        status(result) shouldEqual 201
//
//        val deptId = (contentAsJson(result) \ "id").as[Int]
//        whenReady(database.run(persist.Department.find(Depts.root.struct).headOption)) { optDept ⇒
//          optDept shouldBe defined
//          optDept.map { fromDb ⇒
//            fromDb.name shouldEqual dept.name
//            fromDb.struct shouldEqual dept.struct
//            fromDb.deletedAt should not be defined
//          } getOrElse fail
//        }
//      }
//    }
//
//    def createDeptInvaidJson() = {
//      //ugly workaround for this bug https://revoltingcode.wordpress.com/2013/10/27/play-framework-2-controller-testing-with-json-body-parser/
//      val request = authorizedPOST.withHeaders(HeaderNames.CONTENT_TYPE → "application/json")
//      val body = Json.toJson(Map("titleZZZ" → "Sales dept 2", "struct" → "2.2.1")).toString()
//      val result = deptsController.create()(request).feed(Input.El(body.getBytes)).flatMap(_.run)
//
//      status(result) shouldEqual 406
//    }
//
//    def updateDept() = {
//      whenReady(database.run(persist.Department.create(makeDept(Depts.main)))) { _ ⇒
//        val newTitle = "Uber Main dept"
//        //ugly workaround for this bug https://revoltingcode.wordpress.com/2013/10/27/play-framework-2-controller-testing-with-json-body-parser/
//        val request = authorizedPUT.withHeaders(HeaderNames.CONTENT_TYPE → "application/json")
//        val body = Json.toJson(Map("title" → newTitle)).toString()
//        val result = deptsController.update(Depts.main.struct)(request).feed(Input.El(body.getBytes)).flatMap(_.run)
//
//        status(result) shouldEqual 202
//
//        whenReady(database.run(persist.Department.find(Depts.main.struct).headOption)) { optDept ⇒
//          optDept shouldBe defined
//          optDept.map {
//            _.name shouldEqual newTitle
//          } getOrElse fail
//        }
//        val r = authorizedPUT.withHeaders(HeaderNames.CONTENT_TYPE → "application/json")
//        val b = Json.toJson(Map("title" → Depts.main.name)).toString()
//        deptsController.update(Depts.main.struct)(r).feed(Input.El(b.getBytes)).flatMap(_.run)
//      }
//    }
//
//    def updateDeptInvalidJson() = {
//      //ugly workaround for this bug https://revoltingcode.wordpress.com/2013/10/27/play-framework-2-controller-testing-with-json-body-parser/
//      val request = authorizedPUT.withHeaders(HeaderNames.CONTENT_TYPE → "application/json")
//      val body = Json.toJson(Map("titleZZZ" → "Some other dept")).toString()
//      val result = deptsController.update(Depts.main.struct)(request).feed(Input.El(body.getBytes)).flatMap(_.run)
//
//      status(result) shouldEqual 406
//    }
//
//    def deleteDept() = {
//      whenReady(database.run(persist.Department.create(makeDept(Depts.subDept)))) { _ ⇒
//        val delete = deptsController.delete(Depts.subDept.struct)(authorizedDELETE)
//        status(delete) shouldEqual 202
//        whenReady(database.run(persist.Department.find(Depts.subDept.struct).headOption)) { optDept ⇒
//          optDept.map {
//            _.deletedAt shouldBe defined
//          } getOrElse fail
//        }
//      }
//    }
//
//    def getDept() = {
//      val get = deptsController.get(Depts.root.struct)(authorizedGET)
//      status(get) shouldEqual 200
//
//      val depts = contentAsJson(get).as[JsArray]
//      depts.value should have length 1
//
//      val root = depts(0)
//      (root \ "title").as[String] shouldEqual Depts.root.name
//      (root \ "id").as[Int] shouldEqual LTree(Depts.root.struct).value.mkString.toInt
//      (root \ "internal-id").as[String] shouldEqual LTree(Depts.root.struct).toString
//      val rootItems = (root \ "items").as[JsArray]
//      rootItems.value should have length 1
//
//      val main = rootItems(0)
//      (main \ "title").as[String] shouldEqual Depts.main.name
//      (main \ "id").as[Int] shouldEqual LTree(Depts.main.struct).value.mkString.toInt
//      (main \ "internal-id").as[String] shouldEqual LTree(Depts.main.struct).toString
//      val mainItems = (main \ "items").as[JsArray]
//      mainItems.value should have length 1
//
//      val subDept = mainItems(0)
//      (subDept \ "title").as[String] shouldEqual Depts.subDept.name
//      (subDept \ "id").as[Int] shouldEqual LTree(Depts.subDept.struct).value.mkString.toInt
//      (subDept \ "internal-id").as[String] shouldEqual LTree(Depts.subDept.struct).toString
//      val subDeptItems = (subDept \ "items").as[JsArray]
//      subDeptItems.value shouldBe empty
//    }
//
//    def makeDept(dept: Dept) = {
//      val rnd = ThreadLocalRandom.current()
//      models.Department(IdUtils.nextIntId(rnd), dept.name, LTree(dept.struct))
//    }
//
//  }
//
//}
