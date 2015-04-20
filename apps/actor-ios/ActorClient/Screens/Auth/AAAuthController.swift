//
//  AAAuthController.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 17.04.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class AAAuthController: AAViewController {
    func onAuthenticated() {
        (UIApplication.sharedApplication().delegate as! AppDelegate).onLoggedIn()
    }
}