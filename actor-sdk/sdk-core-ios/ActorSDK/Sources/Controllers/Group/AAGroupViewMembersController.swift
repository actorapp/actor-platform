//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAGroupViewMembersController: AAContentTableController {
    
    fileprivate var membersRow: AAManagedArrayRows<ACGroupMember, AAGroupMemberCell>!
    
    fileprivate var isLoaded = false
    fileprivate var isLoading = false
    open var nextBatch: IOSByteArray! = nil
    
    public init(gid: Int) {
        super.init(style: .plain)
        self.gid = gid
        
        navigationItem.title = AALocalized("GroupViewMembers")
        
        if group.isCanInviteMembers.get().booleanValue() {
            navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.add, target: self, action: #selector(didAddPressed))
        }
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func tableDidLoad() {
        section { (s) in
            self.membersRow = s.arrays({ (r: AAManagedArrayRows<ACGroupMember, AAGroupMemberCell>) -> () in
                r.height = 48
                r.data = [ACGroupMember]()
                
                r.bindData = { (c, d) -> () in
                    let user = Actor.getUserWithUid(d.uid)
                    c.bind(user, isAdmin: d.isAdministrator)
                    
                    // Notify to request onlines
                    Actor.onUserVisible(withUid: d.uid)
                }
                
                r.itemShown = { (index, d) in
                    if index > r.data.count - 10 {
                        self.loadMore()
                    }
                }
                
                r.selectAction = { (d) -> Bool in
                    let user = Actor.getUserWithUid(d.uid)
                    if (user.getId() == Actor.myUid()) {
                        return true
                    }
                    
                    self.alertSheet { (a: AAAlertSetting) -> () in
                        
                        a.cancel = "AlertCancel"
                        
                        a.action("GroupMemberInfo") { () -> () in
                            var controller: AAViewController! = ActorSDK.sharedActor().delegate.actorControllerForUser(Int(user.getId()))
                            if controller == nil {
                                controller = AAUserViewController(uid: Int(user.getId()))
                            }
                            self.navigateNext(controller, removeCurrent: false)
                        }
                        
                        a.action("GroupMemberWrite") { () -> () in
                            if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.user(with: user.getId())) {
                                self.navigateDetail(customController)
                            } else {
                                self.navigateDetail(ConversationViewController(peer: ACPeer.user(with: user.getId())))
                            }
                            self.popover?.dismiss(animated: true)
                        }
                        
                        a.action("GroupMemberCall", closure: { () -> () in
                            let phones = user.getPhonesModel().get()!
                            if phones.size() == 0 {
                                self.alertUser("GroupMemberCallNoPhones")
                            } else if phones.size() == 1 {
                                let number = phones.getWith(0)!
                                ActorSDK.sharedActor().openUrl("telprompt://+\(number.phone)")
                            } else {
                                
                                var numbers = [String]()
                                for i in 0..<phones.size() {
                                    let p = phones.getWith(i)!
                                    numbers.append("\(p.title): +\(p.phone)")
                                }
                                self.showActionSheet(numbers,
                                    cancelButton: "AlertCancel",
                                    destructButton: nil,
                                    sourceView: self.view,
                                    sourceRect: self.view.bounds,
                                    tapClosure: { (index) -> () in
                                        if (index >= 0) {
                                            let number = phones.getWith(jint(index))!
                                            ActorSDK.sharedActor().openUrl("telprompt://+\(number.phone)")
                                        }
                                })
                            }
                        })
                        
                        // Can kick user
                        let canKick: Bool =
                            (self.group.isCanKickAnyone.get().booleanValue() ||
                                (self.group.isCanKickInvited.get().booleanValue() && d.inviterUid == Actor.myUid()))
                        
                        if canKick {
                            let name = Actor.getUserWithUid(d.uid).getNameModel().get()
                            a.destructive("GroupMemberKick") { () -> () in
                                self.confirmDestructive(AALocalized("GroupMemberKickMessage")
                                    .replace("{name}", dest: name!), action: AALocalized("GroupMemberKickAction")) {
                                        self.executeSafe(Actor.kickMemberCommand(withGid: jint(self.gid), withUid: user.getId()))
                                }
                            }
                        }
                    }
                    
                    return true
                }
            })
        }
        
        loadMore()
    }
    
    fileprivate func loadMore() {
        if isLoading {
            return
        }
        if isLoaded {
            return
        }
        
        isLoading = true
        Actor.loadMembers(withGid: jint(gid), withLimit: 20, withNext: nextBatch).then { (slice: ACGroupMembersSlice!) in
            for i in 0..<slice.members.size() {
                self.membersRow.data.append(slice.members.getWith(i) as! ACGroupMember)
            }
            self.tableView.reloadData()
            if slice.next == nil {
                self.nextBatch = nil
                self.isLoaded = true
            }
            self.isLoading = false
        }
    }
    
    func didAddPressed() {
        navigateNext(AAAddParticipantViewController(gid: self.gid))
    }
}
