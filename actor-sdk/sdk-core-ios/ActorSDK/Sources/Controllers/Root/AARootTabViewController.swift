//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import MessageUI

public class AARootTabViewController : UITabBarController, MFMessageComposeViewControllerDelegate, UIAlertViewDelegate {
    
    private let binder = AABinder()
    
    private var appEmptyContainer = UIView()
    private var appIsSyncingPlaceholder = AABigPlaceholderView(topOffset: 44 + 20)
    private var appIsEmptyPlaceholder = AABigPlaceholderView(topOffset: 44 + 20)
    
    public init() {
        super.init(nibName: nil, bundle: nil)
        
        self.preferredContentSize = CGSize(width: 320.0, height: 600.0)
        self.preferredContentSize = CGSize(width: 320.0, height: 600.0)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("Not implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        tabBar.barTintColor = ActorSDK.sharedActor().style.tabBgColor
        
        appEmptyContainer.hidden = true
        appIsEmptyPlaceholder.hidden = true
        appIsEmptyPlaceholder.setImage(
            UIImage.bundled("contacts_list_placeholder"),
            title: AALocalized("Placeholder_Empty_Title"),
            subtitle: AALocalized("Placeholder_Empty_Message").replace("{appname}", dest: ActorSDK.sharedActor().appName),
            actionTitle: AALocalized("Placeholder_Empty_Action"),
            subtitle2: AALocalized("Placeholder_Empty_Message2"),
            actionTarget: self, actionSelector: #selector(AARootTabViewController.showSmsInvitation as (AARootTabViewController) -> () -> ()),
            action2title: AALocalized("Placeholder_Empty_Action2"),
            action2Selector: #selector(AARootTabViewController.doAddContact))
        appEmptyContainer.addSubview(appIsEmptyPlaceholder)
        
        appIsSyncingPlaceholder.hidden = true
        appIsSyncingPlaceholder.setImage(
            UIImage.bundled("chat_list_placeholder"),
            title: AALocalized("Placeholder_Loading_Title"),
            subtitle: AALocalized("Placeholder_Loading_Message"))
        appEmptyContainer.addSubview(appIsSyncingPlaceholder)
        
        view.addSubview(appEmptyContainer)
        
        binder.bind(Actor.getAppState().isAppLoaded, valueModel2: Actor.getAppState().isAppEmpty) { (loaded: JavaLangBoolean?, empty: JavaLangBoolean?) -> () in
            if (empty!.booleanValue()) {
                if (loaded!.booleanValue()) {
                    self.showAppIsEmptyPlaceholder()
                } else {
                    self.showAppIsSyncingPlaceholder()
                }
            } else {
                self.hidePlaceholders()
            }
        }
    }
    
    public func showAppIsSyncingPlaceholder() {
        appIsEmptyPlaceholder.hidden = true
        appIsSyncingPlaceholder.hidden = false
        appEmptyContainer.hidden = false
    }

    public func showAppIsEmptyPlaceholder() {
        appIsEmptyPlaceholder.hidden = false
        appIsSyncingPlaceholder.hidden = true
        appEmptyContainer.hidden = false
    }
    
    public func hidePlaceholders() {
        appEmptyContainer.hidden = true
    }
    
    public func showSmsInvitation(phone: String?) {
        if MFMessageComposeViewController.canSendText() {
            let messageComposeController = MFMessageComposeViewController()
            messageComposeController.messageComposeDelegate = self
            if (phone != nil) {
                 messageComposeController.recipients = [phone!]
            }
            messageComposeController.body = AALocalized("InviteText")
                .replace("{link}", dest: ActorSDK.sharedActor().inviteUrl)
            messageComposeController.navigationBar.tintColor = ActorSDK.sharedActor().style.navigationTitleColor
            presentViewController(messageComposeController, animated: true, completion: { () -> Void in
//                ActorSDK.sharedActor().style.appl
            })
        } else {
            UIAlertView(title: "Error", message: "Cannot send SMS", delegate: nil, cancelButtonTitle: "OK").show()
        }
    }
    
    public func showSmsInvitation() {
        showSmsInvitation(nil)
    }
    
    public func doAddContact() {
        let alertView = UIAlertView(
            title: AALocalized("ContactsAddHeader"),
            message: AALocalized("ContactsAddHint"),
            delegate: self,
            cancelButtonTitle: AALocalized("AlertCancel"),
            otherButtonTitles: AALocalized("AlertNext"))
        
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.show()
    }
    
    // MARK: -
    // MARK: Layout
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        appEmptyContainer.frame = view.bounds
        appIsSyncingPlaceholder.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
        appIsEmptyPlaceholder.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
    }
    
    public override func shouldAutorotate() -> Bool {
        return false
    }
    
    public override func supportedInterfaceOrientations() -> UIInterfaceOrientationMask {
        return UIInterfaceOrientationMask.Portrait
    }
    
    public override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return ActorSDK.sharedActor().style.vcStatusBarStyle
    }
    
    public func messageComposeViewController(controller: MFMessageComposeViewController, didFinishWithResult result: MessageComposeResult) {
        controller.dismissViewControllerAnimated(true, completion: nil)
    }

    public func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
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
                        self.execute(Actor.addContactCommandWithUid(user!.getId())!, successBlock: { (val) -> () in
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