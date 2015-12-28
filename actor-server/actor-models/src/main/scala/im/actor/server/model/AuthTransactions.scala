package im.actor.server.model

import java.time.LocalDateTime

/**
 * Parent model that contains authorization info before user log in.
 */
final case class AuthTransaction(
  transactionHash: String,
  appId:           Int,
  apiKey:          String,
  deviceHash:      Array[Byte],
  deviceTitle:     String,
  accessSalt:      String,
  deviceInfo:      Array[Byte],
  isChecked:       Boolean               = false,
  deletedAt:       Option[LocalDateTime] = None
)

/**
 * Model that contains info about phone authorization
 */
final case class AuthPhoneTransaction(
  phoneNumber:     Long,
  transactionHash: String,
  appId:           Int,
  apiKey:          String,
  deviceHash:      Array[Byte],
  deviceTitle:     String,
  accessSalt:      String,
  deviceInfo:      Array[Byte],
  isChecked:       Boolean               = false,
  deletedAt:       Option[LocalDateTime] = None
) extends AuthTransactionBase

/**
 * Model that contains info about email authorization
 */
final case class AuthEmailTransaction(
  email:           String,
  redirectUri:     Option[String],
  transactionHash: String,
  appId:           Int,
  apiKey:          String,
  deviceHash:      Array[Byte],
  deviceTitle:     String,
  accessSalt:      String,
  deviceInfo:      Array[Byte],
  isChecked:       Boolean               = false,
  deletedAt:       Option[LocalDateTime] = None
) extends AuthTransactionBase

/**
 * Model that contains info about username authorization
 */
final case class AuthUsernameTransaction(
  username:        String,
  userId:          Option[Int],
  transactionHash: String,
  appId:           Int,
  apiKey:          String,
  deviceHash:      Array[Byte],
  deviceTitle:     String,
  accessSalt:      String,
  deviceInfo:      Array[Byte],
  isChecked:       Boolean               = false,
  deletedAt:       Option[LocalDateTime] = None
) extends AuthTransactionBase

sealed trait AuthTransactionBase {
  def transactionHash: String
  def appId: Int
  def apiKey: String
  def deviceHash: Array[Byte]
  def deviceTitle: String
  def isChecked: Boolean
  def accessSalt: String
  def deviceInfo: Array[Byte]
}