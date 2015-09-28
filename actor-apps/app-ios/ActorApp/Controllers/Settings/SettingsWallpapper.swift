//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class SettingsWallpapper: AACollectionViewController, UICollectionViewDelegateFlowLayout {
    
    let padding: CGFloat = 8
    
    init() {
        super.init(collectionLayout: UICollectionViewFlowLayout())
        
        navigationItem.title = localized("WallpapersTitle")
        
        collectionView.registerClass(WallpapperPreviewCell.self, forCellWithReuseIdentifier: "cell")
        collectionView.contentInset = UIEdgeInsets(top: padding, left: padding, bottom: padding, right: padding)
        collectionView.backgroundColor = MainAppTheme.list.bgColor
        view.backgroundColor = MainAppTheme.list.bgColor
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return 100
    }
    
    override func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let res = collectionView.dequeueReusableCellWithReuseIdentifier("cell", forIndexPath: indexPath) as! WallpapperPreviewCell
        res.bind(indexPath.item % 3)
//        if indexPath.item % 2 == 0 {
//            res.backgroundColor = UIColor.redColor()
//        } else {
//            res.backgroundColor = UIColor.greenColor()
//        }
        return res
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAtIndexPath indexPath: NSIndexPath) -> CGSize {
        
        let w = (collectionView.width  - 4 * padding) / 3
        let h = w * (UIScreen.mainScreen().bounds.height / UIScreen.mainScreen().bounds.width)
        
        return CGSize(width: w, height: h)
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAtIndex section: Int) -> CGFloat {
        return padding
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAtIndex section: Int) -> CGFloat {
        return padding
    }
}