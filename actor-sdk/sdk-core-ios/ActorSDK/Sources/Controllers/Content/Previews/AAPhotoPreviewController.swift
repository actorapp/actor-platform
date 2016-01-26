//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAPhotoPreviewController: NYTPhotosViewController, NYTPhotosViewControllerDelegate {
    
    var autoShowBadge = false
    
    let photos: [PreviewImage]
    let controllerPhotos: [AAPhoto]
    var bind = [Int: AAFileCallback]()
    let fromView: UIView?
    
    public convenience init(photos: [PreviewImage], fromView: UIView?) {
        self.init(photos: photos, initialPhoto: 0, fromView: fromView)
    }
    
    public convenience init(photo: PreviewImage, fromView: UIView?) {
        self.init(photos: [photo], fromView: fromView)
    }
    
    public convenience init(file: ACFileReference, previewFile: ACFileReference?, size: CGSize?, fromView: UIView?) {
        self.init(photos: [PreviewImage(file: file, previewFile: previewFile, size: size)], fromView: fromView)
    }
    
    public init(photos: [PreviewImage], initialPhoto: Int, fromView: UIView?) {
        
        var converted = [AAPhoto]()
        for p in photos {
            if p.image != nil {
                converted.append(AAPhoto(image: p.image))
                continue
            }
            
            if p.file != nil  {
                let desc = Actor.findDownloadedDescriptorWithFileId(p.file!.getFileId())
                if desc != nil {
                    let img = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(desc!))
                    if img != nil {
                        converted.append(AAPhoto(image: img))
                        continue
                    }
                }
            }
            
            if p.previewFile != nil {
                let desc = Actor.findDownloadedDescriptorWithFileId(p.previewFile!.getFileId())
                if desc != nil {
                    var img = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(desc!))
                    if img != nil {
                        // img = img!.applyBlur(4)
                        if p.size != nil {
                            img = img!.resize(p.size!.width, h: p.size!.height)
                        }
                        converted.append(AAPhoto(image: nil, placeholderImage: img, attributedCaptionTitle: nil, attributedCaptionSummary: nil, attributedCaptionCredit: nil))
                        continue
                    }
                }
            }
            
            converted.append(AAPhoto(image: nil, placeholderImage: p.preview, attributedCaptionTitle: nil, attributedCaptionSummary: nil, attributedCaptionCredit: nil))
        }
        
        self.photos = photos
        self.controllerPhotos = converted
        
        self.fromView = fromView
        
        super.init(photos: converted, initialPhoto: converted[initialPhoto])
        
        self.delegate = self
    }

    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        // Setting tint color
        navigationController?.navigationBar.tintColor = UIColor.whiteColor()
        
        // Binding files
        for i in 0..<controllerPhotos.count {
            let cp = controllerPhotos[i]
            let p = photos[i]
            
            if cp.image == nil {
                let callback = AAFileCallback(notDownloaded: { () -> () in
                    
                    }, onDownloading: { (progress) -> () in
                        
                    }, onDownloaded: { (reference) -> () in
                        let image = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference))
                        dispatchOnUi({ () -> Void in
                            cp.image = image
                            self.updateImageForPhoto(cp)
                        })
                })
                
                Actor.bindRawFileWithReference(p.file!, autoStart: true, withCallback: callback)
                bind[i] = callback
            }
        }
        
        // Hide Status bar
        UIApplication.sharedApplication().animateStatusBarAppearance(.SlideUp, duration: 0.3)
        
        // Hide badge
        if autoShowBadge {
            AANavigationBadge.hideBadge()
        }
    }
    
    public func photosViewController(photosViewController: NYTPhotosViewController, referenceViewForPhoto photo: NYTPhoto) -> UIView? {
        return self.fromView
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        // Unbind all
        for i in bind {
            Actor.unbindRawFileWithFileId(photos[i.0].file!.getFileId(), autoCancel: true, withCallback: i.1)
        }
        bind.removeAll()
        
        // Restoring status bar
        UIApplication.sharedApplication().animateStatusBarAppearance(.SlideDown, duration: 0.3)
        
        // Restoring badge
        if autoShowBadge {
            AANavigationBadge.showBadge()
        }
    }
}

public class PreviewImage {
    
    let preview: UIImage?
    var image: UIImage?
    let previewFile: ACFileReference?
    let file: ACFileReference?
    let size: CGSize?
    
    init(file: ACFileReference, previewFile: ACFileReference?, size: CGSize?, preview: UIImage? = nil) {
        self.file = file
        self.preview = preview
        self.image = nil
        self.previewFile = previewFile
        self.size = size
    }
    
    init(image: UIImage) {
        self.file = nil
        self.preview = nil
        self.image = image
        self.previewFile = nil
        self.size = nil
    }
}

class AAPhoto: NSObject, NYTPhoto {
    
    var image: UIImage?
    var imageData: NSData?
    var placeholderImage: UIImage?
    let attributedCaptionTitle: NSAttributedString?
    let attributedCaptionSummary: NSAttributedString?
    let attributedCaptionCredit: NSAttributedString?
    
    init(image: UIImage?) {
        self.image = image
        self.placeholderImage = nil
        self.attributedCaptionTitle = nil
        self.attributedCaptionSummary = nil
        self.attributedCaptionCredit = nil
    }
    
    init(image: UIImage?, placeholderImage: UIImage?, attributedCaptionTitle: NSAttributedString?, attributedCaptionSummary: NSAttributedString?, attributedCaptionCredit: NSAttributedString?) {
        self.image = image
        self.placeholderImage = placeholderImage
        self.attributedCaptionTitle = attributedCaptionTitle
        self.attributedCaptionSummary = attributedCaptionSummary
        self.attributedCaptionCredit = attributedCaptionCredit
    }
    
    
    /*
    @available(iOS 2.0, *)
    public var image: UIImage? { get }
    
    /**
    * The image data to display. This will be preferred over the `image` property.
    * In case this is empty `image` will be used. The main advantage of using this is animated gif support.
    */
    public var imageData: NSData? { get }
    
    /**
    *  A placeholder image for display while the image is loading.
    */
    @available(iOS 2.0, *)
    public var placeholderImage: UIImage? { get }
    
    /**
    *  An attributed string for display as the title of the caption.
    */
    @available(iOS 3.2, *)
    public var attributedCaptionTitle: NSAttributedString? { get }
    
    /**
    *  An attributed string for display as the summary of the caption.
    */
    @available(iOS 3.2, *)
    public var attributedCaptionSummary: NSAttributedString? { get }
    
    /**
    *  An attributed string for display as the credit of the caption.
    */
    @available(iOS 3.2, *)
    public var attributedCaptionCredit: NSAttributedString? { get }
    */
    
}