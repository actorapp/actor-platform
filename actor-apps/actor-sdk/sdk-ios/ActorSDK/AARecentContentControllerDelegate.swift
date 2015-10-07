//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public protocol AARecentContentControllerDelegate {
    
    func recentsDidTap(controller: AARecentContentController, dialog: ACDialog) -> Bool
    
    func searchDidTap(controller: AARecentContentController, entity: ACSearchEntity)
}