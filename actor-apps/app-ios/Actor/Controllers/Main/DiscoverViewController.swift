//
//  PublicGroupsController.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 29.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class DiscoverViewController: AATableViewController {
    
    var tableData: UAGrouppedTableData!
    var groups: JavaUtilList!
    
    init() {
        super.init(style: UITableViewStyle.Plain)
        
        var title = "";
        if (MainAppTheme.tab.showText) {
            title = NSLocalizedString("TabDiscover", comment: "Discover Title")
        }
        
        tabBarItem = UITabBarItem(title: title,
            image: MainAppTheme.tab.createUnselectedIcon("ic_discover_outlined"),
            selectedImage: MainAppTheme.tab.createSelectedIcon("ic_discover"))
        
        if (!MainAppTheme.tab.showText) {
            tabBarItem.imageInsets = UIEdgeInsetsMake(6, 0, -6, 0);
        }
        
        navigationItem.title = NSLocalizedString("TabDiscover", comment: "Discover Title")
        extendedLayoutIncludesOpaqueBars = true
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
     
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        
        var footer = AATableViewHeader(frame: CGRectMake(0, 0, 320, 80));
        
//        var footerHint = UILabel(frame: CGRectMake(0, 0, 320, 60));
//        footerHint.textAlignment = NSTextAlignment.Center;
//        footerHint.font = UIFont.systemFontOfSize(16);
//        footerHint.textColor = MainAppTheme.list.hintColor
//        footerHint.text = NSLocalizedString("DialogsHint", comment: "Swipe hint")
//        footer.addSubview(footerHint);
        
        var shadow = UIImageView(image: UIImage(named: "CardBottom2"));
        shadow.frame = CGRectMake(0, 0, 320, 4);
        shadow.contentMode = UIViewContentMode.ScaleToFill;
        footer.addSubview(shadow);
        
        self.tableView.tableFooterView = footer;
        
        // tableView.tableHeaderView = nil
        
        tableView.hidden = true
        
        // tableData = UAGrouppedTableData(tableView: tableView)
        
        view.backgroundColor = UIColor.whiteColor()
        
        execute(MSG.listPublicGroups(), successBlock: { (val) -> Void in
            self.groups = val as! JavaUtilList
            self.tableView.hidden = false
            self.tableView.reloadData()
        }) { (val) -> Void in
            
        }
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return 104
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if self.groups == nil {
            return 0
        }
        
        return Int(self.groups.size())
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var g = groups.getWithInt(jint(indexPath.row)) as! AMPublicGroup
        var res = PublicCell()
        res.bind(g, isLast: (indexPath.row == self.groups.size() - 1))
        return res
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        var g = groups.getWithInt(jint(indexPath.row)) as! AMPublicGroup
        confirmAlertUser("JoinAlertMessage", action: "AlertYes") { () -> () in
            var gid = g.getId()
            self.execute(MSG.joinPublicGroupCommandWithGig(g.getId(), withAccessHash: g.getAccessHash()), successBlock: { (val) -> Void in
                self.navigateNext(ConversationController(peer: AMPeer.groupWithInt(gid)), removeCurrent: false)
            }, failureBlock: { (val) -> Void in
                // Try to open chat, why not?
                // TODO: Better logic
                self.navigateNext(ConversationController(peer: AMPeer.groupWithInt(gid)), removeCurrent: false)
            })
        }
    }
}



