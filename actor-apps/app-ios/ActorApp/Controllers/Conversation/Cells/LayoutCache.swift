//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class LayoutCache {
    
    private var layouts = HashMap<CellLayout>()
    
    func pick(id: Int64) -> CellLayout? {
        // return layouts.objectForKey(id) as? CellLayout
        return layouts.getValueAtKey(id)
    }
    
    func cache(id: Int64, layout: CellLayout) {
        // layouts.setObject(layout, forKey: id)
        layouts.setKey(id, withValue: layout)
    }
}