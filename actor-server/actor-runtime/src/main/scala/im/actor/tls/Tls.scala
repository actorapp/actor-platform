package im.actor.tls

import java.io.FileInputStream
import java.security.{ KeyStore, SecureRandom }
import javax.net.ssl._

import scala.collection.immutable

import akka.http.scaladsl.HttpsConnectionContext
import akka.stream.io._
import akka.stream.scaladsl.{ BidiFlow, Flow, Keep }
import akka.util.ByteString
import com.github.kxbmap.configs._
import com.typesafe.config.Config

case class TlsContext(
  sslContext:          SSLContext,
  enabledCipherSuites: Option[immutable.Seq[String]] = None,
  enabledProtocols:    Option[immutable.Seq[String]] = None,
  clientAuth:          Option[ClientAuth]            = None,
  sslParameters:       Option[SSLParameters]         = None
) {
  def asHttpsContext: HttpsConnectionContext =
    new HttpsConnectionContext(
      sslContext,
      enabledCipherSuites,
      enabledProtocols,
      clientAuth,
      sslParameters
    )
}

object TlsContext {
  def load(kssConfig: Config, ksName: String): Either[Throwable, TlsContext] = {
    for {
      ksConfig ← kssConfig.get[Either[Throwable, Config]](ksName).right
      ctx ← load(ksConfig).right
    } yield ctx
  }

  def load(ksConfig: Config): Either[Throwable, TlsContext] = {
    for {
      sslContext ← initSslContext(ksConfig).right
    } yield {
      val ciphers = ksConfig.opt[List[String]]("ciphers")

      TlsContext(sslContext, ciphers, None, None, None)
    }
  }

  private def initSslContext(ksConfig: Config): Either[Throwable, SSLContext] = {
    for {
      path ← ksConfig.get[Either[Throwable, String]]("path").right
      password ← ksConfig.get[Either[Throwable, String]]("password").right
    } yield {
      val keyStore = KeyStore.getInstance(KeyStore.getDefaultType)
      keyStore.load(new FileInputStream(path), password.toCharArray)

      val trustStore = KeyStore.getInstance(KeyStore.getDefaultType)
      trustStore.load(new FileInputStream(path), password.toCharArray)

      val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
      keyManagerFactory.init(keyStore, password.toCharArray)

      val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
      trustManagerFactory.init(trustStore)

      val context = SSLContext.getInstance("TLS")
      context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
      context
    }
  }
}

object Tls {
  type Connection = Flow[ByteString, ByteString, akka.NotUsed]
  type TlsLayer = BidiFlow[ByteString, ByteString, ByteString, ByteString, akka.NotUsed]

  private type WrapLayer = BidiFlow[ByteString, SslTlsInbound, SslTlsOutbound, ByteString, akka.NotUsed]

  private val unwrapTls: Flow[SslTlsInbound, ByteString, akka.NotUsed] = Flow[SslTlsInbound] collect { case x: SessionBytes ⇒ x.bytes }

  private val wrapTls: Flow[ByteString, SslTlsOutbound, akka.NotUsed] = Flow[ByteString].map[SslTlsOutbound](SendBytes)

  private val wrapLayer: BidiFlow[ByteString, SslTlsOutbound, SslTlsInbound, ByteString, akka.NotUsed] =
    BidiFlow.fromFlowsMat(wrapTls, unwrapTls)(Keep.right)

  /**
   * Generate TlsLayer
   *
   * {{{
   *
   *     UnencryptedOut ~~> +---+ ~~> EncryptedOut
   *                        |   |
   *     UnencryptedIn  <~~ +---+ <~~ EncryptedIn
   * }}}
   *
   * @param flow
   * @return
   */
  def layer(flow: SslTls.ScalaFlow): TlsLayer = wrapLayer atop flow

  def connection(context: TlsContext, connFlow: Flow[ByteString, ByteString, akka.NotUsed]): Connection = {
    val sslTls = SslTls(
      context.sslContext,
      NegotiateNewSession.apply(
        context.enabledCipherSuites,
        context.enabledCipherSuites,
        context.clientAuth,
        context.sslParameters
      ),
      Role.server
    )

    layer(sslTls) join connFlow
  }
}