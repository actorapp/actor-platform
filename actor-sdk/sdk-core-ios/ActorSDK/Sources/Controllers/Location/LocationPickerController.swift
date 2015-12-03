//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import MapKit

class AALocationPickerController: AAViewController {

    let locationManager = CLLocationManager()
    let map = MKMapView()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.title = "Location"
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: .Plain, target: self, action: "cancellDidTap")
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: .Done, target: self, action: "doneDidTap")
        
        locationManager.requestWhenInUseAuthorization()
        
        map.showsUserLocation = true
        map.userTrackingMode = MKUserTrackingMode.Follow
        
        self.view.addSubview(map)
    }
    
    func cancellDidTap() {
        dismiss()
    }
    
    func doneDidTap() {
        dismiss()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        map.frame = self.view.bounds
    }
}