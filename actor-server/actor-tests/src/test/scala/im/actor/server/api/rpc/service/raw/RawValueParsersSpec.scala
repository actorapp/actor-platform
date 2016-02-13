package im.actor.server.api.rpc.service.raw

import im.actor.api.rpc.collections._
import im.actor.api.rpc.raw.RawValueParser
import im.actor.server.BaseAppSuite
import im.actor.server.api.rpc.RawApiExtension

class RawValueParsersSpec extends BaseAppSuite {

  "Raw value parsers" should "parse flat ApiArrayValue's to case class" in simpleArray()

  it should "parse Array with sequence of primitives of same type to case class with sequence of primitives" in arrayWithSeqOfPrimitives()

  it should "parse empty sequence as empty sequence" in emptySequence()

  it should "parse Array with field and sequence of primitives of same type to case class with field and sequence of primitives" in mixedArrayWithSeqOfPrimitives()

  it should "parse Array containing Map with primitive values of same type" in arrayWithMap()

  //  it should "parse nested ApiArrayValue to case classes" in nested()

  import RawValueParser._

  case class Message(date: Long, userId: Int, text: String)

  case class User(id: Int, name: String, nickname: String, age: Int)

  //  case class UsersMessage(user: User, message: Message)

  case class UsersIds(ids: Seq[Int])

  case class ServerNameUsersIds(serverName: String, ids: Seq[Int])

  case class NumberMappings(map: Map[String, Int])

  def simpleArray() = {
    val date = currentTime()
    val text = "Hello world, how is going?"
    val mArr = messageArray(date, 1, text)

    RawValueParser.parse[Message](mArr) shouldEqual Some(Message(date, 1, text))
    RawValueParser.parse[User](mArr) shouldEqual None

    val uArr = userArray(2, "Nick", "rockjam", 23)

    RawValueParser.parse[User](uArr) shouldEqual Some(User(2, "Nick", "rockjam", 23))
    RawValueParser.parse[Message](uArr) shouldEqual None
  }

  def arrayWithSeqOfPrimitives() = {
    val ids = (1 to 10).toVector
    val arrIds = ApiArrayValue(ids map { i ⇒ ApiInt32Value(i) })

    RawValueParser.parse[UsersIds](ApiArrayValue(Vector(arrIds))) shouldEqual Some(UsersIds(ids))

    case class MessagesIds(longs: Seq[Long])
    RawValueParser.parse[MessagesIds](ApiArrayValue(Vector(arrIds))) shouldEqual None
  }

  def emptySequence() = {
    val emptyArr = ApiArrayValue(Vector.empty[ApiRawValue])
    RawValueParser.parse[UsersIds](ApiArrayValue(Vector(emptyArr))) shouldEqual Some(UsersIds(Vector.empty))
  }

  def mixedArrayWithSeqOfPrimitives() = {
    val ids = (1 to 10).toVector
    val arrIds = ApiArrayValue(ids map { i ⇒ ApiInt32Value(i) })

    val struct = ApiArrayValue(Vector(ApiStringValue("Actor server"), arrIds))

    RawValueParser.parse[ServerNameUsersIds](struct) shouldEqual Some(ServerNameUsersIds("Actor server", ids))
  }

  def arrayWithMap() = {
    val apiMap = ApiMapValue(Vector(
      ApiMapValueItem("one", ApiInt32Value(1)),
      ApiMapValueItem("two", ApiInt32Value(2)),
      ApiMapValueItem("three", ApiInt32Value(3)),
      ApiMapValueItem("four", ApiInt32Value(4))
    ))

    val parsed = RawValueParser.parse[NumberMappings](ApiArrayValue(Vector(apiMap)))
    inside(parsed) {
      case Some(NumberMappings(map)) ⇒
        map.keys should contain allOf ("one", "two", "three", "four")
        map.values should contain allOf (1, 2, 3, 4)
    }
  }

  //  def nested() = {
  //    val date = currentTime()
  //    val text = "Hello world, how is going?"
  //    val mArr = ApiArrayValue(Vector(
  //      ApiInt64Value(date),
  //      ApiInt32Value(1),
  //      ApiStringValue(text)
  //    ))
  //
  //    val uArr = ApiArrayValue(Vector(
  //      ApiInt32Value(1),
  //      ApiStringValue("Nick"),
  //      ApiStringValue("rockjam"),
  //      ApiInt32Value(23)
  //    ))
  //
  //    val umArr = ApiArrayValue(Vector(uArr, mArr))
  //
  //    RawValueParser.parse[UsersMessage](umArr) shouldEqual Some(
  //      UsersMessage(
  //        User(1, "Nick", "rockjam", 23),
  //        Message(date, 1, text)
  //      )
  //    )
  //  }

  private def messageArray(date: Long, id: Int, text: String) =
    ApiArrayValue(Vector(
      ApiInt64Value(date),
      ApiInt32Value(id),
      ApiStringValue(text)
    ))

  private def userArray(id: Int, name: String, nickname: String, age: Int) =
    ApiArrayValue(Vector(
      ApiInt32Value(id),
      ApiStringValue(name),
      ApiStringValue(nickname),
      ApiInt32Value(age)
    ))

  private def currentTime(): Long = System.currentTimeMillis

}
