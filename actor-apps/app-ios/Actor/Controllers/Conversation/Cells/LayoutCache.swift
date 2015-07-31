//
//  HeightCell.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 26.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
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