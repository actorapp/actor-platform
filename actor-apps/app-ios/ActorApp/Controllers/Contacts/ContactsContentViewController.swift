//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class ContactsContentViewController: ACContentTableController {
    
    var delegate: ContactsContentViewControllerDelegate?
    var isSearchAutoHide: Bool = true
    var contactRows: ACBindedRows<ContactCell>!
    var searchEnabled: Bool = true
    
    init() {
        super.init(style: .Plain)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func tableDidLoad() {
        
        if searchEnabled {
            search(ContactCell.self) { (s) -> () in
            
                s.searchList = Actor.buildContactsDisplayList()
            
                s.isSearchAutoHide = self.isSearchAutoHide
            
                s.selectAction = { (contact) -> () in
                    if let d = self.delegate {
                        d.contactDidTap(self, contact: contact)
                    }
                }
            }
        }
        
        section { (s) -> () in
            
            s.autoSeparatorsInset = 80
            
            if let d = self.delegate {
                d.willAddContacts(self, section: s)
            }
            
            self.contactRows = s.binded { (r: ACBindedRows<ContactCell>) -> () in
                r.displayList = Actor.buildContactsDisplayList()
                
                r.selectAction = { (contact) -> Bool in
                    if let d = self.delegate {
                        return d.contactDidTap(self, contact: contact)
                    }
                    return true
                }
                
                r.didBind = { (cell, contact) -> () in
                    if let d = self.delegate {
                        return d.contactDidBind(self, contact: contact, cell: cell)
                    }
                }
            }
            
            if let d = self.delegate {
                d.didAddContacts(self, section: s)
            }
        }
        
        placeholder.setImage(
            UIImage(named: "contacts_list_placeholder"),
            title:  NSLocalizedString("Placeholder_Contacts_Title", comment: "Placeholder Title"),
            subtitle: NSLocalizedString("Placeholder_Contacts_Message", comment: "Placeholder Message"),
            actionTitle: NSLocalizedString("Placeholder_Contacts_Action", comment: "Placeholder Action"),
            subtitle2: NSLocalizedString("Placeholder_Contacts_Message2", comment: "Placeholder Message2"),
            actionTarget: self, actionSelector: Selector("showSmsInvitation"),
            action2title: nil,
            action2Selector: nil)
        binder.bind(Actor.getAppState().isContactsEmpty, closure: { (value: Any?) -> () in
            if let empty = value as? JavaLangBoolean {
                if Bool(empty.booleanValue()) == true {
                    self.showPlaceholder()
                } else {
                    self.hidePlaceholder()
                }
            }
        })
    }
}