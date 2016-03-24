package im.actor.server.user

import im.actor.server.bots.BotCommand
import im.actor.server.{ BaseAppSuite, ImplicitAuthService, ImplicitSessionRegion }

class BotCommandsSpec extends BaseAppSuite with ImplicitAuthService with ImplicitSessionRegion {

  behavior of "User's bot commands"

  it should "not add wrong commands" in dontAddWrong()

  it should "add and remove" in addAndRemove()

  val userExt = UserExtension(system)

  def dontAddWrong() = {
    val (botUser, _, _, _) = createUser()

    whenReady(addCommand(botUser.id, BotCommand("/wrong", "", None)).failed) {
      case UserErrors.InvalidBotCommand("/wrong") ⇒
    }
    whenReady(addCommand(botUser.id, BotCommand("wrong command", "", None)).failed) {
      case UserErrors.InvalidBotCommand("wrong command") ⇒
    }
    whenReady(addCommand(botUser.id, BotCommand("", "", None)).failed) {
      case UserErrors.InvalidBotCommand("") ⇒
    }
    whenReady(addCommand(botUser.id, BotCommand("   ", "", None)).failed) {
      case UserErrors.InvalidBotCommand("") ⇒
    }

    whenReady(userExt.getApiStruct(botUser.id, 0, 0L)) { resp ⇒
      resp.botCommands shouldBe empty
    }
  }

  def addAndRemove() = {
    val (botUser, _, _, _) = createUser()

    val fireCommand = BotCommand("fire", "Fire at your enemy with something dangerous", None)
    val runCommand = BotCommand("run", "Run away as fast as you can", Some("run"))

    whenReady(addCommand(botUser.id, fireCommand))(identity)

    whenReady(addCommand(botUser.id, BotCommand("fire", "Fire agian", None)).failed) {
      case UserErrors.BotCommandAlreadyExists("fire") ⇒
    }

    whenReady(addCommand(botUser.id, runCommand))(identity)

    whenReady(userExt.getApiStruct(botUser.id, 0, 0L)) { resp ⇒
      resp.botCommands should have size 2
      val fire = resp.botCommands(0)
      fire.slashCommand shouldEqual fireCommand.slashCommand
      fire.description shouldEqual fireCommand.description
      fire.locKey shouldEqual None

      val run = resp.botCommands(1)
      run.slashCommand shouldEqual runCommand.slashCommand
      run.description shouldEqual runCommand.description
      run.locKey shouldEqual runCommand.locKey
    }

    // respond with success when trying to remove same command multiple times
    whenReady(userExt.removeBotCommand(botUser.id, fireCommand.slashCommand))(identity)
    whenReady(userExt.removeBotCommand(botUser.id, fireCommand.slashCommand))(identity)

    whenReady(userExt.getApiStruct(botUser.id, 0, 0L)) { resp ⇒
      resp.botCommands should have size 1
      val run = resp.botCommands.head
      run.slashCommand shouldEqual runCommand.slashCommand
    }
  }

  private def addCommand(userId: Int, commmand: BotCommand) =
    userExt.addBotCommand(userId, commmand)

}
