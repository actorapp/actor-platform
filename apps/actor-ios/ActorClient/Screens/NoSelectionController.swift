//
//  NoSelectionController.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 15.04.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class NoSelectionController: UIViewController {
    init() {
        super.init(nibName: nil, bundle: nil)
        view.backgroundColor = Resources.BackyardColor
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}