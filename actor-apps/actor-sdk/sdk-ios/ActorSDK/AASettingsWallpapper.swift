//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class AASettingsWallpapper: AACollectionViewController, UICollectionViewDelegateFlowLayout {
    
    public let padding: CGFloat = 8
    
    public init() {
        super.init(collectionLayout: UICollectionViewFlowLayout())
        
        navigationItem.title = localized("WallpapersTitle")
        
        collectionView.registerClass(AAWallpapperPreviewCell.self, forCellWithReuseIdentifier: "cell")
        collectionView.contentInset = UIEdgeInsets(top: padding, left: padding, bottom: padding, right: padding)
        collectionView.backgroundColor = ActorSDK.sharedActor().style.tableBgColor
        view.backgroundColor = ActorSDK.sharedActor().style.tableBgColor
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return 100
    }
    
    public override func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let res = collectionView.dequeueReusableCellWithReuseIdentifier("cell", forIndexPath: indexPath) as! AAWallpapperPreviewCell
        res.bind(indexPath.item % 3)
//        if indexPath.item % 2 == 0 {
//            res.backgroundColor = UIColor.redColor()
//        } else {
//            res.backgroundColor = UIColor.greenColor()
//        }
        return res
    }
    
    public func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAtIndexPath indexPath: NSIndexPath) -> CGSize {
        
        let w = (collectionView.width  - 4 * padding) / 3
        let h = w * (UIScreen.mainScreen().bounds.height / UIScreen.mainScreen().bounds.width)
        
        return CGSize(width: w, height: h)
    }
    
    public func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAtIndex section: Int) -> CGFloat {
        return padding
    }
    
    public func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAtIndex section: Int) -> CGFloat {
        return padding
    }
}