//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAGroupTypeViewController: AAContentTableController {
    
    private let isCreation: Bool
    private var isChannel: Bool = false
    private var isPublic: Bool = false
    private var linkSection: AAManagedSection!
    private var publicRow: AACommonRow!
    private var privateRow: AACommonRow!
    private var shortNameRow: AAEditRow!
    
    public init(gid: Int, isCreation: Bool) {
        self.isCreation = isCreation
        super.init(style: .SettingsGrouped)
        self.gid = gid
        self.isChannel = group.groupType == ACGroupType.CHANNEL()
        if (isChannel) {
            navigationItem.title = AALocalized("GroupTypeTitleChannel")
        } else {
            navigationItem.title = AALocalized("GroupTypeTitle")
        }
        if isCreation {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationNext"), style: .Done, target: self, action: #selector(saveDidTap))
        } else {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationSave"), style: .Done, target: self, action: #selector(saveDidTap))
        }
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func tableDidLoad() {
        
        self.isPublic = group.shortName.get() != nil

        section { (s) in

            if isChannel {
                s.headerText = AALocalized("GroupTypeTitleChannel").uppercaseString
                if self.isPublic {
                    s.footerText = AALocalized("GroupTypeHintPublicChannel")
                } else {
                    s.footerText = AALocalized("GroupTypeHintPrivateChannel")
                }
            } else {
                s.headerText = AALocalized("GroupTypeTitle").uppercaseString
                if self.isPublic {
                    s.footerText = AALocalized("GroupTypeHintPublic")
                } else {
                    s.footerText = AALocalized("GroupTypeHintPrivate")
                }
            }
            self.publicRow = s.common({ (r) in
                if isChannel {
                    r.content = AALocalized("ChannelTypePublicFull")
                } else {
                    r.content = AALocalized("GroupTypePublicFull")
                }
                
                r.selectAction = { () -> Bool in
                    if !self.isPublic {
                        self.isPublic = true
                        self.publicRow.rebind()
                        self.privateRow.rebind()
                        if self.isChannel {
                            s.footerText = AALocalized("GroupTypeHintPublicChannel")
                        } else {
                            s.footerText = AALocalized("GroupTypeHintPublic")
                        }
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
                if isChannel {
                    r.content = AALocalized("ChannelTypePrivateFull")
                } else {
                    r.content = AALocalized("GroupTypePrivateFull")
                }
                
                r.selectAction = { () -> Bool in
                    if self.isPublic {
                        self.isPublic = false
                        self.publicRow.rebind()
                        self.privateRow.rebind()
                        if self.isChannel {
                            s.footerText = AALocalized("GroupTypeHintPrivateChannel")
                        } else {
                            s.footerText = AALocalized("GroupTypeHintPrivate")
                        }
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
            if self.isChannel {
                s.footerText = AALocalized("GroupTypeLinkHintChannel")
            } else {
                s.footerText = AALocalized("GroupTypeLinkHint")
            }
            
            self.shortNameRow = s.edit({ (r) in
                r.autocapitalizationType = .None
                r.prefix = ActorSDK.sharedActor().invitePrefixShort
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
            if let shortNameVal = self.shortNameRow.text?.trim() {
                if shortNameVal.length > 0 {
                    nShortName = shortNameVal
                } else {
                    nShortName = nil
                }
            } else {
                nShortName = nil
            }
        } else {
            nShortName = nil
        }
        
        if nShortName != group.shortName.get() {
            executePromise(Actor.editGroupShortNameWithGid(jint(self.gid), withAbout: nShortName).then({ (r:ARVoid!) in
                if (self.isCreation) {
                    if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.groupWithInt(jint(self.gid))) {
                        self.navigateDetail(customController)
                    } else {
                        self.navigateDetail(ConversationViewController(peer: ACPeer.groupWithInt(jint(self.gid))))
                    }
                    self.dismiss()
                } else {
                    self.navigateBack()
                }
            }))
        } else {
            if (isCreation) {
                if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.groupWithInt(jint(self.gid))) {
                    self.navigateDetail(customController)
                } else {
                    self.navigateDetail(ConversationViewController(peer: ACPeer.groupWithInt(jint(self.gid))))
                }
                self.dismiss()
            } else {
                navigateBack()
            }
        }
    }
}