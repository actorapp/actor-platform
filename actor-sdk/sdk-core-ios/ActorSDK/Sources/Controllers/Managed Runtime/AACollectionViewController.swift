//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AACollectionViewController: AAViewController, UICollectionViewDelegate, UICollectionViewDataSource {
    
    open var collectionView:UICollectionView!
    
    public init(collectionLayout: UICollectionViewLayout) {
        super.init()
        
        collectionView = UICollectionView(frame: CGRect.zero, collectionViewLayout: collectionLayout)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func loadView() {
        super.loadView()
        
        collectionView.delegate = self
        collectionView.dataSource = self
        view.addSubview(collectionView)
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItemStyle.plain, target: nil, action: nil)
    }
    
    open override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        collectionView.frame = view.bounds;
    }

    open func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        fatalError("Not implemented!")
    }
    
    open func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        fatalError("Not implemented!")
    }
}
