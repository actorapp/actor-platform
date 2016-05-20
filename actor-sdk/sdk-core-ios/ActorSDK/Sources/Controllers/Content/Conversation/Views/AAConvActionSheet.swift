//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import Photos

public protocol AAConvActionSheetDelegate {
    func actionSheetPickedImages(images: [UIImage])
    func actionSheetPickCamera()
    func actionSheetPickGallery()
    func actionSheetPickDocument()
    func actionSheetPickLocation()
    func actionSheetPickContact()
    func actionSheetUnblockContact()
}

public class AAConvActionSheet: UIView, AAThumbnailViewDelegate {
    
    public var delegate: AAConvActionSheetDelegate?
    
    private let sheetView = UIView()
    private let backgroundView = UIView()
    
    private var thumbnailView: AAThumbnailView!
    private var btnCamera = UIButton(type: UIButtonType.System)
    private var btnLibrary = UIButton(type: UIButtonType.System)
    private var btnDocuments = UIButton(type: UIButtonType.System)
    private var btnLocation = UIButton(type: UIButtonType.System)
    private var btnContact = UIButton(type: UIButtonType.System)
    private var btnCancel = UIButton(type: UIButtonType.System)
    
    private weak var presentedInController: UIViewController! = nil
    
    public init() {
        super.init(frame: CGRectZero)
        
        self.backgroundColor = UIColor.clearColor()
    }
    
    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func presentInController(controller: UIViewController) {
        
        if controller.navigationController != nil {
            self.presentedInController = controller.navigationController
        } else {
            self.presentedInController = controller
        }
        
        if let navigation = presentedInController as? UINavigationController {
            navigation.interactivePopGestureRecognizer?.enabled = false
        } else if let navigation = presentedInController.navigationController {
            navigation.interactivePopGestureRecognizer?.enabled = false
        }
        
        frame = presentedInController.view.bounds
        presentedInController.view.addSubview(self)
        
        setupAllViews()
        
        self.sheetView.frame = CGRectMake(0, self.frame.height, self.frame.width, 400)
        self.backgroundView.alpha = 0
        dispatchOnUi { () -> Void in
            UIView.animateWithDuration(0.4, delay: 0.0, usingSpringWithDamping: 0.7,
                initialSpringVelocity: 0.6, options: .CurveEaseInOut, animations: {
                    self.sheetView.frame = CGRectMake(0, self.frame.height - 400, self.frame.width, 400)
                    self.backgroundView.alpha = 1
                }, completion: nil)
        }
    }
    
    public func dismiss() {
        var nextFrame = self.sheetView.frame
        nextFrame.origin.y = self.presentedInController.view.height
        
        if let navigation = presentedInController as? UINavigationController {
            navigation.interactivePopGestureRecognizer?.enabled = true
        } else if let navigation = presentedInController.navigationController {
            navigation.interactivePopGestureRecognizer?.enabled = true
        }
        
        UIView.animateWithDuration(0.25, animations: { () -> Void in
            self.sheetView.frame = nextFrame
            self.backgroundView.alpha = 0}) { (bool) -> Void in
                self.delegate = nil
                self.thumbnailView.dismiss()
                self.thumbnailView = nil
                self.removeFromSuperview()
        }
    }

    private func setupAllViews() {
        
        
        // sheet view
        
        let superWidth = presentedInController.view.width
        let superHeight = presentedInController.view.height
        
        self.backgroundView.frame = presentedInController.view.bounds
        self.backgroundView.backgroundColor = UIColor.alphaBlack(0.7)
        self.backgroundView.alpha = 0
        self.addSubview(self.backgroundView)
        
        self.sheetView.frame = CGRectMake(0, superHeight - 400, superWidth, 400)
        self.sheetView.backgroundColor = UIColor.whiteColor()
        self.addSubview(self.sheetView)
        
        self.thumbnailView = AAThumbnailView(frame: CGRectMake(0, 5, superWidth, 90))
        self.thumbnailView.delegate = self
        self.thumbnailView.open()
        self.btnCamera      = UIButton(type: UIButtonType.System)
        self.btnLibrary     = UIButton(type: UIButtonType.System)
        self.btnDocuments   = UIButton(type: UIButtonType.System)
        self.btnLocation    = UIButton(type: UIButtonType.System)
        self.btnContact     = UIButton(type: UIButtonType.System)
        self.btnCancel      = UIButton(type: UIButtonType.System)
        
        // color
        
        self.btnCamera.tintColor        = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
        self.btnLibrary.tintColor       = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
        self.btnDocuments.tintColor     = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
        self.btnLocation.tintColor      = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
        self.btnContact.tintColor       = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
        self.btnCancel.tintColor        = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
        
        // font size
        
        self.btnCamera.titleLabel?.font         = UIFont.systemFontOfSize(17)
        self.btnLibrary.titleLabel?.font        = UIFont.systemFontOfSize(17)
        self.btnDocuments.titleLabel?.font      = UIFont.systemFontOfSize(17)
        self.btnLocation.titleLabel?.font       = UIFont.systemFontOfSize(17)
        self.btnContact.titleLabel?.font        = UIFont.systemFontOfSize(17)
        self.btnCancel.titleLabel?.font         = UIFont.systemFontOfSize(17)
        
        // add buttons as subivews
        
        self.sheetView.addSubview(self.btnCamera)
        self.sheetView.addSubview(self.btnLibrary)
        self.sheetView.addSubview(self.btnDocuments)
        self.sheetView.addSubview(self.btnLocation)
        self.sheetView.addSubview(self.btnContact)
        self.sheetView.addSubview(self.btnCancel)
        self.sheetView.addSubview(self.thumbnailView)
        
        self.thumbnailView.frame    = CGRectMake(0, 5, superWidth, 90)
        self.btnCamera.frame        = CGRectMake(0, 100, superWidth, 50)
        self.btnLibrary.frame       = CGRectMake(0, 150, superWidth, 50)
        self.btnDocuments.frame     = CGRectMake(0, 200, superWidth, 50)
        self.btnLocation.frame      = CGRectMake(0, 250, superWidth, 50)
        self.btnContact.frame       = CGRectMake(0, 300, superWidth, 50)
        self.btnCancel.frame        = CGRectMake(0, 350, superWidth, 50)
        
        // separators
        
        let spearator1 = UIView(frame: CGRectMake(0, 99, superWidth, 1))
        spearator1.backgroundColor = UIColor(red: 223.9/255.0, green: 223.9/255.0, blue: 223.9/255.0, alpha: 0.6)
        let spearator2 = UIView(frame: CGRectMake(10, 149, superWidth - 20, 1))
        spearator2.backgroundColor = UIColor(red: 230.0/255.0, green: 230.0/255.0, blue: 230.0/255.0, alpha: 0.6)
        let spearator3 = UIView(frame: CGRectMake(10, 199, superWidth - 20, 1))
        spearator3.backgroundColor = UIColor(red: 230.0/255.0, green: 230.0/255.0, blue: 230.0/255.0, alpha: 0.6)
        let spearator4 = UIView(frame: CGRectMake(10, 249, superWidth - 20, 1))
        spearator4.backgroundColor = UIColor(red: 230.0/255.0, green: 230.0/255.0, blue: 230.0/255.0, alpha: 0.6)
        let spearator5 = UIView(frame: CGRectMake(10, 299, superWidth - 20, 1))
        spearator5.backgroundColor = UIColor(red: 230.0/255.0, green: 230.0/255.0, blue: 230.0/255.0, alpha: 0.6)
        let spearator6 = UIView(frame: CGRectMake(10, 349, superWidth - 20, 1))
        spearator6.backgroundColor = UIColor(red: 230.0/255.0, green: 230.0/255.0, blue: 230.0/255.0, alpha: 0.6)
        
        // add separatos as subview
        
        self.sheetView.addSubview(spearator1)
        self.sheetView.addSubview(spearator2)
        self.sheetView.addSubview(spearator3)
        self.sheetView.addSubview(spearator4)
        self.sheetView.addSubview(spearator5)
        self.sheetView.addSubview(spearator6)
        
        // set title for buttons
        
        self.btnCamera.setTitle(AALocalized("PhotoCamera"), forState: UIControlState.Normal)
        self.btnLibrary.setTitle(AALocalized("PhotoLibrary"), forState: UIControlState.Normal)
        self.btnDocuments.setTitle(AALocalized("SendDocument"), forState: UIControlState.Normal)
        self.btnLocation.setTitle(AALocalized("ShareLocation"), forState: UIControlState.Normal)
        self.btnContact.setTitle(AALocalized("ShareContact"), forState: UIControlState.Normal)
        self.btnCancel.setTitle(AALocalized("AlertCancel"), forState: UIControlState.Normal)
        
        // add actins
        
        self.btnCamera.addTarget(self, action: #selector(AAConvActionSheet.btnCameraAction), forControlEvents: UIControlEvents.TouchUpInside)
        self.btnLibrary.addTarget(self, action: #selector(AAConvActionSheet.btnLibraryAction), forControlEvents: UIControlEvents.TouchUpInside)
        self.btnDocuments.addTarget(self, action: #selector(AAConvActionSheet.btnDocumentAction), forControlEvents: UIControlEvents.TouchUpInside)
        self.btnLocation.addTarget(self, action: #selector(AAConvActionSheet.btnLocationAction), forControlEvents: UIControlEvents.TouchUpInside)
        self.btnContact.addTarget(self, action: #selector(AAConvActionSheet.btnContactAction), forControlEvents: UIControlEvents.TouchUpInside)
        self.btnCancel.addTarget(self, action: #selector(AAConvActionSheet.btnCloseAction), forControlEvents: UIControlEvents.TouchUpInside)
    }
    
    public func thumbnailSelectedUpdated(selectedAssets: [PHAsset]) {
        if selectedAssets.count > 0 {
            
            var sendString:String
            if selectedAssets.count == 1 {
                sendString = AALocalized("AttachmentsSendPhoto").replace("{count}", dest: "\(selectedAssets.count)")
            } else {
                sendString = AALocalized("AttachmentsSendPhotos").replace("{count}", dest: "\(selectedAssets.count)")
            }
            
            //
            // remove target
            //
            self.btnCamera.removeTarget(self, action: #selector(AAConvActionSheet.btnCameraAction), forControlEvents: UIControlEvents.TouchUpInside)
            
            //
            // add new target
            //
            self.btnCamera.setTitle(sendString, forState: UIControlState.Normal)
            self.btnCamera.addTarget(self, action: #selector(AAConvActionSheet.sendPhotos), forControlEvents: UIControlEvents.TouchUpInside)
            self.btnCamera.titleLabel?.font = UIFont(name: "HelveticaNeue-Medium", size: 17)
            
            
        } else {
            
            //
            // remove target
            //
            self.btnCamera.removeTarget(self, action: #selector(AAConvActionSheet.sendPhotos), forControlEvents: UIControlEvents.TouchUpInside)
            
            //
            // add new target
            //
            self.btnCamera.setTitle(AALocalized("PhotoCamera"), forState: UIControlState.Normal)
            self.btnCamera.addTarget(self, action: #selector(AAConvActionSheet.btnCameraAction), forControlEvents: UIControlEvents.TouchUpInside)
            self.btnCamera.titleLabel?.font = UIFont.systemFontOfSize(17)
            
        }
    }

    //
    // Actions
    //
    
    func sendPhotos() {
        self.thumbnailView.getSelectedAsImages { (images) -> () in
            self.delegate?.actionSheetPickedImages(images)
        }
        dismiss()
    }

    func btnCameraAction() {
        delegate?.actionSheetPickCamera()
        dismiss()
    }
    
    func btnLibraryAction() {
        delegate?.actionSheetPickGallery()
        dismiss()
    }
    
    func btnDocumentAction() {
        delegate?.actionSheetPickDocument()
        dismiss()
    }
    
    func btnLocationAction() {
        delegate?.actionSheetPickLocation()
        dismiss()
    }
    
    func btnContactAction() {
        delegate?.actionSheetPickContact()
        dismiss()
    }
    
    func btnCloseAction() {
        dismiss()
    }
    
    public override func touchesBegan(touches: Set<UITouch>, withEvent event: UIEvent?) {
        dismiss()
    }
}
