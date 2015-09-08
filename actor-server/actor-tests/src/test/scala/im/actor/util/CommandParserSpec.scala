package im.actor.util

import im.actor.server.api.rpc.service.messaging.CommandParser
import org.scalatest.{ Matchers, FlatSpecLike }

object Parser extends CommandParser

class CommandParserSpec extends FlatSpecLike with Matchers {

  "Command parser" should "split command and text" in e1()

  import Parser._

  def e1() = {
    parseCommand("/task kill") shouldEqual Some("task" → Some("kill"))
    parseCommand("   /task      kill") shouldEqual Some("task" → Some("kill"))
    parseCommand("   /task      kill all humans   ") shouldEqual Some("task" → Some("kill all humans"))
    parseCommand("/task_fatal kill") shouldEqual Some("task_fatal" → Some("kill"))
    parseCommand("/task-fatal kill") shouldEqual Some("task-fatal" → Some("kill"))
    parseCommand("/sleep all day") shouldEqual Some("sleep" → Some("all day"))
    parseCommand("/task") shouldEqual Some("task" → None)

    parseCommand("/task:      kill") shouldEqual None
    parseCommand("this is not a /task") shouldEqual None
    parseCommand("http://example.com") shouldEqual None
    parseCommand("/home/rockjam/projectds") shouldEqual None
    parseCommand("   / task      kill") shouldEqual None
    parseCommand("   task      kill") shouldEqual None
    parseCommand("Some text") shouldEqual None
    parseCommand("#Some other text") shouldEqual None
    parseCommand("\\Some text again") shouldEqual None
  }

}
