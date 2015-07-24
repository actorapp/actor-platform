//
//  MessagesViewCollection.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 27.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class MessagesLayout : UICollectionViewLayout {
    
    var deletedIndexPaths = [NSIndexPath]()
    var insertedIndexPaths = [NSIndexPath]()
    var items = [LayoutItem]()
    
    weak var delegate : MessagesLayoutDelegate? {
        get{
            return self.collectionView!.delegate as? MessagesLayoutDelegate
        }
    }
    
    override init() {
        super.init()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func prepareLayout() {
//        println("prepareLayout")
        super.prepareLayout()
        
        var del = self.collectionView!.delegate as! MessagesLayoutDelegate
        
        items.removeAll(keepCapacity: true)
        
        // Validate sections
        var sectionsCount = self.collectionView!.numberOfSections()
        if sectionsCount == 0 {
            return
        }
        if sectionsCount != 1 {
            fatalError("Unsupported more than 1 section")
        }
        
        var itemsCount = self.collectionView!.numberOfItemsInSection(0)
        var offset: CGFloat = 0
        for i in 0..<itemsCount {
            var indexPath = NSIndexPath(forRow: i, inSection: 0)
            var itemId = del.collectionView(self.collectionView!, layout: self, idForItemAtIndexPath: indexPath)
            var itemSize = del.collectionView(self.collectionView!, layout: self, sizeForItemAtIndexPath: indexPath)
            
            var item = LayoutItem(id: itemId)
            
            item.size = itemSize
            
            var attrs = UICollectionViewLayoutAttributes(forCellWithIndexPath: indexPath)
            attrs.size = item.size
            attrs.center = CGPointMake(0, offset + item.size.height / 2.0)
            attrs.frame = CGRect(origin: CGPointMake(0, offset), size: attrs.size)
            attrs.bounds = CGRect(origin: CGPointZero, size: attrs.size)
            
            offset += item.size.height
            item.attrs = attrs
            
            items.append(item)
        }
    }
    
    override func collectionViewContentSize() -> CGSize {
//        println("collectionViewContentSize")
        var width: CGFloat = self.collectionView!.bounds.width
        var height: CGFloat = 0
        for itm in items {
            height += itm.size.height
        }
        return CGSize(width: width, height: height)
    }
    
    override func layoutAttributesForElementsInRect(rect: CGRect) -> [AnyObject]? {
//        println("layoutAttributesForElementsInRect")
        var res = [AnyObject]()
        for itm in items {
            if CGRectIntersectsRect(rect, itm.attrs.frame) {
                res.append(itm.attrs)
            }
        }
        return res
    }
    
    override func layoutAttributesForItemAtIndexPath(indexPath: NSIndexPath) -> UICollectionViewLayoutAttributes! {
//        println("layoutAttributesForItemAtIndexPath")
        return items[indexPath.item].attrs
    }
    
    override func initialLayoutAttributesForAppearingItemAtIndexPath(itemIndexPath: NSIndexPath) -> UICollectionViewLayoutAttributes? {
        return items[itemIndexPath.item].attrs
    }
    
    var height: CGFloat!
    var currentItems = [CachedLayout]()
    var isScrolledToEnd: Bool = false
    
    func beginUpdates() {
//        println("beginUpdates")
        // Saving current visible cells
        currentItems.removeAll(keepCapacity: true)
        var visibleItems = self.collectionView!.indexPathsForVisibleItems()
        var currentOffset = self.collectionView!.contentOffset.y
        for indexPath in visibleItems {
            var index = (indexPath as! NSIndexPath).item
            var topOffset = items[index].attrs.frame.origin.y - currentOffset
            var id = items[index].id
            currentItems.append(CachedLayout(id: id, offset: topOffset))
        }
        
        height = collectionViewContentSize().height
        isScrolledToEnd = self.collectionView!.contentOffset.y < 8
    }
    
    override func prepareForCollectionViewUpdates(updateItems: [AnyObject]!) {
//        println("prepareForCollectionViewUpdates")
        
        super.prepareForCollectionViewUpdates(updateItems)
        
        
        
//        if height == nil {
//            height = collectionViewContentSize().height
//        }
//        println("prepareForCollectionViewUpdates \(self.collectionView!.contentSize.height) - \(collectionViewContentSize().height)")
    }
    
    override func finalizeCollectionViewUpdates() {
//        println("finalizeCollectionViewUpdates")
        
        super.finalizeCollectionViewUpdates()

        //if !isScrolledToEnd {
            var delta: CGFloat! = nil
            for item in items {
                for current in currentItems {
                    if current.id == item.id {
                        var oldOffset = current.offset
                        var newOffset = item.attrs!.frame.origin.y - self.collectionView!.contentOffset.y
                        delta = oldOffset - newOffset
                    }
                }
            }
            
            println("Delta: \(delta)")
            if delta != nil {
                UIView.performWithoutAnimation({ () -> Void in
                    self.collectionView!.contentOffset = CGPointMake(0, self.collectionView!.contentOffset.y - delta)
                })
            }
        //}
    }
    
//    func endUpdates() {
//        println("endUpdates")
////
////            self.collectionView!.setContentOffset(CGPointMake(0, 0), animated: false)
////        } else {
////            var delta: CGFloat! = nil
////            for item in items {
////                for current in currentItems {
////                    if current.id == item.id {
////                        var oldOffset = current.offset
////                        var newOffset = item.attrs!.frame.origin.y - self.collectionView!.contentOffset.y
////                        delta = oldOffset - newOffset
////                    }
////                }
////            }
////            if delta != nil {
////                self.collectionView!.contentOffset = CGPointMake(0, self.collectionView!.contentOffset.y + delta)
////            }
////        }
//
//
//    }
    
//    override func prepareForCollectionViewUpdates(updateItems: [AnyObject]!) {
//        super.prepareForCollectionViewUpdates(updateItems)
//        
//        deletedIndexPaths.removeAll(keepCapacity: true)
//        insertedIndexPaths.removeAll(keepCapacity: true)
//        
//        for u in updateItems {
//            var upd = u as! UICollectionViewUpdateItem
//            if (upd.updateAction == UICollectionUpdateAction.Delete) {
//                deletedIndexPaths.append(upd.indexPathBeforeUpdate!)
//            } else if (upd.updateAction == UICollectionUpdateAction.Insert) {
//                insertedIndexPaths.append(upd.indexPathAfterUpdate!)
//            }
//        }
//    }
//
//    override func finalizeCollectionViewUpdates() {
//        super.finalizeCollectionViewUpdates()
//     
//        deletedIndexPaths.removeAll(keepCapacity: true)
//        insertedIndexPaths.removeAll(keepCapacity: true)
//    }
//    
//    override func initialLayoutAttributesForAppearingItemAtIndexPath(itemIndexPath: NSIndexPath) -> UICollectionViewLayoutAttributes? {
//        var res = super.initialLayoutAttributesForAppearingItemAtIndexPath(itemIndexPath)
//        if insertedIndexPaths.contains(itemIndexPath) {
//            res!.alpha = 0.0
//        }
//        return res
//    }
//
//    override func finalLayoutAttributesForDisappearingItemAtIndexPath(itemIndexPath: NSIndexPath) -> UICollectionViewLayoutAttributes? {
//        var res = super.finalLayoutAttributesForDisappearingItemAtIndexPath(itemIndexPath)
//        if deletedIndexPaths.contains(itemIndexPath) {
//            res!.alpha = 0.0
//        }
//        return res
//    }
//    
//    override func initialLayoutAttributesForAppearingItemAtIndexPath(itemIndexPath: NSIndexPath) -> UICollectionViewLayoutAttributes? {
//        return nil
//    }
//    
//    override func finalLayoutAttributesForDisappearingItemAtIndexPath(itemIndexPath: NSIndexPath) -> UICollectionViewLayoutAttributes? {
//        return nil
//    }
}

class LayoutItem {
    
    var id: Int64
    var invalidated: Bool = true
    var size: CGSize!
    var attrs: UICollectionViewLayoutAttributes!
    
    init(id: Int64) {
        self.id = id
    }
}

class CachedLayout {
    var id: Int64
    var offset: CGFloat
    
    init(id: Int64, offset: CGFloat) {
        self.id = id
        self.offset = offset
    }
}

@objc protocol MessagesLayoutDelegate: UICollectionViewDelegate, UIScrollViewDelegate, NSObjectProtocol {
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, idForItemAtIndexPath indexPath: NSIndexPath) -> Int64
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAtIndexPath indexPath: NSIndexPath) -> CGSize

    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, gravityForItemAtIndexPath indexPath: NSIndexPath) -> MessageGravity
}

@objc enum MessageGravity: Int {
    case Left
    case Right
    case Center
}
