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
}

typealias AAConvActionSheetButtonHandler = (convActionSheet: AAConvActionSheet, presentedInController: UIViewController) -> Void

public class AAConvActionSheetButton {
    
    var title: NSString!
    var handler: AAConvActionSheetButtonHandler
    
    init (title: NSString, handler: AAConvActionSheetButtonHandler) {
        self.title = title
        self.handler = handler
    }
}

public class AAConvActionSheet: UIView, AAThumbnailViewDelegate {
    
    public var delegate: AAConvActionSheetDelegate?
    
    private let sheetView = UIView()
    private let backgroundView = UIView()
    private var thumbnailView: AAThumbnailView!
    private var scrollView : UIScrollView!
    
    private var btnCamera = UIButton(type: UIButtonType.System)
    private var btnCancel = UIButton(type: UIButtonType.System)
    private var additionalButtons = [AAConvActionSheetButton]()
    private var buttons = [UIButton]()
    
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
        
        self.sheetView.frame = CGRectMake(0, self.frame.height, self.frame.width, self.sheetView.frame.size.height)
        self.backgroundView.alpha = 0
        dispatchOnUi { () -> Void in
            UIView.animateWithDuration(0.4, delay: 0.0, usingSpringWithDamping: 0.7,
                initialSpringVelocity: 0.6, options: .CurveEaseInOut, animations: {
                    self.sheetView.frame = CGRectMake(0, self.frame.height - self.sheetView.frame.size.height, self.frame.width, self.sheetView.frame.size.height)
                    self.backgroundView.alpha = 1
                }, completion: { (finish) -> Void in
                    if self.scrollView.frame.size.height < self.scrollView.contentSize.height {
                       self.scrollView.flashScrollIndicators()
                    }
                })
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
        
        // prepare buttons for standard actions
        
        self.btnCamera = self.makeButon(AALocalized("PhotoCamera"), target: self, action: "btnCameraAction")
        self.buttons.append(self.btnCamera)
        self.buttons.append(self.makeButon(AALocalized("PhotoLibrary"), target: self, action: "btnLibraryAction"))
        self.buttons.append(self.makeButon(AALocalized("SendDocument"), target: self, action: "btnDocumentAction"))
        self.buttons.append(self.makeButon(AALocalized("ShareLocation"), target: self, action: "btnLocationAction"))
        self.buttons.append(self.makeButon(AALocalized("ShareContact"), target: self, action: "btnContactAction"))
        
        // add additional buttons (if needed)
        
        self.additionalButtons = ActorSDK.sharedActor().delegate.actorConvActionSheetAdditionalButtons()
        for (index, additionalButton) in self.additionalButtons.enumerate() {
            let button = self.makeButon(AALocalized(additionalButton.title! as String), target: self, action: "btnAction:")
            button.tag = index
            self.buttons.append(button)
        }
        
        // prepare sheet view
        
        let buttonHeight : CGFloat = 50.0
        let thumbnailViewHeight : CGFloat = 90.0
        let thumbnailViewMargin : CGFloat = 5.0
        
        let sheetViewHeight = min(self.maxSheetHeight(), CGFloat(self.buttons.count + 1) * buttonHeight + thumbnailViewMargin * 2 + thumbnailViewHeight)
        let scrollViewHeight = sheetViewHeight - thumbnailViewMargin * 2 - thumbnailViewHeight - buttonHeight
        
        let superWidth = presentedInController.view.width
        let superHeight = presentedInController.view.height
        
        self.backgroundView.frame = presentedInController.view.bounds
        self.backgroundView.backgroundColor = UIColor.alphaBlack(0.7)
        self.backgroundView.alpha = 0
        self.addSubview(self.backgroundView)
        
        self.sheetView.frame = CGRectMake(0, superHeight - sheetViewHeight, superWidth, sheetViewHeight)
        self.sheetView.backgroundColor = UIColor.whiteColor()
        self.addSubview(self.sheetView)
        
        self.scrollView = UIScrollView(frame: CGRectMake(0, thumbnailViewMargin * 2 + thumbnailViewHeight, superWidth, scrollViewHeight))
        self.scrollView.contentSize = CGSizeMake(superWidth, CGFloat(self.buttons.count) * buttonHeight)
        self.sheetView.addSubview(self.scrollView)
        
        self.thumbnailView = AAThumbnailView(frame: CGRectMake(0, thumbnailViewMargin, superWidth, thumbnailViewHeight))
        self.thumbnailView.delegate = self
        self.thumbnailView.open()
        self.sheetView.addSubview(self.thumbnailView)
        
        // put buttons to scrollview
        
        for (index, button) in self.buttons.enumerate() {
            
            button.frame = CGRectMake(0, CGFloat(index) * buttonHeight, superWidth, buttonHeight)
            self.scrollView.addSubview(button)
            
            if index > 0 {
                self.scrollView.addSubview(self.makeSeparator(CGRectMake(15, button.frame.origin.y - 1, superWidth - 30, 1)))
            }
        }
        
        // put cancel button at the bottom
        
        self.btnCancel = self.makeButon(AALocalized("AlertCancel"), target: self, action: "btnCloseAction")
        self.btnCancel.titleLabel?.font = UIFont.boldSystemFontOfSize(17)
        self.btnCancel.frame = CGRectMake(0, sheetViewHeight - buttonHeight, superWidth, buttonHeight)
        self.sheetView.addSubview(self.btnCancel)
        
        // put top and bottom button separators
        
        self.sheetView.addSubview(self.makeSeparator(CGRectMake(0, self.scrollView.frame.origin.y - 1, superWidth, 1)))
        self.sheetView.addSubview(self.makeSeparator(CGRectMake(0, self.scrollView.frame.origin.y + self.scrollView.frame.size.height - 1, superWidth, 1)))
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
            self.btnCamera.removeTarget(self, action: "btnCameraAction", forControlEvents: UIControlEvents.TouchUpInside)
            
            //
            // add new target
            //
            self.btnCamera.setTitle(sendString, forState: UIControlState.Normal)
            self.btnCamera.addTarget(self, action: "sendPhotos", forControlEvents: UIControlEvents.TouchUpInside)
            self.btnCamera.titleLabel?.font = UIFont(name: "HelveticaNeue-Medium", size: 17)
            
            
        } else {
            
            //
            // remove target
            //
            self.btnCamera.removeTarget(self, action: "sendPhotos", forControlEvents: UIControlEvents.TouchUpInside)
            
            //
            // add new target
            //
            self.btnCamera.setTitle(AALocalized("PhotoCamera"), forState: UIControlState.Normal)
            self.btnCamera.addTarget(self, action: "btnCameraAction", forControlEvents: UIControlEvents.TouchUpInside)
            self.btnCamera.titleLabel?.font = UIFont.systemFontOfSize(17)
            
        }
    }
    
    private func maxSheetHeight() -> CGFloat {
        
        if let maxHeight = ActorSDK.sharedActor().delegate.actorConvActionSheetMaxHeight() {
            return maxHeight
        }
        
        if (AADevice.isiPad) {
            return 500
        }
        else if (AADevice.isiPhone4) {
            return 400
        }
        else if (AADevice.isiPhone5 || AADevice.isZoomediPhone6) {
            return 400
        }
        else if (AADevice.isiPhone6 || AADevice.isZoomediPhone6P) {
            return 450
        }
        else if (AADevice.isiPhone6P) {
            return 500
        }
        else {
            return 400
        }
    }
    
    private func makeButon(title: NSString, target: AnyObject?, action: Selector) -> UIButton {
        let button = UIButton(type: UIButtonType.System)
        button.tintColor = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
        button.titleLabel?.font = UIFont.systemFontOfSize(17)
        button.setTitle(AALocalized(title as String), forState: UIControlState.Normal)
        button.addTarget(target, action: action, forControlEvents: UIControlEvents.TouchUpInside)
        return button
    }
    
    private func makeSeparator(frame: CGRect) -> UIView {
        let separator = UIView(frame: frame)
        separator.backgroundColor = UIColor(red: 223.9/255.0, green: 223.9/255.0, blue: 223.9/255.0, alpha: 0.6)
        return separator
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
    
    func btnAction(sender: UIButton!) {
        self.additionalButtons[sender.tag].handler(convActionSheet: self, presentedInController: self.presentedInController)
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
