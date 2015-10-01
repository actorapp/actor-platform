//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

protocol ContactsContentViewControllerDelegate {
    
    func willAddContacts(controller: ContactsContentViewController, section: ACManagedSection)
    
    func didAddContacts(controller: ContactsContentViewController, section: ACManagedSection)
    
    func contactDidTap(controller: ContactsContentViewController, contact: ACContact) -> Bool
    
    func contactDidBind(controller: ContactsContentViewController, contact: ACContact, cell: ContactCell)
    
}

extension ContactsContentViewControllerDelegate {
    
    func willAddContacts(controller: ContactsContentViewController, section: ACManagedSection) {
        // Do Nothing
    }
    
    func didAddContacts(controller: ContactsContentViewController, section: ACManagedSection) {
        // Do Nothing
    }
    
    func contactDidBind(controller: ContactsContentViewController, contact: ACContact, cell: ContactCell) {
        // Do Nothing
    }
}