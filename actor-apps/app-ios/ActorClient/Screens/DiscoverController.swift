//
//  PublicGroupsController.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 29.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class DiscoverController: AATableViewController {
    
    var tableData: UAGrouppedTableData!
    
    init() {
        super.init(style: UITableViewStyle.Grouped)
        
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
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func loadView() {
        super.loadView()
        
        self.extendedLayoutIncludesOpaqueBars = true
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
     
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        
        tableView.hidden = true
        
        tableData = UAGrouppedTableData(tableView: tableView)
        
        view.backgroundColor = UIColor.whiteColor()
        
       // execute(MSG., successBlock: <#((val: Any?) -> Void)?##(val: Any?) -> Void#>, failureBlock: <#((val: Any?) -> Void)?##(val: Any?) -> Void#>)
    }
}