package im.actor.server.notify

import java.time.{ Instant, LocalDateTime, ZoneOffset }

import akka.http.scaladsl.util.FastFuture
import akka.persistence.SnapshotMetadata
import im.actor.server.cqrs.{ Event, Processor, ProcessorState, TaggedEvent }
import im.actor.server.db.DbExtension
import im.actor.server.dialog.{ DialogExtension, DialogGroup }
import im.actor.server.email.{ Content, EmailExtension, Message }
import im.actor.server.persist.UserRepo
import im.actor.server.presences.Presences.{ Offline, Online }
import im.actor.server.presences.{ PresenceExtension, PresenceState }

import scala.collection.SortedSet
import scala.collection.immutable.TreeSet
import scala.concurrent.Future
import scala.concurrent.duration._
import NotifyProcessorState.UserIdScheduledTime
import NotifyProcessorEvents._
import NotifyProcessorCommands._
import akka.actor.{ ActorRef, ActorSystem, PoisonPill, Props }
import akka.cluster.singleton.{ ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings }
import im.actor.serialization.ActorSerializer
import im.actor.server.user.UserExtension

trait NotifyProcessorEvent extends TaggedEvent {
  val ts: Instant

  override def tags: Set[String] = Set("notifyProcessor")
}

trait NotifyProcessorCommand

private[notify] object NotifyProcessorState {
  val ZeroDate = LocalDateTime.of(1970, 1, 1, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli
  type UserIdScheduledTime = (Int, Long)
  private val tsDesc = Ordering.fromLessThan[UserIdScheduledTime] { case ((_, ts1), (_, ts2)) ⇒ ts1 < ts2 }
  val empty: NotifyProcessorState = NotifyProcessorState(ZeroDate, Set.empty, TreeSet.empty[UserIdScheduledTime](tsDesc))
}

private[notify] case class NotifyProcessorState(
  lastUserRegisterDate: Long,
  userIds:              Set[Int],
  scheduled:            SortedSet[UserIdScheduledTime]
) extends ProcessorState[NotifyProcessorState] {
  def updated(e: Event): NotifyProcessorState = e match {
    case NewUsersAdded(_, uids, lastDate) ⇒ this.copy(userIds = this.userIds ++ uids, lastUserRegisterDate = lastDate)
    case NotifyScheduled(_, uid, at)      ⇒ this.copy(scheduled = scheduled + (uid → at))
    case NotifyCanceled(_, uid)           ⇒ this.copy(scheduled = scheduled.filterNot(_._1 == uid))
  }

  def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): NotifyProcessorState = this
}

private[notify] object NotifyProcessor {

  def register() = {
    ActorSerializer.register(
      55000 → classOf[NewUsersAdded],
      55001 → classOf[NotifyScheduled],
      55002 → classOf[NotifyCanceled]
    )
  }

  private val singletonManagerName = "notifyProcessor"

  def startSingleton()(implicit system: ActorSystem): ActorRef = {
    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = props,
        terminationMessage = PoisonPill,
        settings = ClusterSingletonManagerSettings(system)
      ),
      name = singletonManagerName
    )

    system.actorOf(
      ClusterSingletonProxy.props(
        singletonManagerPath = s"/user/$singletonManagerName",
        settings = ClusterSingletonProxySettings(system)
      ),
      name = s"${singletonManagerName}Proxy"
    )
  }

  private def props = Props(classOf[NotifyProcessor]).withDispatcher("notify-prio-dispatcher")

}

private[notify] class NotifyProcessor extends Processor[NotifyProcessorState] {

  private implicit val system = context.system
  import system.dispatcher

  private val db = DbExtension(system).db
  private val dialogExt = DialogExtension(system)
  private val emailExt = EmailExtension(system)
  private val presenceExt = PresenceExtension(system)
  private val userExt = UserExtension(system)

  private val notifyConfig = NotifyConfig.load.get
  private val notifyAfter = notifyConfig.notifyAfter.toMillis
  private val notificationTemplate = NotificationTemplate(notifyConfig.emailTemplatePath)
  private val resolvedDomains = notifyConfig.resolvedDomains

  system.log.debug("Notify config: {}", notifyConfig)

  self ! SubscribeToPresence()
  system.scheduler.schedule(Duration.Zero, 10.minutes, self, FindNewUsers())
  system.scheduler.schedule(1.minute, 1.minute, self, CheckNotify())

  protected def handleCommand: Receive = {
    case FindNewUsers() ⇒
      log.debug("Searching for new users")
      refetchUsers()
    case FindNewUsersResponse(userIds, lastDate) ⇒
      log.debug("Found new users: {}, max register timestamp: {}", userIds, lastDate)
      persist(NewUsersAdded(Instant.now(), userIds, lastDate)) { e ⇒
        commit(e)
        userIds foreach { uid ⇒ presenceExt.subscribe(uid, self) }
      }
    case SubscribeToPresence() ⇒
      log.debug("Subscribing to presences of users: {}", state.userIds)
      state.userIds foreach { uid ⇒ presenceExt.subscribe(uid, self) }
    case PresenceState(userId, Offline, Some(lastSeen)) ⇒
      log.debug("Got Offline(lastSeen: {}) from user: {}", lastSeen, userId)
      persist(NotifyScheduled(Instant.now(), userId, lastSeen.getMillis + notifyAfter)) { e ⇒
        commit(e)
      }
    case PresenceState(userId, Online, Some(lastSeen)) ⇒
      log.debug("Got Online(lastSeen: {}) from user: {}", lastSeen, userId)
      persist(NotifyCanceled(Instant.now(), userId)) { e ⇒ commit(e) }
    case CheckNotify() ⇒
      log.debug("Got check notify message")
      val now = Instant.now.toEpochMilli
      val (toNotify, _) = state.scheduled.partition(_._2 >= now)
      toNotify foreach { case (userId, _) ⇒ self ! Notify(userId) }
    case Notify(userId) ⇒
      log.debug("Got notify message for user: {}", userId)
      if (state.scheduled.exists(_._1 == userId)) notify(userId)
    case CancelNotify(userId) ⇒
      log.debug("Got cancel notify message for user: {}", userId)
      persist(NotifyCanceled(Instant.now(), userId)) { e ⇒ commit(e) }
    case PresenceState(userId, _, None) ⇒
    // new user, don't do anything until next online/offline with lastSeen
  }

  protected def handleQuery: PartialFunction[Any, Future[Any]] = {
    case _ ⇒ FastFuture.successful(())
  }

  override def persistenceId: String = s"Notifier_${self.path.name}"

  protected def getInitialState: NotifyProcessorState = NotifyProcessorState.empty

  private def notify(userId: Int): Unit = for {
    user ← userExt.getUser(userId)
    _ ← user.emails match {
      case userEmail +: _ if resolvedDomains contains userEmail ⇒
        for {
          grouped ← dialogExt.fetchGroupedDialogs(userId)
          _ ← emailIfNeeded(userEmail, user.name, grouped) match {
            case Some(email) ⇒
              log.debug("Sending email notification: {} to email: {}", email.subject, userEmail)
              emailExt.sender.send(email) map { _ ⇒
                self ! CancelNotify(userId)
              }
            case None ⇒
              log.debug("No unread messages for user with email: {}", userEmail)
              FastFuture.successful(())
          }
        } yield ()
      case Seq() ⇒
        log.debug("User: {} doesn't have email, skipping", userId)
        FastFuture.successful(())
    }
  } yield ()

  private def emailIfNeeded(email: String, name: String, grouped: Seq[DialogGroup]): Option[Message] = {
    val dialogs = grouped flatMap (_.dialogs)
    val unreadCount = dialogs.map(_.counter).sum
    if (unreadCount > 0) {
      val dialogCount = dialogs.count(_.counter > 0)
      val subject = s"You have $unreadCount unread messages"
      val content = notificationTemplate.render(name, unreadCount, dialogCount)
      Some(Message(email, subject, content))
    } else {
      None
    }
  }

  private def refetchUsers(): Unit = {
    val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(state.lastUserRegisterDate), ZoneOffset.UTC)
    db.run(UserRepo.activeUserIdsCreatedAfter(date)) foreach { users ⇒
      users.grouped(1000) foreach { part ⇒
        val lastDate = part.lastOption.map(_._2.toInstant(ZoneOffset.UTC).toEpochMilli).getOrElse(state.lastUserRegisterDate)
        self ! FindNewUsersResponse(part.map(_._1), lastDate)
      }
    }
  }

}