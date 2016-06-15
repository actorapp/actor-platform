//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import Photos

public enum ImagePickerMediaType {
    case Image
    case Video
    case ImageAndVideo
}

public protocol AAThumbnailViewDelegate {
    func thumbnailSelectedUpdated(selectedAssets: [(PHAsset,Bool)])
}

public class AAThumbnailView: UIView,UICollectionViewDelegate , UICollectionViewDataSource {

    public var delegate : AAThumbnailViewDelegate?
    
    private var collectionView:UICollectionView!
    private let mediaType: ImagePickerMediaType = ImagePickerMediaType.Image
    
    private var assets = [(PHAsset,Bool)]()
    private var selectedAssets = [(PHAsset, Bool)]()
    private var imageManager : PHCachingImageManager!
    
    private let minimumPreviewHeight: CGFloat = 90
    private var maximumPreviewHeight: CGFloat = 90
    
    private let previewCollectionViewInset: CGFloat = 5
    
    private lazy var requestOptions: PHImageRequestOptions = {
        let options = PHImageRequestOptions()
        options.deliveryMode = .HighQualityFormat
        options.resizeMode = .Fast
        
        return options
    }()
    
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.collectionViewSetup()
    }
    
    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    ///
    
    public func open() {
        
        dispatchBackground { () -> Void in
            
            if PHPhotoLibrary.authorizationStatus() == .Authorized {
                self.imageManager = PHCachingImageManager()
                self.fetchAssets()
                dispatch_async(dispatch_get_main_queue()) {
                    self.collectionView.reloadData()
                }
                
            } else if PHPhotoLibrary.authorizationStatus() == .NotDetermined {
                
                PHPhotoLibrary.requestAuthorization() { status in
                    if status == .Authorized {
                        dispatch_async(dispatch_get_main_queue()) {
                            self.imageManager = PHCachingImageManager()
                            self.fetchAssets()
                            self.collectionView.reloadData()
                        }
                    }
                }

            }
            
        }

    }
    
    private func fetchAssets() {
        self.assets = [(PHAsset,Bool)]()
        
        let options = PHFetchOptions()
        options.sortDescriptors = [NSSortDescriptor(key: "creationDate", ascending: false)]
        
//        switch mediaType {
//        case .Image:
//            options.predicate = NSPredicate(format: "mediaType = %d", PHAssetMediaType.Image.rawValue)
//        case .Video:
//            options.predicate = NSPredicate(format: "mediaType = %d", PHAssetMediaType.Video.rawValue)
//        case .ImageAndVideo:
//            options.predicate = NSPredicate(format: "mediaType = %d OR mediaType = %d", PHAssetMediaType.Image.rawValue, PHAssetMediaType.Video.rawValue)
//        }
        
          options.predicate = NSPredicate(format: "mediaType = %d", PHAssetMediaType.Image.rawValue)
       
        let fetchLimit = 100
        if #available(iOS 9, *) {
            options.fetchLimit = fetchLimit
        }
        
        let result = PHAsset.fetchAssetsWithOptions(options)
        let requestOptions = PHImageRequestOptions()
        requestOptions.synchronous = true
        requestOptions.deliveryMode = .FastFormat
        
        result.enumerateObjectsUsingBlock { asset, _, stop in
            
            if self.assets.count > fetchLimit {
                stop.initialize(true)
            }
            
            if let asset = asset as? PHAsset {
                var isGIF = false
                self.imageManager.requestImageDataForAsset(asset, options: requestOptions) { data, _, _, info in
                    if data != nil {
                        let gifMarker = info!["PHImageFileURLKey"] as! NSURL
                        print(gifMarker.pathExtension)
                        isGIF = (gifMarker.pathExtension == "GIF") ? true : false
                        print(isGIF)
                        self.prefetchImagesForAsset(asset)
                    }
                    self.assets.append((asset,isGIF))
                }
            }
        }
    }
    
    private func prefetchImagesForAsset(asset: PHAsset) {
        let targetSize = sizeForAsset(asset, scale: UIScreen.mainScreen().scale)
        imageManager.startCachingImagesForAssets([asset], targetSize: targetSize, contentMode: .AspectFill, options: requestOptions)
    }
    
    private func requestImageForAsset(asset: PHAsset, completion: (image: UIImage?) -> ()) {
        let targetSize = sizeForAsset(asset, scale: UIScreen.mainScreen().scale)
        requestOptions.synchronous = false
        
        // Workaround because PHImageManager.requestImageForAsset doesn't work for burst images
        if asset.representsBurst {
            imageManager.requestImageDataForAsset(asset, options: requestOptions) { data, _, _, _ in
                let image = data.flatMap { UIImage(data: $0) }
                completion(image: image)
            }
        }
        else {
            imageManager.requestImageForAsset(asset, targetSize: targetSize, contentMode: .AspectFill, options: requestOptions) { image, _ in
                completion(image: image)
            }
        }
    }
    
    private func sizeForAsset(asset: PHAsset, scale: CGFloat = 1) -> CGSize {
        
        var complitedCGSize : CGSize!
        
        if asset.pixelWidth > asset.pixelHeight {
            complitedCGSize = CGSizeMake(CGFloat(asset.pixelHeight),CGFloat(asset.pixelHeight))
        } else {
            complitedCGSize = CGSizeMake(CGFloat(asset.pixelWidth),CGFloat(asset.pixelWidth))
        }
        
        return complitedCGSize
    }
    
    /// collection view delegate
    
    public func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return 1
    }
    
    public func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.assets.count
    }
    
    public func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        
        let cell = self.collectionView.dequeueReusableCellWithReuseIdentifier("AAThumbnailCollectionCell", forIndexPath: indexPath) as! AAThumbnailCollectionCell
        
        cell.bindedThumbView = self
        
        let photoModel = self.assets[indexPath.row].0
        let animated = self.assets[indexPath.row].1
        
        cell.bindedPhotoModel = photoModel
        
        if self.selectedAssets.contains(photoModel) {
            cell.isCheckSelected = true
            cell.imgSelected.image = UIImage.bundled("ImageSelectedOn")
            
        } else {
            cell.isCheckSelected = false
            cell.imgSelected.image = UIImage.bundled("ImageSelectedOff")
            
        }
        
        cell.backgroundColor = UIColor.whiteColor()
        
        let asset = assets[indexPath.row].0
        
        requestImageForAsset(asset) { image in
            
            var complitedImage : UIImage!
            
            if image!.size.width > image!.size.height {
                complitedImage = self.imageByCroppingImage(image!, toSize: CGSizeMake(image!.size.height,image!.size.height))
            } else {
                complitedImage = self.imageByCroppingImage(image!, toSize: CGSizeMake(image!.size.width,image!.size.width))
            }
            
            cell.imgThumbnails.image = complitedImage
            cell.animated = animated
            
        }
        
        
        return cell
    }
    
    ///
    
    public func reloadView() {
        self.collectionView.reloadData()
    }
    
    public func collectionViewSetup() {
        
        let flowLayout = UICollectionViewFlowLayout()
        flowLayout.scrollDirection = .Horizontal
        flowLayout.minimumLineSpacing = 4
        flowLayout.sectionInset = UIEdgeInsetsMake(5.0, 4.0, 5.0, 4.0)
        flowLayout.itemSize = CGSizeMake(90, 90)
        
        self.collectionView = UICollectionView(frame: self.bounds, collectionViewLayout: flowLayout)
        self.collectionView.backgroundColor = UIColor.clearColor()
        self.collectionView.showsHorizontalScrollIndicator = false
        self.collectionView.delegate = self
        self.collectionView.dataSource = self
        self.collectionView.registerClass(AAThumbnailCollectionCell.self, forCellWithReuseIdentifier: "AAThumbnailCollectionCell")
        self.addSubview(self.collectionView)
    }

    public func imageByCroppingImage(image:UIImage,toSize:CGSize) -> UIImage {
        
        let refWidth = CGImageGetWidth(image.CGImage)
        let refHeight = CGImageGetHeight(image.CGImage)
        
        let x = CGFloat((refWidth - Int(toSize.width)) / 2)
        let y = CGFloat((refHeight - Int(toSize.height)) / 2)
        
        let cropRect = CGRectMake(x, y, toSize.height, toSize.width)
        let imageRef = CGImageCreateWithImageInRect(image.CGImage, cropRect)! as CGImageRef
        
        let cropped = UIImage(CGImage: imageRef, scale: 0.0, orientation: UIImageOrientation.Up)
        
        return cropped
    }
    
    public func addSelectedModel(model:PHAsset, animated:Bool) {
        self.selectedAssets.append((model,animated))
        self.delegate?.thumbnailSelectedUpdated(self.selectedAssets)
    }
    
    public func removeSelectedModel(model:PHAsset,animated:Bool) {
        for (index, element) in self.selectedAssets.enumerate() {
            if element.0 == model {
                self.selectedAssets.removeAtIndex(index)
            }
        }
        self.delegate?.thumbnailSelectedUpdated(self.selectedAssets)
    }
   
    public func getSelectedAsImages(completion: (images:[(NSData,Bool)]) -> ()) {
        
        let arrayModelsForSend = self.selectedAssets
        
        var compliedArray = [(NSData,Bool)]()
        var isGif = false
        for (_,model) in arrayModelsForSend.enumerate() {
            self.imageManager.requestImageDataForAsset(model.0, options: requestOptions, resultHandler: { (data, _, _, info) -> Void in
                if data != nil {
                    let gifMarker = info!["PHImageFileURLKey"] as! NSURL
                    isGif = (gifMarker.pathExtension == "GIF") ? true : false
                    print(isGif)
                    compliedArray.append((data!,isGif))
                    if compliedArray.count == arrayModelsForSend.count {
                        completion(images: compliedArray)
                    }
                }
            })
        }
    }
    
    public func dismiss() {
        self.selectedAssets = []
        self.reloadView()
    }
}
