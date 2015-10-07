//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

public class AAComposeController: AAContactsContentViewController, AAContactsContentViewControllerDelegate {

    public override init() {
        super.init()
        
        self.delegate = self
        self.isSearchAutoHide = false
        
        self.navigationItem.title = localized("ComposeTitle")
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func willAddContacts(controller: AAContactsContentViewController, section: ACManagedSection) {
        section.custom { (r:ACCustomRow<AAContactActionCell>) -> () in
            
            r.height = 56
            
            r.closure = { (cell) -> () in
                cell.bind("ic_add_user", actionTitle: localized("CreateGroup"))
            }
            
            r.selectAction = { () -> Bool in
                self.navigateNext(AAGroupCreateViewController(), removeCurrent: true)
                return false
            }
        }
    }
    
    public func contactDidTap(controller: AAContactsContentViewController, contact: ACContact) -> Bool {
        // navigateNext(ConversationViewController(peer: ACPeer.userWithInt(contact.uid)), removeCurrent: true)
        return false
    }
}