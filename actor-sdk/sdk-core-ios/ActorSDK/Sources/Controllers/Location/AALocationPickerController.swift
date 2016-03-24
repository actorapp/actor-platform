//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import MapKit

public class AALocationPickerController: AAViewController, CLLocationManagerDelegate, MKMapViewDelegate {

    public var delegate: AALocationPickerControllerDelegate? = nil
    
    private let locationManager = CLLocationManager()
    private let map = MKMapView()
    private let pinPoint = AAMapPinPointView()
    private let locationPinOffset = CGPointMake(0.0, 33.0)
    
    override init() {
       super.init()
        
        navigationItem.title = AALocalized("LocationTitle")
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: .Plain, target: self, action: #selector(AALocationPickerController.cancellDidTap))
        
        updateAuthStatus(CLLocationManager.authorizationStatus())
        
        locationManager.delegate = self
        
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()
        
        pinPoint.risePin(true, animated: false)
        
        map.showsUserLocation = true
        map.delegate = self
        
        self.view.addSubview(map)
        self.view.addSubview(pinPoint)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
//    override public func viewDidLoad() {
//        super.viewDidLoad()
//        
//        
//    }
    
    public func mapView(mapView: MKMapView, regionWillChangeAnimated animated: Bool) {
        pinPoint.risePin(true, animated: true)
    }
    
    public func mapView(mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
        pinPoint.risePin(false, animated: true)
    }
    
    func cancellDidTap() {
        delegate?.locationPickerDidCancelled(self)
    }
    
    func doneDidTap() {
        delegate?.locationPickerDidPicked(self, latitude: map.centerCoordinate.latitude, longitude:  map.centerCoordinate.longitude)
    }
    
    func updateAuthStatus(status: CLAuthorizationStatus) {
        if (status == CLAuthorizationStatus.Denied) {
            // User explictly denied access to maps
            showPlaceholderWithImage(UIImage.bundled("location_placeholder"), title: AALocalized("Placeholder_Location_Title"), subtitle: AALocalized("Placeholder_Location_Message"))
            map.hidden = true
            pinPoint.hidden = true
            navigationItem.rightBarButtonItem = nil
        } else if (status == CLAuthorizationStatus.Restricted || status == CLAuthorizationStatus.NotDetermined) {
            // App doesn't complete auth request
            map.hidden = false
            pinPoint.hidden = false
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: .Done, target: self, action: #selector(AALocationPickerController.doneDidTap))
            hidePlaceholder()
        } else {
            // Authorised
            map.hidden = false
            pinPoint.hidden = false
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: .Done, target: self, action: #selector(AALocationPickerController.doneDidTap))
            hidePlaceholder()
        }
    }
    
    override public func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        map.frame = self.view.bounds
        pinPoint.centerIn(self.view.bounds)
    }
    
    public func locationManager(manager: CLLocationManager, didChangeAuthorizationStatus status: CLAuthorizationStatus) {
        updateAuthStatus(status)
    }
    
    public func locationManager(manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let location = locations.first!
        map.setRegion(MKCoordinateRegion(center: location.coordinate, span: MKCoordinateSpanMake(0.05, 0.05)), animated: true)
        locationManager.stopUpdatingLocation()
    }
}

public protocol AALocationPickerControllerDelegate {
    func locationPickerDidPicked(controller: AALocationPickerController, latitude: Double, longitude: Double)
    func locationPickerDidCancelled(controller: AALocationPickerController)
}