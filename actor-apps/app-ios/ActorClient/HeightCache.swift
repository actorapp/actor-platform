//
//  HeightCell.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 26.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class HeightCache {
    
    private var sizes: [jlong: CGSize] = [:]
    
    func pick(id: jlong) -> CGSize? {
        return sizes[id]
    }
    
    func cache(id: jlong, size: CGSize) {
        sizes[id] = size
    }
}