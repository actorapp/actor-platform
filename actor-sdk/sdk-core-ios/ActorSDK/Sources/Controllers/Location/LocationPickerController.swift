//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import MapKit

class AALocationPickerController: AAViewController {

    let map = MKMapView()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.addSubview(map)
    }
    
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        map.frame = self.view.bounds
    }
}