package im.actor.utils.cache

import java.util.function.Function

import com.github.benmanes.caffeine.cache.{ Cache, Caffeine }

object CacheHelpers {

  def createCache[K <: AnyRef, V <: AnyRef]: Cache[K, V] = Caffeine.newBuilder().maximumSize(100).build[K, V]

  def withCachedResult[K, V](key: K)(f: ⇒ V)(implicit cache: Cache[K, V]) = {
    cache.get(key, new Function[K, V] { def apply(k: K) = f })
  }

}
