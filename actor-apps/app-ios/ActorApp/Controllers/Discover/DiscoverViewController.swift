//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
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
        
        tableView.hidden = true
        
        // tableData = UAGrouppedTableData(tableView: tableView)
        
        view.backgroundColor = UIColor.whiteColor()
        
        execute(Actor.listPublicGroups(), successBlock: { (val) -> Void in
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
        let g = groups.getWithInt(jint(indexPath.row)) as! ACPublicGroup
        let res = PublicCell()
        res.bind(g, isLast: (indexPath.row == self.groups.size() - 1))
        return res
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        let g = groups.getWithInt(jint(indexPath.row)) as! ACPublicGroup
        confirmAlertUser("JoinAlertMessage", action: "AlertYes") { () -> () in
            let gid = g.getId()
            self.execute(Actor.joinPublicGroupCommandWithGig(g.getId(), withAccessHash: g.getAccessHash()), successBlock: { (val) -> Void in
                self.navigateNext(ConversationViewController(peer: ACPeer.groupWithInt(gid)), removeCurrent: false)
            }, failureBlock: { (val) -> Void in
                // Try to open chat, why not?
                // TODO: Better logic
                self.navigateNext(ConversationViewController(peer: ACPeer.groupWithInt(gid)), removeCurrent: false)
            })
        }
    }
}



