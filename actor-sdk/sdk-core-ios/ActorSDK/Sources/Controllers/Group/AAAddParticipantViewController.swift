//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AAAddParticipantViewController: AAContactsListContentController, AAContactsListContentControllerDelegate {

    public init (gid: Int) {
        super.init()
        
        self.gid = gid
        self.delegate = self
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        title = AALocalized("GroupAddParticipantTitle")
        
//        navigationItem.leftBarButtonItem = UIBarButtonItem(
//                    title: AALocalized("NavigationCancel"),
//                    style: UIBarButtonItemStyle.plain,
//                    target: self, action: #selector(AAViewController.dismiss))
    }
    
    open func willAddContacts(_ controller: AAContactsListContentController, section: AAManagedSection) {
        if group.isCanInviteViaLink.get().booleanValue() {
            section.custom { (r:AACustomRow<AAContactActionCell>) -> () in
                r.height = 56
                r.closure = { (cell) -> () in
                    cell.bind("ic_invite_user", actionTitle: AALocalized("GroupAddParticipantUrl"))
                }
                r.selectAction = { () -> Bool in
                    self.navigateNext(AAInviteLinkViewController(gid: self.gid), removeCurrent: false)
                    return false
                }
            }
        }
    }
    
    open func contactDidBind(_ controller: AAContactsListContentController, contact: ACContact, cell: AAContactCell) {
        cell.bindDisabled(isAlreadyMember(contact.uid))
    }
    
    open func contactDidTap(_ controller: AAContactsListContentController, contact: ACContact) -> Bool {
        
        if !isAlreadyMember(contact.uid) {
            self.executeSafeOnlySuccess(Actor.inviteMemberCommand(withGid: jint(gid), withUid: jint(contact.uid))) { (val) -> () in
                self.dismissController()
            }
        }
        return true
    }
    
    open func isAlreadyMember(_ uid: jint) -> Bool {
        let members: [ACGroupMember] = (group.getMembersModel().get() as AnyObject).toArray().toSwiftArray()
        for m in members {
            if m.uid == uid {
                return true
            }
        }
        return false
    }
}
