//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class AAAuthViewController: AAViewController {
    
    /// Call this method when authentication successful
    public func onAuthenticated() {
        ActorSDK.sharedActor().didLoggedIn()
    }
}