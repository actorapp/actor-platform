//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import Photos

let screenWidth = UIScreen.mainScreen().bounds.size.width
let screenHeigth = UIScreen.mainScreen().bounds.size.height

class AAConvActionSheet: UIView {
    
    var sheetView:UIView!
    var backgroundView:UIView!
    
    var btnCamera:UIButton!
    var btnLibrary:UIButton!
    var btnDocuments:UIButton!
    var btnLocation:UIButton!
    var btnContact:UIButton!
    var btnCancel:UIButton!
    
    var thumbnailView: AAThumbnailView!
    
    weak var weakSuper : ConversationViewController!
    
    init(maxSelected:Int,weakSuperIn:ConversationViewController) {
        super.init(frame: CGRectZero)
        
        
        self.setupAllViews()
        self.configUI()
        self.weakSuper = weakSuperIn
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    /////
    
    func configUI() {
        
        self.alpha = 0
        self.frame = CGRectMake(0, 0, screenWidth, screenHeigth)
        self.backgroundColor = UIColor.clearColor()
        
    }
    
    
    deinit {
        
        self.weakSuper = nil
        
    }
    
    
    func showAnimation() {
        
        var frame = self.sheetView.frame
        frame.origin.y = screenHeigth - 400
        
        self.weakSuper.navigationController!.interactivePopGestureRecognizer!.enabled = false
        
        
        if (self.thumbnailView == nil) {
            
            self.thumbnailView = AAThumbnailView()
            self.sheetView.addSubview(self.thumbnailView)
            self.thumbnailView.frame    = CGRectMake(0, 5, screenWidth, 90)
            self.thumbnailView.bindedConvSheet = self
            
        }
        
        
        self.alpha = 1
        self.backgroundView.alpha = 0
        
        
        UIView.animateWithDuration(0.4, delay: 0.0, usingSpringWithDamping: 0.7, initialSpringVelocity: 0.6, options: UIViewAnimationOptions.CurveEaseInOut, animations: { () -> Void in
            
            self.backgroundView.alpha = 1
            
            self.sheetView.frame = frame
            
            self.thumbnailView.open()
            self.thumbnailView.reloadView()
            
            }, completion: { (complite) -> Void in
                
                // animation complite
                
        })
        
        
    }
    
    func cancelAnimation() {
        
        
        var frame = self.sheetView.frame
        frame.origin.y = screenHeigth
        
        self.weakSuper.rightButton.layoutIfNeeded()
        UIView.animateWithDuration(0.25, animations: { () -> Void in
            self.weakSuper.rightButton.layoutIfNeeded()
            self.sheetView.frame = frame
            
            self.backgroundView.alpha = 0
            
            }) { (bool) -> Void in
                
                self.weakSuper.navigationController!.interactivePopGestureRecognizer!.enabled = true
                
                self.alpha = 0
                
                self.thumbnailView.selectedAssets = [PHAsset]()
                self.thumbnailView.reloadView()
                self.updateSelectedPhotos()
                
                
        }
        
    }
    
    override func touchesBegan(touches: Set<UITouch>, withEvent event: UIEvent?) {
        cancelAnimation()
    }
    
    func setupAllViews() {
        
        
        // sheet view
        
        self.backgroundView = UIView()
        self.backgroundView.frame = CGRectMake(0, 0, screenWidth, screenHeigth)
        self.backgroundView.backgroundColor = UIColor.blackColor().colorWithAlphaComponent(0.7)
        self.backgroundView.alpha = 0
        
        self.addSubview(self.backgroundView)
        
        let frame = CGRectMake(0, screenHeigth, screenWidth, 400)
        self.sheetView = UIView(frame: frame)
        self.sheetView.backgroundColor = UIColor.whiteColor()
        
        self.addSubview(self.sheetView)
        
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
        //self.sheetView.addSubview(self.thumbnailView)
        
        //self.thumbnailView.frame    = CGRectMake(0, 5, screenWidth, 90)
        self.btnCamera.frame        = CGRectMake(0, 100, screenWidth, 50)
        self.btnLibrary.frame       = CGRectMake(0, 150, screenWidth, 50)
        self.btnDocuments.frame     = CGRectMake(0, 200, screenWidth, 50)
        self.btnLocation.frame      = CGRectMake(0, 250, screenWidth, 50)
        self.btnContact.frame       = CGRectMake(0, 300, screenWidth, 50)
        self.btnCancel.frame        = CGRectMake(0, 350, screenWidth, 50)
        
        // separators
        
        let spearator1 = UIView(frame: CGRectMake(0, 99, screenWidth, 1))
        spearator1.backgroundColor = UIColor(red: 223.9/255.0, green: 223.9/255.0, blue: 223.9/255.0, alpha: 0.6)
        let spearator2 = UIView(frame: CGRectMake(10, 149, screenWidth-20, 1))
        spearator2.backgroundColor = UIColor(red: 230.0/255.0, green: 230.0/255.0, blue: 230.0/255.0, alpha: 0.6)
        let spearator3 = UIView(frame: CGRectMake(10, 199, screenWidth-20, 1))
        spearator3.backgroundColor = UIColor(red: 230.0/255.0, green: 230.0/255.0, blue: 230.0/255.0, alpha: 0.6)
        let spearator4 = UIView(frame: CGRectMake(10, 249, screenWidth-20, 1))
        spearator4.backgroundColor = UIColor(red: 230.0/255.0, green: 230.0/255.0, blue: 230.0/255.0, alpha: 0.6)
        let spearator5 = UIView(frame: CGRectMake(10, 299, screenWidth-20, 1))
        spearator5.backgroundColor = UIColor(red: 230.0/255.0, green: 230.0/255.0, blue: 230.0/255.0, alpha: 0.6)
        let spearator6 = UIView(frame: CGRectMake(10, 349, screenWidth-20, 1))
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
        
        self.btnCamera.addTarget(self, action: "btnCameraAction", forControlEvents: UIControlEvents.TouchUpInside)
        self.btnLibrary.addTarget(self, action: "btnLibraryAction", forControlEvents: UIControlEvents.TouchUpInside)
        self.btnDocuments.addTarget(self, action: "btnDocumentAction", forControlEvents: UIControlEvents.TouchUpInside)
        self.btnLocation.addTarget(self, action: "btnLocationAction", forControlEvents: UIControlEvents.TouchUpInside)
        self.btnContact.addTarget(self, action: "btnContactAction", forControlEvents: UIControlEvents.TouchUpInside)
        self.btnCancel.addTarget(self, action: "btnCloseAction", forControlEvents: UIControlEvents.TouchUpInside)
        
        // bineded self
        
        //self.thumbnailView.bindedConvSheet = self
    
    }
    
    //MARK: - Button's actions
    
    func btnCameraAction() {
        cancelAnimation()
        self.weakSuper.pickImage(.Camera)
    }
    
    func btnLibraryAction() {
        cancelAnimation()
        self.weakSuper.pickImage(.PhotoLibrary)
    }
    
    func btnDocumentAction() {
        cancelAnimation()
        
        if (NSFileManager.defaultManager().ubiquityIdentityToken != nil) {
            self.weakSuper.pickDocument()
        }
        
    }
    
    func btnLocationAction() {
        cancelAnimation()
        self.weakSuper.pickLocation()
    }
    
    func btnContactAction() {
        cancelAnimation()
        self.weakSuper.pickContact()
    }
    
    func btnCloseAction() {
        cancelAnimation()
    }
    
    func updateSelectedPhotos() {
        
        if self.thumbnailView.selectedAssets.count > 0 {
            
            var sendString:String!
            
            if self.thumbnailView.selectedAssets.count == 1 {
                sendString = AALocalized("AttachmentsSendPhoto").replace("{count}", dest: "\(self.thumbnailView.selectedAssets.count)")
            } else {
                sendString = AALocalized("AttachmentsSendPhotos").replace("{count}", dest: "\(self.thumbnailView.selectedAssets.count)")
            }
            
            // remove target
            self.btnCamera.removeTarget(self, action: "btnCameraAction", forControlEvents: UIControlEvents.TouchUpInside)
            
            // add new target
            self.btnCamera.setTitle(sendString, forState: UIControlState.Normal)
            self.btnCamera.addTarget(self, action: "sendPhotos", forControlEvents: UIControlEvents.TouchUpInside)
            self.btnCamera.titleLabel?.font = UIFont(name: "HelveticaNeue-Medium", size: 17)
            
            
        } else {
            
            // remove target
            self.btnCamera.removeTarget(self, action: "sendPhotos", forControlEvents: UIControlEvents.TouchUpInside)
            
            
            // add new target
            self.btnCamera.setTitle(AALocalized("PhotoCamera"), forState: UIControlState.Normal)
            self.btnCamera.addTarget(self, action: "btnCameraAction", forControlEvents: UIControlEvents.TouchUpInside)
            self.btnCamera.titleLabel?.font = UIFont.systemFontOfSize(17)
            
        }
        
    }
    
    func sendPhotos() {
        
        self.cancelAnimation()
        
        let priority = DISPATCH_QUEUE_PRIORITY_DEFAULT
        dispatch_async(dispatch_get_global_queue(priority, 0)) {
            
            self.thumbnailView.getSelectedAsImages({ (images) -> () in
                let arrayModelsForSend = images
                self.thumbnailView.selectedAssets = [PHAsset]()
                
                for (_,image) in arrayModelsForSend.enumerate() {
                    
                    self.weakSuper.sendImageFromActionSheet(image)
                    
                }
                
                dispatch_async(dispatch_get_main_queue()) {
                    self.updateSelectedPhotos()
                }
            })
            

        }
        
    }
    

}
