//
//  MainTabController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 10.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
import UIKit
import MessageUI

class MainTabController : UITabBarController, UITabBarDelegate, ABActionShitDelegate {
    
    // MARK: -
    // MARK: Private vars
    
    private var appEmptyContainer = UIView()
    private var appIsSyncingPlaceholder = AAPlaceholderView(topOffset: 44 + 20)
    private var appIsEmptyPlaceholder = AAPlaceholderView(topOffset: 44 + 20)

    // MARK: -
    // MARK: Public vars
    
    var centerButton:UIButton? = nil;
    var isInited = false;
    
    // MARK: -
    // MARK: Constructors
    
    init() {
        super.init(nibName: nil, bundle: nil);
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
            actionTarget: self, actionSelector: Selector("showSmsInvitation"))
        appEmptyContainer.addSubview(appIsEmptyPlaceholder)
        
        appIsSyncingPlaceholder.hidden = true
        appIsSyncingPlaceholder.setImage(
            UIImage(named: "chat_list_placeholder"),
            title: NSLocalizedString("Placeholder_Loading_Title", comment: "Placeholder Title"),
            subtitle: NSLocalizedString("Placeholder_Loading_Message", comment: "Placeholder Message"))
        appEmptyContainer.addSubview(appIsSyncingPlaceholder)
        
        view.addSubview(appEmptyContainer)
    }
    
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if (!isInited) {
            if (MSG.isLoggedIn()) {
                isInited = true
                
                let contactsNavigation = AANavigationController(rootViewController: ContactsViewController())
                let dialogsNavigation = AANavigationController(rootViewController: DialogsViewController())
                let settingsNavigation = AANavigationController(rootViewController: AASettingsController())
                contactsNavigation.navigationBar.barStyle = UIBarStyle.Black
                dialogsNavigation.navigationBar.barStyle = UIBarStyle.Black
                settingsNavigation.navigationBar.barStyle = UIBarStyle.Black
                viewControllers = [contactsNavigation, dialogsNavigation, settingsNavigation];
                
                selectedIndex = 1;
            }
        }
    }
    
    // MARK: -
    // MARK: Methods
    
    func centerButtonTap() {
        var actionShit = ABActionShit()
        actionShit.buttonTitles = ["Add Contact", "Create group", "Write to..."];
        actionShit.delegate = self
        actionShit.showWithCompletion(nil)
    }
    
    // MARK: -
    // MARK: ABActionShit Delegate
    
    func actionShit(actionShit: ABActionShit!, clickedButtonAtIndex buttonIndex: Int) {
        if (buttonIndex == 1) {
            navigationController?.pushViewController(GroupMembersController(), animated: true)
        }
    }
    
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
    
    func showSmsInvitation() {
        if MFMessageComposeViewController.canSendText() {
            let messageComposeController = MFMessageComposeViewController()
            messageComposeController.messageComposeDelegate = self
            messageComposeController.body = NSLocalizedString("InviteText", comment: "Invite Text")
            messageComposeController.navigationBar.tintColor = MainAppTheme.navigation.titleColor
            presentViewController(messageComposeController, animated: true, completion: { () -> Void in
                MainAppTheme.navigation.applyStatusBar()
            })
        } else {
            UIAlertView(title: "Error", message: "Cannot send SMS", delegate: nil, cancelButtonTitle: "OK") // TODO: Show or not to show?
        }
    }
    
    // MARK: -
    // MARK: Layout
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        appEmptyContainer.frame = view.bounds
        appIsSyncingPlaceholder.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
        appIsEmptyPlaceholder.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
    }
    
}

extension MainTabController: MFMessageComposeViewControllerDelegate {
    
    func messageComposeViewController(controller: MFMessageComposeViewController!, didFinishWithResult result: MessageComposeResult) {
        controller.dismissViewControllerAnimated(true, completion: nil)
    }
    
}