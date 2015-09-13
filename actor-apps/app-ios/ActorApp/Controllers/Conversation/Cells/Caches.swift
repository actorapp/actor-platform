//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class LayoutCache {
    
    private var layouts = HashMap<CellLayout>()
    
    func pick(id: Int64) -> CellLayout? {
        return layouts.getValueAtKey(id)
    }
    
    func cache(id: Int64, layout: CellLayout) {
        layouts.setKey(id, withValue: layout)
    }
    
    func revoke(id: Int64) {
        layouts.setKey(id, withValue: nil)
    }
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