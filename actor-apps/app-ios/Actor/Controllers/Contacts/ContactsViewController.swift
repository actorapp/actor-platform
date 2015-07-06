//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
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
        super.init(contentSection: 0)
        
        var title = "";
        if (MainAppTheme.tab.showText) {
            title = NSLocalizedString("TabPeople", comment: "People Title")
        }
        
        tabBarItem = UITabBarItem(title: title,
            image: MainAppTheme.tab.createUnselectedIcon("ic_people_outline"),
            selectedImage: MainAppTheme.tab.createSelectedIcon("ic_people_filled"));
        
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
        
        searchDisplay = UISearchDisplayController(searchBar: searchView, contentsController: self)
        searchDisplay?.searchResultsDelegate = self
        searchDisplay?.searchResultsTableView.rowHeight = 56
        searchDisplay?.searchResultsTableView.separatorStyle = UITableViewCellSeparatorStyle.None
        searchDisplay?.searchResultsTableView.backgroundColor = Resources.BackyardColor
        searchDisplay?.searchResultsTableView.frame = tableView.frame
        
        var header = TableViewHeader(frame: CGRectMake(0, 0, 320, 44))
        header.addSubview(searchView!)
        
//        var headerShadow = UIImageView(frame: CGRectMake(0, -4, 320, 4));
//        headerShadow.image = UIImage(named: "CardTop2");
//        headerShadow.contentMode = UIViewContentMode.ScaleToFill;
//        header.addSubview(headerShadow);
        
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
        
        // SearchBar hack
        var searchBar = searchDisplay!.searchBar
        var superView = searchBar.superview
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
        if (section == 0) {
            return super.tableView(tableView, numberOfRowsInSection: section)
        } else {
            return 2
        }
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if (indexPath.section == 0) {
            return super.tableView(tableView, cellForRowAtIndexPath: indexPath)
        } else {
            if (indexPath.row == 1) {
                let reuseId = "cell_invite";
                var res = ContactActionCell(reuseIdentifier: reuseId)
                res.bind("ic_add_user",
                    actionTitle: NSLocalizedString("ContactsActionAdd", comment: "Action Title"),
                    isLast: true)
                return res
            } else {
                let reuseId = "cell_add";
                var res = ContactActionCell(reuseIdentifier: reuseId)
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
        var alertView = UIAlertView(
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
        
        if (tableView == self.tableView && indexPath.section == 1) {
            if (indexPath.row == 0) {
                showSmsInvitation()
            } else {
                doAddContact()
            }
            var selected = tableView.indexPathForSelectedRow();
            if (selected != nil){
                tableView.deselectRowAtIndexPath(selected!, animated: true);
            }
            return
        }
        
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
    
    func showSmsInvitation(recipients: [AnyObject]?) {
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
        let conversationController = ConversationViewController(peer: AMPeer.userWithInt(uid))
        navigateDetail(conversationController)
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
                execute(MSG.findUsersCommandWithQuery(textField.text), successBlock: { (val) -> () in
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
                        self.execute(MSG.addContactCommandWithUid(user!.getId()), successBlock: { (val) -> () in
                            self.navigateToMessagesWithUid(user!.getId())
                            }, failureBlock: { (val) -> () in
                                self.showSmsInvitation([textField.text])
                        })
                    } else {
                        self.showSmsInvitation([textField.text])
                    }
                    }, failureBlock: { (val) -> () in
                        self.showSmsInvitation([textField.text])
                })
            }
        }
    }
}