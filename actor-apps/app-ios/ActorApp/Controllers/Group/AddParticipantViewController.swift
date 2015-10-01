//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class AddParticipantViewController: ContactsContentViewController, ContactsContentViewControllerDelegate {

    init (gid: Int) {
        super.init()
        
        self.gid = gid
        self.delegate = self
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = localized("GroupAddParticipantTitle")
        
        navigationItem.leftBarButtonItem = UIBarButtonItem(
                    title: localized("NavigationCancel"),
                    style: UIBarButtonItemStyle.Plain,
                    target: self, action: "dismiss")
    }
    
    func willAddContacts(controller: ContactsContentViewController, section: ACManagedSection) {
        section.custom { (r:ACCustomRow<ContactActionCell>) -> () in
            r.height = 56
            r.closure = { (cell) -> () in
                cell.bind("ic_invite_user", actionTitle: NSLocalizedString("GroupAddParticipantUrl", comment: "Action Title"))
            }
            r.selectAction = { () -> Bool in
                self.navigateNext(InviteLinkViewController(gid: self.gid), removeCurrent: false)
                return false
            }
        }
    }
    
    func contactDidBind(controller: ContactsContentViewController, contact: ACContact, cell: ContactCell) {
        cell.bindDisabled(isAlreadyMember(contact.uid))
    }
    
    func contactDidTap(controller: ContactsContentViewController, contact: ACContact) -> Bool {
        
        if !isAlreadyMember(contact.uid) {
            self.executeSafeOnlySuccess(Actor.inviteMemberCommandWithGid(jint(gid), withUid: jint(contact.uid))) { (val) -> () in
                self.dismiss()
            }
        }
        return true
    }
    
    func isAlreadyMember(uid: jint) -> Bool {
        let members: [ACGroupMember] = group.getMembersModel().get().toArray().toSwiftArray()
        for m in members {
            if m.uid == uid {
                return true
            }
        }
        return false
    }
}
