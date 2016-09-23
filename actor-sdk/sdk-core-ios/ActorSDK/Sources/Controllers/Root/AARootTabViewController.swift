//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import MessageUI
fileprivate func < <T : Comparable>(lhs: T?, rhs: T?) -> Bool {
  switch (lhs, rhs) {
  case let (l?, r?):
    return l < r
  case (nil, _?):
    return true
  default:
    return false
  }
}

fileprivate func > <T : Comparable>(lhs: T?, rhs: T?) -> Bool {
  switch (lhs, rhs) {
  case let (l?, r?):
    return l > r
  default:
    return rhs < lhs
  }
}


open class AARootTabViewController : UITabBarController, MFMessageComposeViewControllerDelegate, UIAlertViewDelegate {
    
    fileprivate let binder = AABinder()
    
    fileprivate var appEmptyContainer = UIView()
    fileprivate var appIsSyncingPlaceholder = AABigPlaceholderView(topOffset: 44 + 20)
    fileprivate var appIsEmptyPlaceholder = AABigPlaceholderView(topOffset: 44 + 20)
    
    public init() {
        super.init(nibName: nil, bundle: nil)
        
        self.preferredContentSize = CGSize(width: 320.0, height: 600.0)
        self.preferredContentSize = CGSize(width: 320.0, height: 600.0)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("Not implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        tabBar.barTintColor = ActorSDK.sharedActor().style.tabBgColor
        
        appEmptyContainer.isHidden = true
        appIsEmptyPlaceholder.isHidden = true
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
        
        appIsSyncingPlaceholder.isHidden = true
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
    
    open func showAppIsSyncingPlaceholder() {
        appIsEmptyPlaceholder.isHidden = true
        appIsSyncingPlaceholder.isHidden = false
        appEmptyContainer.isHidden = false
    }

    open func showAppIsEmptyPlaceholder() {
        appIsEmptyPlaceholder.isHidden = false
        appIsSyncingPlaceholder.isHidden = true
        appEmptyContainer.isHidden = false
    }
    
    open func hidePlaceholders() {
        appEmptyContainer.isHidden = true
    }
    
    open func showSmsInvitation(_ phone: String?) {
        if MFMessageComposeViewController.canSendText() {
            let messageComposeController = MFMessageComposeViewController()
            messageComposeController.messageComposeDelegate = self
            if (phone != nil) {
                 messageComposeController.recipients = [phone!]
            }
            messageComposeController.body = AALocalized("InviteText")
                .replace("{link}", dest: ActorSDK.sharedActor().inviteUrl)
                .replace("{appname}", dest: ActorSDK.sharedActor().appName)
            messageComposeController.navigationBar.tintColor = ActorSDK.sharedActor().style.navigationTitleColor
            present(messageComposeController, animated: true, completion: { () -> Void in

            })
        } else {
            UIAlertView(title: "Error", message: "Cannot send SMS", delegate: nil, cancelButtonTitle: "OK").show()
        }
    }
    
    open func showSmsInvitation() {
        showSmsInvitation(nil)
    }
    
    open func doAddContact() {
        let alertView = UIAlertView(
            title: AALocalized("ContactsAddHeader"),
            message: AALocalized("ContactsAddHint"),
            delegate: self,
            cancelButtonTitle: AALocalized("AlertCancel"),
            otherButtonTitles: AALocalized("AlertNext"))
        
        alertView.alertViewStyle = UIAlertViewStyle.plainTextInput
        alertView.show()
    }
    
    // MARK: -
    // MARK: Layout
    
    open override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        appEmptyContainer.frame = view.bounds
        appIsSyncingPlaceholder.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
        appIsEmptyPlaceholder.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: view.bounds.height)
    }
    
    open override var shouldAutorotate : Bool {
        return false
    }
    
    open override var supportedInterfaceOrientations : UIInterfaceOrientationMask {
        return UIInterfaceOrientationMask.portrait
    }
    
    open override var preferredStatusBarStyle : UIStatusBarStyle {
        return ActorSDK.sharedActor().style.vcStatusBarStyle
    }
    
    open func messageComposeViewController(_ controller: MFMessageComposeViewController, didFinishWith result: MessageComposeResult) {
        controller.dismiss(animated: true, completion: nil)
    }

    open func alertView(_ alertView: UIAlertView, clickedButtonAt buttonIndex: Int) {
        // TODO: Localize
        if buttonIndex == 1 {
            let textField = alertView.textField(at: 0)!
            if textField.text?.length > 0 {
                self.execute(Actor.findUsersCommand(withQuery: textField.text), successBlock: { (val) -> Void in
                    var user: ACUserVM?
                    user = val as? ACUserVM
                    if user == nil {
                        if let users = val as? IOSObjectArray {
                            if Int(users.length()) > 0 {
                                if let tempUser = users.object(at: 0) as? ACUserVM {
                                    user = tempUser
                                }
                            }
                        }
                    }
                    if user != nil {
                        self.execute(Actor.addContactCommand(withUid: user!.getId())!, successBlock: { (val) -> () in
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
