package im.actor.server.file.s3

import com.github.kxbmap.configs.syntax._
import com.typesafe.config.{ ConfigFactory, Config }

private[s3] case class S3StorageAdapterConfig(
  bucketName:      String,
  region:          Option[String],
  key:             String,
  secret:          String,
  endpoint:        Option[String],
  pathStyleAccess: Boolean
)

private[s3] object S3StorageAdapterConfig {
  def load(config: Config): S3StorageAdapterConfig = {
    S3StorageAdapterConfig(
      bucketName = config.get[String]("default-bucket"),
      region = config.getOpt[String]("region"),
      key = config.get[String]("access-key"),
      secret = config.get[String]("secret-key"),
      endpoint = config.getOpt[String]("endpoint"),
      pathStyleAccess = config.get[Boolean]("path-style-access")
    )
  }
  def load: S3StorageAdapterConfig = load(ConfigFactory.load().getConfig("services.aws.s3"))
}
