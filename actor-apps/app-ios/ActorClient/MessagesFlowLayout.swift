//
//  MessagesViewCollection.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 27.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class MessagesFlowLayout : UICollectionViewFlowLayout {
    
    var deletedIndexPaths = [NSIndexPath]()
    var insertedIndexPaths = [NSIndexPath]()
    
    override init() {
        super.init()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func prepareForCollectionViewUpdates(updateItems: [AnyObject]!) {
        super.prepareForCollectionViewUpdates(updateItems)
        
        deletedIndexPaths.removeAll(keepCapacity: true)
        insertedIndexPaths.removeAll(keepCapacity: true)
        
        for u in updateItems {
            var upd = u as! UICollectionViewUpdateItem
            if (upd.updateAction == UICollectionUpdateAction.Delete) {
                deletedIndexPaths.append(upd.indexPathBeforeUpdate!)
            } else if (upd.updateAction == UICollectionUpdateAction.Insert) {
                insertedIndexPaths.append(upd.indexPathAfterUpdate!)
            }
        }
    }
    
    override func finalizeCollectionViewUpdates() {
        super.finalizeCollectionViewUpdates()
     
        deletedIndexPaths.removeAll(keepCapacity: true)
        insertedIndexPaths.removeAll(keepCapacity: true)
    }
    
    override func initialLayoutAttributesForAppearingItemAtIndexPath(itemIndexPath: NSIndexPath) -> UICollectionViewLayoutAttributes? {
        var res = super.initialLayoutAttributesForAppearingItemAtIndexPath(itemIndexPath)
        if insertedIndexPaths.contains(itemIndexPath) {
            res!.alpha = 0.0
            res!.transform = CGAffineTransformTranslate(res!.transform, 0, -100)
        }
        return res
    }

    override func finalLayoutAttributesForDisappearingItemAtIndexPath(itemIndexPath: NSIndexPath) -> UICollectionViewLayoutAttributes? {
        var res = super.finalLayoutAttributesForDisappearingItemAtIndexPath(itemIndexPath)
        if deletedIndexPaths.contains(itemIndexPath) {
            res!.alpha = 0.0
        }
        return res
    }
}