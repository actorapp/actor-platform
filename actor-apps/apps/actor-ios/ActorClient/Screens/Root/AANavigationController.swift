//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class AANavigationController: UINavigationController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationBar.translucent = true
        navigationBar.hideBottomHairline()
        view.backgroundColor = MainAppTheme.list.backyardColor
    }
    
    // MARK: -
    // MARK: Methods
//    override func viewWillAppear(animated: Bool) {
//        super.viewWillAppear(animated)
//        navigationBar.hideBottomHairline()
//        view.backgroundColor = MainAppTheme.list.backyardColor
//    }
    
    func makeBarTransparent() {
        navigationBar.setBackgroundImage(UIImage(), forBarMetrics: UIBarMetrics.Default)
        navigationBar.shadowImage = UIImage()
        navigationBar.translucent = true
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
        
        let subviews = (view.subviews as! [UIView])
        for subview: UIView in subviews {
            if let imageView: UIImageView = hairlineImageViewInNavigationBar(subview) {
                return imageView
            }
        }
        
        return nil
    }
    
}