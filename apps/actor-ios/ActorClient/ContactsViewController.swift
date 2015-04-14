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
        
        placeholder.setImage(
            UIImage(named: "contacts_list_placeholder"),
            title:  NSLocalizedString("Placeholder_Contacts_Title", comment: "Placeholder Title"),
            subtitle: NSLocalizedString("Placeholder_Contacts_Message", comment: "Placeholder Message"),
            actionTitle: NSLocalizedString("Placeholder_Contacts_Action", comment: "Placeholder Action"),
            actionTarget: self, actionSelector: Selector("showSmsInvitation"))
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
        } else {
            MainAppTheme.navigation.applyStatusBar()
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
        var alertView = UIAlertView(
            title: NSLocalizedString("ContactsAddHeader", comment: "Alert Title"),
            message: NSLocalizedString("ContactsAddHint", comment: "Alert Hint"),
            delegate: self,
            cancelButtonTitle: NSLocalizedString("AlertCancel", comment: "Alert Cancel"),
            otherButtonTitles: NSLocalizedString("AlertNext", comment: "Alert Next"))
        
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
            messageComposeController.body =  NSLocalizedString("InviteText", comment: "Invite Text")
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

// MARK: -
// MARK: UIAlertView Delegate

extension ContactsViewController: UIAlertViewDelegate {
    
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        // TODO: Localize
        if buttonIndex == 1 {
            let textField = alertView.textFieldAtIndex(0)!
            if count(textField.text) > 0 {
                execute(MSG.findUsersWithNSString(textField.text), successBlock: { (val) -> () in
                    println("\(val.self)")
                    var user: AMUserVM?
                    user = val as? AMUserVM
                    if user == nil {
                        if let users = val as? IOSObjectArray {
                            if Int(users.length()) > 0 {
                                if let tempUser = users.objectAtIndex(0) as? AMUserVM {
                                    user = tempUser
                                }
                            }
                        }
                    }
                    if user != nil {
                        self.execute(MSG.addContactWithInt(user!.getId()), successBlock: { (val) -> () in
                            self.navigateToMessagesWithUid(user!.getId())
                            }, failureBlock: { (val) -> () in
                                UIAlertView(title: "Error", message: "Cannot add user with this phone number", delegate: self, cancelButtonTitle: "Cancel").show()
                        })
                    } else {
                        UIAlertView(title: "Error", message: "Cannot find user with this phone number", delegate: self, cancelButtonTitle: "Cancel").show()
                    }
                    }, failureBlock: { (val) -> () in
                        UIAlertView(title: "Error", message: "Cannot find user with this phone number", delegate: self, cancelButtonTitle: "Cancel").show()
                })
            }
        }
    }
}