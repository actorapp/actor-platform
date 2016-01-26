//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AANavigationController: UINavigationController {
    
    private let binder = AABinder()
    
    public init() {
        super.init(nibName: nil, bundle: nil)
    }
    
    public override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }
    
    public override init(rootViewController: UIViewController) {
        super.init(rootViewController: rootViewController)
    }

    required public init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
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
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        styleNavBar()
        
        UIApplication.sharedApplication().setStatusBarStyle(ActorSDK.sharedActor().style.vcStatusBarStyle, animated: true)
    }
    
    public override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return ActorSDK.sharedActor().style.vcStatusBarStyle
    }
    
    private func styleNavBar() {
        navigationBar.titleTextAttributes =
            [NSForegroundColorAttributeName: ActorSDK.sharedActor().style.navigationTitleColor]
        navigationBar.tintColor = ActorSDK.sharedActor().style.navigationTintColor
        navigationBar.barTintColor = ActorSDK.sharedActor().style.navigationBgColor
        navigationBar.hairlineHidden = ActorSDK.sharedActor().style.navigationHairlineHidden
        
        view.backgroundColor = ActorSDK.sharedActor().style.vcBgColor
    }
}

