//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public protocol AAContactsContentViewControllerDelegate {
    
    func willAddContacts(controller: AAContactsContentViewController, section: ACManagedSection)
    
    func didAddContacts(controller: AAContactsContentViewController, section: ACManagedSection)
    
    func contactDidTap(controller: AAContactsContentViewController, contact: ACContact) -> Bool
    
    func contactDidBind(controller: AAContactsContentViewController, contact: ACContact, cell: AAContactCell)
    
}

public extension AAContactsContentViewControllerDelegate {
    
    public func willAddContacts(controller: AAContactsContentViewController, section: ACManagedSection) {
        // Do Nothing
    }
    
    public func didAddContacts(controller: AAContactsContentViewController, section: ACManagedSection) {
        // Do Nothing
    }
    
    public func contactDidBind(controller: AAContactsContentViewController, contact: ACContact, cell: AAContactCell) {
        // Do Nothing
    }
}