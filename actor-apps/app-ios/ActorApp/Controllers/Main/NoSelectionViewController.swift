//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class NoSelectionViewController: UIViewController {
    init() {
        super.init(nibName: nil, bundle: nil)
        view.backgroundColor = MainAppTheme.list.backyardColor
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}