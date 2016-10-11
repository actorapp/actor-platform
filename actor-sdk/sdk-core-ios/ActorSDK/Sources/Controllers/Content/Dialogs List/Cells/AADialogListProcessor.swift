//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class AADialogListProcessor: NSObject, ARListProcessor {
    
    func process(withItems items: JavaUtilList, withPrevious previous: Any?) -> Any? {
        
        var uids = Set<jint>()
        
        for i in 0..<items.size() {
            let d = items.getWith(i) as! ACDialog
            
            if d.peer.isGroup {
                if d.senderId != 0 {
                    uids.insert(d.senderId)
                }
            }
            if d.relatedUid != 0 {
                uids.insert(d.relatedUid)
            }
        }
        
        for i in uids {
            Actor.getUserWithUid(i)
        }
        
        return nil
    }
}
