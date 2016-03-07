//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAAuthViewController: AAViewController {
    
    public let nextBar = UIView()
    public let nextBarButton = UIButton()
    private let nextBarLine = UIView()
    private var keyboardHeight: CGFloat = 0
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        
        nextBarButton.setTitle("Next", forState: .Normal)
        nextBarButton.setTitleColor(UIColor.whiteColor(), forState: .Normal)
        nextBarButton.setBackgroundImage(Imaging.roundedImage(UIColor(red: 94, green: 142, blue: 192), radius: 4), forState: .Normal)
        nextBarButton.setBackgroundImage(Imaging.roundedImage(UIColor(red: 94, green: 142, blue: 192).alpha(0.7), radius: 4), forState: .Highlighted)
        nextBarButton.addTarget(self, action: "nextDidTap", forControlEvents: .TouchUpInside)
        
        nextBarLine.backgroundColor = UIColor.alphaBlack(0.2)
        nextBarLine.hidden = true
        
        nextBar.addSubview(nextBarButton)
        nextBar.addSubview(nextBarLine)
        
        view.addSubview(nextBar)
    }

    override public func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        layoutNextBar()
    }
    
    override public func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        // Forcing initial layout before keyboard show to avoid weird animations
        layoutNextBar()
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "keyboardWillAppear:", name: UIKeyboardWillShowNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "keyboardWillDisappear:", name: UIKeyboardWillHideNotification, object: nil)
    }
    
    func keyboardWillAppear(notification: NSNotification) {
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
    }
    
    private func layoutNextBar() {
        nextBar.frame = CGRectMake(0, view.height - 44 - keyboardHeight, view.width, 44)
        nextBarButton.frame = CGRectMake(nextBar.width - 95, 6, 85, 32)
        nextBarLine.frame = CGRectMake(0, 0, nextBar.width, 0.5)
    }
    
    func keyboardWillDisappear(notification: NSNotification) {
        keyboardHeight = 0
        layoutNextBar()
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