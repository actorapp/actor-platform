//
//  AATermsController.swift
//  ActorSDK


import UIKit

class AATermsController: UIViewController,UIViewControllerTransitioningDelegate {
    
    var alertView : UIView!
    var alertTitleLabel : UILabel!
    var alertTextView : UITextView!
    var buttonOk : UIButton!
    

    ////////////////////////////////
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)!
        
        self.commonInit()
        
    }
    
    override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!)  {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        
        self.commonInit()
        
    }
    
    func commonInit() {
        self.modalPresentationStyle = .Custom
        self.transitioningDelegate = self
    }
    
    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
    
    // ---- UIViewControllerTransitioningDelegate methods
    func presentationControllerForPresentedViewController(presented: UIViewController, presentingViewController presenting: UIViewController, sourceViewController source: UIViewController) -> UIPresentationController? {
        
        if presented == self {
            return AACustomPresentationController(presentedViewController: presented, presentingViewController: presenting)
        }
        
        return nil
    }
    
    func animationControllerForPresentedController(presented: UIViewController, presentingController presenting: UIViewController, sourceController source: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        
        if presented == self {
            return AACustomPresentationAnimationController(isPresenting: true)
        }
        else {
            return nil
        }
    }
    
    func animationControllerForDismissedController(dismissed: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        
        if dismissed == self {
            return AACustomPresentationAnimationController(isPresenting: false)
        }
        else {
            return nil
        }
    }
    
    override func loadView() {
        super.loadView()
        
        self.alertView = UIView()
        self.alertView.frame = CGRectMake(self.view.frame.width/2 - 120, self.view.frame.height/2 - 165, 240, 330)
        self.alertView.backgroundColor = UIColor.whiteColor()
        self.alertView.layer.cornerRadius = 10
        self.alertView.layer.masksToBounds = true
        self.view.addSubview(self.alertView)
        
        self.alertTitleLabel = UILabel()
        self.alertTitleLabel.font = UIFont(name: "HelveticaNeue-Bold", size: 17)
        self.alertTitleLabel.frame = CGRectMake(10,10,220,30)
        self.alertTitleLabel.text = AALocalized("AuthTermsOfService")
        self.alertTitleLabel.backgroundColor = UIColor.clearColor()
        self.alertTitleLabel.textAlignment = .Center
        self.alertView.addSubview(self.alertTitleLabel)
        
        self.alertTextView = UITextView()
        self.alertTextView.font = UIFont(name: "HelveticaNeue-Light", size: 13)
        self.alertTextView.backgroundColor = UIColor.clearColor()
        self.alertTextView.editable = false
        self.alertTextView.text = AALocalized("AuthTerms").replace("{app_name}", dest: ActorSDK.sharedActor().appNameInLocStrings)
        self.alertTextView.frame = CGRectMake(10, 45, 220, 245);
        self.alertTextView.userInteractionEnabled = true
        self.alertView.addSubview(self.alertTextView)
        
        let separatorView = UIView()
        separatorView.frame = CGRectMake(0, 290, 240, 0.5)
        separatorView.backgroundColor = UIColor.lightGrayColor().colorWithAlphaComponent(0.5)
        self.alertView.addSubview(separatorView)
        
        self.buttonOk = UIButton(type: UIButtonType.System)
        self.buttonOk.setTitle("OK", forState: UIControlState.Normal)
        self.buttonOk.setTitleColor(UIColor.blueColor(), forState: UIControlState.Normal)
        self.buttonOk.frame = CGRectMake(0,291,240,39)
        self.buttonOk.addTarget(self, action: "closeController", forControlEvents: UIControlEvents.TouchUpInside)
        self.alertView.addSubview(self.buttonOk)
        
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let touch = UITapGestureRecognizer(target: self, action: "closeController")
        self.view.addGestureRecognizer(touch)
        
    }
    
    // actions
    func closeController() {
        self.dismissViewControllerAnimated(true, completion: nil)
    }

}
