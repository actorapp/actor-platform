//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import MessageUI

class MainTabViewController : UITabBarController {
    
    // MARK: -
    // MARK: Private vars
    
    private var appEmptyContainer = UIView()
    private var appIsSyncingPlaceholder = BigPlaceholderView(topOffset: 44 + 20)
    private var appIsEmptyPlaceholder = BigPlaceholderView(topOffset: 44 + 20)

    // MARK: -
    // MARK: Public vars
    
    var centerButton:UIButton? = nil
    var isInited = false
    var isAfterLogin = false
    
    // MARK: -
    // MARK: Constructors
    
    init(isAfterLogin: Bool) {
        super.init(nibName: nil, bundle: nil);
        self.isAfterLogin = isAfterLogin
        self.preferredContentSize = CGSize(width: 320.0, height: 600.0)
        self.preferredContentSize = CGSize(width: 320.0, height: 600.0)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("Not implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        appEmptyContainer.hidden = true
        appIsEmptyPlaceholder.hidden = true
        appIsEmptyPlaceholder.setImage(
            UIImage(named: "contacts_list_placeholder"),
            title: NSLocalizedString("Placeholder_Empty_Title", comment: "Placeholder Title"),
            subtitle: NSLocalizedString("Placeholder_Empty_Message", comment: "Placeholder Message"),
            actionTitle: NSLocalizedString("Placeholder_Empty_Action", comment: "Placeholder Action"),
            subtitle2: NSLocalizedString("Placeholder_Empty_Message2", comment: "Placeholder Message2"),
            actionTarget: self, actionSelector: Selector("showSmsInvitation"),
            action2title: NSLocalizedString("Placeholder_Empty_Action2", comment: "Placeholder Action2"),
            action2Selector: Selector("doAddContact"))
        appEmptyContainer.addSubview(appIsEmptyPlaceholder)
        
        appIsSyncingPlaceholder.hidden = true
        appIsSyncingPlaceholder.setImage(
            UIImage(named: "chat_list_placeholder"),
            title: NSLocalizedString("Placeholder_Loading_Title", comment: "Placeholder Title"),
            subtitle: NSLocalizedString("Placeholder_Loading_Message", comment: "Placeholder Message"))
        appEmptyContainer.addSubview(appIsSyncingPlaceholder)
        
        view.addSubview(appEmptyContainer)
        view.backgroundColor = UIColor.whiteColor()
    }
    
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if (!isInited) {
            if (Actor.isLoggedIn()) {
                isInited = true
                
                let contactsNavigation = AANavigationController(rootViewController: ContactsViewController())
                let dialogsNavigation = AANavigationController(rootViewController: DialogsViewController())
                let settingsNavigation = AANavigationController(rootViewController: SettingsViewController())
                
                viewControllers = [contactsNavigation, dialogsNavigation, settingsNavigation]

                selectedIndex = 0;
                selectedIndex = 1;
            }
        }
    }
    
    // MARK: -
    // MARK: Methods
    
    func centerButtonTap() {
//        var actionShit = ABActionShit()
//        actionShit.buttonTitles = ["Add Contact", "Create group", "Write to..."];
//        actionShit.delegate = self
//        actionShit.showWithCompletion(nil)
    }
    
    // MARK: -
    // MARK: ABActionShit Delegate
    
//    func actionShit(actionShit: ABActionShit!, clickedButtonAtIndex buttonIndex: Int) {
//        if (buttonIndex == 1) {
//            navigationController?.pushViewController(GroupMembersController(), animated: true)
//        }
//    }
    
    // MARK: -
    // MARK: Placeholder
    
    func showAppIsSyncingPlaceholder() {
        appIsEmptyPlaceholder.hidden = true
        appIsSyncingPlaceholder.hidden = false
        appEmptyContainer.hidden = false
    }

    func showAppIsEmptyPlaceholder() {
        appIsEmptyPlaceholder.hidden = false
        appIsSyncingPlaceholder.hidden = true
        appEmptyContainer.hidden = false
    }
    
    func hidePlaceholders() {
        appEmptyContainer.hidden = true
    }
    
    func showSmsInvitation(phone: String?) {
        if MFMessageComposeViewController.canSendText() {

            // Silently ignore if not configured
            if AppConfig.appInviteUrl == nil {
                return
            }
            
            let messageComposeController = MFMessageComposeViewController()
            messageComposeController.messageComposeDelegate = self
            if (phone != nil) {
                 messageComposeController.recipients = [phone!]
            }
            messageComposeController.body = localized("InviteText")
                .replace("{link}", dest: AppConfig.appInviteUrl!)
            messageComposeController.navigationBar.tintColor = MainAppTheme.navigation.titleColor
            presentViewController(messageComposeController, animated: true, completion: { () -> Void in
                MainAppTheme.navigation.applyStatusBarFast()
            })
        } else {
            UIAlertView(title: "Error", message: "Cannot send SMS", delegate: nil, cancelButtonTitle: "OK").show()
        }
    }
    
    func showSmsInvitation() {
        showSmsInvitation(nil)
    }
    
    func doAddContact() {
        let alertView = UIAlertView(
            title: NSLocalizedString("ContactsAddHeader", comment: "Alert Title"),
            message: NSLocalizedString("ContactsAddHint", comment: "Alert Hint"),
            delegate: self,
            cancelButtonTitle: NSLocalizedString("AlertCancel", comment: "Alert Cancel"),
            otherButtonTitles: NSLocalizedString("AlertNext", comment: "Alert Next"))
        
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.show()
    }
    
    // MARK: -
    // MARK: Layout
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        appEmptyContainer.frame = view.bounds
        appIsSyncingPlaceholder.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
        appIsEmptyPlaceholder.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
    }
    
    override func shouldAutorotate() -> Bool {
        return false
    }
    
    override func supportedInterfaceOrientations() -> UIInterfaceOrientationMask {
        return UIInterfaceOrientationMask.Portrait
    }
    
    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
}

extension MainTabViewController: MFMessageComposeViewControllerDelegate {
    
    func messageComposeViewController(controller: MFMessageComposeViewController, didFinishWithResult result: MessageComposeResult) {
        controller.dismissViewControllerAnimated(true, completion: nil)
    }
    
}

extension MainTabViewController: UIAlertViewDelegate {
    
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        // TODO: Localize
        if buttonIndex == 1 {
            let textField = alertView.textFieldAtIndex(0)!
            if textField.text?.length > 0 {
                self.execute(Actor.findUsersCommandWithQuery(textField.text), successBlock: { (val) -> Void in
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
                                // DO Nothing
                            }, failureBlock: { (val) -> () in
                                self.showSmsInvitation(textField.text)
                        })
                    } else {
                        self.showSmsInvitation(textField.text)
                    }
                }, failureBlock: { (val) -> Void in
                    self.showSmsInvitation(textField.text)
                })
            }
        }
    }
}