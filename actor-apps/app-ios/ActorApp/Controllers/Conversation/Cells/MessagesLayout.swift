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
        var processedList = del.getProcessedList()
        
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

        contentHeight = 0.0
        items.removeAll(keepCapacity: true)
        frames.removeAll(keepCapacity: true)
        
        if processedList != nil {
            for i in 0..<processedList!.items.count {
                var indexPath = NSIndexPath(forRow: i, inSection: 0)
                var itemId = processedList!.items[i].rid
                var itemSize = CGSizeMake(self.collectionView!.bounds.width,  processedList!.heights[i])
                
                var frame = CGRect(origin: CGPointMake(0, contentHeight), size: itemSize)
                var item = LayoutItem(id: itemId)
                
                item.size = itemSize
                
                var attrs = UICollectionViewLayoutAttributes(forCellWithIndexPath: indexPath)
                attrs.frame = frame
                item.attrs = attrs
                
                items.append(item)
                frames.append(frame)
                
                contentHeight += item.size.height
            }
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
    
    @objc func getProcessedList() -> PreprocessedList?
}

@objc enum MessageGravity: Int {
    case Left
    case Right
    case Center
}
