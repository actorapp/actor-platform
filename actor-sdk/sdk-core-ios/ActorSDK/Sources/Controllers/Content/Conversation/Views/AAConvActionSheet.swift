//
//  AAConvActionSheet.swift
//  ActorSDK
//
//  Created by kioshimafx on 1/13/16.
//  Copyright © 2016 Steve Kite. All rights reserved.
//

import UIKit

protocol AAActionSheetDelegate{
    func actionSheetDidFinished(selectedObjs:Array<AnyObject>)
}

class AAConvActionSheet: UIView {

    var delegate:AAActionSheetDelegate?
    
    var sheetView:UIView!
    var btnAlbum:UIButton!
    var btnCamera:UIButton!
    var btnCancel:UIButton!
    var thumbnailView:AAThumbnailView!
    
    weak var weakSuper : ConversationViewController!
    
    
    let screenWidth = UIScreen.mainScreen().bounds.size.width
    let screenHeigth = UIScreen.mainScreen().bounds.size.height
    
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
        
        self.addSubview(self.sheetView)
        
//        self.sheetView.addSubview(self.btnCancel)
//        self.sheetView.addSubview(self.btnAlbum)
//        self.sheetView.addSubview(self.btnCamera)
//        self.sheetView.addSubview(self.thumbnailView)
        
        self.configNotification()
        
    }
    
    func configNotification() {
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "", name: kNotificationSendPhotos, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "", name: kNotificationUpdateSelected, object: nil)
        
    }
    
    deinit {
        
        self.weakSuper = nil
        
        AAASAssetManager.sharedInstance.clearData()
        NSNotificationCenter.defaultCenter().removeObserver(self)
        
    }
    
    
    func showAnimation() {
        
        //self.btnCamera.selected = true
        //self.thumbnailView.reloadView()
        //self.btnCamera.setTitle("Camera", forState: UIControlState.Normal)
        
        var frame = self.sheetView.frame
        frame.origin.y = screenHeigth - 350
        
        UIView.animateWithDuration(0.25) { () -> Void in
            self.sheetView.frame = frame
            self.alpha = 1
            
        }
        
        
    }
    
    func cancelAnimation() {
        
        var frame = self.sheetView.frame
        frame.origin.y = screenHeigth
        
        UIView.animateWithDuration(0.25) { () -> Void in
            self.sheetView.frame = frame
            self.alpha = 0
            
        }
        
    }
    
    override func touchesBegan(touches: Set<UITouch>, withEvent event: UIEvent?) {
        cancelAnimation()
    }
    
    func setupAllViews() {
        
        
        // sheet view
        let frame = CGRectMake(0, screenHeigth, screenWidth, 350)
        self.sheetView = UIView(frame: frame)
        self.sheetView.backgroundColor = UIColor(red: 230.0/255.0, green: 231.0/255.0, blue: 234.0/255.0, alpha: 1)
        
        
        // button cancel
        
        self.btnCancel = UIButton(type: UIButtonType.Custom)
        
        // thumbnail view 

        
        
        
    }
    

}
