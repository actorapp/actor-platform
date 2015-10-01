//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class InviteLinkViewController: ACContentTableController {

    // Data
    
    var currentUrl: String?
    
    // Rows
    
    var urlRow: ACCommonRow!
    
    init(gid: Int) {
        super.init(style: ACContentTableStyle.SettingsGrouped)
        
        self.gid = gid
        
        self.title = localized("GroupInviteLinkPageTitle")
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func tableDidLoad() {
        
        tableView.hidden = true
        
        section { (s) -> () in
            s.headerText = localized("GroupInviteLinkTitle")
            s.footerText = localized("GroupInviteLinkHint")
            
            self.urlRow = s.common { (r) -> () in
                r.bindAction = { (r) -> () in
                    r.content = self.currentUrl
                }
            }
        }
        
        section { (s) -> () in
            s.action("ActionCopyLink") { (r) -> () in
                r.selectAction = { () -> Bool in
                    UIPasteboard.generalPasteboard().string = self.currentUrl
                    self.alertUser("AlertLinkCopied")
                    return true
                }
            }
            s.action("ActionShareLink") { (r) -> () in
                r.selectAction = { () -> Bool in
                    var sharingItems = [AnyObject]()
                    sharingItems.append(self.currentUrl!)
                    let activityViewController = UIActivityViewController(activityItems: sharingItems, applicationActivities: nil)
                    self.presentViewController(activityViewController, animated: true, completion: nil)
                    return true
                }
            }
        }
        
        section { (s) -> () in
            s.danger("ActionRevokeLink") { (r) -> () in
                r.selectAction = { () -> Bool in
                    self.confirmDestructive(localized("GroupInviteLinkRevokeMessage"), action: localized("GroupInviteLinkRevokeAction"), yes: { () -> () in
                        self.reloadLink()
                    })
                    return true
                }
            }
        }
        
        executeSafe(Actor.requestInviteLinkCommandWithGid(jint(gid))) { (val) -> Void in
            self.currentUrl = val as? String
            self.urlRow.reload()
            self.tableView.hidden = false
        }
    }
    
    func reloadLink() {
        executeSafe(Actor.requestRevokeLinkCommandWithGid(jint(gid))) { (val) -> Void in
            self.currentUrl = val as? String
            self.urlRow.reload()
            self.tableView.hidden = false
        }
    }
}