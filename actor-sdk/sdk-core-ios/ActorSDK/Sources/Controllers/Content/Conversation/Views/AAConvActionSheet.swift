//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import Photos

public protocol AAConvActionSheetDelegate {
    func actionSheetPickedImages(images:[(NSData,Bool)])
    func actionSheetPickCamera()
    func actionSheetPickGallery()
    func actionSheetCustomButton(index: Int)
}

public class AAConvActionSheet: UIView, AAThumbnailViewDelegate {
    
    public var delegate: AAConvActionSheetDelegate?
    
    private let sheetView = UIView()
    private let backgroundView = UIView()
    private var sheetViewHeight: CGFloat = 0
    
    private var thumbnailView: AAThumbnailView!
    private var buttons = [UIButton]()
    private var btnCamera: UIButton!
    private var btnLibrary: UIButton!
    private var btnCancel: UIButton!
    
    private weak var presentedInController: UIViewController! = nil
    
    public var enablePhotoPicker: Bool = true
    private var customActions = [String]()
    
    public init() {
        super.init(frame: CGRectZero)
        
        self.backgroundColor = UIColor.clearColor()
    }
    
    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func addCustomButton(title: String){
        customActions.append(title)
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
        
        self.sheetView.frame = CGRectMake(0, self.frame.height, self.frame.width, sheetViewHeight)
        self.backgroundView.alpha = 0
        dispatchOnUi { () -> Void in
            UIView.animateWithDuration(0.4, delay: 0.0, usingSpringWithDamping: 0.7,
                initialSpringVelocity: 0.6, options: .CurveEaseInOut, animations: {
                    self.sheetView.frame = CGRectMake(0, self.frame.height - self.sheetViewHeight, self.frame.width, self.sheetViewHeight)
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
                if self.thumbnailView != nil {
                    self.thumbnailView.dismiss()
                    self.thumbnailView = nil
                }
                self.removeFromSuperview()
        }
    }

    private func setupAllViews() {
        
        
        //
        // Root Views
        //
        
        let superWidth = presentedInController.view.width
        let superHeight = presentedInController.view.height
        
        self.backgroundView.frame = presentedInController.view.bounds
        self.backgroundView.backgroundColor = UIColor.alphaBlack(0.7)
        self.backgroundView.alpha = 0
        self.addSubview(self.backgroundView)
        
        
        //
        // Init Action Views
        //
        
        self.sheetViewHeight = 10
        
        self.buttons.removeAll()
        
        if enablePhotoPicker {
            
            self.thumbnailView = AAThumbnailView(frame: CGRectMake(0, 5, superWidth, 90))
            self.thumbnailView.delegate = self
            self.thumbnailView.open()
            
            self.btnCamera = {
                let button = UIButton(type: UIButtonType.System)
                button.tintColor = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
                button.titleLabel?.font = UIFont.systemFontOfSize(17)
                button.setTitle(AALocalized("PhotoCamera"), forState: UIControlState.Normal)
                button.addTarget(self, action: #selector(AAConvActionSheet.btnCameraAction), forControlEvents: UIControlEvents.TouchUpInside)
                return button
            }()
            self.buttons.append(self.btnCamera)
            
            self.btnLibrary = {
                let button = UIButton(type: UIButtonType.System)
                button.tintColor = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
                button.titleLabel?.font = UIFont.systemFontOfSize(17)
                button.setTitle(AALocalized("PhotoLibrary"), forState: UIControlState.Normal)
                button.addTarget(self, action: #selector(AAConvActionSheet.btnLibraryAction), forControlEvents: UIControlEvents.TouchUpInside)
                return button
            }()
            self.buttons.append(self.btnLibrary)
            
            sheetViewHeight = 100
        }
        
        for i in 0..<customActions.count {
            let b = customActions[i]
            self.buttons.append({
                let button = UIButton(type: UIButtonType.System)
                button.tintColor = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
                button.titleLabel?.font = UIFont.systemFontOfSize(17)
                button.setTitle(AALocalized(b), forState: UIControlState.Normal)
                button.tag = i
                button.addTarget(self, action: #selector(AAConvActionSheet.btnCustomAction(_:)), forControlEvents: UIControlEvents.TouchUpInside)
                return button
            }())
        }
        
        self.btnCancel = {
            let button = UIButton(type: UIButtonType.System)
            button.tintColor = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
            button.titleLabel?.font = UIFont.systemFontOfSize(17)
            button.setTitle(AALocalized("AlertCancel"), forState: UIControlState.Normal)
            button.addTarget(self, action: #selector(AAConvActionSheet.btnCloseAction), forControlEvents: UIControlEvents.TouchUpInside)
            return button
        }()
        self.buttons.append(self.btnCancel)
        
        sheetViewHeight += CGFloat(self.buttons.count * 50)
        
        
        //
        // Adding Elements
        //
        
        for b in self.buttons {
            self.sheetView.addSubview(b)
        }
        if self.thumbnailView != nil {
            self.sheetView.addSubview(self.thumbnailView)
        }
        
        
        //
        // Layouting
        //
        
        self.sheetView.frame = CGRectMake(0, superHeight - sheetViewHeight, superWidth, sheetViewHeight)
        self.sheetView.backgroundColor = UIColor.whiteColor()
        self.addSubview(self.sheetView)
        
        var topOffset: CGFloat = 10
        if self.thumbnailView != nil {
            self.thumbnailView.frame = CGRectMake(0, 5, superWidth, 90)
            topOffset += 90
        }
        for b in self.buttons {
            
            b.frame = CGRectMake(0, topOffset, superWidth, 50)
            
            let spearator = UIView(frame: CGRectMake(0, topOffset - 1, superWidth, 1))
            spearator.backgroundColor = UIColor(red: 223.9/255.0, green: 223.9/255.0, blue: 223.9/255.0, alpha: 0.6)
            self.sheetView.addSubview(spearator)
            
            topOffset += 50
        }
    }
    
    public func thumbnailSelectedUpdated(selectedAssets: [(PHAsset,Bool)]) {
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
            self.btnCamera.addTarget(self, action:#selector(AAConvActionSheet.sendPhotos), forControlEvents: UIControlEvents.TouchUpInside)
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
        if self.thumbnailView != nil {
            self.thumbnailView.getSelectedAsImages { (images:[(NSData,Bool)]) -> () in
                self.delegate?.actionSheetPickedImages(images)
            }
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
    
    func btnCustomAction(sender: UIButton) {
        delegate?.actionSheetCustomButton(sender.tag)
        dismiss()
    }
    
    func btnCloseAction() {
        dismiss()
    }
    
    public override func touchesBegan(touches: Set<UITouch>, withEvent event: UIEvent?) {
        dismiss()
    }
}
