//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import YYImage

public protocol AAStickersKeyboardDelegate {
    func stickerDidSelected(keyboard: AAStickersKeyboard, sticker: ACSticker)
}

public class AAStickersKeyboard: UIView, UICollectionViewDelegate, UICollectionViewDataSource {

    public var delegate : AAStickersKeyboardDelegate?
    
    private let collectionView: UICollectionView!
    private var stickers = Array<ACSticker>()
    private let binder = AABinder()

    public override init(frame: CGRect) {
        
        // one item size
        let widthHightItem = frame.width / 4 - 15;
        
        // layout for collection view
        let layoutCV = UICollectionViewFlowLayout()
        layoutCV.scrollDirection = .Vertical
        layoutCV.itemSize = CGSizeMake(widthHightItem, widthHightItem)
        layoutCV.sectionInset = UIEdgeInsets(top: 5, left: 0, bottom: 10, right: 0)
        
        // init collection view
        self.collectionView = UICollectionView(frame: frame, collectionViewLayout: layoutCV)
        
        
        // init self view
        super.init(frame: frame)
        
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
        
        // Bind To Stickers
        binder.bind(Actor.getAvailableStickersVM().getOwnStickerPacks()) { (value: JavaUtilArrayList!) -> () in
            self.stickers.removeAll(keepCapacity: true)
            
            for i in 0..<value.size() {
                let pack = value.getWithInt(i) as! ACStickerPack
                
                for j in 0..<pack.stickers.size() {
                    let sticker = pack.stickers.getWithInt(j) as! ACSticker
                    self.stickers.append(sticker)
                }
            }
            
            self.collectionView.reloadData()
        }
    }
    
    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        binder.unbindAll()
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        self.collectionView.frame = self.frame
    }
    
    
    //
    // Collection View
    //

    public func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return stickers.count
    }
    
    public func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let stickerCell = collectionView
            .dequeueReusableCellWithReuseIdentifier("AAStickersViewCell", forIndexPath: indexPath) as! AAStickersViewCell
        stickerCell.bind(stickers[indexPath.row], clearPrev: true)
        return stickerCell
    }
    
    public func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        let sticker = stickers[indexPath.row]
        self.delegate?.stickerDidSelected(self, sticker: sticker)
    }
}

public class AAStickersViewCell : UICollectionViewCell {
    
    private let stickerImage = AAStickerView()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = UIColor.clearColor()
        self.contentView.backgroundColor = UIColor.clearColor()
        self.addSubview(self.stickerImage)
    }
    
    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func bind(sticker: ACSticker!, clearPrev: Bool) {
        var fileLocation: ACFileReference? = sticker.getImage128()
        if sticker.getImage256() != nil {
            fileLocation = sticker.getImage256()
        }
        self.stickerImage.setSticker(fileLocation)
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        self.stickerImage.frame = self.contentView.frame
    }
    
    public override func prepareForReuse() {
        super.prepareForReuse()
        self.stickerImage.setSticker(nil)
    }
}
