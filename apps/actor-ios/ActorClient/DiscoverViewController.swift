//
//  DiscoverViewController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 12.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class DiscoverViewController: UIViewController {

    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder);
        initCommon();
    }
    
    init() {
        super.init(nibName: "DiscoverViewController", bundle: nil)
        initCommon();
    }
    
    func initCommon(){
        var icon = UIImage(named: "ic_search_blue_24")!;
        tabBarItem = UITabBarItem(title: nil,
            image: icon.tintImage(Resources.BarTintUnselectedColor)
                .imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal),
            selectedImage: icon);
        tabBarItem.imageInsets=UIEdgeInsetsMake(6, 0, -6, 0);
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
