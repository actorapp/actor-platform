//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class AACache<T> {
    fileprivate var cache = AAHashMap<T>()
    
    func pick(_ id: Int64) -> T? {
        return cache.getValueAtKey(id)
    }
    
    func cache(_ id: Int64, value: T) {
        cache.setKey(id, withValue: value)
    }
    
    func revoke(_ id: Int64) {
        cache.setKey(id, withValue: nil)
    }

}

struct AACachedSetting {
    
    let prevId: jlong?
    let nextId: jlong?
    let cached: AACellSetting
    
    init(cached: AACellSetting, prevId: jlong?, nextId: jlong?) {
        self.prevId = prevId
        self.nextId = nextId
        self.cached = cached
    }
    
    func isValid(_ prev: ACMessage?, next:ACMessage?) -> Bool {
        if prev?.rid != prevId {
            return false
        }
        
        if next?.rid != nextId {
            return false
        }

        return true
    }
}

class AALayoutCache : AACache<AACellLayout> {

}

class AAFastThumbCache {
    fileprivate var thumbs = AAHashMap<UIImage>()
    
    func pick(_ id: Int64) -> UIImage? {
        return thumbs.getValueAtKey(id)
    }
    
    func cache(_ id: Int64, image: UIImage) {
        thumbs.setKey(id, withValue: image)
    }
}
