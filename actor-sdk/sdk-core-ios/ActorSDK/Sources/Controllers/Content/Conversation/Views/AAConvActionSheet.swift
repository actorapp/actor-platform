//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import Photos

public protocol AAConvActionSheetDelegate {
    func actionSheetPickedImages(_ images:[(Data,Bool)])
    func actionSheetPickCamera()
    func actionSheetPickGallery()
    func actionSheetCustomButton(_ index: Int)
}

open class AAConvActionSheet: UIView, AAThumbnailViewDelegate {
    
    open var delegate: AAConvActionSheetDelegate?
    
    fileprivate let sheetView = UIView()
    fileprivate let backgroundView = UIView()
    fileprivate var sheetViewHeight: CGFloat = 0
    
    fileprivate var thumbnailView: AAThumbnailView!
    fileprivate var buttons = [UIButton]()
    fileprivate var btnCamera: UIButton!
    fileprivate var btnLibrary: UIButton!
    fileprivate var btnCancel: UIButton!
    
    fileprivate weak var presentedInController: UIViewController! = nil
    
    open var enablePhotoPicker: Bool = true
    fileprivate var customActions = [String]()
    
    public init() {
        super.init(frame: CGRect.zero)
        
        self.backgroundColor = UIColor.clear
    }
    
    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open func addCustomButton(_ title: String){
        customActions.append(title)
    }
    
    open func presentInController(_ controller: UIViewController) {
        
        if controller.navigationController != nil {
            self.presentedInController = controller.navigationController
        } else {
            self.presentedInController = controller
        }
        
        if let navigation = presentedInController as? UINavigationController {
            navigation.interactivePopGestureRecognizer?.isEnabled = false
        } else if let navigation = presentedInController.navigationController {
            navigation.interactivePopGestureRecognizer?.isEnabled = false
        }
        
        frame = presentedInController.view.bounds
        presentedInController.view.addSubview(self)
        
        setupAllViews()
        
        self.sheetView.frame = CGRect(x: 0, y: self.frame.height, width: self.frame.width, height: sheetViewHeight)
        self.backgroundView.alpha = 0
        dispatchOnUi { () -> Void in
            UIView.animate(withDuration: 0.4, delay: 0.0, usingSpringWithDamping: 0.7,
                initialSpringVelocity: 0.6, options: UIViewAnimationOptions(), animations: {
                    self.sheetView.frame = CGRect(x: 0, y: self.frame.height - self.sheetViewHeight, width: self.frame.width, height: self.sheetViewHeight)
                    self.backgroundView.alpha = 1
                }, completion: nil)
        }
    }
    
    open func dismiss() {
        var nextFrame = self.sheetView.frame
        nextFrame.origin.y = self.presentedInController.view.height
        
        if let navigation = presentedInController as? UINavigationController {
            navigation.interactivePopGestureRecognizer?.isEnabled = true
        } else if let navigation = presentedInController.navigationController {
            navigation.interactivePopGestureRecognizer?.isEnabled = true
        }
        
        UIView.animate(withDuration: 0.25, animations: { () -> Void in
            self.sheetView.frame = nextFrame
            self.backgroundView.alpha = 0}, completion: { (bool) -> Void in
                self.delegate = nil
                if self.thumbnailView != nil {
                    self.thumbnailView.dismiss()
                    self.thumbnailView = nil
                }
                self.removeFromSuperview()
        }) 
    }

    fileprivate func setupAllViews() {
        
        
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
            
            self.thumbnailView = AAThumbnailView(frame: CGRect(x: 0, y: 5, width: superWidth, height: 90))
            self.thumbnailView.delegate = self
            self.thumbnailView.open()
            
            self.btnCamera = {
                let button = UIButton(type: UIButtonType.system)
                button.tintColor = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
                button.titleLabel?.font = UIFont.systemFont(ofSize: 17)
                button.setTitle(AALocalized("PhotoCamera"), for: UIControlState())
                button.addTarget(self, action: #selector(AAConvActionSheet.btnCameraAction), for: UIControlEvents.touchUpInside)
                return button
            }()
            self.buttons.append(self.btnCamera)
            
            self.btnLibrary = {
                let button = UIButton(type: UIButtonType.system)
                button.tintColor = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
                button.titleLabel?.font = UIFont.systemFont(ofSize: 17)
                button.setTitle(AALocalized("PhotoLibrary"), for: UIControlState())
                button.addTarget(self, action: #selector(AAConvActionSheet.btnLibraryAction), for: UIControlEvents.touchUpInside)
                return button
            }()
            self.buttons.append(self.btnLibrary)
            
            sheetViewHeight = 100
        }
        
        for i in 0..<customActions.count {
            let b = customActions[i]
            self.buttons.append({
                let button = UIButton(type: UIButtonType.system)
                button.tintColor = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
                button.titleLabel?.font = UIFont.systemFont(ofSize: 17)
                button.setTitle(AALocalized(b), for: UIControlState())
                button.tag = i
                button.addTarget(self, action: #selector(AAConvActionSheet.btnCustomAction(_:)), for: UIControlEvents.touchUpInside)
                return button
            }())
        }
        
        self.btnCancel = {
            let button = UIButton(type: UIButtonType.system)
            button.tintColor = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
            button.titleLabel?.font = UIFont.systemFont(ofSize: 17)
            button.setTitle(AALocalized("AlertCancel"), for: UIControlState())
            button.addTarget(self, action: #selector(AAConvActionSheet.btnCloseAction), for: UIControlEvents.touchUpInside)
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
        
        self.sheetView.frame = CGRect(x: 0, y: superHeight - sheetViewHeight, width: superWidth, height: sheetViewHeight)
        self.sheetView.backgroundColor = UIColor.white
        self.addSubview(self.sheetView)
        
        var topOffset: CGFloat = 10
        if self.thumbnailView != nil {
            self.thumbnailView.frame = CGRect(x: 0, y: 5, width: superWidth, height: 90)
            topOffset += 90
        }
        for b in self.buttons {
            
            b.frame = CGRect(x: 0, y: topOffset, width: superWidth, height: 50)
            
            let spearator = UIView(frame: CGRect(x: 0, y: topOffset - 1, width: superWidth, height: 1))
            spearator.backgroundColor = UIColor(red: 223.9/255.0, green: 223.9/255.0, blue: 223.9/255.0, alpha: 0.6)
            self.sheetView.addSubview(spearator)
            
            topOffset += 50
        }
    }
    
    open func thumbnailSelectedUpdated(_ selectedAssets: [(PHAsset,Bool)]) {
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
            self.btnCamera.removeTarget(self, action: #selector(AAConvActionSheet.btnCameraAction), for: UIControlEvents.touchUpInside)
            
            //
            // add new target
            //
    
            self.btnCamera.setTitle(sendString, for: UIControlState())
            self.btnCamera.addTarget(self, action:#selector(AAConvActionSheet.sendPhotos), for: UIControlEvents.touchUpInside)
            self.btnCamera.titleLabel?.font = UIFont(name: "HelveticaNeue-Medium", size: 17)
            
            
        } else {
            
            //
            // remove target
            //
            self.btnCamera.removeTarget(self, action: #selector(AAConvActionSheet.sendPhotos), for: UIControlEvents.touchUpInside)
            
            //
            // add new target
            //
            self.btnCamera.setTitle(AALocalized("PhotoCamera"), for: UIControlState())
            self.btnCamera.addTarget(self, action: #selector(AAConvActionSheet.btnCameraAction), for: UIControlEvents.touchUpInside)
            self.btnCamera.titleLabel?.font = UIFont.systemFont(ofSize: 17)
            
        }
    }

    //
    // Actions
    //
    
    func sendPhotos() {
        if self.thumbnailView != nil {
            self.thumbnailView.getSelectedAsImages { (images:[(Data,Bool)]) -> () in
                (self.delegate?.actionSheetPickedImages(images))!
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
    
    func btnCustomAction(_ sender: UIButton) {
        delegate?.actionSheetCustomButton(sender.tag)
        dismiss()
    }
    
    func btnCloseAction() {
        dismiss()
    }
    
    open override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        dismiss()
    }
}
