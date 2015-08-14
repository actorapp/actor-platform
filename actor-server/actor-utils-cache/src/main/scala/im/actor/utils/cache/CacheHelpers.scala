package im.actor.utils.cache

import com.github.benmanes.caffeine.cache.{ Cache, Caffeine }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{Failure, Success}

object CacheHelpers {

  def createCache[K <: AnyRef, V <: AnyRef](maxSize: Long): Cache[K, V] = Caffeine.newBuilder().maximumSize(maxSize).build[K, V]

  def withCachedFuture[K, V](key: K)(future: ⇒ Future[V])(
    implicit
    cache: Cache[K, Future[V]],
    ec:    ExecutionContext
  ): Future[V] =
    Option(cache getIfPresent key) match {
      case Some(result) ⇒
        result
      case None ⇒
        val result = future
        cache.put(key, result)

        result recover {
          case e =>
            cache.invalidate(key)
            throw e
        }
    }
}