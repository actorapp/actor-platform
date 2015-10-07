//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class CocoaAnalytics {
    
    private let internalAnalytics: ACActorAnalytics
    
    public init(internalAnalytics: ACActorAnalytics) {
        self.internalAnalytics = internalAnalytics
    }
    
    public func trackPageVisible(page: ACPage) {

        // Notify Fabric stats
        // TODO: Pass all ids
//        Answers.logContentViewWithName(page.getContentTypeDisplay(), contentType: page.getContentType(), contentId: page.getContentId(), customAttributes: nil)
        
        // Notify internal stats
        internalAnalytics.trackContentVisibleWithACPage(page)
    }
    
    public func trackPageHidden(page: ACPage) {

        // Unsupported in Fabric
        
        // Notify internal stats
        internalAnalytics.trackContentHiddenWithACPage(page)
    }
    
    public func track(event: ACEvent) {

        // Notify Fabric stats
        // TODO: Pass all ids
//        Answers.logCustomEventWithName(event.getActionType(), customAttributes: ["type": event.getActionId()])
        
        // Notify internal stats
        internalAnalytics.trackEventWithACEvent(event)
    }
}