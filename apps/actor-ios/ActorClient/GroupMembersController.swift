//
//  GroupMembersController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 23.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class GroupMembersController: ContactsBaseController, VENTokenFieldDataSource, VENTokenFieldDelegate {

    @IBOutlet weak var tokenField: UIView!
    @IBOutlet weak var contactsTable: UITableView!
    var tokenFieldView: VENTokenField?;
    var selectedNames: Array<AMContact> = []
    
    override init() {
        super.init(nibName: "GroupMembersController", bundle: nil)
        
        navigationItem.title = "Group Members";
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "Next", style: UIBarButtonItemStyle.Done, target: self, action: "doNext")
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        tokenFieldView = VENTokenField(frame: CGRectMake(0, 0, tokenField.frame.width, tokenField.frame.height))
        tokenFieldView!.delegate = self
        tokenFieldView!.dataSource = self
        tokenFieldView!.maxHeight = 96
        
        view.addSubview(tokenFieldView!)
        
        bindTable(contactsTable)
        
        super.viewDidLoad()
    }
    
    func doNext() {
        
    }
    
    func tokenField(tokenField: VENTokenField!, didChangeText text: String!) {
        filter(text)
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        var contact = objectAtIndexPath(indexPath) as! AMContact
        selectedNames.append(contact)
        tokenFieldView?.reloadData()
        
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
    }
    
    func tokenField(tokenField: VENTokenField!, didDeleteTokenAtIndex index: UInt) {
        self.selectedNames.removeAtIndex(Int(index))
        tokenFieldView?.reloadData()
    }
    
    func tokenField(tokenField: VENTokenField!, titleForTokenAtIndex index: UInt) -> String! {
        return self.selectedNames[Int(index)].getName()
    }
    
    func tokenFieldCollapsedText(tokenField: VENTokenField!) -> String! {
        return "selected \(self.selectedNames.count)"
    }
    
    func numberOfTokensInTokenField(tokenField: VENTokenField!) -> UInt {
        return UInt(self.selectedNames.count)
    }
}
