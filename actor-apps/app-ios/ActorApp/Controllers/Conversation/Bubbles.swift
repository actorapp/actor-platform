//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class Bubbles {
    
    private static let textLayouter = AABubbleTextCellLayouter()
    private static let mediaLayouter = AABubbleMediaCellLayouter()
    private static let documentLayouter = AABubbleDocumentCellLayout()
    private static let serviceLayouter = AABubbleServiceCellLayouter()
    
    private static let layouters:[AABubbleLayouter] = [
        serviceLayouter,
        mediaLayouter,
        documentLayouter,
        textLayouter
    ]
    
    class func initCollectionView(collectionView: UICollectionView) {
        for layouter in layouters {
            collectionView.registerClass(layouter.cellClass(), forCellWithReuseIdentifier: layouter.reuseId)
        }
    }
    
    class func cellTypeForMessage(message: ACMessage) -> String {
        
        for layouter in layouters {
            if (layouter.isSuitable(message)) {
                return layouter.reuseId
            }
        }
        
        fatalError("No layouter for cell")
    }
    
    class func buildLayout(peer: ACPeer, message: ACMessage) -> CellLayout {
        for layouter in layouters {
            if (layouter.isSuitable(message)) {
                return layouter.buildLayout(peer, message: message)
            }
        }
        
        fatalError("No layouter for cell")
    }
}