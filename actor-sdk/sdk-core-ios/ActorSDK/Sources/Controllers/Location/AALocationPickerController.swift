//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import MapKit

public class AALocationPickerController: AAViewController, CLLocationManagerDelegate, MKMapViewDelegate {

    public var delegate: AALocationPickerControllerDelegate? = nil
    
    private let locationManager = CLLocationManager()
    private let map = MKMapView()
    private let pinPoint = AAMapPinPointView()
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.title = "Location"
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: .Plain, target: self, action: "cancellDidTap")
        
        updateAuthStatus(CLLocationManager.authorizationStatus())
        
        locationManager.delegate = self
        
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()
        
        map.showsUserLocation = true
        map.delegate = self
        
        self.view.addSubview(map)
        self.view.addSubview(pinPoint)
    }
    
    public func mapView(mapView: MKMapView, regionWillChangeAnimated animated: Bool) {
        pinPoint.risePin(true, animated: animated)
    }
    
    public func mapView(mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
        pinPoint.risePin(false, animated: animated)
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
            showPlaceholderWithImage(UIImage.bundled(""), title: "Enable location",subtitle: "Enable location services in settings.")
            map.hidden = true
            navigationItem.rightBarButtonItem = nil
        } else if (status == CLAuthorizationStatus.Restricted || status == CLAuthorizationStatus.NotDetermined) {
            // App doesn't complete auth request
            map.hidden = false
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: .Done, target: self, action: "doneDidTap")
            hidePlaceholder()
        } else {
            // Authorised
            map.hidden = false
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: .Done, target: self, action: "doneDidTap")
            hidePlaceholder()
        }
    }
    
    override public func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        map.frame = self.view.bounds
//        pinPoint.bounds = CGRectMake(0,0,100,100)
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