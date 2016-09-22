//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AADialogsListContentController: AAContentTableController, UISearchBarDelegate, UISearchDisplayDelegate {
    
    open var enableDeletion: Bool = true
    open var enableSearch: Bool = true
    
    open var delegate: AADialogsListContentControllerDelegate!
    
    public init() {
        super.init(style: .plain)
        
        unbindOnDissapear = true
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func tableDidLoad() {
        
        managedTable.canEditAll = true
        managedTable.canDeleteAll = true
        managedTable.fixedHeight = 76
        tableView.estimatedRowHeight = 76
        tableView.rowHeight = 76
        
        if enableSearch {
            search(AADialogSearchCell.self) { (s) -> () in
            
                s.searchModel = Actor.buildGlobalSearchModel()
            
                s.selectAction = { (itm) -> () in
                    self.delegate?.searchDidTap(self, entity: itm)
                }
            }
        }
        
        section { (s) -> () in
            
            s.autoSeparatorsInset = 75
            
            s.binded { (r:AABindedRows<AADialogCell>) -> () in
                
                r.differental = true
                
                r.animated = true
                
                r.displayList = Actor.getDialogsDisplayList()
                if r.displayList.getProcessor() == nil {
                   r.displayList.setListProcessor(AADialogListProcessor())
                }
                
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
                    if dialog.peer.isGroup {
                        let g = Actor.getGroupWithGid(dialog.peer.peerId)
                        let isChannel = g.groupType == ACGroupType.channel()
                        self.alertSheet({ (a) in
                            
                            // Clear History
                            if g.isCanClear.get().booleanValue() {
                                a.action(AALocalized("ActionClearHistory"), closure: {
                                    self.confirmAlertUserDanger("ActionClearHistoryMessage", action: "ActionClearHistoryAction", tapYes: {
                                        self.executeSafe(Actor.clearChatCommand(with: dialog.peer))
                                    })
                                })
                            }
                            
                            // Delete
                            if g.isCanLeave.get().booleanValue() && g.isMember.get().booleanValue() {
                                if isChannel {
                                    a.destructive(AALocalized("ActionLeaveChannel"), closure: {
                                        self.confirmAlertUserDanger("ActionLeaveChannelMessage", action: "ActionLeaveChannelAction", tapYes: {
                                            self.executePromise(Actor.leaveAndDeleteGroup(withGid: dialog.peer.peerId))
                                        })
                                    })
                                } else {
                                    a.destructive(AALocalized("ActionDeleteAndExit"), closure: {
                                        self.confirmAlertUserDanger("ActionDeleteAndExitMessage", action: "ActionDeleteAndExitAction", tapYes: {
                                            self.executePromise(Actor.leaveAndDeleteGroup(withGid: dialog.peer.peerId))
                                        })
                                    })
                                }
                            } else if g.isCanDelete.get().booleanValue()  && g.isMember.get().booleanValue(){
                                a.destructive(AALocalized(isChannel ? "ActionDeleteChannel" : "ActionDeleteGroup"), closure: {
                                    self.confirmAlertUserDanger(isChannel ? "ActionDeleteChannelMessage" : "ActionDeleteGroupMessage", action: "ActionDelete", tapYes: {
                                        self.executePromise(Actor.deleteGroup(withGid: g.groupId))
                                    })
                                })
                            } else {
                                a.destructive(AALocalized("ActionDelete"), closure: {
                                    self.confirmAlertUserDanger("ActionDeleteMessage", action: "ActionDelete", tapYes: {
                                        self.executeSafe(Actor.deleteChatCommand(with: dialog.peer))
                                    })
                                })
                            }
                            
                            // Cancel
                            a.cancel = AALocalized("ActionCancel")
                        })
                        
                    } else {
                        self.alertSheet({ (a) in
                            a.action(AALocalized("ActionClearHistory"), closure: {
                                self.executeSafe(Actor.clearChatCommand(with: dialog.peer))
                            })
                            a.destructive(AALocalized("ActionDelete"), closure: {
                                self.executeSafe(Actor.deleteChatCommand(with: dialog.peer))
                            })
                            a.cancel = AALocalized("ActionCancel")
                        })
                    }
                }
            }
        }
        
        
        
        placeholder.setImage(
            UIImage.bundled("chat_list_placeholder"),
            title: AALocalized("Placeholder_Dialogs_Title"),
            subtitle: AALocalized("Placeholder_Dialogs_Message"))
        
    }
    
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        // Binding empty dialogs placeholder
        
        binder.bind(Actor.getAppState().isDialogsEmpty, closure: { (value: Any?) -> () in
            if let empty = value as? JavaLangBoolean {
                if Bool(empty.booleanValue()) == true {
                    self.navigationItem.leftBarButtonItem = nil
                    self.showPlaceholder()
                } else {
                    self.hidePlaceholder()
                    self.navigationItem.leftBarButtonItem = self.editButtonItem
                }
            }
        })
    }
}
