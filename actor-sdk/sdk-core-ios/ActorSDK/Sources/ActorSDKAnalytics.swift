//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public protocol ActorSDKAnalytics {
    
    /// Called when page visible
    func analyticsPageVisible(page: ACPage)
    
    /// Called when page hidden
    func analyticsPageHidden(page: ACPage)
    
    /// Called when event occurs
    func analyticsEvent(event: ACEvent)
}