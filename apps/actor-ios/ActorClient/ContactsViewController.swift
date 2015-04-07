//
//  ContactsViewController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 10.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class ContactsViewController: ContactsBaseController {
    
    // MARK: -
    // MARK: Public vars
    
    @IBOutlet var rootView: UIView!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var emptyView: UIView!
    
    // MARK: -
    // MARK: Constructors
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder);
        initCommon();
    }
    
    override init() {
        super.init(nibName: "ContactsViewController", bundle: nil)
        initCommon();
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        bindTable(tableView);
        
        emptyView.hidden = true;
    
        super.viewDidLoad();
        
        navigationItem.title = "People";
        navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Add, target: self, action: "doAddContact")
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        var selected = tableView.indexPathForSelectedRow();
        if (selected != nil){
            tableView.deselectRowAtIndexPath(selected!, animated: animated);
        }
    }
    
    // MARK: -
    // MARK: Methods
    
    func initCommon(){
        var icon = UIImage(named: "ic_users_blue_24")!;
        tabBarItem = UITabBarItem(title: nil,
            image: icon.tintImage(Resources.BarTintUnselectedColor)
                .imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal),
            selectedImage: icon);
        tabBarItem.imageInsets=UIEdgeInsetsMake(6, 0, -6, 0);
    }
    
    func doAddContact() {
        var alertView = UIAlertView(title: "Add Contact", message: "Please, specify phone number", delegate: nil, cancelButtonTitle: "Cancel")
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.show()
    }
    
    // MARK: -
    // MARK: UITableView Delegate
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        var contact = objectAtIndexPath(indexPath) as! AMContact;
        navigateToMessagesWithUid(contact.getUid())
    }
    
    // MARK: -
    // MARK: Navigation
    
    private func navigateToMessagesWithUid(uid: jint) {
        let messagesController = MessagesViewController(peer: AMPeer.userWithInt(uid))
        messagesController.hidesBottomBarWhenPushed = true
        navigationController?.pushViewController(messagesController, animated: true);
    }
    
}
