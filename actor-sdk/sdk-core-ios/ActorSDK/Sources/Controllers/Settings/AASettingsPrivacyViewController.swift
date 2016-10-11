//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AASettingsPrivacyViewController: AAContentTableController {
    
    fileprivate var sessionsCell: AAManagedArrayRows<ARApiAuthSession, AACommonCell>?
    
    public init() {
        super.init(style: AAContentTableStyle.settingsGrouped)
        
        navigationItem.title = AALocalized("PrivacyTitle")
        
        content = ACAllEvents_Settings.privacy()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func tableDidLoad() {
        
        section { (s) -> () in
            
            s.headerText = AALocalized("PrivacyHeader")
            
            // Settings: Last seen
            s.navigate("PrivacyLastSeen", controller: AASettingsLastSeenController.self)
            
            s.footerText = AALocalized("PrivacyLastSeenHint")
        }
        
        section { (s) -> () in
            
            s.headerText = AALocalized("PrivacySecurityHeader")
            
            // Settings: All sessions
            s.navigate("PrivacyAllSessions", controller: AASettingsSessionsController.self)
        }
    }
    
}
