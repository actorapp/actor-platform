//
//  AATableViewHeader.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 21.04.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class AATableViewHeader: UIView {
    override func layoutSubviews() {
        super.layoutSubviews()
        for view in self.subviews as! [UIView] {
            view.frame = CGRectMake(view.frame.minX, view.frame.minY, self.frame.width, view.frame.height)
            
            // Fix for UISearchBar disappear
            // http://stackoverflow.com/questions/19044156/searchbar-disappears-from-headerview-in-ios-7
            if let search = view as? UISearchBar {
                if let buggyView = search.subviews.first as? UIView {
                    buggyView.bounds = search.bounds
                    buggyView.center = CGPointMake(buggyView.bounds.width/2,buggyView.bounds.height/2)
                }
            }
        }
        
    }
}