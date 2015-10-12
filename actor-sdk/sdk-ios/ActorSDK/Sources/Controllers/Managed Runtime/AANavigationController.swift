//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

public class AANavigationController: UINavigationController {
    
    private let binder = AABinder()
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        // Style navigation bar
        
        navigationBar.titleTextAttributes =
            [NSForegroundColorAttributeName: ActorSDK.sharedActor().style.navigationTitleColor]
        navigationBar.tintColor = ActorSDK.sharedActor().style.navigationTintColor
        navigationBar.barTintColor = ActorSDK.sharedActor().style.navigationBgColor
        navigationBar.hairlineHidden = true
        
        view.backgroundColor = ActorSDK.sharedActor().style.vcBgColor
        
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
        
        UIApplication.sharedApplication().setStatusBarStyle(ActorSDK.sharedActor().style.vcStatusBarStyle, animated: true)
    }
    
    public override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return ActorSDK.sharedActor().style.vcStatusBarStyle
    }
}

