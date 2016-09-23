//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import MobileCoreServices
import RSKImageCropper
import DZNWebViewController
import SafariServices

open class AAViewController: UIViewController, UINavigationControllerDelegate, UIImagePickerControllerDelegate, RSKImageCropViewControllerDelegate, UIViewControllerTransitioningDelegate  {
    
    // MARK: -
    // MARK: Public vars
    
    var placeholder = AABigPlaceholderView(topOffset: 0)
    
    var pendingPickClosure: ((_ image: UIImage) -> ())?
    
    var popover: UIPopoverController?
    
    // Content type for view tracking
    
    open var content: ACPage?
    
    // Data for views
    
    open var autoTrack: Bool = false {
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
    
    open var uid: Int! {
        didSet {
            if self.uid != nil {
                self.user = Actor.getUserWithUid(jint(self.uid))
                self.isBot = user.isBot()
            }
        }
    }
    open var user: ACUserVM!
    open var isBot: Bool!
    
    open var gid: Int! {
        didSet {
            if self.gid != nil {
                self.group = Actor.getGroupWithGid(jint(self.gid))
            }
        }
    }
    open var group: ACGroupVM!
    
    // Style
    
    open var appStyle: ActorStyle {
        get {
            return ActorSDK.sharedActor().style
        }
    }
    
    public init() {
        super.init(nibName: nil, bundle: nil)
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItemStyle.plain, target: nil, action: nil)
    }
    
    public override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItemStyle.plain, target: nil, action: nil)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override var preferredStatusBarStyle : UIStatusBarStyle {
        return ActorSDK.sharedActor().style.vcStatusBarStyle
    }
    
    open func showPlaceholderWithImage(_ image: UIImage?, title: String?, subtitle: String?) {
        placeholder.setImage(image, title: title, subtitle: subtitle)
        showPlaceholder()
    }
    
    open func showPlaceholder() {
        if placeholder.superview == nil {
            placeholder.frame = view.bounds
            view.addSubview(placeholder)
        }
    }
    
    open func hidePlaceholder() {
        if placeholder.superview != nil {
            placeholder.removeFromSuperview()
        }
    }
    
    open func shakeView(_ view: UIView, originalX: CGFloat) {
        var r = view.frame
        r.origin.x = originalX
        let originalFrame = r
        var rFirst = r
        rFirst.origin.x = r.origin.x + 4
        r.origin.x = r.origin.x - 4
        
        UIView.animate(withDuration: 0.05, delay: 0.0, options: .autoreverse, animations: { () -> Void in
            view.frame = rFirst
            }) { (finished) -> Void in
                if (finished) {
                    UIView.animate(withDuration: 0.05, delay: 0.0, options: [.repeat, .autoreverse], animations: { () -> Void in
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
    
    open func pickAvatar(_ takePhoto:Bool, closure: @escaping (_ image: UIImage) -> ()) {
        self.pendingPickClosure = closure
        
        let pickerController = AAImagePickerController()
        pickerController.sourceType = (takePhoto ? UIImagePickerControllerSourceType.camera : UIImagePickerControllerSourceType.photoLibrary)
        pickerController.mediaTypes = [kUTTypeImage as String]
        pickerController.delegate = self
        self.navigationController!.present(pickerController, animated: true, completion: nil)
    }
    
    open override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()

        placeholder.frame = CGRect(x: 0, y: 64, width: view.bounds.width, height: view.bounds.height - 64)
    }
    
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if let c = content {
            ActorSDK.sharedActor().trackPageVisible(c)
        }
        if let u = uid {
            Actor.onProfileOpen(withUid: jint(u))
        }
    }
    
    open override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        if let c = content {
            ActorSDK.sharedActor().trackPageHidden(c)
        }
        if let u = uid {
            Actor.onProfileClosed(withUid: jint(u))
        }
    }
    
    open func dismissController() {
        self.dismiss(animated: true, completion: nil)
    }
    
    open func presentInNavigation(_ controller: UIViewController) {
        let navigation = AANavigationController()
        navigation.viewControllers = [controller]
        present(navigation, animated: true, completion: nil)
    }
    
    open func openUrl(_ url: String) {
        if let url = URL(string: url) {
            if #available(iOS 9.0, *) {
                self.presentElegantViewController(SFSafariViewController(url: url))
            } else {
                self.presentElegantViewController(AANavigationController(rootViewController: DZNWebViewController(url: url)))
            }
        }
    }
    
    // Image pick and crop
    
    open func cropImage(_ image: UIImage) {
        let cropController = RSKImageCropViewController(image: image)
        cropController.delegate = self
        navigationController!.present(UINavigationController(rootViewController: cropController), animated: true, completion: nil)
    }
    
    open func imageCropViewController(_ controller: RSKImageCropViewController, didCropImage croppedImage: UIImage, usingCropRect cropRect: CGRect) {
        if (pendingPickClosure != nil){
            pendingPickClosure!(croppedImage)
        }
        pendingPickClosure = nil
        navigationController!.dismiss(animated: true, completion: nil)
    }
    
    open func imageCropViewControllerDidCancelCrop(_ controller: RSKImageCropViewController) {
        pendingPickClosure = nil
        navigationController!.dismiss(animated: true, completion: nil)
    }
    
    
    open func imagePickerController(_ picker: UIImagePickerController, didFinishPickingImage image: UIImage, editingInfo: [String : AnyObject]?) {
        navigationController!.dismiss(animated: true, completion: { () -> Void in
            self.cropImage(image)
        })
    }
    
    open func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
        let image = info[UIImagePickerControllerOriginalImage] as! UIImage
        navigationController!.dismiss(animated: true, completion: { () -> Void in
            self.cropImage(image)
        })
    }
    
    open func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        pendingPickClosure = nil
        self.dismiss(animated: true, completion: nil)
    }
    
    open func presentationController(forPresented presented: UIViewController, presenting: UIViewController?, source: UIViewController) -> UIPresentationController? {
        return ElegantPresentations.controller(presentedViewController: presented, presentingViewController: presenting!, options: [])
    }
    
    open func presentElegantViewController(_ controller: UIViewController) {
        if AADevice.isiPad {
            controller.modalPresentationStyle = .formSheet
            present(controller, animated: true, completion: nil) 
        } else {
            // controller.modalPresentationStyle = .custom
            // controller.modalPresentationStyle = .custom
            // controller.transitioningDelegate = self
            present(controller, animated: true, completion: nil)
        }
    }
}
