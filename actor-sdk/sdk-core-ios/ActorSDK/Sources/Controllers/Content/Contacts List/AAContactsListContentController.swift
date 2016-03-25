//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAContactsListContentController: AAContentTableController {
    
    public var delegate: AAContactsListContentControllerDelegate?
    public var isSearchAutoHide: Bool = true
    public var contactRows: AABindedRows<AAContactCell>!
    public var searchEnabled: Bool = true
    
    public init() {
        super.init(style: .Plain)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func tableDidLoad() {
        
        if searchEnabled {
            search(AAContactCell.self) { (s) -> () in
            
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
            
            self.contactRows = s.binded { (r: AABindedRows<AAContactCell>) -> () in
                
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
            UIImage.bundled("contacts_list_placeholder"),
            title:  AALocalized("Placeholder_Contacts_Title"),
            subtitle: AALocalized("Placeholder_Contacts_Message").replace("{appname}", dest: ActorSDK.sharedActor().appName),
            actionTitle: AALocalized("Placeholder_Contacts_Action"),
            subtitle2: AALocalized("Placeholder_Contacts_Message2"),
            actionTarget: self, actionSelector: Selector("showSmsInvitation"),
            action2title: nil,
            action2Selector: nil)
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
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