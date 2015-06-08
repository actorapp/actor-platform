//
//  CropController.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 09.05.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class CropController: AAViewController {
    
    let cropView = PECropView()
    var img: UIImage!
    
    init(image: UIImage) {
        super.init()
        img = image
        // cropView.keepingCropAspectRatio = true
        cropView.backgroundColor = UIColor.blackColor()
        view.backgroundColor = UIColor.blackColor()
        view.addSubview(cropView)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        cropView.frame = CGRect(x: 0, y: 0, width: 100, height: 100)
        cropView.image = img
        cropView.cropRect = CGRect(x: 0, y: 0, width: img.size.height, height: img.size.width)
        cropView.cropAspectRatio = 1.0
        cropView.keepingCropAspectRatio = true
    }
    
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        cropView.frame = CGRectMake(0, 0, view.frame.width, view.frame.height)
    }
}