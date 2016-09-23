//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import MapKit

open class AALocationPickerController: AAViewController, CLLocationManagerDelegate, MKMapViewDelegate {

    open var delegate: AALocationPickerControllerDelegate? = nil
    
    fileprivate let locationManager = CLLocationManager()
    fileprivate let map = MKMapView()
    fileprivate let pinPoint = AAMapPinPointView()
    fileprivate let locationPinOffset = CGPoint(x: 0.0, y: 33.0)
    
    override init() {
       super.init()
        
        navigationItem.title = AALocalized("LocationTitle")
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: .plain, target: self, action: #selector(AALocationPickerController.cancellDidTap))
        
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
    
    open func mapView(_ mapView: MKMapView, regionWillChangeAnimated animated: Bool) {
        pinPoint.risePin(true, animated: true)
    }
    
    open func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
        pinPoint.risePin(false, animated: true)
    }
    
    func cancellDidTap() {
        delegate?.locationPickerDidCancelled(self)
    }
    
    func doneDidTap() {
        delegate?.locationPickerDidPicked(self, latitude: map.centerCoordinate.latitude, longitude:  map.centerCoordinate.longitude)
    }
    
    func updateAuthStatus(_ status: CLAuthorizationStatus) {
        if (status == CLAuthorizationStatus.denied) {
            // User explictly denied access to maps
            showPlaceholderWithImage(UIImage.bundled("location_placeholder"), title: AALocalized("Placeholder_Location_Title"), subtitle: AALocalized("Placeholder_Location_Message"))
            map.isHidden = true
            pinPoint.isHidden = true
            navigationItem.rightBarButtonItem = nil
        } else if (status == CLAuthorizationStatus.restricted || status == CLAuthorizationStatus.notDetermined) {
            // App doesn't complete auth request
            map.isHidden = false
            pinPoint.isHidden = false
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: .done, target: self, action: #selector(AALocationPickerController.doneDidTap))
            hidePlaceholder()
        } else {
            // Authorised
            map.isHidden = false
            pinPoint.isHidden = false
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: .done, target: self, action: #selector(AALocationPickerController.doneDidTap))
            hidePlaceholder()
        }
    }
    
    override open func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        map.frame = self.view.bounds
        pinPoint.centerIn(self.view.bounds)
    }
    
    open func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        updateAuthStatus(status)
    }
    
    open func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let location = locations.first!
        map.setRegion(MKCoordinateRegion(center: location.coordinate, span: MKCoordinateSpanMake(0.05, 0.05)), animated: true)
        locationManager.stopUpdatingLocation()
    }
}

public protocol AALocationPickerControllerDelegate {
    func locationPickerDidPicked(_ controller: AALocationPickerController, latitude: Double, longitude: Double)
    func locationPickerDidCancelled(_ controller: AALocationPickerController)
}
