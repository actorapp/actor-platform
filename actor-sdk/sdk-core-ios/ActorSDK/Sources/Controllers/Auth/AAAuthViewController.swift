//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAAuthViewController: AAViewController {
    
    public let nextBarButton = UIButton()
    private var keyboardHeight: CGFloat = 0
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        
        nextBarButton.setTitle(AALocalized("NavigationNext"), forState: .Normal)
        nextBarButton.setTitleColor(UIColor.whiteColor(), forState: .Normal)
        nextBarButton.setBackgroundImage(Imaging.roundedImage(UIColor(red: 94, green: 142, blue: 192), radius: 4), forState: .Normal)
        nextBarButton.setBackgroundImage(Imaging.roundedImage(UIColor(red: 94, green: 142, blue: 192).alpha(0.7), radius: 4), forState: .Highlighted)
        nextBarButton.addTarget(self, action: #selector(AAAuthViewController.nextDidTap), forControlEvents: .TouchUpInside)

        view.addSubview(nextBarButton)
    }
    
    override public func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        layoutNextBar()
    }
    
    override public func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        // Forcing initial layout before keyboard show to avoid weird animations
        layoutNextBar()
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: #selector(AAAuthViewController.keyboardWillAppearInt(_:)), name: UIKeyboardWillShowNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: #selector(AAAuthViewController.keyboardWillDisappearInt(_:)), name: UIKeyboardWillHideNotification, object: nil)
    }
    
    private func layoutNextBar() {
        nextBarButton.frame = CGRectMake(view.width - 95, view.height - 44 - keyboardHeight + 6, 85, 32)
    }
    
    func keyboardWillAppearInt(notification: NSNotification) {
        let dict = notification.userInfo!
        let rect = dict[UIKeyboardFrameBeginUserInfoKey]!.CGRectValue
        
        let orientation = UIApplication.sharedApplication().statusBarOrientation
        let frameInWindow = self.view.superview!.convertRect(view.bounds, toView: nil)
        let windowBounds = view.window!.bounds
        
        let keyboardTop: CGFloat = windowBounds.size.height - rect.height
        let heightCoveredByKeyboard: CGFloat
        if AADevice.isiPad {
            if orientation == .LandscapeLeft || orientation == .LandscapeRight {
                heightCoveredByKeyboard = frameInWindow.maxY - keyboardTop - 52 /*???*/
            } else if orientation == .Portrait || orientation == .PortraitUpsideDown {
                heightCoveredByKeyboard = CGRectGetMaxY(frameInWindow) - keyboardTop
            } else {
                heightCoveredByKeyboard = 0
            }
        } else {
            heightCoveredByKeyboard = rect.height
        }
        
        keyboardHeight = max(0, heightCoveredByKeyboard)
        layoutNextBar()
        keyboardWillAppear(keyboardHeight)
    }
    
    
    func keyboardWillDisappearInt(notification: NSNotification) {
        keyboardHeight = 0
        layoutNextBar()
        keyboardWillDisappear()
    }
    
    public func keyboardWillAppear(height: CGFloat) {
        
    }
    
    public func keyboardWillDisappear() {
        
    }
    
    override public func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        NSNotificationCenter.defaultCenter().removeObserver(self)
        keyboardHeight = 0
        layoutNextBar()
    }
    
    /// Call this method when authentication successful
    public func onAuthenticated() {
        ActorSDK.sharedActor().didLoggedIn()
    }
    
    public func nextDidTap() {
        
    }
}