//
//  AAAddParticipantController.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 4/4/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AAAddParticipantController: ContactsBaseController {
    
    // MARK: -
    // MARK: Private vars
    
    private var tableView: UITableView!
    
    // MARK: -
    // MARK: Public vars
    
    let gid: Int
    
    // MARK: -
    // MARK: Constructors
    
    init (gid: Int) {
        self.gid = gid
        
        // TODO: Parse array of current members so they are fade out
        
        super.init()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        title = "Contacts" // TODO: Localize
        
        tableView = UITableView(frame: view.bounds, style: UITableViewStyle.Plain)
        view = tableView
        
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: "Cancel", style: UIBarButtonItemStyle.Plain, target: self, action: Selector("dismiss")) // TODO: Localize
        
        bindTable(tableView, fade: true);
        super.viewDidLoad();
    }
    
    // MARK: -
    // MARK: UITableView Delegate
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        var contact = objectAtIndexPath(indexPath) as! AMContact;
        
        execute(MSG.addMemberToGroupWithInt(jint(gid), withInt: contact.getUid()), successBlock: { (val) -> () in
            self.dismiss()
            }, failureBlock: { (val) -> () in
                self.dismiss()
        })
    }

}
