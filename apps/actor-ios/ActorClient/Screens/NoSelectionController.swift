//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
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