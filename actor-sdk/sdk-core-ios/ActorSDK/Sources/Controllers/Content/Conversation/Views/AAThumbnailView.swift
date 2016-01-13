//
//  AAThumbnailView.swift
//  ActorSDK
//
//  Created by kioshimafx on 1/13/16.
//  Copyright © 2016 Steve Kite. All rights reserved.
//

import UIKit

class AAThumbnailView: UIView,UICollectionViewDelegate , UICollectionViewDataSource {

    var collectionView:UICollectionView!
    
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.configUI()
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    ///
    
    func configUI() {
        self.collectionViewSetup()
        self.addSubview(self.collectionView)
    }
    
    /// collection view delegate
    
    func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return 0
    }
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return 0
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        return UICollectionViewCell()
    }
    
    ///
    
    func reloadView() {
        self.collectionView.reloadData()
    }
    
    func collectionViewSetup() {
        
        if (self.collectionView == nil) {
            
            let flowLayout = UICollectionViewFlowLayout()
            flowLayout.scrollDirection = .Horizontal
            flowLayout.minimumLineSpacing = 4
            flowLayout.sectionInset = UIEdgeInsetsMake(5.0, 4.0, 5.0, 4.0)
            
            flowLayout.itemSize = CGSizeMake(105, 180)
            
            self.collectionView = UICollectionView(frame: self.bounds, collectionViewLayout: flowLayout)
            self.collectionView.backgroundColor = UIColor(red: 230.0/255.0, green: 231.0/255.0, blue: 234.0/255.0, alpha: 1.0)
            self.collectionView.showsHorizontalScrollIndicator = false
            self.collectionView.delegate = self
            self.collectionView.dataSource = self
            
            self.collectionView.registerClass(AAThumbnailCollectionCell.self, forCellWithReuseIdentifier: "AAThumbnailCollectionCell")
            
            //AAASAssetManager.sharedInstance
            
            /*
            
                [[UUAssetManager sharedInstance] getGroupList:^(NSArray *obj) {
                
                    [[UUAssetManager sharedInstance] getPhotoListOfGroupByIndex:[UUAssetManager sharedInstance].currentGroupIndex result:^(NSArray *obj) {
                
                    [_collectionView reloadData];
                
                    }];
                }];
            
            */
            
            
            
        }
        
    }

}
