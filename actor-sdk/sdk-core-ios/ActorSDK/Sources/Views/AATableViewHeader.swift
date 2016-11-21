//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AATableViewHeader: UIView {
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        for view in self.subviews {
            view.frame = CGRect(x: view.frame.minX, y: view.frame.minY, width: self.frame.width, height: view.frame.height)
            
            // Fix for UISearchBar disappear
            // http://stackoverflow.com/questions/19044156/searchbar-disappears-from-headerview-in-ios-7
            if let search = view as? UISearchBar {
                if let buggyView = search.subviews.first {
                    buggyView.bounds = search.bounds
                    buggyView.center = CGPoint(x: buggyView.bounds.width/2,y: buggyView.bounds.height/2)
                }
            }
        }
    }
}
