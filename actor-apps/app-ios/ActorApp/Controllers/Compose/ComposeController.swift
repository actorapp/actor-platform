//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class ComposeController: ContactsContentViewController, ContactsContentViewControllerDelegate {

    override init() {
        super.init()
        
        self.delegate = self
        self.isSearchAutoHide = false
        
        self.navigationItem.title = localized("ComposeTitle")
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func willAddContacts(controller: ContactsContentViewController, section: ACManagedSection) {
        section.custom { (r:ACCustomRow<ContactActionCell>) -> () in
            
            r.height = 56
            
            r.closure = { (cell) -> () in
                cell.bind("ic_add_user", actionTitle: localized("CreateGroup"))
            }
            
            r.selectAction = { () -> Bool in
                self.navigateNext(GroupCreateViewController(), removeCurrent: true)
                return false
            }
        }
    }
    
    func contactDidTap(controller: ContactsContentViewController, contact: ACContact) -> Bool {
        navigateNext(ConversationViewController(peer: ACPeer.userWithInt(contact.uid)), removeCurrent: true)
        return false
    }
}