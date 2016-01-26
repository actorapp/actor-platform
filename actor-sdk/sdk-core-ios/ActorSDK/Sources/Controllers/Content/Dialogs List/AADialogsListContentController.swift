//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AADialogsListContentController: AAContentTableController, UISearchBarDelegate, UISearchDisplayDelegate {
    
    public var enableDeletion: Bool = true
    public var enableSearch: Bool = true
    
    public var delegate: AADialogsListContentControllerDelegate!
    
    public init() {
        super.init(style: .Plain)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func tableDidLoad() {
        
        tableView.estimatedRowHeight = 76
        
        if enableSearch {
            search(AADialogSearchCell.self) { (s) -> () in
            
                s.searchList = Actor.buildSearchDisplayList()
            
                s.selectAction = { (itm) -> () in
                    self.delegate?.searchDidTap(self, entity: itm)
                }
            }
        }
        
        section { (s) -> () in
            
            s.autoSeparatorsInset = 75
            
            s.binded { (r:AABindedRows<AADialogCell>) -> () in
                
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
            UIImage.bundled("chat_list_placeholder"),
            title: AALocalized("Placeholder_Dialogs_Title"),
            subtitle: AALocalized("Placeholder_Dialogs_Message"))
        
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
