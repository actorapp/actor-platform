//
//  InviteLinkController.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 08.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class InviteLinkController: AATableViewController {

    let gid: Int
    var tableData: UAGrouppedTableData!
    var currentUrl: String?
    var urlCell: UACommonCellRegion!
    
    init(gid: Int) {
        self.gid = gid
        super.init(style: UITableViewStyle.Grouped)
        
        title = NSLocalizedString("GroupInviteLinkPageTitle", comment: "Invite Link Title")
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
            .setHeaderText(NSLocalizedString("GroupInviteLinkTitle", comment: "Link title"))
            .setFooterText(NSLocalizedString("GroupInviteLinkHint", comment: "Link hint"))
            .addCommonCell()
            .setStyle(AATableViewCellStyle.Normal)
        
        var section = tableData.addSection()

        section.addActionCell("GroupInviteLinkActionCopy", actionClosure: { () -> () in
                UIPasteboard.generalPasteboard().string = self.currentUrl
                self.alertUser("GroupInviteLinkCopied")
            })
            .showBottomSeparator(15)
            .showTopSeparator(0)
        
        section.addActionCell("GroupInviteLinkActionShare", actionClosure: { () -> () in
                UIApplication.sharedApplication().openURL(NSURL(string: self.currentUrl!)!)
            })
            .hideTopSeparator()
            .showBottomSeparator(0)
        
        tableData.addSection()
            .addActionCell("GroupInviteLinkActionRevoke", actionClosure: { () -> () in
                self.confirmAlertUser("GroupInviteLinkRevokeMessage", action: "GroupInviteLinkRevokeAction", tapYes: { () -> () in
                    self.reloadLink()
                })
            })
            .setStyle(AATableViewCellStyle.Destructive)
        
        
        execute(MSG.requestInviteLinkCommandWithGid(jint(gid)), successBlock: { (val) -> Void in
                self.currentUrl = val as! String
                self.urlCell.setContent(self.currentUrl!)
                self.tableView.hidden = false
                self.tableView.reloadData()
            }) { (val) -> Void in
                // TODO: Implement
        }
    }
    
    func reloadLink() {
        execute(MSG.requestRevokeLinkCommandWithGid(jint(gid)), successBlock: { (val) -> Void in
                self.currentUrl = val as! String
                self.urlCell.setContent(self.currentUrl!)
                self.tableView.hidden = false
                self.tableView.reloadData()
            }) { (val) -> Void in
                // TODO: Implement
        }
    }
}