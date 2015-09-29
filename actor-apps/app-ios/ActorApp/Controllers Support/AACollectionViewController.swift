//
//  AACollectionViewController.swift
//  ActorApp
//
//  Created by Steve Kite on 24.09.15.
//  Copyright © 2015 Actor LLC. All rights reserved.
//

import Foundation

class AACollectionViewController: AAViewController, UICollectionViewDelegate, UICollectionViewDataSource {
    
    var collectionView:UICollectionView!
    
    init(collectionLayout: UICollectionViewLayout) {
        super.init()
        
        collectionView = UICollectionView(frame: CGRectZero, collectionViewLayout: collectionLayout)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func loadView() {
        super.loadView()
        
        collectionView.delegate = self
        collectionView.dataSource = self
        view.addSubview(collectionView)
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        collectionView.frame = view.bounds;
    }

    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        fatalError("Not implemented!")
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        fatalError("Not implemented!")
    }
}