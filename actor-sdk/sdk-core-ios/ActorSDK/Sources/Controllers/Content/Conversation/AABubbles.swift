//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
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
    static let videoLayouter = AABubbleVideoCellLayouter()
    
    static let builtInLayouters: [AABubbleLayouter] = [
        serviceLayouter,
        videoLayouter,
        mediaLayouter,
        voiceLayouter,
        stickerLayouter,
        documentLayouter,
        locationLayouter,
        contactLayouter,
        textLayouter
    ]
    
    static var layouters: [AABubbleLayouter] = builtInLayouters
    
    class func layouterForMessage(message: ACMessage) -> AABubbleLayouter {
        for layouter in layouters {
            if layouter.isSuitable(message) {
                return layouter
            }
        }
        return textLayouter
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