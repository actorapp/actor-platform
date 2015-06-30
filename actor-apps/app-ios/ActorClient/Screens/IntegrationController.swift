//
//  IntegrationToken.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 29.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class IntegrationController: AATableViewController {

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
            .setStyle(AATableViewCellStyle.Normal)
        
        var section = tableData.addSection()
        
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
            .setStyle(AATableViewCellStyle.Destructive)
        
        execute(MSG.requestIntegrationTokenCommandWithGid(gid), successBlock: { (val) -> Void in
            self.currentUrl = val as! String
            self.urlCell.setContent(self.currentUrl!)
            self.tableView.hidden = false
            self.tableView.reloadData()
        }) { (val) -> Void in
            // TODO: Implement?
        }
    }
    
    func reloadLink() {
        execute(MSG.revokeIntegrationTokenCommandWithGid(jint(gid)), successBlock: { (val) -> Void in
            self.currentUrl = val as! String
            self.urlCell.setContent(self.currentUrl!)
            self.tableView.hidden = false
            self.tableView.reloadData()
        }) { (val) -> Void in
            // TODO: Implement?
        }
    }

}