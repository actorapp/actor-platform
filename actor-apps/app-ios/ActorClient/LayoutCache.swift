//
//  HeightCell.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 26.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class LayoutCache {
    
    private var layouts: [jlong: CellLayout] = [:]
    
    func pick(id: jlong) -> CellLayout? {
        return layouts[id]
    }
    
    func cache(id: jlong, layout: CellLayout) {
        layouts[id] = layout
    }
}