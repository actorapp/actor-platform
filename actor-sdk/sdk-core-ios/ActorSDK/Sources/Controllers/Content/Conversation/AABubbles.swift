//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class AABubbles {
    
    static let textLayouter = AABubbleTextCellLayouter()
    static let mediaLayouter = AABubbleMediaCellLayouter()
    static let documentLayouter = AABubbleDocumentCellLayout()
    static let serviceLayouter = AABubbleServiceCellLayouter()
    static let locationLayouter = AABubbleLocationCellLayouter()
    static let contactLayouter = AABubbleContactCellLayouter()
    static let voiceLayouter = AABubbleVoiceCellLayouter()
    static let stickerLayouter = AABubbleStickerCellLayouter()
    
    static let builtInLayouters: [AABubbleLayouter] = [
        serviceLayouter,
        mediaLayouter,
        voiceLayouter,
        stickerLayouter,
        documentLayouter,
        locationLayouter,
        contactLayouter,
        textLayouter
    ]
    
    static var layouters: [AABubbleLayouter] = builtInLayouters
    
    class func cellClassForMessage(message: ACMessage) -> AnyClass {
        for layouter in layouters {
            if (layouter.isSuitable(message)) {
                return layouter.cellClass()
            }
        }
        
        return textLayouter.cellClass()
    }
    
    class func buildLayout(peer: ACPeer, message: ACMessage) -> AACellLayout {
        
        for layouter in layouters {
            if (layouter.isSuitable(message)) {
                return layouter.buildLayout(peer, message: message)
            }
        }
        
        return textLayouter.buildLayout(peer, message: message)
    }
    
}