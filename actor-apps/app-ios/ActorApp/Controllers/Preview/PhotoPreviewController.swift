//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class PhotoPreviewController: NYTPhotosViewController, NYTPhotosViewControllerDelegate {
    
    // let binder = Binder()
    let photos: [PreviewImage]
    let controllerPhotos: [AAPhoto]
    var bind = [Int: CocoaDownloadCallback]()
    let fromView: UIView?
    
    convenience init(photos: [PreviewImage], fromView: UIView?) {
        self.init(photos: photos, initialPhoto: 0, fromView: fromView)
    }
    
    convenience init(photo: PreviewImage, fromView: UIView?) {
        self.init(photos: [photo], fromView: fromView)
    }
    
    convenience init(file: ACFileReference, previewFile: ACFileReference?, size: CGSize?, fromView: UIView?) {
        self.init(photos: [PreviewImage(file: file, previewFile: previewFile, size: size)], fromView: fromView)
    }
    
    init(photos: [PreviewImage], initialPhoto: Int, fromView: UIView?) {
        
        var converted = [AAPhoto]()
        for p in photos {
            if p.image != nil {
                converted.append(AAPhoto(image: p.image))
                continue
            }
            
            if p.file != nil  {
                let desc = Actor.findDownloadedDescriptorWithFileId(p.file!.getFileId())
                if desc != nil {
                    let img = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(desc))
                    if img != nil {
                        converted.append(AAPhoto(image: img))
                        continue
                    }
                }
            }
            
            if p.previewFile != nil {
                let desc = Actor.findDownloadedDescriptorWithFileId(p.previewFile!.getFileId())
                if desc != nil {
                    var img = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(desc))
                    if img != nil {
                        img = img!.applyBlur(4)
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

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        // Setting tint color
        navigationController?.navigationBar.tintColor = UIColor.whiteColor()
        
        // Binding files
        for i in 0..<controllerPhotos.count {
            let cp = controllerPhotos[i]
            let p = photos[i]
            
            if cp.image == nil {
                let callback = CocoaDownloadCallback(notDownloaded: { () -> () in
                    
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
    }
    
    func photosViewController(photosViewController: NYTPhotosViewController!, referenceViewForPhoto photo: NYTPhoto!) -> UIView! {
        return self.fromView
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        // Unbind all
        for i in bind {
            Actor.unbindRawFileWithFileId(photos[i.0].file!.getFileId(), autoCancel: true, withCallback: i.1)
        }
        bind.removeAll()
        
        // Restoring status bar
        UIApplication.sharedApplication().animateStatusBarAppearance(.SlideDown, duration: 0.3)
    }
}

class PreviewImage {
    
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