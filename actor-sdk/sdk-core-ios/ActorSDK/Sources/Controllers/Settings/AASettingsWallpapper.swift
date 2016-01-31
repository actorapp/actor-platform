//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AASettingsWallpapper: AACollectionViewController, UICollectionViewDelegateFlowLayout {
    
    let padding: CGFloat = 8
    
    init() {
        super.init(collectionLayout: UICollectionViewFlowLayout())
        
        navigationItem.title = AALocalized("WallpapersTitle")
        
        collectionView.registerClass(AAWallpapperPreviewCell.self, forCellWithReuseIdentifier: "cell")
        collectionView.contentInset = UIEdgeInsets(top: padding, left: padding, bottom: padding, right: padding)
        collectionView.backgroundColor = ActorSDK.sharedActor().style.vcBgColor
        view.backgroundColor = ActorSDK.sharedActor().style.vcBgColor
    }

    required public init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override public func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return 100
    }
    
    override public func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let res = collectionView.dequeueReusableCellWithReuseIdentifier("cell", forIndexPath: indexPath) as! AAWallpapperPreviewCell
        res.bind(indexPath.item % 3)
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