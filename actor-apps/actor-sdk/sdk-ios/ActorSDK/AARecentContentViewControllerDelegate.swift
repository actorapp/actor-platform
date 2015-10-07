//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public protocol AARecentContentViewControllerDelegate {
    
    func recentsDidTap(controller: AARecentContentViewController, dialog: ACDialog) -> Bool
    
    func searchDidTap(controller: AARecentContentViewController, entity: ACSearchEntity)
}