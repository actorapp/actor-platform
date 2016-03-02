//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import YYKit

public struct AAStickersPack {
    
    var pack_id: Int!
    var stickers: Array<ACSticker>!
    
}


public class AAStickersViewCell : UICollectionViewCell {
    
    let stickerImage : YYAnimatedImageView!
    private var callback: AAFileCallback? = nil
    private static var stickerCache = Dictionary<Int, AASwiftlyLRU<Int64, UIImage>>()
    private static let cacheSize = 30
    
    override init(frame: CGRect) {
        
        self.stickerImage = YYAnimatedImageView()
        self.stickerImage.backgroundColor = UIColor.clearColor()
        self.stickerImage.contentMode = .ScaleAspectFit
        
        super.init(frame: frame)
        
        self.backgroundColor = UIColor.clearColor()
        self.contentView.backgroundColor = UIColor.clearColor()
        
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
            
            if (data == nil) {
                return
            }
            
            var image:UIImage!
            image = YYImage(data: data!)
            
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
    
    private var stickersPacks   = Array<AAStickersPack>()
    
    private let binder = AABinder()

    init(frame: CGRect, convController: ConversationViewController) {
        
        // one item size
        let widthHightItem = frame.width/4-15;
        
        // layout for collection view
        let layoutCV = UICollectionViewFlowLayout()
        layoutCV.scrollDirection = .Vertical
        layoutCV.itemSize = CGSizeMake(widthHightItem, widthHightItem)
        layoutCV.sectionInset = UIEdgeInsets(top: 5, left: 0, bottom: 10, right: 0)
        
        // init collection view
        self.collectionView = UICollectionView(frame: frame, collectionViewLayout: layoutCV)
        
        
        // init self view
        super.init(frame: frame)
        
        // bind convController
        
        self.conv = convController
        
        // delegate/datasource
        
        
        self.collectionView.delegate = self
        self.collectionView.dataSource = self
        self.collectionView.backgroundColor = UIColor(red: 0.7728, green: 0.8874, blue: 0.9365, alpha: 1.0)
        
        self.collectionView.registerClass(AAStickersViewCell.self, forCellWithReuseIdentifier: "AAStickersViewCell")
        
        self.collectionView.contentInset = UIEdgeInsetsMake(10, 5, 10, 5)
        
        self.collectionView.preservesSuperviewLayoutMargins = false
        self.collectionView.layoutMargins = UIEdgeInsetsZero
        
        
        // add collection view as subview
        self.addSubview(self.collectionView)
        
        self.backgroundColor = UIColor.clearColor()
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        self.collectionView.frame = self.frame
        
    }

    
    /// collectionView
    
    func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return self.stickersPacks.count
    }
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        
        let sickerPack = self.stickersPacks[section];
        return sickerPack.stickers.count
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        
        let stickerCell = self.collectionView.dequeueReusableCellWithReuseIdentifier("AAStickersViewCell", forIndexPath: indexPath) as! AAStickersViewCell
        
        let sickerPack = self.stickersPacks[indexPath.section];
        
        stickerCell.stickerImage.backgroundColor = UIColor.clearColor()
        stickerCell.bind(sickerPack.stickers[indexPath.row], clearPrev: true)
        
        
        return stickerCell;
        
    }
    
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        
        let sickerPack = self.stickersPacks[indexPath.section];
        let stickerModel = sickerPack.stickers[indexPath.row]
        
        self.conv.sendSticker(stickerModel)
        
    }
    
    func collectionView(collectionView: UICollectionView, willDisplayCell cell: UICollectionViewCell, forItemAtIndexPath indexPath: NSIndexPath) {
        
        
        
        
    }
    
    
    func loadStickers() {
        
        Actor.loadStickers()
        
        self.stickersPacks = Array<AAStickersPack>()
        
        if let packsArray = Actor.getOwnStickerPacksIdsVM()!.get() {
            
            let packesArrarChecked = packsArray as! JavaUtilArrayList
            
                if packesArrarChecked.size() > 0 {
                    
                    for x in 0..<packesArrarChecked.size() {
                        
                        let sickersPack = packesArrarChecked.getWithInt(x) as! ACStickerPackVM
                        let sickers = sickersPack.stickers.get() as! JavaUtilArrayList
                        
                        var parsedStickerPack = AAStickersPack()
                        
                        parsedStickerPack.pack_id = Int(sickersPack.getId())
                        parsedStickerPack.stickers = Array<ACSticker>()
                        
                        for i in 0..<sickers.size() {
                            let stickerModel = sickers.getWithInt(i) as! ACSticker
                            parsedStickerPack.stickers.append(stickerModel)
                        }
                        
                        if (parsedStickerPack.stickers.count > 0) {
                            self.stickersPacks.append(parsedStickerPack)
                        }
                        
                    }
                    
                }
            
        }
        
        
        self.collectionView.reloadData()
        
    }

}
