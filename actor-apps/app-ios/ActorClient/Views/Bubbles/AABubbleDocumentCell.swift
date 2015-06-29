//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class AABubbleDocumentCell: AABubbleBaseFileCell {
    
    // MARK: -
    // MARK: Private vars
    
    private let progressBg = UIImageView()
    private let circullarNode = CircullarNode()
    private let fileIcon = UIImageView()
    
    private let titleLabel = UILabel()
    private let sizeLabel = UILabel()
    
    private let dateLabel = UILabel()
    private let statusView = UIImageView()
    
    private var bindedExt = ""
    
    // MARK: -
    // MARK: Constructors
    
    init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        dateLabel.font = UIFont(name: "HelveticaNeue-Italic", size: 11)
        dateLabel.lineBreakMode = .ByClipping
        dateLabel.numberOfLines = 1
        dateLabel.contentMode = UIViewContentMode.TopLeft
        dateLabel.textAlignment = NSTextAlignment.Right
        dateLabel.textColor = MainAppTheme.bubbles.textDateOut
        
        statusView.contentMode = UIViewContentMode.Center
        
        titleLabel.font = UIFont.systemFontOfSize(16.0)
        titleLabel.textColor = MainAppTheme.bubbles.textOut
        titleLabel.text = " "
        titleLabel.sizeToFit()
        titleLabel.lineBreakMode = NSLineBreakMode.ByTruncatingTail
        
        sizeLabel.font = UIFont.systemFontOfSize(13.0)
        sizeLabel.textColor = MainAppTheme.bubbles.textOut
        sizeLabel.text = " "
        sizeLabel.sizeToFit()
        
        progressBg.image = Imaging.roundedImage(UIColor(red: 0, green: 0, blue: 0, alpha: 0x64/255.0), size: CGSizeMake(CGFloat(64.0),CGFloat(64.0)), radius: CGFloat(32.0))
        
        mainView.addSubview(titleLabel)
        mainView.addSubview(sizeLabel)
        
        mainView.addSubview(dateLabel)
        mainView.addSubview(statusView)
        
        mainView.addSubview(progressBg)
        mainView.addSubview(fileIcon)
        mainView.addSubview(circullarNode.view)
        
        self.bubbleInsets = UIEdgeInsets(
            top: 3,
            left: 10,
            bottom: 3,
            right: 10)
        self.contentInsets = UIEdgeInsetsMake(0, 0, 0, 0)        
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Bind
    
    override func bind(message: AMMessage, reuse: Bool, cellLayout: CellLayout, setting: CellSetting) {
        let document = message.getContent() as! AMDocumentContent
        
        if (!reuse) {
            if (isOut) {
                bindBubbleType(.MediaOut, isCompact: false)
                dateLabel.textColor = MainAppTheme.bubbles.textDateOut
            } else {
                bindBubbleType(.MediaIn, isCompact: false)
                dateLabel.textColor = MainAppTheme.bubbles.textDateIn
            }
            
            titleLabel.text = document.getName()
            bindedExt = document.getExt().lowercaseString
            sizeLabel.text = MSG.getFormatter().formatFileSize(document.getSource().getSize())
            
            // Reset progress
            circullarNode.hidden = true
            circullarNode.setProgress(0, animated: false)
            UIView.animateWithDuration(0, animations: { () -> Void in
                self.circullarNode.alpha = 0
                self.fileIcon.alpha = 0
                self.progressBg.alpha = 0
            })
            
            // Bind file
            fileBind(message, autoDownload: document.getSource().getSize() < 1024 * 1025 * 1024)
        }
        
        // Always update date and state
        dateLabel.text = cellLayout.date
        if (isOut) {
            switch(UInt(message.getMessageState().ordinal())) {
            case AMMessageState.PENDING.rawValue:
                self.statusView.image = Resources.iconClock
                self.statusView.tintColor = MainAppTheme.bubbles.statusSending
                break
            case AMMessageState.SENT.rawValue:
                self.statusView.image = Resources.iconCheck1
                self.statusView.tintColor = MainAppTheme.bubbles.statusSent
                break
            case AMMessageState.RECEIVED.rawValue:
                self.statusView.image = Resources.iconCheck2
                self.statusView.tintColor = MainAppTheme.bubbles.statusReceived
                break
            case AMMessageState.READ.rawValue:
                self.statusView.image = Resources.iconCheck2
                self.statusView.tintColor = MainAppTheme.bubbles.statusRead
                break
            case AMMessageState.ERROR.rawValue:
                self.statusView.image = Resources.iconError
                self.statusView.tintColor = MainAppTheme.bubbles.statusError
                break
            default:
                self.statusView.image = Resources.iconClock
                self.statusView.tintColor = MainAppTheme.bubbles.statusSending
                break
            }
        }
    }
    
    override func fileUploadPaused(reference: String, selfGeneration: Int) {
        bgShowState(selfGeneration)
        bgShowIcon(UIImage(named: "ic_upload")!.tintImage(UIColor.whiteColor()), selfGeneration: selfGeneration)
        bgHideProgress(selfGeneration)
    }
    
    override func fileUploading(reference: String, progress: Double, selfGeneration: Int) {
        bgShowState(selfGeneration)
        bgHideIcon(selfGeneration)
        bgShowProgress(progress, selfGeneration: selfGeneration)
    }
    
    override func fileDownloadPaused(selfGeneration: Int) {
        bgShowState(selfGeneration)
        bgShowIcon(UIImage(named: "ic_download")!.tintImage(UIColor.whiteColor()), selfGeneration: selfGeneration)
        bgHideProgress(selfGeneration)
    }
    
    override func fileDownloading(progress: Double, selfGeneration: Int) {
        bgShowState(selfGeneration)
        bgHideIcon(selfGeneration)
        bgShowProgress(progress, selfGeneration: selfGeneration)
    }
    
    override func fileReady(reference: String, selfGeneration: Int) {
        bgHideState(selfGeneration)
        var fileName = "file_unknown"
        if (FileTypes[bindedExt] != nil) {
            switch(FileTypes[bindedExt]!) {
            case FileType.Music:
                fileName = "file_music"
                break
            case FileType.Doc:
                fileName = "file_doc"
                break
            case FileType.Spreadsheet:
                fileName = "file_xls"
                break
            case FileType.Video:
                fileName = "file_video"
                break
            case FileType.Presentation:
                fileName = "file_ppt"
                break
            case FileType.PDF:
                fileName = "file_pdf"
                break
            case FileType.APK:
                fileName = "file_apk"
                break
            case FileType.RAR:
                fileName = "file_rar"
                break
            case FileType.ZIP:
                fileName = "file_zip"
                break
            case FileType.CSV:
                fileName = "file_csv"
                break
            case FileType.HTML:
                fileName = "file_html"
                break
            default:
                fileName = "file_unknown"
                break
            }
        }
        bgShowIcon(UIImage(named: fileName)!, selfGeneration: selfGeneration)
        bgHideProgress(selfGeneration)
    }
    
    // Progress show/hide
    func bgHideProgress(selfGeneration: Int) {
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            UIView.animateWithDuration(0.4, animations: { () -> Void in
                self.circullarNode.alpha = 0
                }, completion: { (val) -> Void in
                    if (val) {
                        self.circullarNode.hidden = true
                    }
            })
        })
    }
    func bgShowProgress(value: Double, selfGeneration: Int) {
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            if (self.circullarNode.hidden) {
                self.circullarNode.hidden = false
                self.circullarNode.alpha = 0
            }
            self.circullarNode.postProgress(value, animated: true)
            UIView.animateWithDuration(0.3, animations: { () -> Void in
                self.circullarNode.alpha = 1
            })
        })
    }
    
    // State show/hide
    func bgHideState(selfGeneration: Int) {
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            self.progressBg.hideView()
        })
    }
    
    func bgShowState(selfGeneration: Int) {
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            self.progressBg.showView()
        })
    }
    
    // Icon show/hide
    func bgShowIcon(image: UIImage, selfGeneration: Int) {
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            self.fileIcon.image = image
            self.fileIcon.showView()
        })
    }
    func bgHideIcon(selfGeneration: Int) {
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            self.fileIcon.hideView()
        })
    }
    
    // MARK: -
    // MARK: Methods
    
    // MARK: -
    // MARK: Getters
    
    class func measureServiceHeight(message: AMMessage) -> CGFloat {
        return 66 + 6
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        var insets = fullContentInsets
        
        var contentWidth = self.contentView.frame.width
        var contentHeight = self.contentView.frame.height
        
        layoutBubble(200, contentHeight: 66)
        
        var contentLeft = self.isOut ? contentWidth - 200 - insets.right - contentInsets.left : insets.left
        
        // Content
        self.titleLabel.frame = CGRectMake(contentLeft + 62, 16, 200 - 64, 22)
        self.sizeLabel.frame = CGRectMake(contentLeft + 62, 16 + 22, 200 - 64, 22)
        
        // Progress state
        var progressRect = CGRectMake(contentLeft + 8, 12, 48, 48)
        self.progressBg.frame = progressRect
        self.fileIcon.frame = CGRectMake(contentLeft + 16, 20, 32, 32)
        self.circullarNode.frame = progressRect
        
        // Message state
        if (self.isOut) {
            self.dateLabel.frame = CGRectMake(self.bubble.frame.maxX - 70 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
            self.statusView.frame = CGRectMake(self.bubble.frame.maxX - 24 - self.bubblePadding, self.bubble.frame.maxY - 24, 20, 26)
            self.statusView.hidden = false
        } else {
            self.dateLabel.frame = CGRectMake(self.bubble.frame.maxX - 47 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
            self.statusView.hidden = true
        }
    }

}
