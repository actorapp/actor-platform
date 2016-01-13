//
//  AAASAssetManager.swift
//  ActorSDK
//
//  Created by kioshimafx on 1/13/16.
//  Copyright © 2016 Steve Kite. All rights reserved.
//

import UIKit
import AssetsLibrary

let ASSET_PHOTO_THUMBNAIL:Int =          0
let ASSET_PHOTO_ASPECT_THUMBNAIL:Int =   1
let ASSET_PHOTO_SCREEN_SIZE:Int =        2
let ASSET_PHOTO_FULL_RESOLUTION:Int =    3

let kNotificationSendPhotos     = "NotificationSendPhotos"
let kNotificationUpdateSelected = "NotificationUpdateSelected"

class AAAssetPhoto {
    
    var asset : ALAsset!
    var index : Int!
    var isSelected : Bool!
    var groupIndex : String!
    
    var group : Int!
    
    init(groupIn:Int,indexIn:Int,assetIn:ALAsset) {
        
        self.group = groupIn
        self.index = indexIn
        self.asset = assetIn
        self.isSelected = true
        
        self.groupIndex = "\(group)-\(index)"
        
    }
    
}

class AAASAssetManager: NSObject {
    
    // ### singleton ###
    static let sharedInstance = AAASAssetManager()
    
    var maxSelected : Int!
    var currentGroupIndex :Int!
    
    ///
    
    var assetPhotos : Array<ALAsset>!
    var selectedPhotos : Array<AAAssetPhoto>!
    
    ///
    
    var assetsLibrary : ALAssetsLibrary!
    var assetGroups : Array<ALAssetsGroup>!
    var selectedAsset : ALAsset!
    
    override init() {
        
        self.selectedPhotos = Array<AAAssetPhoto>()
        self.assetsLibrary = ALAssetsLibrary()
        self.assetsLibrary.writeImageToSavedPhotosAlbum(nil, metadata: nil) { (_, _) -> Void in
            //
        }
        
    }
    
    //messages_setActivity(user_id:Int,reqResponse:(VKResponse!) -> ()
    
    func getSavedPhotoList(result:(Array<AnyObject>!) -> (),error:(String) -> ()) {
        
        
        dispatch_async(dispatch_get_main_queue()) { () -> Void in
            //
        }
        
        
    }
    
    
    func getGroupCount() -> Int {
        return self.assetGroups.count
    }
    
    func getPhotoCountOfCurrentGroup() -> Int {
        return self.assetPhotos.count
    }
    
    func getSelectedPhotoCount() -> Int {
        
        let selectedArray = self.selectedPhotos.filter({
            $0.isSelected == true
        })
        
        return selectedArray.count
    }
    
    func clearData() {
        
        self.selectedPhotos.removeAll()
        self.assetGroups.removeAll()
        self.assetPhotos.removeAll()
        
        
        self.selectedPhotos = nil
        self.assetGroups = nil
        self.assetPhotos = nil
        
    }
    
    ////
    
    
    func getImageFromAsset(asset:ALAsset,type:Int) -> UIImage {
        /*
        NSString *strXMP = asset.defaultRepresentation.metadata[@"AdjustmentXMP"];
        if (strXMP == nil || [strXMP isKindOfClass:[NSNull class]])
        {
        iRef = [asset.defaultRepresentation fullResolutionImage];
        return [UIImage imageWithCGImage:iRef scale:1.0 orientation:(UIImageOrientation)asset.defaultRepresentation.orientation];
        }
        else
        {
        NSData *dXMP = [strXMP dataUsingEncoding:NSUTF8StringEncoding];
        
        CIImage *image = [CIImage imageWithCGImage:asset.defaultRepresentation.fullResolutionImage];
        
        NSError *error = nil;
        NSArray *filterArray = [CIFilter filterArrayFromSerializedXMP:dXMP
        inputImageExtent:image.extent
        error:&error];
        if (error) {
        NSLog(@"Error during CIFilter creation: %@", [error localizedDescription]);
        }
        
        for (CIFilter *filter in filterArray) {
        [filter setValue:image forKey:kCIInputImageKey];
        image = [filter outputImage];
        }
        CIContext *context = [CIContext contextWithOptions:nil];
        CGImageRef cgimage = [context createCGImage:image fromRect:[image extent]];
        UIImage *iImage = [UIImage imageWithCGImage:cgimage scale:1.0 orientation:(UIImageOrientation)asset.defaultRepresentation.orientation];
        return iImage;
        */
        
        var imageRef:CGImageRef!
        
        if (type == ASSET_PHOTO_THUMBNAIL) {
            imageRef = asset.thumbnail() as! CGImageRef
        } else if (type == ASSET_PHOTO_ASPECT_THUMBNAIL) {
            imageRef = asset.aspectRatioThumbnail() as! CGImageRef
        } else if (type == ASSET_PHOTO_SCREEN_SIZE) {
            imageRef = asset.defaultRepresentation() as! CGImageRef
        } else if (type == ASSET_PHOTO_FULL_RESOLUTION) {
            imageRef = asset.defaultRepresentation() as! CGImageRef
            //let strXMP = asset.defaultRepresentation().metadata()["AdjustmentXMP"]
            
            
            
            
        }
        
        return UIImage(CGImage: imageRef)
    }
    
    func getImageAtIndex(nIndex:Int,nType:Int) -> UIImage {
        return getImageFromAsset(self.assetPhotos[nIndex], type: nType)
    }
    
    func getImagePreviewAtIndex(nIndex:Int,nType:Int) -> UIImage {
        let photoObj = self.selectedPhotos[nIndex]
        return getImageFromAsset(photoObj.asset, type: nType)
    }
    
    func getAssetAtIndex(index:Int) -> ALAsset {
        return self.assetPhotos[index]
    }
    
    func getGroupAtIndex(index:Int) -> ALAssetsGroup {
        return self.assetGroups[index]
    }
    
    func sendSelectedPhotos(type:Int) -> Array<UIImage> {
        
        var sendArray = Array<UIImage>()
        
        for (_, photoObj) in self.selectedPhotos.enumerate() {
            
            let image = self.getImageFromAsset(photoObj.asset, type: type)
            sendArray.append(image)
            
        }
        
        self.selectedPhotos.removeAll()
        
        return sendArray
    }
    
    func addObjectWithIndex(index:Int) {
        
        let model = AAAssetPhoto(groupIn: self.currentGroupIndex, indexIn: index, assetIn: self.assetPhotos[index])
        
        self.selectedPhotos.append(model)
        
        //[[NSNotificationCenter defaultCenter] postNotificationName:kNotificationUpdateSelected object:nil];
        
    }
    
    
    func removeObjectWithIndex(index:Int) {
        
        let groupIndex = "\(self.currentGroupIndex!)-\(index)"
        
        let results = self.selectedPhotos.filter({
            $0.groupIndex == groupIndex
        })
        
        if (results.count > 0) {
            
            //let model = results[0]
    
            
        }
        
    
    }
    
    
    func currentGroupFirstIndex() -> Int {
        
        let results = self.selectedPhotos.filter({
            $0.group == self.currentGroupIndex
        })
        
        if (results.count > 0) {
            
            let model = results[0]
            return model.index
            
        }
        
        return 0
    }
    
    

}
