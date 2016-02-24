package im.actor.server.file.s3

import com.github.kxbmap.configs._
import com.typesafe.config.{ ConfigFactory, Config }

import scala.util.Try

case class S3StorageAdapterConfig(bucketName: String, key: String, secret: String, endpoint: String, pathStyleAccess: Boolean)

object S3StorageAdapterConfig {
  def load(config: Config): Try[S3StorageAdapterConfig] = {
    for {
      bucketName ← config.get[Try[String]]("default-bucket")
      key ← config.get[Try[String]]("access-key")
      secret ← config.get[Try[String]]("secret-key")
      endpoint ← config.get[Try[String]]("endpoint")
      pathStyleAccess ← config.get[Try[Boolean]]("path-style-access")
    } yield S3StorageAdapterConfig(bucketName, key, secret, endpoint, pathStyleAccess)
  }

  def load: Try[S3StorageAdapterConfig] = {
    val config = ConfigFactory.load().getConfig("services.aws.s3")
    load(config)
  }
}
