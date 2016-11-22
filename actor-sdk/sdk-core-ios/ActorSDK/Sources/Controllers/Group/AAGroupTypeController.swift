//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAGroupTypeViewController: AAContentTableController {
    
    fileprivate let isCreation: Bool
    fileprivate var isChannel: Bool = false
    fileprivate var isPublic: Bool = false
    fileprivate var linkSection: AAManagedSection!
    fileprivate var publicRow: AACommonRow!
    fileprivate var privateRow: AACommonRow!
    fileprivate var shortNameRow: AAEditRow!
    
    public init(gid: Int, isCreation: Bool) {
        self.isCreation = isCreation
        super.init(style: .settingsGrouped)
        self.gid = gid
        self.isChannel = group.groupType == ACGroupType.channel()
        if (isChannel) {
            navigationItem.title = AALocalized("GroupTypeTitleChannel")
        } else {
            navigationItem.title = AALocalized("GroupTypeTitle")
        }
        if isCreation {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationNext"), style: .done, target: self, action: #selector(saveDidTap))
        } else {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationSave"), style: .done, target: self, action: #selector(saveDidTap))
        }
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func tableDidLoad() {
        
        self.isPublic = group.shortName.get() != nil

        section { (s) in

            if isChannel {
                s.headerText = AALocalized("GroupTypeTitleChannel").uppercased()
                if self.isPublic {
                    s.footerText = AALocalized("GroupTypeHintPublicChannel")
                } else {
                    s.footerText = AALocalized("GroupTypeHintPrivateChannel")
                }
            } else {
                s.headerText = AALocalized("GroupTypeTitle").uppercased()
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
                        self.tableView.reloadSection(0, with: .automatic)
                        self.managedTable.sections.append(self.linkSection)
                        self.tableView.insertSection(1, with: .fade)
                    }
                    return true
                }
                r.bindAction = { (r) in
                    if self.isPublic {
                        r.style = .checkmark
                    } else {
                        r.style = .normal
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
                        self.tableView.reloadSection(0, with: .automatic)
                        self.managedTable.sections.remove(at: 1)
                        self.tableView.deleteSection(1, with: .fade)
                    }
                    return true
                }
                r.bindAction = { (r) in
                    if !self.isPublic {
                        r.style = .checkmark
                    } else {
                        r.style = .normal
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
                r.autocapitalizationType = .none
                r.prefix = ActorSDK.sharedActor().invitePrefixShort
                r.text = self.group.shortName.get()
            })
        }
        if !self.isPublic {
            managedTable.sections.remove(at: 1)
        }
    }
    
    open func saveDidTap() {
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
            executePromise(Actor.editGroupShortName(withGid: jint(self.gid), withAbout: nShortName).then({ (r:ARVoid!) in
                if (self.isCreation) {
                    if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.group(with: jint(self.gid))) {
                        self.navigateDetail(customController)
                    } else {
                        self.navigateDetail(ConversationViewController(peer: ACPeer.group(with: jint(self.gid))))
                    }
                    self.dismissController()
                } else {
                    self.navigateBack()
                }
            }))
        } else {
            if (isCreation) {
                if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.group(with: jint(self.gid))) {
                    self.navigateDetail(customController)
                } else {
                    self.navigateDetail(ConversationViewController(peer: ACPeer.group(with: jint(self.gid))))
                }
                self.dismissController()
            } else {
                navigateBack()
            }
        }
    }
}
