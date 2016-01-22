//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AAComposeController: AAContactsListContentController, AAContactsListContentControllerDelegate {

    public override init() {
        super.init()
        
        self.delegate = self
        self.isSearchAutoHide = false
        
        self.navigationItem.title = AALocalized("ComposeTitle")
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func willAddContacts(controller: AAContactsListContentController, section: AAManagedSection) {
        section.custom { (r:AACustomRow<AAContactActionCell>) -> () in
            
            r.height = 56
            
            r.closure = { (cell) -> () in
                cell.bind("ic_add_user", actionTitle: AALocalized("CreateGroup"))
            }
            
            r.selectAction = { () -> Bool in
                self.navigateNext(AAGroupCreateViewController(), removeCurrent: true)
                return false
            }
        }
    }
    
    public func contactDidTap(controller: AAContactsListContentController, contact: ACContact) -> Bool {
        navigateDetail(ConversationViewController(peer: ACPeer.userWithInt(contact.uid)))
        return false
    }
}