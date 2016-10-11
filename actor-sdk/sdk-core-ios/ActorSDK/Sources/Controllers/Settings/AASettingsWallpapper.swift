//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AASettingsWallpapper: AACollectionViewController, UICollectionViewDelegateFlowLayout {
    
    let padding: CGFloat = 8
    
    init() {
        super.init(collectionLayout: UICollectionViewFlowLayout())
        
        navigationItem.title = AALocalized("WallpapersTitle")
        
        collectionView.register(AAWallpapperPreviewCell.self, forCellWithReuseIdentifier: "cell")
        collectionView.contentInset = UIEdgeInsets(top: padding, left: padding, bottom: padding, right: padding)
        collectionView.backgroundColor = ActorSDK.sharedActor().style.vcBgColor
        view.backgroundColor = ActorSDK.sharedActor().style.vcBgColor
    }

    required public init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override open func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return 100
    }
    
    override open func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let res = collectionView.dequeueReusableCell(withReuseIdentifier: "cell", for: indexPath) as! AAWallpapperPreviewCell
        res.bind((indexPath as NSIndexPath).item % 3)
        return res
    }
    
    open func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        
        let w = (collectionView.width  - 4 * padding) / 3
        let h = w * (UIScreen.main.bounds.height / UIScreen.main.bounds.width)
        
        return CGSize(width: w, height: h)
    }
    
    open func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return padding
    }
    
    open func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return padding
    }
}
