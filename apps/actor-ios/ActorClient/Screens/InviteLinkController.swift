//
//  InviteLinkController.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 08.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class InviteLinkController: AATableViewController {

    var tableData: UATableData!
    
    init() {
       super.init(style: UITableViewStyle.Plain)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        
        tableData = UATableData(tableView: tableView)
        tableData.addSection()
            .addCommonCell()
            .setStyle(AATableViewCellStyle.Hint)
            .setContent("Share this link with anyone in your business network and easily start chat")
    }
}