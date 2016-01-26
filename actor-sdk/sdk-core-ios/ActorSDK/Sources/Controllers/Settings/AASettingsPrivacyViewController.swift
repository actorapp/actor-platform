//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AASettingsPrivacyViewController: AAContentTableController {
    
    private var sessionsCell: AAManagedArrayRows<ARApiAuthSession, AACommonCell>?
    
    public init() {
        super.init(style: AAContentTableStyle.SettingsGrouped)
        
        navigationItem.title = AALocalized("SecurityTitle")
        
        content = ACAllEvents_Settings.PRIVACY()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func tableDidLoad() {
        
        section { (s) -> () in
        
            // Settings: All sessions
            s.navigate("SettingsAllSessions", controller: AASettingsSessionsController.self)
            
            // Settings: Last seen
            s.navigate("SettingsLastSeen", controller: AASettingsLastSeenController.self)
            
            s.footerText = AALocalized("SettingsLastSeenHint")
            
        }
        
    }
    
}
