//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class RecentContentViewController: ACContentTableController, UISearchBarDelegate, UISearchDisplayDelegate {
    
    var enableDeletion: Bool = true
    
    var delegate: RecentContentViewControllerDelegate!
    
    init() {
        super.init(style: .Plain)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func tableDidLoad() {
        
        search(DialogsSearchCell.self) { (s) -> () in
            
            s.searchList = Actor.buildSearchDisplayList()
            
            s.selectAction = { (itm) -> () in
                self.delegate?.searchDidTap(self, entity: itm)
            }
        }
        
        section { (s) -> () in
            
            s.autoSeparatorsInset = 75
            
            s.binded { (r:ACBindedRows<DialogCell>) -> () in
                
                r.displayList = Actor.getDialogsDisplayList()
                
                r.selectAction = { (dialog: ACDialog) -> Bool in
                    if let d = self.delegate {
                        return d.recentsDidTap(self, dialog: dialog)
                    }
                    return true
                }
                
                r.canEditAction = { (dialog: ACDialog) -> Bool in
                    return self.enableDeletion
                }
                
                r.editAction = { (dialog: ACDialog) -> () in
                    self.executeSafe(Actor.deleteChatCommandWithPeer(dialog.peer))
                }
            }
        }
        
        // Binding empty dialogs placeholder
        
        placeholder.setImage(
            UIImage(named: "chat_list_placeholder"),
            title: NSLocalizedString("Placeholder_Dialogs_Title", comment: "Placeholder Title"),
            subtitle: NSLocalizedString("Placeholder_Dialogs_Message", comment: "Placeholder Message"))
        
        binder.bind(Actor.getAppState().isDialogsEmpty, closure: { (value: Any?) -> () in
            if let empty = value as? JavaLangBoolean {
                if Bool(empty.booleanValue()) == true {
                    self.navigationItem.leftBarButtonItem = nil
                    self.showPlaceholder()
                } else {
                    self.hidePlaceholder()
                    self.navigationItem.leftBarButtonItem = self.editButtonItem()
                }
            }
        })
    }
}
