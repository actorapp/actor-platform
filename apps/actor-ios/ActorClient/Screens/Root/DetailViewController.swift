//
//  DefailView.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 15.04.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

protocol DetailViewController {
    
}

protocol MasterViewController {
    
}

extension UIViewController {
    func navigateDetail(controller: UIViewController) {
        if (isIPad) {
            var split = self.splitViewController;
            if (split == nil) {
                split = navigationController?.splitViewController
            }
            var master = split!.viewControllers[0]
            var detail = AANavigationController()
            detail.viewControllers = [controller]
            split!.viewControllers = [master, detail]
        } else {
            controller.hidesBottomBarWhenPushed = true
            navigationController?.pushViewController(controller, animated: true);
        }
    }
}

extension UIViewController {
    func navigateReplace(controller: UIViewController) {
        
    }
    func navigateNext(controller: UIViewController) {
        
    }
}