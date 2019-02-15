import UIKit
import GoogleMaps
import CoreBluetooth


class YourViewController: UIViewController {
    
    // You don't need to modify the default init(nibName:bundle:) method.
    
    override func loadView() {
        
        GMSServices.provideAPIKey("AIzaSyBskyTpdYQC8g-nyZ79N6MUKwT3nC5SsaY")
        // Create a GMSCameraPosition
        let camera = GMSCameraPosition.camera(withLatitude: 37.621262, longitude: -122.378945, zoom: 10)
        let mapView = GMSMapView.map(withFrame: CGRect.zero, camera: camera)
        view = mapView
        
        // Creates a marker in the center of the map.
        let marker = GMSMarker()
        marker.position = CLLocationCoordinate2D(latitude: 37.621262, longitude: -122.378945)
        marker.title = "SFO Airport"
        marker.map = mapView
    }
}
//class ViewController: UIViewController, MGLMapViewDelegate,CBPeripheralDelegate {
//    //bluetooth part
//    let arduinoName = "BT05"
//    //  var myCharacteristic: CBCharacteristic?
//    let arduinoServiceCBUUID = CBUUID.init(string: "0C2A2217-D852-3685-2D4F-C005A1453DF2")
//    let arduinoConnect = CBUUID(string: "FFE0")
//    var centralManager: CBCentralManager!
//    var hatPeripheral: CBPeripheral!
//
//    //map part
//    var mapView: NavigationMapView!
//    var directionsRoute: Route?
//    var navigateButton: UIButton!
//    let disneyCoordinate = CLLocationCoordinate2D(latitude: 43.666829 , longitude: -79.388191000000006)
//    var counter = 0
//    var direction = "straight"
//    override func viewDidLoad() {
//        super.viewDidLoad()
//        // centralManager = CBCentralManager(delegate: self, queue: nil)
//        // Do any additional setup after loading the view, typically from a nib.
//        mapView = NavigationMapView(frame: view.bounds)
//        mapView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
//        view.addSubview(mapView)
//        mapView.delegate = self
//        mapView.showsUserLocation = true
//        mapView.setUserTrackingMode(.follow, animated: true)
//        addButton()
//    }
//
//    func addButton() {
//        navigateButton = UIButton(frame: CGRect(x: (view.frame.width/2)-100, y: view.frame.height - 75, width: 200, height:50))
//        navigateButton.backgroundColor = UIColor.white
//        navigateButton.setTitle("💩Go💩 ", for: .normal)
//        navigateButton.setTitleColor(UIColor(red: 59/255, green: 178/255, blue: 200/255, alpha: 1), for: .normal)
//        navigateButton.titleLabel?.font = UIFont(name: "AvenirNext-DemiBold", size: 18)
//        navigateButton.layer.cornerRadius = 25
//        navigateButton.layer.shadowOffset = CGSize(width: 0, height:10)
//        navigateButton.layer.shadowColor = UIColor.darkGray.cgColor
//        navigateButton.layer.shadowRadius = 5
//        navigateButton.layer.shadowOpacity = 0.3
//        navigateButton.addTarget(self, action: #selector(navigateButtonWasPressed( _sender: )), for: .touchUpInside)
//        view.addSubview(navigateButton)
//    }
//
//    @objc func navigateButtonWasPressed(_sender: UIButton) {
//        mapView.setUserTrackingMode(.none, animated: true)
//
//        let annotation = MGLPointAnnotation()
//        annotation.coordinate = disneyCoordinate
//        annotation.title = "Start Navigation"
//        mapView.addAnnotation(annotation)
//
//
//        calculateRoute(from: (mapView.userLocation!.coordinate), to: disneyCoordinate, completion: {(route, error) in
//            if error != nil {
//                print("error getting coordinate")
//            }
//        })
//    }
//
//    func calculateRoute(from originCoor: CLLocationCoordinate2D, to destinationCoor: CLLocationCoordinate2D, completion: @escaping(Route?, Error?) -> Void) {
//        print("calculate route")
//        let origin = Waypoint(coordinate:  CLLocationCoordinate2DMake(43.668799999999997,-79.393261999999993), coordinateAccuracy: -1, name: "Start")
//        //        let seg1 = Waypoint(coordinate: CLLocationCoordinate2DMake(43.669293000000003,-79.391035000000002), coordinateAccuracy: -1, name: "seg1")
//        //        let seg2 = Waypoint(coordinate: CLLocationCoordinate2DMake(43.667945000000003,-79.390452999999994), coordinateAccuracy: -1, name: "seg2")
//        //        let seg3 = Waypoint(coordinate: CLLocationCoordinate2DMake(43.668140999999999,-79.388796999999997), coordinateAccuracy: -1, name: "seg3")
//        let seg4 = Waypoint(coordinate: CLLocationCoordinate2DMake(43.666829,-79.388191000000006), coordinateAccuracy: -1, name: "seg4")
//
//        // let destination = Waypoint(coordinate: destinationCoor, coordinateAccuracy: -1, name: "Finish")
//
//        let options = NavigationRouteOptions(waypoints: [origin,/* seg1,seg2,seg3,*/seg4], profileIdentifier: .walking)
//
//        _ = Directions.shared.calculate(options, completionHandler: { (waypoints, routes, error) in
//            self.directionsRoute = routes?.first
//            // draw the line
//            let leg = self.directionsRoute?.legs.first
//            //  for step in (leg?.steps)! {
//            //
//            //                print("\(step.maneuverType)")
//            //                print("\(step.maneuverDirection)")
//            //                print("\(step.maneuverLocation)")
//            //                print(" ")
//            //}
//            print(leg as Any)
//            self.drawRoute(route: (self.directionsRoute!))
//
//
//            let coordinateBounds = MGLCoordinateBounds(sw: destinationCoor, ne: originCoor)
//            let insets = UIEdgeInsets(top: 50, left: 50, bottom: 50, right: 50)
//            let routeCam = self.mapView.cameraThatFitsCoordinateBounds(coordinateBounds, edgePadding: insets)
//            self.mapView.setCamera(routeCam, animated: true)
//
//        })
//    }
//
//    func drawRoute(route: Route){
//        guard route.coordinateCount > 0 else { return }
//        var routeCoordinates = route.coordinates!
//        let polyline = MGLPolylineFeature(coordinates: &routeCoordinates, count: route.coordinateCount)
//
//        if let source = mapView.style?.source(withIdentifier: "route-source") as? MGLShapeSource {
//            source.shape = polyline
//        } else {
//            let source = MGLShapeSource(identifier: "route-source", features: [polyline], options: nil)
//            let lineStyle = MGLLineStyleLayer(identifier: "route-style", source: source)
//            //lineStyle.lineColor = MGLStyleValue(rawValue: #colorLiteral(red: 0.2392156869, green: 0.6745098233, blue: 0.9686274529, alpha: 1))
//            //clineStyle.lineWidth = MGLStyleValue(rawValue: 4.0)
//            mapView.style?.addSource(source)
//            mapView.style?.addLayer(lineStyle)
//
//        }
//    }
//
//    func mapView(_ mapView: MGLMapView, annotationCanShowCallout annotation: MGLAnnotation) -> Bool {
//        return true
//    }
//
//    func mapView(_ mapView: MGLMapView, tapOnCalloutFor annotation: MGLAnnotation) {
//        let navigationVC = NavigationViewController(for: directionsRoute!)
//        navigationVC.delegate = self
//
//        //    navigationVC.routeController.locationManager = SimulatedLocationManager(route: directionsRoute!)
//        present(navigationVC, animated: true, completion: nil)
//    }
//    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
//
//        guard let services = peripheral.services else { return }
//        for service in services {
//            //    print(service)
//            peripheral.discoverCharacteristics(nil, for: service)
//        }
//    }
//
//
//    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
//
//        guard let characteristics = service.characteristics else { return }
//
//        for characteristic in characteristics {
//            print(characteristic)
//            //self.myCharacteristic = characteristic
//
//            if characteristic.properties.contains(.write) {
//                // print("\(characteristic.uuid): properties contains .write")
//                var temp: String
//                switch direction {
//                case "left":
//                    temp = "L"
//
//                case "right":
//                    temp = "R"
//
//                default:
//                    temp = "R"
//                }
//                let data: Data? = temp.data(using: .ascii)
//
//                peripheral.writeValue(data!,for: characteristic,type: CBCharacteristicWriteType.withoutResponse)
//                centralManager.cancelPeripheralConnection(hatPeripheral)
//
//            }
//        }
//    }
//
//}
//
//
//extension ViewController: NavigationViewControllerDelegate, RouteControllerDelegate{
//    func navigationViewController(_ navigationViewController: NavigationViewController, didArriveAt waypoint: Waypoint) -> Bool {
//        return true
//    }
//    func routeController(_ routeController: RouteController, didUpdate locations: [CLLocation]) {
//        print(locations.debugDescription)
//        // print("aaaaaaawaaaafwfwfwfsafasfs")
//    }
//    @objc func navigationViewControllerDidOpenFeedback(_ viewController: NavigationViewController){
//        //        let gg = RouteProgress(route: directionsRoute!)
//        //        print(gg.distanceTraveled)
//        //        print(gg.currentLeg)
//        //        //print(currentLeg.steps.first?.maneuverLocation as Any)
//        centralManager = CBCentralManager(delegate: self, queue: nil)
//        switch counter {
//        case 0:
//            direction = "right"
//            counter = 1 ;
//        // write_to_arduino()
//        case 1:
//            direction = "left"
//            counter = 2 ;
//            //  write_to_arduino()
//
//        case 2:
//            direction = "right"
//            counter =  3;
//        // write_to_arduino()
//        default:
//            direction = "straight"
//            counter = 0;
//        }
//    }
//}
//
//
//
//extension ViewController: CBCentralManagerDelegate {
//
//
//    func centralManagerDidUpdateState(_ central: CBCentralManager) {
//        switch central.state {
//        case .unknown:
//            print("central.state is .unknown")
//        case .resetting:
//            print("central.state is .resetting")
//        case .unsupported:
//            print("central.state is .unsupported")
//        case .unauthorized:
//            print("central.state is .unauthorized")
//        case .poweredOff:
//            print("central.state is .poweredOff")
//        case .poweredOn:
//            print("central.state is .poweredOn")
//            centralManager.scanForPeripherals(withServices: nil )
//            //[arduinoServiceCBUUID]
//        }
//    }
//    func centralManager(_ central: CBCentralManager,
//                        didDiscover peripheral: CBPeripheral
//        ,advertisementData: [String : Any],
//         rssi RSSI: NSNumber) {
//        let t = peripheral.name
//        if t == "BT05"{
//            hatPeripheral = peripheral
//            centralManager.stopScan()
//            //print("find it: \(hatPeripheral)")
//            //connect 0C2A2217-D852-3685-2D4F-C005A1453DF2
//            hatPeripheral.delegate = self
//            centralManager.connect(hatPeripheral)
//
//
//        }
//    }
//
//    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
//        print("Connected!")
//        hatPeripheral.discoverServices([arduinoConnect])
//        // hatPeripheral.delegate = self
//
//    }
//}
//
