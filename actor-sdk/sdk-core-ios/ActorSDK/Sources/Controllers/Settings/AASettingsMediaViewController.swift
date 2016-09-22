//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AASettingsMediaViewController: AAContentTableController {
    
    fileprivate var sessionsCell: AAManagedArrayRows<ARApiAuthSession, AACommonCell>?
    
    public init() {
        super.init(style: AAContentTableStyle.settingsGrouped)
        
        navigationItem.title = AALocalized("MediaTitle")
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func tableDidLoad() {
        
        section { (s) -> () in
    
            s.headerText = AALocalized("MediaPhotoDownloadHeader")
            
            s.common { (r) -> () in
                r.style = .switch
                r.content = AALocalized("SettingsPrivateChats")
                
                r.switchOn = ActorSDK.sharedActor().isPhotoAutoDownloadPrivate
                
                r.switchAction = { (v) -> () in
                    ActorSDK.sharedActor().isPhotoAutoDownloadPrivate = v
                }
            }
            
            s.common { (r) -> () in
                r.style = .switch
                r.content = AALocalized("SettingsGroupChats")
                
                r.switchOn = ActorSDK.sharedActor().isPhotoAutoDownloadGroup
                
                r.switchAction = { (v) -> () in
                    ActorSDK.sharedActor().isPhotoAutoDownloadGroup = v
                }
            }
        }
        
        section { (s) -> () in
            
            s.headerText = AALocalized("MediaAudioDownloadHeader")
            
            s.common { (r) -> () in
                r.style = .switch
                r.content = AALocalized("SettingsPrivateChats")
                
                r.switchOn = ActorSDK.sharedActor().isAudioAutoDownloadPrivate
                
                r.switchAction = { (v) -> () in
                    ActorSDK.sharedActor().isAudioAutoDownloadPrivate = v
                }
            }
            
            s.common { (r) -> () in
                r.style = .switch
                r.content = AALocalized("SettingsGroupChats")
                
                r.switchOn = ActorSDK.sharedActor().isAudioAutoDownloadGroup
                
                r.switchAction = { (v) -> () in
                    ActorSDK.sharedActor().isAudioAutoDownloadGroup = v
                }
            }
        }
        
        section { (s) -> () in
            
            s.headerText = AALocalized("MediaOtherHeader")
            
            s.common { (r) -> () in
                r.style = .switch
                r.content = AALocalized("MediaAutoplayGif")
                
                r.switchOn = ActorSDK.sharedActor().isGIFAutoplayEnabled
                
                r.switchAction = { (v) -> () in
                    ActorSDK.sharedActor().isGIFAutoplayEnabled = v
                }
            }
        }
    }
}
