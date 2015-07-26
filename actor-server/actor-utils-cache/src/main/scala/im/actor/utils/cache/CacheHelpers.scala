package im.actor.utils.cache

import com.github.benmanes.caffeine.cache.{Cache, Caffeine}

import scala.concurrent.{ExecutionContext, Future}

object CacheHelpers {

  def createCache[K <: AnyRef, V <: AnyRef](maxSize: Long): Cache[K, V] = Caffeine.newBuilder().maximumSize(maxSize).build[K, V]

  def withCachedFuture[K, V](key: K)(computation: () ⇒ Future[V])(
    implicit
    cache: Cache[K, Future[V]],
    ec:    ExecutionContext
  ) =
    Option(cache getIfPresent key) match {
      case Some(result) ⇒ result
      case None ⇒
        val result = computation()
        cache.put(key, result)

        result onFailure {
          case _ ⇒ cache.invalidate(key)
        }

        result
    }

}
