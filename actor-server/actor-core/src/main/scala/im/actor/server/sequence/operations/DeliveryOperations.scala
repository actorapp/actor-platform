package im.actor.server.sequence.operations

import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.StringValue
import im.actor.api.rpc.Update
import im.actor.server.model.{ Peer, SerializedUpdate, UpdateMapping }
import im.actor.server.sequence.UserSequenceCommands.{ DeliverUpdate, Envelope }
import im.actor.server.sequence.{ PushData, PushRules, SeqState, SeqUpdatesExtension }
import akka.pattern.ask

import scala.concurrent.Future

trait DeliveryOperations { this: SeqUpdatesExtension ⇒

  def pushRules(isFat: Boolean, pushText: Option[String], excludeAuthIds: Seq[Long] = Seq.empty): PushRules =
    PushRules(isFat = isFat)
      .withData(PushData().withText(pushText.getOrElse("")))
      .withExcludeAuthIds(excludeAuthIds)

  /**
   * Send update to all devices of user and return `SeqState` associated with `authId`
   */
  def deliverClientUpdate(
    userId:      Int,
    authId:      Long,
    update:      Update,
    pushRules:   PushRules      = PushRules(),
    reduceKey:   Option[String] = None,
    deliveryId:  String         = "",
    deliveryTag: Option[String] = None
  ): Future[SeqState] =
    deliverUpdate(
      userId,
      authId,
      UpdateMapping(default = Some(serializedUpdate(update))),
      pushRules,
      reduceKey,
      deliveryId,
      deliveryTag
    )

  /**
   * Send update to all devices of user and ignore returned `SeqState`.
   *
   * @note Returned `SeqState` doesn't associated with real `authId`
   * of user, so it doesn't make any sense to return it.
   */
  def deliverUserUpdate(
    userId:      Int,
    update:      Update,
    pushRules:   PushRules      = PushRules(),
    reduceKey:   Option[String] = None,
    deliveryId:  String         = "",
    deliveryTag: Option[String] = None
  ): Future[Unit] =
    deliverUpdate(
      userId,
      0L,
      UpdateMapping(default = Some(serializedUpdate(update))),
      pushRules,
      reduceKey,
      deliveryId,
      deliveryTag
    ) map (_ ⇒ ())

  /**
   * Send update to all devices of user
   * with ability to customize update for specific `authId`.
   * Customized updates are provided via map from `authId` to `Update`.
   * For `authId`s not included in `custom` map, `default`
   * update will be sent.
   * Returned `SeqState` is associated with `authId`
   */
  def deliverCustomUpdate(
    userId:      Int,
    authId:      Long,
    default:     Option[Update],
    custom:      Map[Long, Update],
    pushRules:   PushRules         = PushRules(),
    deliveryId:  String            = "",
    deliveryTag: Option[String]    = None
  ): Future[SeqState] = deliverUpdate(
    userId,
    authId,
    mapping = UpdateMapping(
      default = default map serializedUpdate,
      custom = custom mapValues serializedUpdate
    ),
    pushRules = pushRules,
    deliveryId = deliveryId,
    deliveryTag = deliveryTag
  )

  /**
   * Send update to all devices of users from `userIds` set and return `Unit`
   */
  def broadcastPeopleUpdate(
    userIds:    Set[Int],
    update:     Update,
    pushRules:  PushRules      = PushRules(),
    reduceKey:  Option[String] = None,
    deliveryId: String         = ""
  ): Future[Unit] = {
    val mapping = UpdateMapping(default = Some(serializedUpdate(update)))
    val deliver = buildDeliver(0L, mapping, pushRules, reduceKey, deliveryId, deliveryTag = None) // TODO: add deliveryTag when needed
    broadcastUpdate(userIds, deliver)
  }

  /**
   * Send update to all devices of users from `bcastUserIds` set;
   * send update to all devices of user with id `userId` and
   * return `SeqState` associated with `authId`
   */
  //TODO: maybe automatically remove `userId` from `bcastUserIds`
  def broadcastClientUpdate(
    userId:       Int,
    authId:       Long,
    bcastUserIds: Set[Int],
    update:       Update,
    pushRules:    PushRules      = PushRules(),
    reduceKey:    Option[String] = None,
    deliveryId:   String         = ""
  ): Future[SeqState] = {
    val mapping = UpdateMapping(default = Some(serializedUpdate(update)))
    val deliver = buildDeliver(authId, mapping, pushRules, reduceKey, deliveryId, deliveryTag = None) // TODO: add deliveryTag when needed
    for {
      seqState ← deliverUpdate(userId, deliver)
      _ ← broadcastUpdate(bcastUserIds, deliver)
    } yield seqState
  }

  private def deliverUpdate(
    userId:      Int,
    authId:      Long,
    mapping:     UpdateMapping,
    pushRules:   PushRules      = PushRules(),
    reduceKey:   Option[String] = None,
    deliveryId:  String         = "",
    deliveryTag: Option[String] = None
  ): Future[SeqState] =
    deliverUpdate(userId, buildDeliver(authId, mapping, pushRules, reduceKey, deliveryId, deliveryTag))

  private def broadcastUpdate(
    userIds: Set[Int],
    deliver: DeliverUpdate
  ): Future[Unit] =
    Future.sequence(userIds.toSeq map (deliverUpdate(_, deliver))) map (_ ⇒ ())

  private def deliverUpdate(userId: Int, deliver: DeliverUpdate): Future[SeqState] = {
    val isUpdateDefined =
      deliver.getMapping.default.isDefined || deliver.getMapping.custom.nonEmpty
    require(isUpdateDefined, "No default update nor authId-specific")
    (region.ref ? Envelope(userId).withDeliverUpdate(deliver)).mapTo[SeqState]
  }

  private def serializedUpdate(u: Update): SerializedUpdate =
    SerializedUpdate(u.header, ByteString.copyFrom(u.toByteArray), u._relatedUserIds, u._relatedGroupIds)

  private def buildDeliver(
    authId:      Long,
    mapping:     UpdateMapping,
    pushRules:   PushRules,
    reduceKey:   Option[String],
    deliveryId:  String,
    deliveryTag: Option[String]
  ): DeliverUpdate =
    DeliverUpdate(
      authId = authId, // this is clientAuthId, not the auth id we should deliver to
      mapping = Some(mapping),
      pushRules = Some(pushRules),
      reduceKey = reduceKey,
      deliveryId = deliveryId,
      deliveryTag = deliveryTag
    )

}
