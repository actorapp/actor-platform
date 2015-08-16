package im.actor.server

import akka.actor.ActorSystem
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider

import im.actor.server.util.{ FileStorageAdapter, S3StorageAdapter, S3StorageAdapterConfig }

trait ImplicitFileStorageAdapter {
  protected implicit val system: ActorSystem

  protected implicit lazy val awsCredentials = new EnvironmentVariableCredentialsProvider()
  protected implicit lazy val fsAdapterS3: S3StorageAdapter = new S3StorageAdapter(S3StorageAdapterConfig.load.get, system)
  protected implicit lazy val fsAdapter: FileStorageAdapter = fsAdapterS3
}
