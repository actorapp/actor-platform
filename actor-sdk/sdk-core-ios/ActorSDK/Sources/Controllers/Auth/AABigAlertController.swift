//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//


import Foundation
import UIKit

open class AABigAlertController: UIViewController,UIViewControllerTransitioningDelegate {
    
    fileprivate let alertTitle: String
    fileprivate let alertMessage: String
    
    fileprivate var alertView : UIView!
    fileprivate var alertTitleLabel : UILabel!
    fileprivate var alertTextView : UITextView!
    fileprivate var buttonOk : UIButton!
    
    public init(alertTitle: String, alertMessage: String)  {
        self.alertTitle = alertTitle
        self.alertMessage = alertMessage
        super.init(nibName: nil, bundle: nil)
        self.modalPresentationStyle = .custom
        self.transitioningDelegate = self
    }

    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func loadView() {
        super.loadView()
        
        self.alertView = UIView()
        self.alertView.frame = CGRect(x: self.view.frame.width/2 - 120, y: self.view.frame.height/2 - 165, width: 240, height: 330)
        self.alertView.backgroundColor = UIColor.white
        self.alertView.layer.cornerRadius = 10
        self.alertView.layer.masksToBounds = true
        self.view.addSubview(self.alertView)
        
        self.alertTitleLabel = UILabel()
        self.alertTitleLabel.font = UIFont.boldSystemFont(ofSize: 17)
        self.alertTitleLabel.frame = CGRect(x: 10,y: 10,width: 220,height: 30)
        self.alertTitleLabel.text = alertTitle
        self.alertTitleLabel.backgroundColor = UIColor.clear
        self.alertTitleLabel.textAlignment = .center
        self.alertView.addSubview(self.alertTitleLabel)
        
        self.alertTextView = UITextView()
        self.alertTextView.font = UIFont.lightSystemFontOfSize(13)
        self.alertTextView.backgroundColor = UIColor.clear
        self.alertTextView.isEditable = false
        self.alertTextView.text = alertMessage
        self.alertTextView.frame = CGRect(x: 10, y: 45, width: 220, height: 245);
        self.alertTextView.isUserInteractionEnabled = true
        self.alertView.addSubview(self.alertTextView)
        
        let separatorView = UIView()
        separatorView.frame = CGRect(x: 0, y: 290, width: 240, height: 0.5)
        separatorView.backgroundColor = UIColor.lightGray.withAlphaComponent(0.5)
        self.alertView.addSubview(separatorView)
        
        self.buttonOk = UIButton(type: UIButtonType.system)
        self.buttonOk.setTitle(AALocalized("AlertOk"), for: UIControlState())
        self.buttonOk.setTitleColor(UIColor.blue, for: UIControlState())
        self.buttonOk.frame = CGRect(x: 0,y: 291,width: 240,height: 39)
        self.buttonOk.addTarget(self, action: #selector(AABigAlertController.closeController), for: UIControlEvents.touchUpInside)
        self.alertView.addSubview(self.buttonOk)
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        let touch = UITapGestureRecognizer(target: self, action: #selector(AABigAlertController.closeController))
        self.view.addGestureRecognizer(touch)
    }
    
    open func closeController() {
        self.dismiss(animated: true, completion: nil)
    }
    
    open func presentationController(forPresented presented: UIViewController, presenting: UIViewController?, source: UIViewController) -> UIPresentationController? {
        
        if presented == self {
            return AACustomPresentationController(presentedViewController: presented, presenting: presenting)
        }
        
        return nil
    }
    
    open func animationController(forPresented presented: UIViewController, presenting: UIViewController, source: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        
        if presented == self {
            return AACustomPresentationAnimationController(isPresenting: true)
        }
        else {
            return nil
        }
    }
    
    open func animationController(forDismissed dismissed: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        
        if dismissed == self {
            return AACustomPresentationAnimationController(isPresenting: false)
        }
        else {
            return nil
        }
    }
}
