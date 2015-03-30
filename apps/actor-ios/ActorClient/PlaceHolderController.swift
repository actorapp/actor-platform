//
//  PlaceHolderController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 12.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
import UIKit;

class PlaceHolderController : UIViewController {
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder);
        initCommon();
    }
    
    init() {
        super.init(nibName: "DiscoverViewController", bundle: nil)
        initCommon();
    }
    
    func initCommon(){
        tabBarItem = UITabBarItem(title: nil,
            image: nil,
            selectedImage: nil);
        tabBarItem.enabled = false;
    }
}