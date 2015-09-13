//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit
import MessageUI

class ContactsViewController: ContactsBaseViewController, UISearchBarDelegate, UISearchDisplayDelegate {
    
    // MARK: -
    // MARK: Public vars
    
    var tableView: UITableView!
    
    var searchView: UISearchBar?
    var searchDisplay: UISearchDisplayController?
    var searchSource: ContactsSearchSource?
    
    var binder = Binder()
    
    // MARK: -
    // MARK: Constructors
    
    init() {
        super.init(contentSection: 1)
        
        var title = "";
        if (MainAppTheme.tab.showText) {
            title = NSLocalizedString("TabPeople", comment: "People Title")
        }
        
        tabBarItem = UITabBarItem(title: title,
            image: MainAppTheme.tab.createUnselectedIcon("TabIconContacts"),
            selectedImage: MainAppTheme.tab.createSelectedIcon("TabIconContactsHighlighted"));
        
        if (!MainAppTheme.tab.showText) {
            tabBarItem.imageInsets = UIEdgeInsetsMake(6, 0, -6, 0);
        }
        
        tableView = UITableView()
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.rowHeight = 76
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        self.extendedLayoutIncludesOpaqueBars = true
        view.addSubview(tableView)
        view.backgroundColor = MainAppTheme.list.bgColor
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        self.extendedLayoutIncludesOpaqueBars = true
        
        bindTable(tableView, fade: false);
        
        searchView = UISearchBar()
        searchView!.delegate = self
        searchView!.frame = CGRectMake(0, 0, 0, 44)
        searchView!.keyboardAppearance = MainAppTheme.common.isDarkKeyboard ? UIKeyboardAppearance.Dark : UIKeyboardAppearance.Light
        
        MainAppTheme.search.styleSearchBar(searchView!)
        
        searchDisplay = UISearchDisplayController(searchBar: searchView!, contentsController: self)
        searchDisplay?.searchResultsDelegate = self
        searchDisplay?.searchResultsTableView.rowHeight = 56
        searchDisplay?.searchResultsTableView.separatorStyle = UITableViewCellSeparatorStyle.None
        searchDisplay?.searchResultsTableView.backgroundColor = Resources.BackyardColor
        searchDisplay?.searchResultsTableView.frame = tableView.frame
        
        let header = TableViewHeader(frame: CGRectMake(0, 0, 320, 44))
        header.addSubview(searchView!)
        tableView.tableHeaderView = header
        
        searchSource = ContactsSearchSource(searchDisplay: searchDisplay!)
        
        super.viewDidLoad();
        
        navigationItem.title = NSLocalizedString("TabPeople", comment: "People Title")
        navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Add, target: self, action: "doAddContact")
        
        placeholder.setImage(
            UIImage(named: "contacts_list_placeholder"),
            title:  NSLocalizedString("Placeholder_Contacts_Title", comment: "Placeholder Title"),
            subtitle: NSLocalizedString("Placeholder_Contacts_Message", comment: "Placeholder Message"),
            actionTitle: NSLocalizedString("Placeholder_Contacts_Action", comment: "Placeholder Action"),
            subtitle2: NSLocalizedString("Placeholder_Contacts_Message2", comment: "Placeholder Message2"),
            actionTarget: self, actionSelector: Selector("showSmsInvitation"),
            action2title: nil,
            action2Selector: nil)
        binder.bind(Actor.getAppState().getIsContactsEmpty(), closure: { (value: Any?) -> () in
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
        
        let selected = tableView.indexPathForSelectedRow
        if (selected != nil){
            tableView.deselectRowAtIndexPath(selected!, animated: animated);
        }
        
        // SearchBar hack
        let searchBar = searchDisplay!.searchBar
        let superView = searchBar.superview
        if !(superView is UITableView) {
            searchBar.removeFromSuperview()
            superView?.addSubview(searchBar)
        }
        
        // Header hack
        tableView.tableHeaderView?.setNeedsLayout()
        tableView.tableFooterView?.setNeedsLayout()
        
        if (searchDisplay != nil && searchDisplay!.active){
            MainAppTheme.search.applyStatusBar()
        } else {
            MainAppTheme.navigation.applyStatusBar()
        }
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        tableView.frame = CGRectMake(0, 0, view.frame.width, view.frame.height)
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        searchDisplay?.setActive(false, animated: animated)
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 2
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (section == 1) {
            return super.tableView(tableView, numberOfRowsInSection: section)
        } else {
            return 2
        }
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if (indexPath.section == 1) {
            return super.tableView(tableView, cellForRowAtIndexPath: indexPath)
        } else {
            if (indexPath.row == 1) {
                let reuseId = "cell_invite";
                let res = ContactActionCell(reuseIdentifier: reuseId)
                res.bind("ic_add_user",
                    actionTitle: NSLocalizedString("ContactsActionAdd", comment: "Action Title"),
                    isLast: false)
                return res
            } else {
                let reuseId = "cell_add";
                let res = ContactActionCell(reuseIdentifier: reuseId)
                res.bind("ic_invite_user",
                    actionTitle: NSLocalizedString("ContactsActionInvite", comment: "Action Title"),
                    isLast: false)
                return res
            }
        }
    }
    
    // MARK: -
    // MARK: Methods
    
    
    func doAddContact() {
        let alertView = UIAlertView(
            title: NSLocalizedString("ContactsAddHeader", comment: "Alert Title"),
            message: NSLocalizedString("ContactsAddHint", comment: "Alert Hint"),
            delegate: self,
            cancelButtonTitle: NSLocalizedString("AlertCancel", comment: "Alert Cancel"),
            otherButtonTitles: NSLocalizedString("AlertNext", comment: "Alert Next"))
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.textFieldAtIndex(0)!.keyboardAppearance = MainAppTheme.common.isDarkKeyboard ? UIKeyboardAppearance.Dark : UIKeyboardAppearance.Light
        alertView.show()
    }
    
    func showSmsInvitation() {
        showSmsInvitation(nil)
    }
    
    // MARK: -
    // MARK: UITableView Delegate
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        if (tableView == self.tableView && indexPath.section == 0) {
            if (indexPath.row == 0) {
                showSmsInvitation()
            } else {
                doAddContact()
            }
            let selected = tableView.indexPathForSelectedRow
            if (selected != nil){
                tableView.deselectRowAtIndexPath(selected!, animated: true);
            }
            return
        }
        
        var contact: ACContact!;
        
        if (tableView == self.tableView) {
            contact = objectAtIndexPath(indexPath) as! ACContact
        } else {
            contact = searchSource!.objectAtIndexPath(indexPath) as! ACContact
        }
        
        navigateToMessagesWithUid(contact.getUid())
    }
    
    // MARK: -
    // MARK: Navigation
    
    func showSmsInvitation(recipients: [String]?) {
        if MFMessageComposeViewController.canSendText() {
            let messageComposeController = MFMessageComposeViewController()
            messageComposeController.messageComposeDelegate = self
            messageComposeController.body =  NSLocalizedString("InviteText", comment: "Invite Text")
            messageComposeController.recipients = recipients
            messageComposeController.navigationBar.tintColor = MainAppTheme.navigation.titleColor
            presentViewController(messageComposeController, animated: true, completion: { () -> Void in
                MainAppTheme.navigation.applyStatusBarFast()
            })
        } else {
             // TODO: Show or not to show?
            UIAlertView(title: "Error", message: "Cannot send SMS", delegate: nil, cancelButtonTitle: "OK").show()
        }
    }
    
    private func navigateToMessagesWithUid(uid: jint) {
        let conversationController = ConversationViewController(peer: ACPeer.userWithInt(uid))
        navigateDetail(conversationController)
        MainAppTheme.navigation.applyStatusBar()
    }    
}

// MARK: -
// MARK: MFMessageComposeViewController Delegate

extension ContactsViewController: MFMessageComposeViewControllerDelegate {
    func messageComposeViewController(controller: MFMessageComposeViewController, didFinishWithResult result: MessageComposeResult) {
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
            if textField.text?.length > 0 {
                execute(Actor.findUsersCommandWithQuery(textField.text), successBlock: { (val) -> () in
                    var user: ACUserVM?
                    user = val as? ACUserVM
                    if user == nil {
                        if let users = val as? IOSObjectArray {
                            if Int(users.length()) > 0 {
                                if let tempUser = users.objectAtIndex(0) as? ACUserVM {
                                    user = tempUser
                                }
                            }
                        }
                    }
                    if user != nil {
                        self.execute(Actor.addContactCommandWithUid(user!.getId()), successBlock: { (val) -> () in
                            self.navigateToMessagesWithUid(user!.getId())
                            }, failureBlock: { (val) -> () in
                                self.showSmsInvitation([textField.text!])
                        })
                    } else {
                        self.showSmsInvitation([textField.text!])
                    }
                    }, failureBlock: { (val) -> () in
                        self.showSmsInvitation([textField.text!])
                })
            }
        }
    }
}