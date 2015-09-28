//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class AANavigationController: UINavigationController {
    
    private let binder = Binder()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationBar.hideBottomHairline()
        view.backgroundColor = MainAppTheme.list.backyardColor
        
        // Enabling app state sync progress
        self.setPrimaryColor(MainAppTheme.navigation.progressPrimary)
        self.setSecondaryColor(MainAppTheme.navigation.progressSecondary)
        
        binder.bind(Actor.getAppState().isSyncing, valueModel2: Actor.getAppState().isConnecting) { (value1: JavaLangBoolean?, value2: JavaLangBoolean?) -> () in
            if value1!.booleanValue() || value2!.booleanValue() {
                self.showProgress()
                self.setIndeterminate(true)
            } else {
                self.finishProgress()
            }
        }
    }
    
    func makeBarTransparent() {
        navigationBar.setBackgroundImage(UIImage(), forBarMetrics: UIBarMetrics.Default)
        navigationBar.shadowImage = UIImage()
    }
    
    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
}

extension UINavigationBar {
    
    func hideBottomHairline() {
        let navigationBarImageView = hairlineImageViewInNavigationBar(self)
        navigationBarImageView!.hidden = true
    }
    
    func showBottomHairline() {
        let navigationBarImageView = hairlineImageViewInNavigationBar(self)
        navigationBarImageView!.hidden = false
    }
    
    private func hairlineImageViewInNavigationBar(view: UIView) -> UIImageView? {
        if view.isKindOfClass(UIImageView) && view.bounds.height <= 1.0 {
            return (view as! UIImageView)
        }
        
        for subview: UIView in view.subviews {
            if let imageView: UIImageView = hairlineImageViewInNavigationBar(subview) {
                return imageView
            }
        }
        
        return nil
    }
    
}