//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AAAddParticipantViewController: AAContactsListContentController, AAContactsListContentControllerDelegate {

    public init (gid: Int) {
        super.init()
        
        self.gid = gid
        self.delegate = self
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        title = AALocalized("GroupAddParticipantTitle")
        
        navigationItem.leftBarButtonItem = UIBarButtonItem(
                    title: AALocalized("NavigationCancel"),
                    style: UIBarButtonItemStyle.Plain,
                    target: self, action: #selector(AAViewController.dismiss))
    }
    
    public func willAddContacts(controller: AAContactsListContentController, section: AAManagedSection) {
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
    
    public func contactDidBind(controller: AAContactsListContentController, contact: ACContact, cell: AAContactCell) {
        cell.bindDisabled(isAlreadyMember(contact.uid))
    }
    
    public func contactDidTap(controller: AAContactsListContentController, contact: ACContact) -> Bool {
        
        if !isAlreadyMember(contact.uid) {
            self.executeSafeOnlySuccess(Actor.inviteMemberCommandWithGid(jint(gid), withUid: jint(contact.uid))!) { (val) -> () in
                self.dismiss()
            }
        }
        return true
    }
    
    public func isAlreadyMember(uid: jint) -> Bool {
        let members: [ACGroupMember] = group.getMembersModel().get().toArray().toSwiftArray()
        for m in members {
            if m.uid == uid {
                return true
            }
        }
        return false
    }
}
