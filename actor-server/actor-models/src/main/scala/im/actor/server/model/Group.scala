package im.actor.server.model

import java.time.Instant

case class Group(
  id:            Int,
  creatorUserId: Int,
  accessHash:    Long,
  title:         String,
  isPublic:      Boolean,
  createdAt:     Instant,
  about:         Option[String],
  topic:         Option[String]
)

object Group {
  def fromFull(fullGroup: FullGroup): Group =
    Group(
      id = fullGroup.id,
      creatorUserId = fullGroup.creatorUserId,
      accessHash = fullGroup.accessHash,
      title = fullGroup.title,
      isPublic = fullGroup.isPublic,
      createdAt = fullGroup.createdAt,
      about = fullGroup.about,
      topic = fullGroup.topic
    )
}

case class FullGroup(
  id:                   Int,
  creatorUserId:        Int,
  accessHash:           Long,
  title:                String,
  isPublic:             Boolean,
  createdAt:            Instant,
  about:                Option[String],
  topic:                Option[String],
  titleChangerUserId:   Int,
  titleChangedAt:       Instant,
  titleChangeRandomId:  Long,
  avatarChangerUserId:  Int,
  avatarChangedAt:      Instant,
  avatarChangeRandomId: Long,
  isHidden:             Boolean
)
