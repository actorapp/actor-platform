//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class AAAuthNavigationController: UINavigationController {
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationBar.setTransparentBackground()
        navigationBar.tintColor = UIColor.redColor()
    }

    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        UIApplication.sharedApplication().setStatusBarStyle(.Default, animated: true)
    }
}