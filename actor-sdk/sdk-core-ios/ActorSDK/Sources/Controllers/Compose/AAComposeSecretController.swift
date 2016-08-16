//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AAComposeSecretController: AAContactsListContentController, AAContactsListContentControllerDelegate {
    
    public override init() {
        super.init()
        
        self.delegate = self
        self.isSearchAutoHide = false
        
        self.navigationItem.title = AALocalized("ComposeTitle")
        
        if AADevice.isiPad {
            self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: UIBarButtonItemStyle.Plain, target: self, action: #selector(AAViewController.dismiss))
        }
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func contactDidTap(controller: AAContactsListContentController, contact: ACContact) -> Bool {
        let peer = ACPeer_secretWithInt_(contact.uid)
        if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(peer) {
            navigateDetail(customController)
        } else {
            navigateDetail(ConversationViewController(peer: peer))
        }
        return false
    }
}