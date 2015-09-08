package im.actor.util.cache

import com.github.benmanes.caffeine.cache.{Caffeine, Cache}

import scala.concurrent.{ExecutionContext, Future}

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
