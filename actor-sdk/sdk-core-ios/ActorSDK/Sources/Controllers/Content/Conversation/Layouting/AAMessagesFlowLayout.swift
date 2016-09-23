//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

private let ENABLE_LOGS = false

class AAMessagesFlowLayout : UICollectionViewLayout {
    
    var deletedIndexPaths = [IndexPath]()
    var insertedIndexPaths = [IndexPath]()
    var items = [AALayoutItem?]()
    var frames = [CGRect?]()
    var isLoadMore: Bool = false
    
    var contentHeight: CGFloat = 0.0
    var currentItems = [AACachedLayout]()
    var isScrolledToEnd: Bool = false
    
    var list: AAPreprocessedList?
    var unread: jlong?
    
    override init() {
        super.init()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func beginUpdates(_ isLoadMore: Bool, list: AAPreprocessedList?, unread: jlong?) {
        
        self.isLoadMore = isLoadMore
        self.list = list
        self.unread = unread
        
        // Saving current visible cells
        currentItems.removeAll(keepingCapacity: true)
        let visibleItems = self.collectionView!.indexPathsForVisibleItems
        let currentOffset = self.collectionView!.contentOffset.y
        for indexPath in visibleItems {
            let index = (indexPath as NSIndexPath).item
            let topOffset = items[index]!.attrs.frame.origin.y - currentOffset
            let id = items[index]!.id
            currentItems.append(AACachedLayout(id: id, offset: topOffset))
        }
        
        isScrolledToEnd = self.collectionView!.contentOffset.y < 8
    }
    
    override func prepare() {
        super.prepare()
        
        autoreleasepool {
            
            // Validate sections
            let sectionsCount = self.collectionView!.numberOfSections
            if sectionsCount == 0 {
                items.removeAll(keepingCapacity: false)
                contentHeight = 0.0
                return
            }
            if sectionsCount != 1 {
                fatalError("Unsupported more than 1 section")
            }
            
            if AADevice.isiPad {
                contentHeight = 16.0
            } else {
                contentHeight = 6.0
            }
            // items.removeAll(keepCapacity: false)
            // frames.removeAll(keepCapacity: false)
            items.removeAll(keepingCapacity: true)
            frames.removeAll(keepingCapacity: true)
            
            if list != nil {
                
//                items = [AALayoutItem?](count: Int(list!.items.count), repeatedValue: nil)
//                frames = [CGRect?](count: Int(list!.items.count), repeatedValue: nil)
                
                for i in 0..<list!.items.count {
                    let itemId = list!.items[i].rid
                    var height = list!.heights[i]
                    if itemId == unread {
                        height += AABubbleCell.newMessageSize
                    }
                    let itemSize = CGSize(width: self.collectionView!.bounds.width, height: height)
                    
                    let frame = CGRect(origin: CGPoint(x: 0, y: contentHeight), size: itemSize)
                    var item = AALayoutItem(id: itemId)
                    
                    item.size = itemSize
                    
                    let attrs = UICollectionViewLayoutAttributes(forCellWith: IndexPath(row: i, section: 0))
                    attrs.frame = frame
                    item.attrs = attrs
                    
//                    attrs.frame.size = itemSize
                    
//                    items[i] = item
//                    frames[i] = frame
                    items.append(item)
                    frames.append(frame)
                    
                    contentHeight += item.size.height
                }
            } else {
                items = []
                frames = []
            }
            
            contentHeight += 100
        }
    }
    
    override var collectionViewContentSize : CGSize {
        return CGSize(width: self.collectionView!.bounds.width, height: contentHeight)
    }
    
    override func layoutAttributesForElements(in rect: CGRect) -> [UICollectionViewLayoutAttributes]? {
        var res = [UICollectionViewLayoutAttributes]()
        for i in 0..<items.count {
            if rect.intersects(frames[i]!) {
                res.append(items[i]!.attrs)
            }
        }
        return res
    }
    
    override func prepare(forCollectionViewUpdates updateItems: [UICollectionViewUpdateItem]) {
        let start = CFAbsoluteTimeGetCurrent()
        super.prepare(forCollectionViewUpdates: updateItems)
        
        if !isLoadMore {
            
            insertedIndexPaths.removeAll(keepingCapacity: true)
            deletedIndexPaths.removeAll(keepingCapacity: true)
            
            for update in updateItems {
                if update.updateAction == .insert {
                    insertedIndexPaths.append(update.indexPathAfterUpdate!)
                } else if update.updateAction == .delete {
                    deletedIndexPaths.append(update.indexPathBeforeUpdate!)
                }
            }
        }
        
        if ENABLE_LOGS { NSLog("🙇 prepareForCollectionViewUpdates: \(CFAbsoluteTimeGetCurrent() - start)") }
    }

    override func layoutAttributesForItem(at indexPath: IndexPath) -> UICollectionViewLayoutAttributes? {
        return items[(indexPath as NSIndexPath).item]!.attrs
    }    
    
    override func initialLayoutAttributesForAppearingItem(at itemIndexPath: IndexPath) -> UICollectionViewLayoutAttributes? {
        let res = super.initialLayoutAttributesForAppearingItem(at: itemIndexPath)
        if !isLoadMore && insertedIndexPaths.contains(itemIndexPath) {
            res?.alpha = 0
            res?.transform = CGAffineTransform.identity.translatedBy(x: 0, y: -44)
        } else {
            res?.alpha = 1
        }
        return res
    }
    
    override func finalizeCollectionViewUpdates() {
        super.finalizeCollectionViewUpdates()
        
        let start = CFAbsoluteTimeGetCurrent()
        
        if isLoadMore {
            var delta: CGFloat! = nil
            outer: for item in items {
                for current in currentItems {
                    if current.id == item!.id {
                        let oldOffset = current.offset
                        let newOffset = item!.attrs!.frame.origin.y - self.collectionView!.contentOffset.y
                        delta = oldOffset - newOffset
                        break outer
                    }
                }
            }
            
            if delta != nil {
                self.collectionView!.contentOffset = CGPoint(x: 0, y: self.collectionView!.contentOffset.y - delta)
            }
        }
        
        if ENABLE_LOGS { NSLog("🙇 finalizeCollectionViewUpdates: \(CFAbsoluteTimeGetCurrent() - start)") }
    }
}

struct AALayoutItem {
    
    var id: Int64
    var invalidated: Bool = true
    var size: CGSize!
    var attrs: UICollectionViewLayoutAttributes!
    
    init(id: Int64) {
        self.id = id
    }
}

struct AACachedLayout {
    var id: Int64
    var offset: CGFloat
    
    init(id: Int64, offset: CGFloat) {
        self.id = id
        self.offset = offset
    }
}

@objc enum AAMessageGravity: Int {
    case left
    case right
    case center
}
