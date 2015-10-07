//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class Cache<T> {
    private var cache = HashMap<T>()
    
    func pick(id: Int64) -> T? {
        return cache.getValueAtKey(id)
    }
    
    func cache(id: Int64, value: T) {
        cache.setKey(id, withValue: value)
    }
    
    func revoke(id: Int64) {
        cache.setKey(id, withValue: nil)
    }

}

struct CachedSetting {
    
    let prevId: jlong?
    let nextId: jlong?
    let cached: CellSetting
    
    init(cached: CellSetting, prevId: jlong?, nextId: jlong?) {
        self.prevId = prevId
        self.nextId = nextId
        self.cached = cached
    }
    
    func isValid(prev: ACMessage?, next:ACMessage?) -> Bool {
        if prev?.rid != prevId {
            return false
        }
        
        if next?.rid != nextId {
            return false
        }

        return true
    }
}

class LayoutCache : Cache<CellLayout> {

}

class FastThumbCache {
    private var thumbs = HashMap<UIImage>()
    
    func pick(id: Int64) -> UIImage? {
        return thumbs.getValueAtKey(id)
    }
    
    func cache(id: Int64, image: UIImage) {
        thumbs.setKey(id, withValue: image)
    }
}