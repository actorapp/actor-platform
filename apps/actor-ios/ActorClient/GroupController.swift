//
//  GroupControllerViewController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 23.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class GroupController: UIViewController {

    let gid: Int
    var group: AMGroupVM?;
    var binder = Binder()
    
    init(gid:Int) {
        self.gid = gid
        super.init(nibName: "GroupController", bundle: nil)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    
    override func viewDidLoad() {
        super.viewDidLoad()

        group = MSG.getGroups().getWithLong(jlong(gid)) as! AMGroupVM;
        
        binder.bind(group!.getName()!, closure: { (value:String?) -> () in
            self.navigationItem.title = value
        })
    }
}
