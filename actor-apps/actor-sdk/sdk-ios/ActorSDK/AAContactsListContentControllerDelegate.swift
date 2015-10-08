//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public protocol AAContactsListContentControllerDelegate {
    
    func willAddContacts(controller: AAContactsListContentController, section: ACManagedSection)
    
    func didAddContacts(controller: AAContactsListContentController, section: ACManagedSection)
    
    func contactDidTap(controller: AAContactsListContentController, contact: ACContact) -> Bool
    
    func contactDidBind(controller: AAContactsListContentController, contact: ACContact, cell: AAContactCell)
    
}

public extension AAContactsListContentControllerDelegate {
    
    public func willAddContacts(controller: AAContactsListContentController, section: ACManagedSection) {
        // Do Nothing
    }
    
    public func didAddContacts(controller: AAContactsListContentController, section: ACManagedSection) {
        // Do Nothing
    }
    
    public func contactDidBind(controller: AAContactsListContentController, contact: ACContact, cell: AAContactCell) {
        // Do Nothing
    }
}