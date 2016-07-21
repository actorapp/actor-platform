//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAGroupTypeViewController: AAContentTableController {
    
    private var isChannel: Bool = false
    private var isPublic: Bool = false
    private var linkSection: AAManagedSection!
    private var publicRow: AACommonRow!
    private var privateRow: AACommonRow!
    private var shortNameRow: AAEditRow!
    
    public init(gid: Int) {
        super.init(style: .SettingsGrouped)
        self.gid = gid
        self.isChannel = group.groupType == ACGroupType.CHANNEL()
        if (isChannel) {
            navigationItem.title = AALocalized("GroupTypeTitleChannel")
        } else {
            navigationItem.title = AALocalized("GroupTypeTitle")
        }
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationSave"), style: .Done, target: self, action: #selector(saveDidTap))
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func tableDidLoad() {
        
        self.isPublic = group.shortName.get() != nil

        section { (s) in

            s.headerText = "Group Type".uppercaseString
            if self.isPublic {
                s.footerText = "Public groups can be found in search and anyone can joing"
            } else {
                s.footerText = "Private groups can be joined only via personal invitation"
            }
            self.publicRow = s.common({ (r) in
                r.content = "Public Group"
                r.selectAction = { () -> Bool in
                    if !self.isPublic {
                        self.isPublic = true
                        self.publicRow.rebind()
                        self.privateRow.rebind()
                        s.footerText = "Public groups can be found in search and anyone can joing"
                        self.tableView.reloadSection(0, withRowAnimation: .Automatic)
                        self.managedTable.sections.append(self.linkSection)
                        self.tableView.insertSection(1, withRowAnimation: .Fade)
                    }
                    return true
                }
                r.bindAction = { (r) in
                    if self.isPublic {
                        r.style = .Checkmark
                    } else {
                        r.style = .Normal
                    }
                }
            })
            
            self.privateRow = s.common({ (r) in
                r.content = "Private Group"
                r.selectAction = { () -> Bool in
                    if self.isPublic {
                        self.isPublic = false
                        self.publicRow.rebind()
                        self.privateRow.rebind()
                        s.footerText = "Private groups can be joined only via personal invitation"
                        self.tableView.reloadSection(0, withRowAnimation: .Automatic)
                        self.managedTable.sections.removeAtIndex(1)
                        self.tableView.deleteSection(1, withRowAnimation: .Fade)
                    }
                    return true
                }
                r.bindAction = { (r) in
                    if !self.isPublic {
                        r.style = .Checkmark
                    } else {
                        r.style = .Normal
                    }
                }
            })
        }
        
        self.linkSection = section { (s) in
            s.footerText = "People can share this link with others and find your channel using search."
            self.shortNameRow = s.edit({ (r) in
                r.autocapitalizationType = .None
                r.prefix = "actor.im/join/"
                r.text = self.group.shortName.get()
            })
        }
        if !self.isPublic {
            managedTable.sections.removeAtIndex(1)
        }
    }
    
    public func saveDidTap() {
        let nShortName: String?
        if self.isPublic {
            if self.shortNameRow.text!.trim().length > 0 {
                nShortName = self.shortNameRow.text!.trim()
            } else {
                nShortName = nil
            }
        } else {
            nShortName = nil
        }
        
        if nShortName != group.shortName.get() {
            executePromise(Actor.editGroupShortNameWithGid(jint(self.gid), withAbout: nShortName).then({ (r:ARVoid!) in
                self.navigateBack()
            }))
        } else {
            navigateBack()
        }
    }
}