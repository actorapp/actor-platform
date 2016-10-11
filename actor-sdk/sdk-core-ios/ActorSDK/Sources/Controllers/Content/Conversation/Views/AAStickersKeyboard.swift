//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import YYImage

public protocol AAStickersKeyboardDelegate {
    func stickerDidSelected(_ keyboard: AAStickersKeyboard, sticker: ACSticker)
}

open class AAStickersKeyboard: UIView, UICollectionViewDelegate, UICollectionViewDataSource {

    open var delegate : AAStickersKeyboardDelegate?
    
    fileprivate let collectionView: UICollectionView!
    fileprivate var stickers = Array<ACSticker>()
    fileprivate let binder = AABinder()

    public override init(frame: CGRect) {
        
        // one item size
        let widthHightItem = frame.width / 4 - 15;
        
        // layout for collection view
        let layoutCV = UICollectionViewFlowLayout()
        layoutCV.scrollDirection = .vertical
        layoutCV.itemSize = CGSize(width: widthHightItem, height: widthHightItem)
        layoutCV.sectionInset = UIEdgeInsets(top: 5, left: 0, bottom: 10, right: 0)
        
        // init collection view
        self.collectionView = UICollectionView(frame: frame, collectionViewLayout: layoutCV)
        
        
        // init self view
        super.init(frame: frame)
        
        // delegate/datasource
        
        self.collectionView.delegate = self
        self.collectionView.dataSource = self
        self.collectionView.backgroundColor = UIColor(red: 0.7728, green: 0.8874, blue: 0.9365, alpha: 1.0)
        
        self.collectionView.register(AAStickersViewCell.self, forCellWithReuseIdentifier: "AAStickersViewCell")
        
        self.collectionView.contentInset = UIEdgeInsetsMake(10, 5, 10, 5)
        
        self.collectionView.preservesSuperviewLayoutMargins = false
        self.collectionView.layoutMargins = UIEdgeInsets.zero
        
        // add collection view as subview
        self.addSubview(self.collectionView)
        
        self.backgroundColor = UIColor.clear
        
        // Bind To Stickers
        binder.bind(Actor.getAvailableStickersVM().getOwnStickerPacks()) { (value: JavaUtilArrayList?) -> () in
            self.stickers.removeAll(keepingCapacity: true)
            
            for i in 0..<value!.size() {
                let pack = value!.getWith(i) as! ACStickerPack
                
                for j in 0..<pack.stickers.size() {
                    let sticker = pack.stickers.getWith(j) as! ACSticker
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
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        self.collectionView.frame = self.frame
    }
    
    
    //
    // Collection View
    //

    open func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return stickers.count
    }
    
    open func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let stickerCell = collectionView
            .dequeueReusableCell(withReuseIdentifier: "AAStickersViewCell", for: indexPath) as! AAStickersViewCell
        stickerCell.bind(stickers[(indexPath as NSIndexPath).row], clearPrev: true)
        return stickerCell
    }
    
    open func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let sticker = stickers[(indexPath as NSIndexPath).row]
        self.delegate?.stickerDidSelected(self, sticker: sticker)
    }
}

open class AAStickersViewCell : UICollectionViewCell {
    
    fileprivate let stickerImage = AAStickerView()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = UIColor.clear
        self.contentView.backgroundColor = UIColor.clear
        self.addSubview(self.stickerImage)
    }
    
    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open func bind(_ sticker: ACSticker!, clearPrev: Bool) {
        var fileLocation: ACFileReference? = sticker.getImage128()
        if sticker.getImage256() != nil {
            fileLocation = sticker.getImage256()
        }
        self.stickerImage.setSticker(fileLocation)
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        self.stickerImage.frame = self.contentView.frame
    }
    
    open override func prepareForReuse() {
        super.prepareForReuse()
        self.stickerImage.setSticker(nil)
    }
}
