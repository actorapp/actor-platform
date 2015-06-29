package im.actor.server.models

import java.time.LocalDateTime

/**
 * Parent model that stores authorization info before user log in.
 */
case class AuthTransaction(
  transactionHash: String,
  appId:           Int,
  apiKey:          String,
  deviceHash:      Array[Byte],
  deviceTitle:     String,
  accessSalt:      String,
  isChecked:       Boolean               = false,
  deletedAt:       Option[LocalDateTime] = None
)

/**
 * Model that stores info about phone authorization
 */
case class AuthPhoneTransaction(
  phoneNumber:     Long,
  transactionHash: String,
  appId:           Int,
  apiKey:          String,
  deviceHash:      Array[Byte],
  deviceTitle:     String,
  accessSalt:      String,
  isChecked:       Boolean               = false,
  deletedAt:       Option[LocalDateTime] = None
) extends AuthTransactionChildren

/**
 * Model that stores info about email authorization
 */
case class AuthEmailTransaction(
  email:           String,
  redirectUri:     Option[String],
  transactionHash: String,
  appId:           Int,
  apiKey:          String,
  deviceHash:      Array[Byte],
  deviceTitle:     String,
  accessSalt:      String,
  isChecked:       Boolean               = false,
  deletedAt:       Option[LocalDateTime] = None
) extends AuthTransactionChildren

sealed trait AuthTransactionChildren {
  def transactionHash: String
  def appId: Int
  def apiKey: String
  def deviceHash: Array[Byte]
  def deviceTitle: String
  def isChecked: Boolean
  def accessSalt: String
}