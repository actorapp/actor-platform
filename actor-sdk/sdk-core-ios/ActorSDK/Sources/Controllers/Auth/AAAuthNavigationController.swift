//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAAuthNavigationController: UINavigationController {
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationBar.setTransparentBackground()
        navigationBar.tintColor = ActorSDK.sharedActor().style.authTintColor
        navigationBar.hairlineHidden = true
        
        view.backgroundColor = UIColor.white
    }

    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        UIApplication.shared.setStatusBarStyle(.default, animated: true)
    }
    
    open override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        UIApplication.shared.setStatusBarStyle(.lightContent, animated: true)
    }
    
    open override var preferredStatusBarStyle : UIStatusBarStyle {
        return .default
    }
}
