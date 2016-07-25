//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAGroupAdministrationViewController: AAContentTableController {
    
    private var isChannel: Bool = false
    private var shortNameRow: AACommonRow!
    private var shareHistoryRow: AACommonRow!
    
    public init(gid: Int) {
        super.init(style: .SettingsGrouped)
        self.gid = gid
        self.isChannel = group.groupType == ACGroupType.CHANNEL()
        navigationItem.title = AALocalized("GroupAdministration")
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func tableDidLoad() {
        
        section { (s) in
            if isChannel {
                s.footerText = AALocalized("GroupPermissionsHintChannel")
            } else {
                s.footerText = AALocalized("GroupPermissionsHint")
            }
            self.shortNameRow = s.common({ (r) in
                
                if (self.isChannel) {
                    r.content = AALocalized("GroupTypeTitleChannel")
                } else {
                    r.content = AALocalized("GroupTypeTitle")
                }
                
                r.bindAction = { (r) in
                    if self.group.shortName.get() != nil {
                        if self.isChannel {
                            r.hint = AALocalized("ChannelTypePublic")
                        } else {
                            r.hint = AALocalized("GroupTypePublic")
                        }
                    } else {
                        if self.isChannel {
                            r.hint = AALocalized("ChannelTypePrivate")
                        } else {
                            r.hint = AALocalized("GroupTypePrivate")
                        }
                    }
                }
                
                if group.isCanEditAdministration.get().booleanValue() {
                    r.style = .Navigation
                    r.selectAction = { () -> Bool in
                        self.navigateNext(AAGroupTypeViewController(gid: self.gid, isCreation: false))
                        return false
                    }
                }
            })
        }
        
        if group.isCanEditAdministration.get().booleanValue() && !isChannel {
            section { (s) in
                s.footerText = AALocalized("GroupShareHint")
                self.shareHistoryRow = s.common({ (r) in
                    r.content = AALocalized("GroupShareTitle")
                    r.bindAction = { (r) in
                        if self.group.isHistoryShared.get().booleanValue() {
                            r.hint = AALocalized("GroupShareEnabled")
                            r.selectAction = nil
                        } else {
                            r.hint = nil
                            r.selectAction = { () -> Bool in
                                self.confirmAlertUser("GroupShareMessage", action: "GroupShareAction", tapYes: { 
                                    self.executePromise(Actor.shareHistoryWithGid(jint(self.gid)))  
                                })
                                return true
                            }
                        }
                    }
                })
            }
        }
        
        if group.isCanDelete.get().booleanValue() {
            section { (s) in
                let action: String
                if isChannel {
                    action = AALocalized("ActionDeleteChannel")
                    s.footerText = AALocalized("GroupDeleteHintChannel")
                } else {
                    action = AALocalized("ActionDeleteGroup")
                    s.footerText = AALocalized("GroupDeleteHint")
                }
                s.danger(action, closure: { (r) in
                    r.selectAction = { () -> Bool in
                        self.confirmAlertUserDanger(self.isChannel ? "ActionDeleteChannelMessage" : "ActionDeleteGroupMessage", action: "ActionDelete", tapYes: {
                            self.executePromise(Actor.deleteGroupWithGid(jint(self.gid))).after {
                                let first = self.navigationController!.viewControllers.first!
                                self.navigationController!.setViewControllers([first], animated: true)
                            }
                        })
                        return true
                    }
                })
            }
        }
    }
    
    public override func tableWillBind(binder: AABinder) {
        
        binder.bind(self.group.shortName) { (value: String!) in
            if let row = self.shortNameRow {
                row.reload()
            }
        }
        
        binder.bind(self.group.isHistoryShared) { (value: JavaLangBoolean!) in
            if let row = self.shareHistoryRow {
                row.reload()
            }
        }
    }
}