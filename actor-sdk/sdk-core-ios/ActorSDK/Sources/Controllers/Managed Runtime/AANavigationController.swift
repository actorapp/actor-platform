//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AANavigationController: UINavigationController {
    
    fileprivate let binder = AABinder()
    
    public init() {
        super.init(nibName: nil, bundle: nil)
    }
    
    public override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }
    
    public override init(rootViewController: UIViewController) {
        super.init(rootViewController: rootViewController)
    }

    required public init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        styleNavBar()
        
//         Enabling app state sync progress
//        self.setPrimaryColor(MainAppTheme.navigation.progressPrimary)
//        self.setSecondaryColor(MainAppTheme.navigation.progressSecondary)
//        
//        binder.bind(Actor.getAppState().isSyncing, valueModel2: Actor.getAppState().isConnecting) { (value1: JavaLangBoolean?, value2: JavaLangBoolean?) -> () in
//            if value1!.booleanValue() || value2!.booleanValue() {
//                self.showProgress()
//                self.setIndeterminate(true)
//            } else {
//                self.finishProgress()
//            }
//        }
    }
    
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        styleNavBar()
        
        UIApplication.shared.setStatusBarStyle(ActorSDK.sharedActor().style.vcStatusBarStyle, animated: true)
    }
    
    open override var preferredStatusBarStyle : UIStatusBarStyle {
        return ActorSDK.sharedActor().style.vcStatusBarStyle
    }
    
    fileprivate func styleNavBar() {
        navigationBar.titleTextAttributes =
            [NSForegroundColorAttributeName: ActorSDK.sharedActor().style.navigationTitleColor]
        navigationBar.tintColor = ActorSDK.sharedActor().style.navigationTintColor
        navigationBar.barTintColor = ActorSDK.sharedActor().style.navigationBgColor
        navigationBar.hairlineHidden = ActorSDK.sharedActor().style.navigationHairlineHidden
        
        view.backgroundColor = ActorSDK.sharedActor().style.vcBgColor
    }
}

