//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class TableViewHeader: UIView {
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