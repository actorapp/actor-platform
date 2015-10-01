//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

protocol RecentContentViewControllerDelegate {
    
    func recentsDidTap(controller: RecentContentViewController, dialog: ACDialog) -> Bool
    
    func searchDidTap(controller: RecentContentViewController, entity: ACSearchEntity)
}