//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class IntegrationViewController: AATableViewController {

    let gid: jint
    var tableData: UAGrouppedTableData!
    var currentUrl: String?
    var urlCell: UACommonCellRegion!
    
    init(gid: jint) {
        self.gid = gid
        super.init(style: UITableViewStyle.Grouped)
        
        title = NSLocalizedString("GroupIntegrationPageTitle", comment: "Integration Link Title")
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        
        tableView.hidden = true

        tableData = UAGrouppedTableData(tableView: tableView)
        
        urlCell = tableData.addSection()
            .setHeaderText(NSLocalizedString("GroupIntegrationLinkTitle", comment: "Link title"))
            .setFooterText(NSLocalizedString("GroupIntegrationLinkHint", comment: "Link hint"))
            .addCommonCell()
            .setStyle(.Normal)
        
        let section = tableData.addSection()
        
        section.addActionCell("ActionCopyLink", actionClosure: { () -> () in
                UIPasteboard.generalPasteboard().string = self.currentUrl
                self.alertUser("AlertLinkCopied")
            })
            .showBottomSeparator(15)
            .showTopSeparator(0)
        
        section.addActionCell("GroupIntegrationDoc", actionClosure: { () -> () in
                UIApplication.sharedApplication().openURL(NSURL(string: "https://actor.im/integrations")!)
            })
            .showBottomSeparator(0)
            .hideTopSeparator()
        
        tableData.addSection()
            .addActionCell("ActionRevokeLink", actionClosure: { () -> () in
                self.confirmAlertUser("GroupIntegrationLinkRevokeMessage", action: "GroupIntegrationLinkRevokeAction", tapYes: { () -> () in
                    self.reloadLink()
                })
            })
            .setStyle(.Destructive)
        
        execute(Actor.requestIntegrationTokenCommandWithGid(gid), successBlock: { (val) -> Void in
            self.currentUrl = val as? String
            self.urlCell.setContent(self.currentUrl!)
            self.tableView.hidden = false
            self.tableView.reloadData()
        }) { (val) -> Void in
            // TODO: Implement?
        }
    }
    
    func reloadLink() {
        execute(Actor.revokeIntegrationTokenCommandWithGid(jint(gid)), successBlock: { (val) -> Void in
            self.currentUrl = val as? String
            self.urlCell.setContent(self.currentUrl!)
            self.tableView.hidden = false
            self.tableView.reloadData()
        }) { (val) -> Void in
            // TODO: Implement?
        }
    }

}