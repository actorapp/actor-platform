//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public protocol AADialogsListContentControllerDelegate {
    
    func recentsDidTap(_ controller: AADialogsListContentController, dialog: ACDialog) -> Bool
    
    func searchDidTap(_ controller: AADialogsListContentController, entity: ACSearchResult)
}
