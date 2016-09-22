//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAAuthViewController: AAViewController {
    
    open let nextBarButton = UIButton()
    fileprivate var keyboardHeight: CGFloat = 0
    
    override open func viewDidLoad() {
        super.viewDidLoad()
        
        nextBarButton.setTitle(AALocalized("NavigationNext"), for: UIControlState())
        nextBarButton.setTitleColor(UIColor.white, for: UIControlState())
        nextBarButton.setBackgroundImage(Imaging.roundedImage(UIColor(red: 94, green: 142, blue: 192), radius: 4), for: UIControlState())
        nextBarButton.setBackgroundImage(Imaging.roundedImage(UIColor(red: 94, green: 142, blue: 192).alpha(0.7), radius: 4), for: .highlighted)
        nextBarButton.addTarget(self, action: #selector(AAAuthViewController.nextDidTap), for: .touchUpInside)

        view.addSubview(nextBarButton)
    }
    
    override open func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        layoutNextBar()
    }
    
    override open func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        // Forcing initial layout before keyboard show to avoid weird animations
        layoutNextBar()
        
        NotificationCenter.default.addObserver(self, selector: #selector(AAAuthViewController.keyboardWillAppearInt(_:)), name: NSNotification.Name.UIKeyboardWillShow, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(AAAuthViewController.keyboardWillDisappearInt(_:)), name: NSNotification.Name.UIKeyboardWillHide, object: nil)
    }
    
    fileprivate func layoutNextBar() {
        nextBarButton.frame = CGRect(x: view.width - 95, y: view.height - 44 - keyboardHeight + 6, width: 85, height: 32)
    }
    
    func keyboardWillAppearInt(_ notification: Notification) {
        let dict = (notification as NSNotification).userInfo!
        let rect = (dict[UIKeyboardFrameBeginUserInfoKey]! as AnyObject).cgRectValue
        
        let orientation = UIApplication.shared.statusBarOrientation
        let frameInWindow = self.view.superview!.convert(view.bounds, to: nil)
        let windowBounds = view.window!.bounds
        
        let keyboardTop: CGFloat = windowBounds.size.height - rect!.height
        let heightCoveredByKeyboard: CGFloat
        if AADevice.isiPad {
            if orientation == .landscapeLeft || orientation == .landscapeRight {
                heightCoveredByKeyboard = frameInWindow.maxY - keyboardTop - 52 /*???*/
            } else if orientation == .portrait || orientation == .portraitUpsideDown {
                heightCoveredByKeyboard = frameInWindow.maxY - keyboardTop
            } else {
                heightCoveredByKeyboard = 0
            }
        } else {
            heightCoveredByKeyboard = (rect?.height)!
        }
        
        keyboardHeight = max(0, heightCoveredByKeyboard)
        layoutNextBar()
        keyboardWillAppear(keyboardHeight)
    }
    
    
    func keyboardWillDisappearInt(_ notification: Notification) {
        keyboardHeight = 0
        layoutNextBar()
        keyboardWillDisappear()
    }
    
    open func keyboardWillAppear(_ height: CGFloat) {
        
    }
    
    open func keyboardWillDisappear() {
        
    }
    
    override open func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        NotificationCenter.default.removeObserver(self)
        keyboardHeight = 0
        layoutNextBar()
    }
    
    /// Call this method when authentication successful
    open func onAuthenticated() {
        ActorSDK.sharedActor().didLoggedIn()
    }
    
    open func nextDidTap() {
        
    }
}
