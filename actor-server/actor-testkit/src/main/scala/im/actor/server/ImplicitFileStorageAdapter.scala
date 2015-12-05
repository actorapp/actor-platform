package im.actor.server

import akka.actor.ActorSystem
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import im.actor.server.file.s3.{ S3StorageAdapter, S3StorageAdapterConfig }
import im.actor.server.file.FileStorageAdapter

trait ImplicitFileStorageAdapter {
  protected implicit val system: ActorSystem

  protected implicit lazy val awsCredentials = new EnvironmentVariableCredentialsProvider()
  protected implicit lazy val fsAdapterS3: S3StorageAdapter = new S3StorageAdapter(system)
  protected implicit lazy val fsAdapter: FileStorageAdapter = fsAdapterS3
}
