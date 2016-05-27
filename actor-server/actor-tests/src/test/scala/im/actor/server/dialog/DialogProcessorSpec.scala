package im.actor.server.dialog

import cats.data.Xor
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeer, ApiPeerType }
import im.actor.api.rpc.{ AuthData, ClientData, Ok, PeersImplicits }
import im.actor.server._
import im.actor.server.acl.ACLUtils
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.model.SeqUpdate
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.persist.sequence.UserSequenceRepo
import im.actor.server.sequence.{ SeqStateDate, SeqUpdatesExtension }

import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.Random

final class DialogProcessorSpec extends BaseAppSuite
  with ImplicitAuthService
  with ImplicitSessionRegion
  with PeersImplicits
  with MessageParsing
  with GroupsServiceHelpers {

  behavior of "Dialog Processor"

  it should "pass reads after read with later date came from another user" in passReads()

  it should "not allow time out when there are highly frequent messages" in noTimeout()

  it should "keep UpdateReadByMe in right order in difference" in readsProduceGoodSequence()

  it should "not allow duplicated timestamp in messages" in uniqueTimestamp()

  private implicit val messService = MessagingServiceImpl()
  private implicit val groupsService = new GroupsServiceImpl(GroupInviteConfig("https://actor.im"))
  private val seqExt = SeqUpdatesExtension(system)

  def passReads() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val alicePeer = ApiPeer(ApiPeerType.Private, alice.id)
    val bobPeer = ApiPeer(ApiPeerType.Private, bob.id)

    def sendMessageToBob(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(bobPeer, alice.id, aliceAuthSid, Some(aliceAuthId), ACLUtils.randomLong(), textMessage(text))

    def sendMessageToAlice(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(alicePeer, bob.id, bobAuthSid, Some(bobAuthId), ACLUtils.randomLong(), textMessage(text))

    val dateToAlice = whenReady(sendMessageToAlice("Hi"))(_.date)
    val dateToBob = whenReady(sendMessageToBob("Hi"))(_.date)

    whenReady(dialogExt.messageRead(alicePeer, bob.id, 0, dateToBob))(identity)
    whenReady(dialogExt.getDialogInfo(alice.id, bobPeer.asModel)) { info ⇒
      info.counter should be(1)
    }

    Thread.sleep(1)

    whenReady(dialogExt.messageRead(bobPeer, alice.id, 0, dateToAlice))(identity)
    whenReady(dialogExt.getDialogInfo(alice.id, bobPeer.asModel)) { info ⇒
      info.counter should be(0)
    }
  }

  def noTimeout() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val alicePeer = ApiPeer(ApiPeerType.Private, alice.id)
    val bobPeer = ApiPeer(ApiPeerType.Private, bob.id)

    def sendMessageToBob(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(bobPeer, alice.id, aliceAuthSid, Some(aliceAuthId), ACLUtils.randomLong(), textMessage(text))

    def sendMessageToAlice(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(alicePeer, bob.id, bobAuthSid, Some(bobAuthId), ACLUtils.randomLong(), textMessage(text))

    val toAlice = for (i ← 1 to 50) yield sendMessageToAlice(s"Hello $i")
    val toBob = for (i ← 1 to 50) yield sendMessageToBob(s"Hello you back $i")

    toAlice foreach {
      whenReady(_)(identity)
    }
    toBob foreach {
      whenReady(_)(identity)
    }

    {
      implicit val clientData = ClientData(bobAuthId, 2, Some(AuthData(bob.id, bobAuthSid, 42)))
      val aliceOutPeer = whenReady(ACLUtils.getOutPeer(alicePeer, bobAuthId))(identity)
      whenReady(messService.handleLoadHistory(aliceOutPeer, 0L, None, Int.MaxValue, Vector.empty)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadHistory(messages, _, _, _, _)) ⇒
            val (aliceMessages, bobsMessages) = messages map { mess ⇒
              val parsed = parseMessage(mess.message.toByteArray)
              parsed.isRight shouldEqual true
              val message = parsed.right.toOption.get
              message match {
                case ApiTextMessage(text, _, _) ⇒ text
                case _                          ⇒ fail()
              }
            } partition (mess ⇒ mess startsWith "Hello you back")
            aliceMessages should have length 50
            bobsMessages should have length 50
        }
      }
    }

  }

  def readsProduceGoodSequence() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()
    val (eve, eveAuthId, eveAuthSid, _) = createUser()

    val aliceCd = ClientData(aliceAuthId, 3L, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobCd = ClientData(bobAuthId, 3L, Some(AuthData(bob.id, bobAuthSid, 42)))

    // Alice creates group
    val groupOutPeer = {
      implicit val cd = aliceCd
      val g = createGroup("Group to test differnece", Set(bob.id, eve.id)).groupPeer
      ApiOutPeer(ApiPeerType.Group, g.groupId, g.accessHash)
    }
    val groupPeer = ApiPeer(ApiPeerType.Group, groupOutPeer.id)

    // Alice sends 100 messages
    val aliceRids = (1 to 100) map { i ⇒
      implicit val cd = aliceCd
      val rid = ACLUtils.randomLong()
      dialogExt.sendMessage(groupPeer, alice.id, aliceAuthSid, Some(aliceAuthId), rid, textMessage(s"Hello from Alice #$i")) map (_ ⇒ rid)
    } map (e ⇒ whenReady(e)(identity))

    val dates = whenReady(db.run(HistoryMessageRepo.find(alice.id, groupPeer.asModel, aliceRids.toSet))) { messages ⇒
      (messages map (_.date.getMillis)).sorted
    }

    // Bob reads 100 messages in random order, repeats requests, etc

    val (reads, expectedSize) = {
      implicit val cd = bobCd

      // 20 messages - read in original order
      val dates1 = dates.slice(0, 25)
      val bobReads1 = dates1 map { date ⇒
        //          dialogExt.messageRead(groupOutPeer.asPeer, bob.id, bobAuthSid, date)
        messService.handleMessageRead(groupOutPeer, date)
      }
      // these reads should produce 20 UpdateReadByMe
      val expectedUpdatesCount1 = dates1.length

      // 20 messages - read in random order
      val dates2 = Random.shuffle(dates.slice(25, 50))
      val bobReads2 = dates2 map { date ⇒
        //          dialogExt.messageRead(groupOutPeer.asPeer, bob.id, bobAuthSid, date)
        messService.handleMessageRead(groupOutPeer, date)
      }
      // these reads should produce UpdateReadByMe only with ascending dates
      val expectedUpdatesCount2 = (dates2.reverse foldRight List.empty[Long]) {
        case (el, Nil)                    ⇒ List(el)
        case (el, acc @ h :: t) if h < el ⇒ el :: acc
        case (_, acc)                     ⇒ acc
      }.length

      // 20 messages - read in reverse order
      val bobReads3 = dates.slice(50, 75).reverse map { date ⇒
        //          dialogExt.messageRead(groupOutPeer.asPeer, bob.id, bobAuthSid, date)
        messService.handleMessageRead(groupOutPeer, date)
      }
      // these reads should produce only one UpdateReadByMe, cause dates are descending
      val expectedUpdatesCount3 = 1

      // 20 messages - read in original order, repeat each read multiple times
      val dates4 = dates.slice(75, 100)
      val bobReads4 = dates4 flatMap { date ⇒
        val upperBound = Random.nextInt(10) + 1
        1 to upperBound map { _ ⇒
          messService.handleMessageRead(groupOutPeer, date)
          //            dialogExt.messageRead(groupOutPeer.asPeer, bob.id, bobAuthSid, date)
        }
      }
      // these reads should not repeat UpdateReadByMe with same startDate, thus - 20 updates
      val expectedUpdatesCount4 = dates4.length

      (
        bobReads1 ++ bobReads2 ++ bobReads3 ++ bobReads4,
        expectedUpdatesCount1 + expectedUpdatesCount2 + expectedUpdatesCount3 + expectedUpdatesCount4
      )
    }

    reads foreach { whenReady(_)(identity) }

    // this is not how we get exact difference, this is how we store it in database
    whenReady(db.run(UserSequenceRepo.fetchAfterSeq(bob.id, 0, Long.MaxValue))) { updates ⇒
      val readsByMe = updates flatMap { seq ⇒
        val upd = seq.getMapping.getDefault
        if (upd.header == UpdateMessageReadByMe.header) {
          Xor.fromEither(UpdateMessageReadByMe.parseFrom(upd.body)).toOption map (upd ⇒ (seq.seq, seq.timestamp, upd))
        } else None
      }

      readsByMe should have length expectedSize.toLong

      readsByMe.zip(readsByMe.tail) foreach {
        case ((fSeq, fTs, fUpd), (sSeq, sTs, sUpd)) ⇒
          assert(fUpd.startDate < sUpd.startDate, "Update start dates are not ascending")
          assert(fSeq < sSeq, "Seqs are not ascending")
          assert(fTs <= sTs, "Seq update timestamps are not ascending")
          assert(fUpd.unreadCounter.get > sUpd.unreadCounter.get, "Counters are not descending")
      }
      readsByMe.head._3.unreadCounter.get shouldEqual 99
      readsByMe.last._3.unreadCounter.get shouldEqual 0
    }

    // UpdateMessageReadByMe should be reduces in final difference
    whenReady(seqExt.getDifference(bob.id, 0, bobAuthSid, Long.MaxValue)) {
      case (diff, _) ⇒
        val readsByMe = diff flatMap { seq ⇒
          val upd = seq.getMapping.getDefault
          if (upd.header == UpdateMessageReadByMe.header) {
            Xor.fromEither(UpdateMessageReadByMe.parseFrom(upd.body)).toOption
          } else None
        }
        readsByMe should have length 1L
        readsByMe.head shouldEqual UpdateMessageReadByMe(groupPeer, dates.last, Some(0))
    }
  }

  def uniqueTimestamp() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val alicePeer = ApiPeer(ApiPeerType.Private, alice.id)
    val bobPeer = ApiPeer(ApiPeerType.Private, bob.id)

    def sendMessageToBob(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(bobPeer, alice.id, aliceAuthSid, Some(aliceAuthId), ACLUtils.randomLong(), textMessage(text))

    def sendMessageToAlice(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(alicePeer, bob.id, bobAuthSid, Some(bobAuthId), ACLUtils.randomLong(), textMessage(text))

    val toAlice = for (i ← 1 to 50) yield sendMessageToAlice(s"Hello $i")
    val toBob = for (i ← 1 to 50) yield sendMessageToBob(s"Hello you back $i")

    toAlice foreach {
      whenReady(_)(identity)
    }
    toBob foreach {
      whenReady(_)(identity)
    }

    {
      implicit val clientData = ClientData(bobAuthId, 2, Some(AuthData(bob.id, bobAuthSid, 42)))
      val aliceOutPeer = whenReady(ACLUtils.getOutPeer(alicePeer, bobAuthId))(identity)
      whenReady(messService.handleLoadHistory(aliceOutPeer, 0L, None, Int.MaxValue, Vector.empty)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadHistory(messages, _, _, _, _)) ⇒
            val (aliceMessages, bobsMessages) = messages partition (m ⇒ m.senderUserId == alice.id)
            (aliceMessages map (_.date) distinct) should have length 50
            (bobsMessages map (_.date) distinct) should have length 50
          //            (messages map (_.date) distinct) should have length 100 // todo: this one is future goal. Ensure uniqueness across dialog participants
        }
      }
    }
  }

}