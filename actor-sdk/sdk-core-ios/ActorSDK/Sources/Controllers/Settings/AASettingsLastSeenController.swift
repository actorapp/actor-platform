//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

public class AASettingsLastSeenController: AAContentTableController {

    private var sessionsCell: AAManagedArrayRows<ARApiAuthSession, AACommonCell>?
    
    public init() {
        super.init(style: AAContentTableStyle.SettingsGrouped)
        
        navigationItem.title = AALocalized("SettingsLastSeen")
        
        content = ACAllEvents_Settings.PRIVACY()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func tableDidLoad() {
        
        section { (s) -> () in
            
            s.common({ (r) -> () in
                r.content = "Everybody"
                
                r.selectAction = {
                    
                    print("tap in Everybody")
                    return true
                }
                
            })
            
            s.common({ (r) -> () in
                r.content = "My Contacts"
                
                r.selectAction = {
                    
                    print("tap in My Contacts")
                    return true
                }
                
            })
            
            s.common({ (r) -> () in
                r.content = "Nobody"
                
                r.selectAction = {
                    
                    print("tap in Nobody")
                    return true
                }
                
            })
            
            
        }
        
    }
    

}
