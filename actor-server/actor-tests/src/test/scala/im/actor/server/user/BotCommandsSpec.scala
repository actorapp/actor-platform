package im.actor.server.user

import im.actor.api.rpc.messaging.ApiTextMessage
import im.actor.api.rpc.users.UpdateUserBotCommandsChanged
import im.actor.api.rpc.{ AuthData, ClientData }
import im.actor.server.bots.BotCommand
import im.actor.server._
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.sequence.{ SeqState, SeqUpdatesExtension }

class BotCommandsSpec extends BaseAppSuite
  with ImplicitAuthService
  with ImplicitSessionRegion
  with MessagingSpecHelpers
  with SeqUpdateMatchers {

  behavior of "User's bot commands"

  it should "not add wrong commands" in dontAddWrong()

  it should "add and remove" in addAndRemove()

  it should "send updates to related users, when commands change" in changeCommands()

  val userExt = UserExtension(system)
  implicit val msgService = new MessagingServiceImpl()

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
    whenReady(removeCommand(botUser.id, fireCommand.slashCommand))(identity)
    whenReady(removeCommand(botUser.id, fireCommand.slashCommand))(identity)

    whenReady(userExt.getApiStruct(botUser.id, 0, 0L)) { resp ⇒
      resp.botCommands should have size 1
      val run = resp.botCommands.head
      run.slashCommand shouldEqual runCommand.slashCommand
    }
  }

  def changeCommands() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val sessionId = createSessionId()
    val clientData = ClientData(aliceAuthId, sessionId, Some(AuthData(alice.id, aliceAuthSid, 42)))

    val (botUser, _, _, _) = createUser()

    {
      // send message to note relation
      implicit val cd = clientData
      sendPrivateMessage(botUser.id, ApiTextMessage("text", Vector.empty, None))
    }

    val runCommand = BotCommand("run", "Run away as fast as you can", Some("run"))

    whenReady(addCommand(botUser.id, runCommand))(identity)

    {
      implicit val cd = clientData
      expectUpdate(classOf[UpdateUserBotCommandsChanged]) { upd ⇒
        upd.userId shouldEqual botUser.id
        upd.commands should have size 1
        val run = upd.commands.head
        run.slashCommand shouldEqual runCommand.slashCommand
        run.description shouldEqual runCommand.description
        run.locKey shouldEqual runCommand.locKey
      }
    }

    val SeqState(seq, _) = whenReady(SeqUpdatesExtension(system).getSeqState(alice.id))(identity)

    whenReady(removeCommand(botUser.id, runCommand.slashCommand))(identity)

    {
      implicit val cd = clientData
      expectUpdate(seq, classOf[UpdateUserBotCommandsChanged]) { upd ⇒
        upd.userId shouldEqual botUser.id
        upd.commands shouldBe empty
      }
    }
  }

  private def addCommand(userId: Int, commmand: BotCommand) =
    userExt.addBotCommand(userId, commmand)

  private def removeCommand(userId: Int, slashCommand: String) =
    userExt.removeBotCommand(userId, slashCommand)

}
