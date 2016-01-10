//
//  AAStickersView.swift
//  ActorSDK
//
//  Created by kioshimafx on 1/10/16.
//  Copyright © 2016 Steve Kite. All rights reserved.
//

import UIKit
import SDWebImage

public class AAStickersViewCell : UICollectionViewCell {
    
    let stickerImage : UIImageView!
    private var callback: AAFileCallback? = nil
    private static var stickerCache = Dictionary<Int, AASwiftlyLRU<Int64, UIImage>>()
    private static let cacheSize = 30
    
    override init(frame: CGRect) {
        
        self.stickerImage = UIImageView()
        self.stickerImage.backgroundColor = UIColor.clearColor()
        
        super.init(frame: frame)
        
        
        self.addSubview(self.stickerImage)
        
    }
    
    required public init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override public func layoutSubviews() {
        super.layoutSubviews()
        
        self.stickerImage.frame = self.contentView.frame
        
    }
    
    override public func prepareForReuse() {
        super.prepareForReuse()
        
        self.stickerImage.image = nil
        
    }
    
    func bind(sticker: ACSticker!, clearPrev: Bool) {
        
        
        var fileLocation: ACFileReference?
        fileLocation = sticker.getFileReference128()
        
        let cached = checkCache(512, id: Int64(fileLocation!.getFileId()))
        if (cached != nil) {
            self.stickerImage.image = cached
            return
        }
        
    
        self.callback = AAFileCallback(onDownloaded: { (reference) -> () in
            
            
            let data = NSFileManager.defaultManager().contentsAtPath(CocoaFiles.pathFromDescriptor(reference))
            let image = UIImage.sd_imageWithWebPData(data)
            
            
            if (image == nil) {
                return
            }
            
            dispatchOnUi {
                
                self.putToCache(512, id: Int64((fileLocation?.getFileId())!), image: image!)
                
                UIView.transitionWithView(self, duration: 0.3, options: UIViewAnimationOptions.TransitionCrossDissolve, animations: { () -> Void in
                    self.stickerImage.image = image;
                }, completion: nil)
                
            }
        });
        Actor.bindRawFileWithReference(fileLocation, autoStart: true, withCallback: self.callback)
    }
    
    //
    // Caching
    //
    
    private func checkCache(size: Int, id: Int64) -> UIImage? {
        if let cache = AAStickersViewCell.stickerCache[size] {
            if let img = cache[id] {
                return img
            }
        }
        return nil
    }
    
    private func putToCache(size: Int, id: Int64, image: UIImage) {
        if let cache = AAStickersViewCell.stickerCache[size] {
            cache[id] = image
        } else {
            let cache = AASwiftlyLRU<jlong, UIImage>(capacity: AAStickersViewCell.cacheSize);
            cache[id] = image
            AAStickersViewCell.stickerCache.updateValue(cache, forKey: size)
        }
    }
    
    
}

class AAStickersView: UIView , UICollectionViewDelegate, UICollectionViewDataSource {

    private let collectionView  : UICollectionView!
    private weak var conv       : ConversationViewController!
    private var visualEffectView : UIVisualEffectView!
    
    private var stickersArray   = Array<ACSticker>()
    
    private let binder = AABinder()
    
    init(frame: CGRect,convContrller:ConversationViewController) {
        
        // one item size
        let widthHightItem = frame.width/4-20;
        
        // layout for collection view
        let layoutCV = UICollectionViewFlowLayout()
        layoutCV.scrollDirection = .Vertical
        layoutCV.itemSize = CGSizeMake(widthHightItem, widthHightItem)
        
        // init collection view
        self.collectionView = UICollectionView(frame: frame, collectionViewLayout: layoutCV)
        
        
        // init self view
        super.init(frame: frame)
        
        // bind convController
        self.conv = convContrller
        
        self.visualEffectView = UIVisualEffectView(effect: UIBlurEffect(style: .Light))
        
        visualEffectView.frame = frame
        
        self.addSubview(visualEffectView)
        
        // delegate/datasource
        
        
        self.collectionView.delegate = self
        self.collectionView.dataSource = self
        self.collectionView.backgroundColor = UIColor.clearColor()
        
        self.collectionView.registerClass(AAStickersViewCell.self, forCellWithReuseIdentifier: "AAStickersViewCell")
        
        self.collectionView.contentInset = UIEdgeInsetsMake(15, 10, 15, 10)
        
        
        // add collection view as subview
        self.visualEffectView.addSubview(self.collectionView)
        
        self.backgroundColor = UIColor.clearColor()
        
        self.loadStickers()
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        self.collectionView.frame = self.frame
        self.visualEffectView.frame = self.frame
        
    }
    
    
    /// collectionView
    
    func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return 1
    }
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.stickersArray.count
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        
        let stickerCell = self.collectionView.dequeueReusableCellWithReuseIdentifier("AAStickersViewCell", forIndexPath: indexPath) as! AAStickersViewCell
        
        stickerCell.stickerImage.backgroundColor = UIColor.clearColor()
        stickerCell.bind(self.stickersArray[indexPath.row], clearPrev: true)
        
        return stickerCell;
        
    }
    
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        
        let stickerModel = self.stickersArray[indexPath.row]
        
        self.conv.sendSticker(stickerModel)
        
    }
    
    
    func loadStickers() {
        
        self.stickersArray = Array<ACSticker>()
        
        Actor.loadStickers()
        
        let packsArray = Actor.getOwnStickerPacksIdsVM()!.get() as! JavaUtilArrayList
        let sickersPack = packsArray.getWithInt(0) as! ACStickerPackVM
        let sickers = sickersPack.stickers.get() as! JavaUtilArrayList
        
        for i in 0..<sickers.size() {
            let stickerModel = sickers.getWithInt(i) as! ACSticker
            self.stickersArray.append(stickerModel)
        }
        
        print("packs class === \(sickers)")
        
        self.collectionView.reloadData()
        
    }

}
