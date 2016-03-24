//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//


import Foundation
import UIKit

public class AABigAlertController: UIViewController,UIViewControllerTransitioningDelegate {
    
    private let alertTitle: String
    private let alertMessage: String
    
    private var alertView : UIView!
    private var alertTitleLabel : UILabel!
    private var alertTextView : UITextView!
    private var buttonOk : UIButton!
    
    public init(alertTitle: String, alertMessage: String)  {
        self.alertTitle = alertTitle
        self.alertMessage = alertMessage
        super.init(nibName: nil, bundle: nil)
        self.modalPresentationStyle = .Custom
        self.transitioningDelegate = self
    }

    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func loadView() {
        super.loadView()
        
        self.alertView = UIView()
        self.alertView.frame = CGRectMake(self.view.frame.width/2 - 120, self.view.frame.height/2 - 165, 240, 330)
        self.alertView.backgroundColor = UIColor.whiteColor()
        self.alertView.layer.cornerRadius = 10
        self.alertView.layer.masksToBounds = true
        self.view.addSubview(self.alertView)
        
        self.alertTitleLabel = UILabel()
        self.alertTitleLabel.font = UIFont.boldSystemFontOfSize(17)
        self.alertTitleLabel.frame = CGRectMake(10,10,220,30)
        self.alertTitleLabel.text = alertTitle
        self.alertTitleLabel.backgroundColor = UIColor.clearColor()
        self.alertTitleLabel.textAlignment = .Center
        self.alertView.addSubview(self.alertTitleLabel)
        
        self.alertTextView = UITextView()
        self.alertTextView.font = UIFont.lightSystemFontOfSize(13)
        self.alertTextView.backgroundColor = UIColor.clearColor()
        self.alertTextView.editable = false
        self.alertTextView.text = alertMessage
        self.alertTextView.frame = CGRectMake(10, 45, 220, 245);
        self.alertTextView.userInteractionEnabled = true
        self.alertView.addSubview(self.alertTextView)
        
        let separatorView = UIView()
        separatorView.frame = CGRectMake(0, 290, 240, 0.5)
        separatorView.backgroundColor = UIColor.lightGrayColor().colorWithAlphaComponent(0.5)
        self.alertView.addSubview(separatorView)
        
        self.buttonOk = UIButton(type: UIButtonType.System)
        self.buttonOk.setTitle(AALocalized("AlertOk"), forState: UIControlState.Normal)
        self.buttonOk.setTitleColor(UIColor.blueColor(), forState: UIControlState.Normal)
        self.buttonOk.frame = CGRectMake(0,291,240,39)
        self.buttonOk.addTarget(self, action: #selector(AABigAlertController.closeController), forControlEvents: UIControlEvents.TouchUpInside)
        self.alertView.addSubview(self.buttonOk)
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        let touch = UITapGestureRecognizer(target: self, action: #selector(AABigAlertController.closeController))
        self.view.addGestureRecognizer(touch)
    }
    
    public func closeController() {
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    public func presentationControllerForPresentedViewController(presented: UIViewController, presentingViewController presenting: UIViewController, sourceViewController source: UIViewController) -> UIPresentationController? {
        
        if presented == self {
            return AACustomPresentationController(presentedViewController: presented, presentingViewController: presenting)
        }
        
        return nil
    }
    
    public func animationControllerForPresentedController(presented: UIViewController, presentingController presenting: UIViewController, sourceController source: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        
        if presented == self {
            return AACustomPresentationAnimationController(isPresenting: true)
        }
        else {
            return nil
        }
    }
    
    public func animationControllerForDismissedController(dismissed: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        
        if dismissed == self {
            return AACustomPresentationAnimationController(isPresenting: false)
        }
        else {
            return nil
        }
    }
}