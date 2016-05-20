//
//  AAUnblockActionSheet.swift
//  ActorSDK
//
//  Created by Alexey Galaev on 4/17/16.
//  Copyright © 2016 Steve Kite. All rights reserved.
//

import UIKit

class AAUnblockActionSheet: AAConvActionSheet {
    
    private let sheetView               = UIView()
    private let backgroundView          = UIView()
    
    private var unBlockButton = UIButton(type: UIButtonType.System)
    
    private weak var presentedInController: UIViewController! = nil
    
    override init() {
        super.init()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func presentInController(controller: UIViewController) {
        
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
        
        self.sheetView.frame = CGRectMake(0, self.frame.height, self.frame.width, 50)
        self.backgroundView.alpha = 0
        dispatchOnUi { () -> Void in
            UIView.animateWithDuration(0.4, delay: 0.0, usingSpringWithDamping: 0.7,
                initialSpringVelocity: 0.6, options: .CurveEaseInOut, animations: {
                    self.sheetView.frame = CGRectMake(0, self.frame.height - 50, self.frame.width, 50)
                    self.backgroundView.alpha = 1
                }, completion: nil)
        }
    }
    
    override func dismiss() {
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
                self.removeFromSuperview()
        }
    }

    func setupAllViews() {
        
        // sheet view
        
        let superWidth = presentedInController.view.width
        let superHeight = presentedInController.view.height
        
        self.sheetView.frame = CGRectMake(0, superHeight - 50, superWidth, 50)
        self.sheetView.backgroundColor = UIColor.whiteColor()
        self.addSubview(self.sheetView)
        
        self.unBlockButton = UIButton(type: UIButtonType.System)
        
        // color
        
        self.unBlockButton.tintColor = UIColor(red: 5.0/255.0, green: 124.0/255.0, blue: 226.0/255.0, alpha: 1)
        
        // font size
        
        self.unBlockButton.titleLabel?.font = UIFont.systemFontOfSize(17)
        
        // add buttons as subivews
        
        self.sheetView.addSubview(self.unBlockButton)
        
        self.unBlockButton.frame  = CGRectMake(0, 0, superWidth, 50)
        
        // separators
        
        let spearator1 = UIView(frame: CGRectMake(0, 0, superWidth, 1))
        spearator1.backgroundColor = UIColor(red: 223.9/255.0, green: 223.9/255.0, blue: 223.9/255.0, alpha: 0.6)
        
        // add separatos as subview
        
        self.sheetView.addSubview(spearator1)
        
        // set title for buttons
        
        self.unBlockButton.setTitle(AALocalized("AlertUnblock"), forState: UIControlState.Normal)
        
        // add actins
        
        self.unBlockButton.addTarget(self, action: #selector(AAUnblockActionSheet.btnUnblockAction), forControlEvents: UIControlEvents.TouchUpInside)
    }
    
    //
    // Actions
    //
    
    func btnUnblockAction() {
        self.delegate?.actionSheetUnblockContact()
        dismiss()
    }
    
}
