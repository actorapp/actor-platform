//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public protocol AAContactsListContentControllerDelegate {
    
    func willAddContacts(_ controller: AAContactsListContentController, section: AAManagedSection)
    
    func didAddContacts(_ controller: AAContactsListContentController, section: AAManagedSection)
    
    func contactDidTap(_ controller: AAContactsListContentController, contact: ACContact) -> Bool
    
    func contactDidBind(_ controller: AAContactsListContentController, contact: ACContact, cell: AAContactCell)
    
}

public extension AAContactsListContentControllerDelegate {
    
    public func willAddContacts(_ controller: AAContactsListContentController, section: AAManagedSection) {
        // Do Nothing
    }
    
    public func didAddContacts(_ controller: AAContactsListContentController, section: AAManagedSection) {
        // Do Nothing
    }
    
    public func contactDidBind(_ controller: AAContactsListContentController, contact: ACContact, cell: AAContactCell) {
        // Do Nothing
    }
}
