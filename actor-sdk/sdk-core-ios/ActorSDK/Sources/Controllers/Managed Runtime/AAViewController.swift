//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import MobileCoreServices
import RSKImageCropper
import DZNWebViewController
import SafariServices

public class AAViewController: UIViewController, UINavigationControllerDelegate, UIImagePickerControllerDelegate, RSKImageCropViewControllerDelegate, UIViewControllerTransitioningDelegate  {
    
    // MARK: -
    // MARK: Public vars
    
    var placeholder = AABigPlaceholderView(topOffset: 0)
    
    var pendingPickClosure: ((image: UIImage) -> ())?
    
    var popover: UIPopoverController?
    
    // Content type for view tracking
    
    public var content: ACPage?
    
    // Data for views
    
    public var autoTrack: Bool = false {
        didSet {
            if self.autoTrack {
                if let u = self.uid {
                    content = ACAllEvents_Profile_viewWithInt_(jint(u))
                }
                if let g = self.gid {
                    content = ACAllEvents_Group_viewWithInt_(jint(g))
                }
            }
        }
    }
    
    public var uid: Int! {
        didSet {
            if self.uid != nil {
                self.user = Actor.getUserWithUid(jint(self.uid))
                self.isBot = user.isBot().boolValue
            }
        }
    }
    public var user: ACUserVM!
    public var isBot: Bool!
    
    public var gid: Int! {
        didSet {
            if self.gid != nil {
                self.group = Actor.getGroupWithGid(jint(self.gid))
            }
        }
    }
    public var group: ACGroupVM!
    
    // Style
    
    public var appStyle: ActorStyle {
        get {
            return ActorSDK.sharedActor().style
        }
    }
    
    public init() {
        super.init(nibName: nil, bundle: nil)
    }
    
    public override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
    }
    
    public override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return ActorSDK.sharedActor().style.vcStatusBarStyle
    }
    
    public func showPlaceholderWithImage(image: UIImage?, title: String?, subtitle: String?) {
        placeholder.setImage(image, title: title, subtitle: subtitle)
        showPlaceholder()
    }
    
    public func showPlaceholder() {
        if placeholder.superview == nil {
            placeholder.frame = view.bounds
            view.addSubview(placeholder)
        }
    }
    
    public func hidePlaceholder() {
        if placeholder.superview != nil {
            placeholder.removeFromSuperview()
        }
    }
    
    public func shakeView(view: UIView, originalX: CGFloat) {
        var r = view.frame
        r.origin.x = originalX
        let originalFrame = r
        var rFirst = r
        rFirst.origin.x = r.origin.x + 4
        r.origin.x = r.origin.x - 4
        
        UIView.animateWithDuration(0.05, delay: 0.0, options: .Autoreverse, animations: { () -> Void in
            view.frame = rFirst
            }) { (finished) -> Void in
                if (finished) {
                    UIView.animateWithDuration(0.05, delay: 0.0, options: [.Repeat, .Autoreverse], animations: { () -> Void in
                        UIView.setAnimationRepeatCount(3)
                        view.frame = r
                        }, completion: { (finished) -> Void in
                            view.frame = originalFrame
                    })
                } else {
                    view.frame = originalFrame
                }
        }
    }
    
    public func pickAvatar(takePhoto:Bool, closure: (image: UIImage) -> ()) {
        self.pendingPickClosure = closure
        
        let pickerController = AAImagePickerController()
        pickerController.sourceType = (takePhoto ? UIImagePickerControllerSourceType.Camera : UIImagePickerControllerSourceType.PhotoLibrary)
        pickerController.mediaTypes = [kUTTypeImage as String]
        pickerController.delegate = self
        self.navigationController!.presentViewController(pickerController, animated: true, completion: nil)
    }
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()

        placeholder.frame = CGRectMake(0, 64, view.bounds.width, view.bounds.height - 64)
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if let c = content {
            ActorSDK.sharedActor().trackPageVisible(c)
        }
        if let u = uid {
            Actor.onProfileOpenWithUid(jint(u))
        }
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        if let c = content {
            ActorSDK.sharedActor().trackPageHidden(c)
        }
        if let u = uid {
            Actor.onProfileClosedWithUid(jint(u))
        }
    }
    
    public func dismiss() {
        dismissViewControllerAnimated(true, completion: nil)
    }
    
    public func presentInNavigation(controller: UIViewController) {
        let navigation = AANavigationController()
        navigation.viewControllers = [controller]
        presentViewController(navigation, animated: true, completion: nil)
    }
    
    public func openUrl(url: String) {
        if let url = NSURL(string: url) {
            if #available(iOS 9.0, *) {
                self.presentElegantViewController(SFSafariViewController(URL: url))
            } else {
                self.presentElegantViewController(AANavigationController(rootViewController: DZNWebViewController(URL: url)))
            }
        }
    }
    
    // Image pick and crop
    
    public func cropImage(image: UIImage) {
        let cropController = RSKImageCropViewController(image: image)
        cropController.delegate = self
        navigationController!.presentViewController(UINavigationController(rootViewController: cropController), animated: true, completion: nil)
    }
    
    public func imageCropViewController(controller: RSKImageCropViewController, didCropImage croppedImage: UIImage, usingCropRect cropRect: CGRect) {
        if (pendingPickClosure != nil){
            pendingPickClosure!(image: croppedImage)
        }
        pendingPickClosure = nil
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    public func imageCropViewControllerDidCancelCrop(controller: RSKImageCropViewController) {
        pendingPickClosure = nil
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    
    public func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage, editingInfo: [String : AnyObject]?) {
        navigationController!.dismissViewControllerAnimated(true, completion: { () -> Void in
            self.cropImage(image)
        })
    }
    
    public func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        let image = info[UIImagePickerControllerOriginalImage] as! UIImage
        navigationController!.dismissViewControllerAnimated(true, completion: { () -> Void in
            self.cropImage(image)
        })
    }
    
    public func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        pendingPickClosure = nil
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    public func presentationControllerForPresentedViewController(presented: UIViewController, presentingViewController presenting: UIViewController, sourceViewController source: UIViewController) -> UIPresentationController? {
        return ElegantPresentations.controller(presentedViewController: presented, presentingViewController: presenting, options: [])
    }
    
    public func presentElegantViewController(controller: UIViewController) {
        if AADevice.isiPad {
            controller.modalPresentationStyle = .FormSheet
            presentViewController(controller, animated: true, completion: nil) 
        } else {
            controller.modalPresentationStyle = .Custom
            controller.transitioningDelegate = self
            presentViewController(controller, animated: true, completion: nil)
        }
    }
}