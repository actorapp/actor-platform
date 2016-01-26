//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public protocol AADialogsListContentControllerDelegate {
    
    func recentsDidTap(controller: AADialogsListContentController, dialog: ACDialog) -> Bool
    
    func searchDidTap(controller: AADialogsListContentController, entity: ACSearchEntity)
}