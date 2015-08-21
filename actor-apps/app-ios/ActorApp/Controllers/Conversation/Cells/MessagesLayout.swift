//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class MessagesLayout : UICollectionViewLayout {
    
    var deletedIndexPaths = [NSIndexPath]()
    var insertedIndexPaths = [NSIndexPath]()
    var items = [LayoutItem]()
    var frames = [CGRect]()
    var disableAutoScroll: Bool = false
    
    var contentHeight: CGFloat = 0.0
    var currentItems = [CachedLayout]()
    var isScrolledToEnd: Bool = false
    
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
    
    func beginUpdates(disableAutoScroll: Bool) {
        
        var start = CFAbsoluteTimeGetCurrent()
        
        self.disableAutoScroll = disableAutoScroll
        
//        cachedLayoutPool.acquire(currentItems)
        currentItems.removeAll(keepCapacity: true)

        // Saving current visible cells
        var visibleItems = self.collectionView!.indexPathsForVisibleItems()
        var currentOffset = self.collectionView!.contentOffset.y
        for indexPath in visibleItems {
            var index = (indexPath as! NSIndexPath).item
            var topOffset = items[index].attrs.frame.origin.y - currentOffset
            var id = items[index].id
            currentItems.append(CachedLayout(id: id, offset: topOffset))
        }
        
        isScrolledToEnd = self.collectionView!.contentOffset.y < 8
        
        println("beginUpdates: \(CFAbsoluteTimeGetCurrent() - start)")
    }
    
    override func prepareLayout() {
        
        var start = CFAbsoluteTimeGetCurrent()
        
        super.prepareLayout()
        
        println("prepareLayout(super): \(CFAbsoluteTimeGetCurrent() - start)")
        start = CFAbsoluteTimeGetCurrent()
        
        var del = self.collectionView!.delegate as! MessagesLayoutDelegate
        
        // Validate sections
        var sectionsCount = self.collectionView!.numberOfSections()
        if sectionsCount == 0 {
            items.removeAll(keepCapacity: true)
            contentHeight = 0.0
            return
        }
        if sectionsCount != 1 {
            fatalError("Unsupported more than 1 section")
        }
        
        
        var itemsCount = self.collectionView!.numberOfItemsInSection(0)
        var offset: CGFloat = 0
        contentHeight = 0.0
        
        // layoutItemPool.acquire(items)
        items.removeAll(keepCapacity: true)
        frames.removeAll(keepCapacity: true)
        
        for i in 0..<itemsCount {
            var indexPath = NSIndexPath(forRow: i, inSection: 0)
            var itemId = del.collectionView(self.collectionView!, layout: self, idForItemAtIndexPath: indexPath)
            var itemSize = del.collectionView(self.collectionView!, layout: self, sizeForItemAtIndexPath: indexPath)
            //var itemId = Int64(i)
            
            // var itemSize = CGSizeMake(300, 44)
            
//            var item:LayoutItem! = layoutItemPool.get()
//            if (item == nil) {
//                item = LayoutItem(id: itemId)
//            } else {
//                item.id = itemId
//            }
            var frame = CGRect(origin: CGPointMake(0, offset), size: itemSize)
            var item = LayoutItem(id: itemId)
            
            item.size = itemSize
            
            var attrs = UICollectionViewLayoutAttributes(forCellWithIndexPath: indexPath)
            // attrs.size = item.size
            attrs.frame = CGRect(origin: CGPointMake(0, offset), size: itemSize)
            // attrs.center = CGPointMake(0, offset + item.size.height / 2.0)
            //attrs.frame = CGRect(origin: CGPointMake(0, offset), size: attrs.size)
            //attrs.bounds = CGRect(origin: CGPointZero, size: attrs.size)
                // attrs.alpha = 0
            
            offset += item.size.height
            item.attrs = attrs
            
            items.append(item)
            frames.append(frame)
            
            contentHeight += item.size.height
        }
        
        println("prepareLayout: \(CFAbsoluteTimeGetCurrent() - start)")
    }
    
    override func collectionViewContentSize() -> CGSize {
        return CGSize(width: self.collectionView!.bounds.width, height: contentHeight)
    }
    
    override func layoutAttributesForElementsInRect(rect: CGRect) -> [AnyObject]? {
        var res = [AnyObject]()
        for i in 0..<items.count {
            if CGRectIntersectsRect(rect, frames[i]) {
                res.append(items[i].attrs)
            }
        }        
        return res
    }
    
    override func prepareForCollectionViewUpdates(updateItems: [AnyObject]!) {
        
        var start = CFAbsoluteTimeGetCurrent()
        super.prepareForCollectionViewUpdates(updateItems)
        
        insertedIndexPaths.removeAll(keepCapacity: true)
        deletedIndexPaths.removeAll(keepCapacity: true)
        for update in updateItems {
            if let upd = update as? UICollectionViewUpdateItem {
                if upd.updateAction == .Insert {
                    insertedIndexPaths.append(upd.indexPathAfterUpdate!)
                } else if upd.updateAction == .Delete {
                    deletedIndexPaths.append(upd.indexPathBeforeUpdate!)
                }
            }
        }
        println("prepareForCollectionViewUpdates: \(CFAbsoluteTimeGetCurrent() - start)")
    }
    
    override func layoutAttributesForItemAtIndexPath(indexPath: NSIndexPath) -> UICollectionViewLayoutAttributes! {
        return items[indexPath.item].attrs
    }    
    
    override func initialLayoutAttributesForAppearingItemAtIndexPath(itemIndexPath: NSIndexPath) -> UICollectionViewLayoutAttributes? {
        var res = super.initialLayoutAttributesForAppearingItemAtIndexPath(itemIndexPath)
//        if insertedIndexPaths.contains(itemIndexPath) {
//            res?.alpha = 0
//            res?.transform = CGAffineTransformTranslate(CGAffineTransformIdentity, 0, -44)
//        } else {
//            res?.alpha = 1
//        }
        return res
    }
    
    override func finalizeCollectionViewUpdates() {
        super.finalizeCollectionViewUpdates()
        
        var start = CFAbsoluteTimeGetCurrent()
        
//        if disableAutoScroll {
//            var size = collectionViewContentSize()
//            var delta: CGFloat! = nil
//            for item in items {
//                for current in currentItems {
//                    if current.id == item.id {
//                        var oldOffset = current.offset
//                        var newOffset = item.attrs!.frame.origin.y - self.collectionView!.contentOffset.y
//                        delta = oldOffset - newOffset
//                    }
//                }
//            }
//            
//            if delta != nil {
//                self.collectionView!.contentOffset = CGPointMake(0, self.collectionView!.contentOffset.y - delta)
//            }
//        }
        
        println("finalizeCollectionViewUpdates: \(CFAbsoluteTimeGetCurrent() - start)")
    }
}

struct LayoutItem {
    
    var id: Int64
    var invalidated: Bool = true
    var size: CGSize!
    var attrs: UICollectionViewLayoutAttributes!
    
    init(id: Int64) {
        self.id = id
    }
}

struct CachedLayout {
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
