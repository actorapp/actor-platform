//
//  MainTabController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 10.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
import UIKit

class MainTabController : UITabBarController, UITabBarDelegate, ABActionShitDelegate {

    var centerButton:UIButton? = nil;
    var isInited = false;
    
    required init(coder aDecoder: NSCoder) {
        fatalError("Not implemented")
    }
    
    init(){
        super.init(nibName: nil, bundle: nil);
        initControllers()
    }
    
    func initControllers() {
        
//        centerButton = UIButton(frame: CGRect(x: 0, y: 0, width: 66, height: 58));
//        centerButton!.setBackgroundImage(UIImage(named: "ic_round_button_red"), forState: UIControlState.Normal);
//        centerButton!.setImage(UIImage(named: "ic_add_white_24"), forState: UIControlState.Normal);
//        centerButton!.imageEdgeInsets = UIEdgeInsetsMake(4, 0, -4, 0);
//        centerButton!.addTarget(self, action: "centerButtonTap", forControlEvents: UIControlEvents.TouchUpInside)
//        self.view.addSubview(centerButton!);
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "Back", style: UIBarButtonItemStyle.Plain, target: nil, action: nil)

    }
    
    func centerButtonTap() {
        var actionShit = ABActionShit()
        actionShit.buttonTitles = ["Add Contact", "Create group", "Write to..."];
        actionShit.delegate = self
        actionShit.showWithCompletion(nil)
    }
    
    func actionShit(actionShit: ABActionShit!, clickedButtonAtIndex buttonIndex: Int) {
        if (buttonIndex == 0) {
            doAddContact()
        } else if (buttonIndex == 1) {
            navigationController?.pushViewController(GroupMembersController(), animated: true)
        } else if (buttonIndex == 2) {
            doCompose()
        }
    }
    
    func doCompose() {
        navigationController?.pushViewController(ComposeController(), animated: true)
    }
    
    func doAddContact() {
        var alertView = UIAlertView(title: "Add Contact", message: "Please, specify phone number", delegate: nil, cancelButtonTitle: "Cancel")
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.show()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if (!isInited) {
            if (MSG.isLoggedIn()) {
                isInited = true
                
                viewControllers = [ContactsViewController(),
                                   DialogsViewController(),
                                   SettingsViewController()];
        
                selectedIndex = 1;
                applyTitle(1);
            }
        }
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        // centerButton!.frame = CGRectMake(view.center.x-31, view.frame.height-58, 66, 58)
    }
    
    override func tabBar(tabBar: UITabBar, didSelectItem item: UITabBarItem!) {
        var item = (tabBar.items! as NSArray).indexOfObject(item);
        applyTitle(item);
    }
    
    func applyTitle(item: Int){
        switch(item){
        case 0:
            navigationItem.title = "People";
            navigationItem.leftBarButtonItem = nil;
            navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Add, target: self, action: "doAddContact")
            break;
        case 1:
            navigationItem.title = "Chats";
            if ((self.viewControllers![1] as! DialogsViewController).isTableEditing()) {
                navigationItem.leftBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Cancel, target: self, action: "editDialogs");
                navigationItem.rightBarButtonItem = nil
            } else {
                navigationItem.leftBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Edit, target: self, action: "editDialogs");
                navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Compose, target: self, action: "doCompose")
            }
        case 2:
            navigationItem.title = "You";
            navigationItem.leftBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Edit, target: self, action: "editProfile");
            navigationItem.rightBarButtonItem = nil;
            break;
        default:
            navigationItem.leftBarButtonItem = nil;
            navigationItem.rightBarButtonItem = nil;
            navigationItem.title = "";
            break;
        }
    }
    
    func editDialogs() {
        (self.viewControllers![1] as! DialogsViewController).toggleEdit();
        applyTitle(1)
    }
    
    func editProfile() {
        
    }
}