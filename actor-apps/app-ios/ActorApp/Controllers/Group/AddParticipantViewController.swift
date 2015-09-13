//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class AddParticipantViewController: ContactsBaseViewController {
    
    var tableView: UITableView!
    let gid: Int
    
    init (gid: Int) {
        self.gid = gid
        super.init(contentSection: 1)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        
        title = NSLocalizedString("GroupAddParticipantTitle", comment: "Participant Title")
        
        navigationItem.leftBarButtonItem = UIBarButtonItem(
            title: NSLocalizedString("NavigationCancel", comment: "Cancel"),
            style: UIBarButtonItemStyle.Plain,
            target: self, action: Selector("dismiss"))
        
        tableView = UITableView(frame: view.bounds, style: UITableViewStyle.Plain)
        tableView.backgroundColor = UIColor.whiteColor()
        view = tableView
        
        bindTable(tableView, fade: true);
        
        super.viewDidLoad();
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 2
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (section == 0) {
            return 1
        } else {
            return super.tableView(tableView, numberOfRowsInSection: section)
        }
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if (indexPath.section == 0) {
            let reuseId = "cell_invite";
            let res = ContactActionCell(reuseIdentifier: reuseId)
            res.bind("ic_invite_user",
                actionTitle: NSLocalizedString("GroupAddParticipantUrl", comment: "Action Title"),
                isLast: false)
            return res
        } else {
            return super.tableView(tableView, cellForRowAtIndexPath: indexPath)
        }
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        if indexPath.section == 0 {
            navigateNext(InviteLinkViewController(gid: gid), removeCurrent: false)
        } else {
            let contact = objectAtIndexPath(indexPath) as! ACContact;
            execute(Actor.inviteMemberCommandWithGid(jint(gid), withUid: contact.getUid()), successBlock: { (val) -> () in
                    self.dismiss()
                }, failureBlock: { (val) -> () in
                    self.dismiss()
                })
        }
    }
}
