//
//  ContactsViewController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 10.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit
import MessageUI

class ContactsViewController: ContactsBaseController, UISearchBarDelegate, UISearchDisplayDelegate {
    
    // MARK: -
    // MARK: Public vars
    
    @IBOutlet var rootView: UIView!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var emptyView: UIView!
    
    var searchView: UISearchBar?
    var searchDisplay: UISearchDisplayController?
    var searchSource: ContactsSource?
    
    var binder = Binder()
    
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
        
        view.backgroundColor = UIColor.whiteColor()
        
        bindTable(tableView, fade: false);
        
        searchView = UISearchBar()
        searchView!.delegate = self
        searchView!.frame = CGRectMake(0, 0, 0, 44)
        
        MainAppTheme.search.styleSearchBar(searchView!)
        
        searchDisplay = UISearchDisplayController(searchBar: searchView, contentsController: self)
        searchDisplay?.searchResultsDelegate = self
        searchDisplay?.searchResultsTableView.rowHeight = 56
        searchDisplay?.searchResultsTableView.separatorStyle = UITableViewCellSeparatorStyle.None
        searchDisplay?.searchResultsTableView.backgroundColor = Resources.BackyardColor
        searchDisplay?.searchResultsTableView.frame = tableView.frame
        
        tableView.tableHeaderView = searchView
        
        searchSource = ContactsSource(searchDisplay: searchDisplay!)
        
        emptyView.hidden = true;
    
        super.viewDidLoad();
        
        navigationItem.title = NSLocalizedString("TabPeople", comment: "People Title")
        navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Add, target: self, action: "doAddContact")
        
        placeholder.setImage(nil, title: "Empty", subtitle: "Your contact list is empty. You can add coworkers by pressing top right button or invite them by pressing button below.", actionTitle: "Invite coworkers", actionTarget: self, actionSelector: Selector("showSmsInvitation"))
        binder.bind(MSG.getAppState().getIsContactsEmpty(), closure: { (value: Any?) -> () in
            if let empty = value as? JavaLangBoolean {
                if Bool(empty.booleanValue()) == true {
                    self.showPlaceholder()
                } else {
                    self.hidePlaceholder()
                }
            }
        })
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        var selected = tableView.indexPathForSelectedRow();
        if (selected != nil){
            tableView.deselectRowAtIndexPath(selected!, animated: animated);
        }
        
        if (searchDisplay != nil && searchDisplay!.active){
            MainAppTheme.search.applyStatusBar()
        }
    }
    
    // MARK: -
    // MARK: Methods
    
    func initCommon(){
        
        var title = "";
        if (MainAppTheme.tab.showText) {
            title = NSLocalizedString("TabPeople", comment: "People Title")
        }
        
        tabBarItem = UITabBarItem(title: title,
            image: UIImage(named: "ic_people_outline")!
                .tintImage(Resources.BarTintUnselectedColor)
                .imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal),
            selectedImage: UIImage(named: "ic_people_filled")!
                .tintImage(Resources.BarTintColor)
                .imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal));
        
        if (!MainAppTheme.tab.showText) {
            tabBarItem.imageInsets = UIEdgeInsetsMake(6, 0, -6, 0);
        }
    }
    
    func doAddContact() {
        var alertView = UIAlertView(title: "Add Contact", message: "Please, specify phone number", delegate: nil, cancelButtonTitle: "Cancel")
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.show()
    }
    
    // MARK: -
    // MARK: UITableView Delegate
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        var contact: AMContact!;
        
        if (tableView == self.tableView) {
            contact = objectAtIndexPath(indexPath) as! AMContact
        } else {
            contact = searchSource!.objectAtIndexPath(indexPath) as! AMContact
        }
        
        navigateToMessagesWithUid(contact.getUid())
    }
    
    // MARK: -
    // MARK: Navigation
    
    func showSmsInvitation() {
        if MFMessageComposeViewController.canSendText() {
            let messageComposeController = MFMessageComposeViewController()
            messageComposeController.messageComposeDelegate = self
            messageComposeController.body = "Hi! Let's switch to Connector! https://actor.im/mdl" // TODO: Localize
            presentViewController(messageComposeController, animated: true, completion: nil)
        } else {
            UIAlertView(title: "Error", message: "Cannot send SMS", delegate: nil, cancelButtonTitle: "OK") // TODO: Show or not to show?
        }
    }
    
    private func navigateToMessagesWithUid(uid: jint) {
        let conversationController = AAConversationController(peer: AMPeer.userWithInt(uid))
        conversationController.hidesBottomBarWhenPushed = true
        navigationController?.pushViewController(conversationController, animated: true);
        MainAppTheme.navigation.applyStatusBar()
    }
    
}

// MARK: -
// MARK: MFMessageComposeViewController Delegate

extension ContactsViewController: MFMessageComposeViewControllerDelegate {
    
    func messageComposeViewController(controller: MFMessageComposeViewController!, didFinishWithResult result: MessageComposeResult) {
        controller.dismissViewControllerAnimated(true, completion: nil)
    }
    
}