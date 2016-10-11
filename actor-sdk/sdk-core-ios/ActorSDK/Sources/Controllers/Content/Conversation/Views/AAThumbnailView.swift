//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import Photos

public enum ImagePickerMediaType {
    case image
    case video
    case imageAndVideo
}

public protocol AAThumbnailViewDelegate {
    func thumbnailSelectedUpdated(_ selectedAssets: [(PHAsset,Bool)])
}

open class AAThumbnailView: UIView,UICollectionViewDelegate , UICollectionViewDataSource {

    open var delegate : AAThumbnailViewDelegate?
    
    fileprivate var collectionView:UICollectionView!
    fileprivate let mediaType: ImagePickerMediaType = ImagePickerMediaType.image
    
    fileprivate var assets = [(PHAsset,Bool)]()
    fileprivate var selectedAssets = [(PHAsset, Bool)]()
    fileprivate var imageManager : PHCachingImageManager!
    
    fileprivate let minimumPreviewHeight: CGFloat = 90
    fileprivate var maximumPreviewHeight: CGFloat = 90
    
    fileprivate let previewCollectionViewInset: CGFloat = 5
    
    fileprivate lazy var requestOptions: PHImageRequestOptions = {
        let options = PHImageRequestOptions()
        options.deliveryMode = .highQualityFormat
        options.resizeMode = .fast
        
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
    
    open func open() {
        
        dispatchBackground { () -> Void in
            
            if PHPhotoLibrary.authorizationStatus() == .authorized {
                self.imageManager = PHCachingImageManager()
                self.fetchAssets()
                DispatchQueue.main.async {
                    self.collectionView.reloadData()
                }
                
            } else if PHPhotoLibrary.authorizationStatus() == .notDetermined {
                
                PHPhotoLibrary.requestAuthorization() { status in
                    if status == .authorized {
                        DispatchQueue.main.async {
                            self.imageManager = PHCachingImageManager()
                            self.fetchAssets()
                            self.collectionView.reloadData()
                        }
                    }
                }

            }
            
        }

    }
    
    fileprivate func fetchAssets() {
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
        
          options.predicate = NSPredicate(format: "mediaType = %d", PHAssetMediaType.image.rawValue)
       
        let fetchLimit = 100
        if #available(iOS 9, *) {
            options.fetchLimit = fetchLimit
        }
        
        let result = PHAsset.fetchAssets(with: options)
        let requestOptions = PHImageRequestOptions()
        requestOptions.isSynchronous = true
        requestOptions.deliveryMode = .fastFormat
        
//        result.enumerateObjects { asset, _, stop in
//            
//            if self.assets.count > fetchLimit {
//                stop.initialize(to: true)
//            }
//            
//            if let asset = asset as? PHAsset {
//                var isGIF = false
//                self.imageManager.requestImageData(for: asset, options: requestOptions) { data, _, _, info in
//                    if data != nil {
//                        let gifMarker = info!["PHImageFileURLKey"] as! URL
//                        print(gifMarker.pathExtension)
//                        isGIF = (gifMarker.pathExtension == "GIF") ? true : false
//                        print(isGIF)
//                        self.prefetchImagesForAsset(asset)
//                    }
//                    self.assets.append((asset,isGIF))
//                }
//            }
//        }
    }
    
    fileprivate func prefetchImagesForAsset(_ asset: PHAsset) {
        let targetSize = sizeForAsset(asset, scale: UIScreen.main.scale)
        imageManager.startCachingImages(for: [asset], targetSize: targetSize, contentMode: .aspectFill, options: requestOptions)
    }
    
    fileprivate func requestImageForAsset(_ asset: PHAsset, completion: @escaping (_ image: UIImage?) -> ()) {
        let targetSize = sizeForAsset(asset, scale: UIScreen.main.scale)
        requestOptions.isSynchronous = false
        
        // Workaround because PHImageManager.requestImageForAsset doesn't work for burst images
        if asset.representsBurst {
            imageManager.requestImageData(for: asset, options: requestOptions) { data, _, _, _ in
                let image = data.flatMap { UIImage(data: $0) }
                completion(image)
            }
        }
        else {
            imageManager.requestImage(for: asset, targetSize: targetSize, contentMode: .aspectFill, options: requestOptions) { image, _ in
                completion(image)
            }
        }
    }
    
    fileprivate func sizeForAsset(_ asset: PHAsset, scale: CGFloat = 1) -> CGSize {
        
        var complitedCGSize : CGSize!
        
        if asset.pixelWidth > asset.pixelHeight {
            complitedCGSize = CGSize(width: CGFloat(asset.pixelHeight),height: CGFloat(asset.pixelHeight))
        } else {
            complitedCGSize = CGSize(width: CGFloat(asset.pixelWidth),height: CGFloat(asset.pixelWidth))
        }
        
        return complitedCGSize
    }
    
    /// collection view delegate
    
    open func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    
    open func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.assets.count
    }
    
    open func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        
        let cell = self.collectionView.dequeueReusableCell(withReuseIdentifier: "AAThumbnailCollectionCell", for: indexPath) as! AAThumbnailCollectionCell
        
        cell.bindedThumbView = self
        
        let photoModel = self.assets[(indexPath as NSIndexPath).row].0
        let animated = self.assets[(indexPath as NSIndexPath).row].1
        
        cell.bindedPhotoModel = photoModel
        
        if self.selectedAssets.contains(photoModel) {
            cell.isCheckSelected = true
            cell.imgSelected.image = UIImage.bundled("ImageSelectedOn")
            
        } else {
            cell.isCheckSelected = false
            cell.imgSelected.image = UIImage.bundled("ImageSelectedOff")
        }
        
        cell.backgroundColor = UIColor.white
        
        let asset = assets[(indexPath as NSIndexPath).row].0
        
        requestImageForAsset(asset) { image in
            
            var complitedImage : UIImage!
            
            if image!.size.width > image!.size.height {
                complitedImage = self.imageByCroppingImage(image!, toSize: CGSize(width: image!.size.height,height: image!.size.height))
            } else {
                complitedImage = self.imageByCroppingImage(image!, toSize: CGSize(width: image!.size.width,height: image!.size.width))
            }
            
            cell.imgThumbnails.image = complitedImage
            cell.animated = animated
            
        }
        
        
        return cell
    }
    
    ///
    
    open func reloadView() {
        self.collectionView.reloadData()
    }
    
    open func collectionViewSetup() {
        
        let flowLayout = UICollectionViewFlowLayout()
        flowLayout.scrollDirection = .horizontal
        flowLayout.minimumLineSpacing = 4
        flowLayout.sectionInset = UIEdgeInsetsMake(5.0, 4.0, 5.0, 4.0)
        flowLayout.itemSize = CGSize(width: 90, height: 90)
        
        self.collectionView = UICollectionView(frame: self.bounds, collectionViewLayout: flowLayout)
        self.collectionView.backgroundColor = UIColor.clear
        self.collectionView.showsHorizontalScrollIndicator = false
        self.collectionView.delegate = self
        self.collectionView.dataSource = self
        self.collectionView.register(AAThumbnailCollectionCell.self, forCellWithReuseIdentifier: "AAThumbnailCollectionCell")
        self.addSubview(self.collectionView)
    }

    open func imageByCroppingImage(_ image:UIImage,toSize:CGSize) -> UIImage {
        
        let refWidth = image.cgImage?.width
        let refHeight = image.cgImage?.height
        
        let x = CGFloat((refWidth! - Int(toSize.width)) / 2)
        let y = CGFloat((refHeight! - Int(toSize.height)) / 2)
        
        let cropRect = CGRect(x: x, y: y, width: toSize.height, height: toSize.width)
        let imageRef = (image.cgImage?.cropping(to: cropRect)!)! as CGImage
        
        let cropped = UIImage(cgImage: imageRef, scale: 0.0, orientation: UIImageOrientation.up)
        
        return cropped
    }
    
    open func addSelectedModel(_ model:PHAsset, animated:Bool) {
        self.selectedAssets.append((model,animated))
        self.delegate?.thumbnailSelectedUpdated(self.selectedAssets)
    }
    
    open func removeSelectedModel(_ model:PHAsset,animated:Bool) {
        for (index, element) in self.selectedAssets.enumerated() {
            if element.0 == model {
                self.selectedAssets.remove(at: index)
            }
        }
        self.delegate?.thumbnailSelectedUpdated(self.selectedAssets)
    }
   
    open func getSelectedAsImages(_ completion: @escaping (_ images:[(Data,Bool)]) -> ()) {
        
        let arrayModelsForSend = self.selectedAssets
        
        var compliedArray = [(Data,Bool)]()
        var isGif = false
        for (_,model) in arrayModelsForSend.enumerated() {
            self.imageManager.requestImageData(for: model.0, options: requestOptions, resultHandler: { (data, _, _, info) -> Void in
                if data != nil {
                    let gifMarker = info!["PHImageFileURLKey"] as! URL
                    isGif = (gifMarker.pathExtension == "GIF") ? true : false
                    print(isGif)
                    compliedArray.append((data!,isGif))
                    if compliedArray.count == arrayModelsForSend.count {
                        completion(compliedArray)
                    }
                }
            })
        }
    }
    
    open func dismiss() {
        self.selectedAssets = []
        self.reloadView()
    }
}
