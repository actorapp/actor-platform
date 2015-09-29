//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import Crashlytics

class CocoaAnalytics {
    
    private let internalAnalytics: ACActorAnalytics
    
    init(internalAnalytics: ACActorAnalytics) {
        self.internalAnalytics = internalAnalytics
    }
    
    func trackPageVisible(page: ACPage) {

        // Notify Fabric stats
        // TODO: Pass all ids
        Answers.logContentViewWithName(page.getContentTypeDisplay(), contentType: page.getContentType(), contentId: page.getContentId(), customAttributes: nil)
        
        // Notify internal stats
        internalAnalytics.trackContentVisibleWithACPage(page)
    }
    
    func trackPageHidden(page: ACPage) {

        // Unsupported in Fabric
        
        // Notify internal stats
        internalAnalytics.trackContentHiddenWithACPage(page)
    }
    
    func track(event: ACEvent) {

        // Notify Fabric stats
        // TODO: Pass all ids
        Answers.logCustomEventWithName(event.getActionType(), customAttributes: ["type": event.getActionId()])
        
        // Notify internal stats
        internalAnalytics.trackEventWithACEvent(event)
    }
}