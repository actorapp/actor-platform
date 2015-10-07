//
//  Copyright (C) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

import Fabric
import Crashlytics
import ActorSDK

class AppDelegate : ActorApplicationDelegate {
    
    // Badge
    
//    private var binder = Binder()
//    private let badgeView = UIImageView()
//    private var badgeCount = 0
//    private var isBadgeVisible = false
    
    override init() {
        super.init()
        
        // Even when Fabric/Crashlytics not configured
        // this method doesn't crash
        Fabric.with([Crashlytics.self()])
        
        // Creating Actor
        createActor()
    }
    
    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject : AnyObject]?) -> Bool {
        
        // Register hockey app
//        if AppConfig.hockeyapp != nil {
//            BITHockeyManager.sharedHockeyManager().configureWithIdentifier(AppConfig.hockeyapp!)
//            BITHockeyManager.sharedHockeyManager().disableCrashManager = true
//            BITHockeyManager.sharedHockeyManager().updateManager.checkForUpdateOnLaunch = true
//            BITHockeyManager.sharedHockeyManager().startManager()
//            BITHockeyManager.sharedHockeyManager().authenticator.authenticateInstallation()
//        }
        
        ActorSDK.sharedActor().presentMessengerInNewWindow()
        
        // Badge
        
//        badgeView.image = Imaging.roundedImage(UIColor.RGB(0xfe0000), size: CGSizeMake(16, 16), radius: 8)
//        // badgeView.frame = CGRectMake(16, 22, 32, 16)
//        badgeView.alpha = 0
//        
//        let badgeText = UILabel()
//        badgeText.text = "0"
//        badgeText.textColor = UIColor.whiteColor()
//        // badgeText.frame = CGRectMake(0, 0, 32, 16)
//        badgeText.font = UIFont.systemFontOfSize(12)
//        badgeText.textAlignment = NSTextAlignment.Center
//        badgeView.addSubview(badgeText)
//        
//        window.addSubview(badgeView)
//        
//        // Bind badge counter
//        binder.bind(Actor.getAppState().globalCounter, closure: { (value: JavaLangInteger?) -> () in
//            self.badgeCount = Int((value!).integerValue)
//            application.applicationIconBadgeNumber = self.badgeCount
//            badgeText.text = "\(self.badgeCount)"
//            if (self.isBadgeVisible && self.badgeCount > 0) {
//                self.badgeView.showView()
//            } else if (self.badgeCount == 0) {
//                self.badgeView.hideView()
//            }
//            
//            badgeText.frame = CGRectMake(0, 0, 128, 16)
//            badgeText.sizeToFit()
//            
//            if badgeText.frame.width < 8 {
//                self.badgeView.frame = CGRectMake(16, 22, 16, 16)
//            } else {
//                self.badgeView.frame = CGRectMake(16, 22, badgeText.frame.width + 8, 16)
//            }
//            badgeText.frame = self.badgeView.bounds
//        })
        
        return true;
    }
    
//    override func actorDidLoggedIn(logInViewController: UIViewController?) {
//        // Create root layout for app
//        var rootController : UIViewController? = nil
//        if (isIPad) {
//            let splitController = MainSplitViewController()
//            splitController.viewControllers = [MainTabViewController(isAfterLogin: logInViewController != nil), NoSelectionViewController()]
//            
//            rootController = splitController
//        } else {
//            let tabController = MainTabViewController(isAfterLogin: logInViewController != nil)
//            binder.bind(Actor.getAppState().isAppLoaded, valueModel2: Actor.getAppState().isAppEmpty) { (loaded: JavaLangBoolean?, empty: JavaLangBoolean?) -> () in
//                if (empty!.booleanValue()) {
//                    if (loaded!.booleanValue()) {
//                        tabController.showAppIsEmptyPlaceholder()
//                    } else {
//                        tabController.showAppIsSyncingPlaceholder()
//                    }
//                } else {
//                    tabController.hidePlaceholders()
//                }
//            }
//            rootController = tabController
//        }
//        
//        window?.rootViewController = rootController!
//
//    }
    
    func application(application: UIApplication, openURL url: NSURL, sourceApplication: String?, annotation: AnyObject) -> Bool {
        
        if (url.scheme == "actor") {
            if (url.host == "invite") {
                if (Actor.isLoggedIn()) {
                    let token = url.query?.componentsSeparatedByString("=")[1]
                    if token != nil {
                        UIAlertView.showWithTitle(nil, message: localized("GroupJoinMessage"), cancelButtonTitle: localized("AlertNo"), otherButtonTitles: [localized("GroupJoinAction")], tapBlock: { (view, index) -> Void in
                            if (index == view.firstOtherButtonIndex) {
                                Executions.execute(Actor.joinGroupViaLinkCommandWithUrl(token), successBlock: { (val) -> Void in
                                    let groupId = val as! JavaLangInteger
                                    self.openChat(ACPeer.groupWithInt(groupId.intValue))
                                    }, failureBlock: { (val) -> Void in
                                        
                                        if let res = val as? ACRpcException {
                                            if res.getTag() == "USER_ALREADY_INVITED" {
                                                UIAlertView.showWithTitle(nil, message: localized("ErrorAlreadyJoined"), cancelButtonTitle: localized("AlertOk"), otherButtonTitles: nil, tapBlock: nil)
                                                return
                                            }
                                        }
                                        
                                        UIAlertView.showWithTitle(nil, message: localized("ErrorUnableToJoin"), cancelButtonTitle: localized("AlertOk"), otherButtonTitles: nil, tapBlock: nil)
                                })
                            }
                        })
                    }
                }
                
                return true
            }
        }
        return false
    }
        
    // Obsolete
    
    func openChat(peer: ACPeer) {
//        for i in UIApplication.sharedApplication().windows {
//            if let tab = i.rootViewController as? MainTabViewController {
//                let controller = tab.viewControllers![tab.selectedIndex] as! AANavigationController
//                let destController = ConversationViewController(peer: peer)
//                destController.hidesBottomBarWhenPushed = true
//                controller.pushViewController(destController, animated: true)
//                return
//            } else if let split = i.rootViewController as? MainSplitViewController {
//                split.navigateDetail(ConversationViewController(peer: peer))
//                return
//            }
//        }
    }
    
    func showBadge() {
//        isBadgeVisible = true
//        if badgeCount > 0 {
//            self.badgeView.showView()
//        }
    }
    
    func hideBadge() {
//        isBadgeVisible = false
//        self.badgeView.hideView()
    }
}