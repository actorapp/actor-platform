//
//  InterestsController.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 23.05.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class ALInterestsController: AATableViewController {
    
    private let CellIdentifier = "CellIdentifier"
    var interests: [APInterest] = []
    
    init() {
        super.init(style: UITableViewStyle.Plain)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func loadView() {
        super.loadView()
        
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        tableView.registerClass(AATableViewCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.reloadData()
        tableView.clipsToBounds = false
        tableView.tableFooterView = UIView()
        
        navigationItem.title = "Pick Your Interests"
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "Done", style: UIBarButtonItemStyle.Done, target: self, action: "done")
        
        dispatch_async(dispatch_get_main_queue(), { () -> Void in
                    self.execute(MSG.executeExternalCommand(APRequestGetAvailableInterests()), successBlock: { (val) -> Void in
                        var response = val as! APResponseGetAvailableInterests
                        var rootInterests = response.getRootInterests()
                        NSLog("Loaded items: \(rootInterests.size())")
                        self.interests = []
                        for i in 0..<rootInterests.size() {
                            var item = rootInterests.getWithInt(i) as! APInterest
                            self.interests.append(item)
                            NSLog("Item: \(item.getTitle())")
                        }
                        self.tableView.reloadData()
                        if (rootInterests.size() == 0) {
                            self.dismissViewControllerAnimated(true, completion: nil)
                        }
                    }) { (val) -> Void in
                        self.dismissViewControllerAnimated(true, completion: nil)
                    }
        })
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var interest = interests[indexPath.row]
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.setContent(interest.getTitle())
        cell.setSwitcherOn(interest.isSelected())
        
        cell.switchBlock = { (value: Bool) -> () in
            var list = JavaUtilArrayList()
            list.addWithId(JavaLangInteger(int: interest.getId()))
            var serialized = APRequestEnableInterests(javaUtilList: list).toByteArray().toNSData();
            if (value) {
                self.execute(MSG.executeExternalCommand(APRequestEnableInterests(javaUtilList: list)), successBlock: { (val) -> Void in
                    
                }, failureBlock: { (val) -> Void in
                    cell.setSwitcherOn(false, animated: true)
                })
            } else {
                self.execute(MSG.executeExternalCommand(APRequestDisableInterests(javaUtilList: list)), successBlock: { (val) -> Void in
                    
                    }, failureBlock: { (val) -> Void in
                        cell.setSwitcherOn(true, animated: true)
                })
            }
        }
        
        cell.setLeftInset(15.0)
        
        if (indexPath.row == 0) {
           cell.showTopSeparator()
        } else {
            cell.hideTopSeparator()
        }
        
        cell.showBottomSeparator()

        return cell
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return interests.count
    }
    
    func done() {
        self.dismissViewControllerAnimated(true, completion: nil)
    }
}