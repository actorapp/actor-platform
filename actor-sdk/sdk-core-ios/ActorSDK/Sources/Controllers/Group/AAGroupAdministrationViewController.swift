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
            s.footerText = "Control what is possible in this group"
            self.shortNameRow = s.common({ (r) in
                
                if (self.isChannel) {
                    r.content = AALocalized("GroupTypeTitleChannel")
                } else {
                    r.content = AALocalized("GroupTypeTitle")
                }
                
                r.bindAction = { (r) in
                    if self.group.shortName.get() != nil {
                        r.hint = "Public"
                    } else {
                        r.hint = "Private"
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
                s.footerText = "All members will see all messages"
                self.shareHistoryRow = s.common({ (r) in
                    r.content = "Share History"
                    r.bindAction = { (r) in
                        if self.group.isHistoryShared.get().booleanValue() {
                            r.hint = "Shared"
                        } else {
                            r.hint = nil
                        }
                    }
                    
                })
            }
        }
        
        if group.isCanDelete.get().booleanValue() {
            section { (s) in
                s.footerText = "You will lose all messages in this group"
                s.danger("Delete Group", closure: { (r) in
                    r.selectAction = { () -> Bool in
                        self.executePromise(Actor.deleteGroupWithGid(jint(self.gid))).after {
                            let first = self.navigationController!.viewControllers.first!
                            self.navigationController!.setViewControllers([first], animated: true)
                        }
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