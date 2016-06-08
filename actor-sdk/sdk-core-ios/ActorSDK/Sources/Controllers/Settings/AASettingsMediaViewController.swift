//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AASettingsMediaViewController: AAContentTableController {
    
    private var sessionsCell: AAManagedArrayRows<ARApiAuthSession, AACommonCell>?
    
    public init() {
        super.init(style: AAContentTableStyle.SettingsGrouped)
        
        navigationItem.title = AALocalized("MediaTitle")
        
        //content = ACAllEvents_Settings.PRIVACY()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func tableDidLoad() {
        
        section { (s) -> () in
    
                s.common { (r) -> () in
                    
                    r.style = .Switch
                    r.content = AALocalized("SettingsAutomaticDownloadAndSave")
                    
                    r.bindAction = { (r) -> () in
                        r.switchOn = ActorSDK.sharedActor().storage.preferences.getBoolWithKey("isAutomaticDownloadEnabled", withDefault: true)
                    }
                    
                    r.switchAction = { (on: Bool) -> () in
                        ActorSDK.sharedActor().setAutomaticDownloads(on)
                    }
                }
            }
        }
}
