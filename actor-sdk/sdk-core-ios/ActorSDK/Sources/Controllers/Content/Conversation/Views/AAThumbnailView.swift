//
//  AAThumbnailView.swift
//  ActorSDK
//
//  Created by kioshimafx on 1/13/16.
//  Copyright © 2016 Steve Kite. All rights reserved.
//

import UIKit
import Photos

public enum ImagePickerMediaType {
    case Image
    case Video
    case ImageAndVideo
}

class AAThumbnailView: UIView,UICollectionViewDelegate , UICollectionViewDataSource {

    var collectionView:UICollectionView!
    let mediaType: ImagePickerMediaType = ImagePickerMediaType.Image
    
    private var assets = [PHAsset]()
    private let imageManager = PHCachingImageManager()
    
    private let minimumPreviewHeight: CGFloat = 70
    private var maximumPreviewHeight: CGFloat = 70
    
    private lazy var requestOptions: PHImageRequestOptions = {
        let options = PHImageRequestOptions()
        options.deliveryMode = .HighQualityFormat
        options.resizeMode = .Fast
        
        return options
    }()
    
    private let previewCollectionViewInset: CGFloat = 5
    
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.collectionViewSetup()
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    ///
    
    func open() {
        
        if PHPhotoLibrary.authorizationStatus() == .Authorized {
            fetchAssets()
            self.collectionView.reloadData()
        } else if PHPhotoLibrary.authorizationStatus() == .NotDetermined {
            PHPhotoLibrary.requestAuthorization() { status in
                if status == .Authorized {
                    dispatch_async(dispatch_get_main_queue()) {
                        self.fetchAssets()
                        self.collectionView.reloadData()
                    }
                }
            }
        }
        
    }
    
    private func fetchAssets() {
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
        
        let fetchLimit = 50
        if #available(iOS 9, *) {
            options.fetchLimit = fetchLimit
        }
        
        let result = PHAsset.fetchAssetsWithOptions(options)
        let requestOptions = PHImageRequestOptions()
        requestOptions.synchronous = true
        requestOptions.deliveryMode = .FastFormat
        
        result.enumerateObjectsUsingBlock { asset, _, stop in
            defer {
                if self.assets.count > fetchLimit {
                    stop.initialize(true)
                }
            }
            
            if let asset = asset as? PHAsset {
                self.imageManager.requestImageDataForAsset(asset, options: requestOptions) { data, _, _, info in
                    if data != nil {
                        self.assets.append(asset)
                    }
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
        requestOptions.synchronous = true
        
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
        let proportion = CGFloat(asset.pixelWidth)/CGFloat(asset.pixelHeight)
        
        let imageHeight = maximumPreviewHeight - 2 * previewCollectionViewInset
        let imageWidth = floor(proportion * imageHeight)
        
        return CGSize(width: imageWidth * scale, height: imageHeight * scale)
    }
    
    /// collection view delegate
    
    func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return 1
    }
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        print("ASSSEEETS === \(self.assets.count)")
        return self.assets.count
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        
        let cell = self.collectionView.dequeueReusableCellWithReuseIdentifier("AAThumbnailCollectionCell", forIndexPath: indexPath) as! AAThumbnailCollectionCell
        
        cell.backgroundColor = UIColor.whiteColor()
        
        let asset = assets[indexPath.row]
        
        requestImageForAsset(asset) { image in
            cell.imgSelected.image = image
        }
        
        
        return cell
    }
    
    ///
    
    func reloadView() {
        self.collectionView.reloadData()
    }
    
    func collectionViewSetup() {
            
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
        self.collectionView.frame = CGRectMake(0,0,screenWidth,80)
        
        self.collectionView.registerClass(AAThumbnailCollectionCell.self, forCellWithReuseIdentifier: "AAThumbnailCollectionCell")

        self.addSubview(self.collectionView)
        
    }

    func imageByCroppingImage(image:UIImage,toSize:CGSize) -> UIImage {
        
        let refWidth = CGImageGetWidth(image.CGImage)
        let refHeight = CGImageGetHeight(image.CGImage)
        
        let x = CGFloat((refWidth - Int(toSize.width)) / 2)
        let y = CGFloat((refHeight - Int(toSize.height)) / 2)
        
        let cropRect = CGRectMake(x, y, toSize.height, toSize.width)
        let imageRef = CGImageCreateWithImageInRect(image.CGImage, cropRect)! as CGImageRef
        
        let cropped = UIImage(CGImage: imageRef, scale: 0.0, orientation: UIImageOrientation.Up)
        
        return cropped;
    }
    
}
