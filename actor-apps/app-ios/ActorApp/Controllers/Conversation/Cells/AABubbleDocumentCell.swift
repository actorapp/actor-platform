//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class AABubbleDocumentCell: AABubbleBaseFileCell, UIDocumentInteractionControllerDelegate {
    
    private let progress = CircullarLayerProgress(size: CGSizeMake(48, 48))
    private let fileIcon = UIImageView()
    
    private let titleLabel = UILabel()
    private let sizeLabel = UILabel()
    
    private let dateLabel = UILabel()
    private let statusView = UIImageView()
    
    private var bindedLayout: DocumentCellLayout!
    
    init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        dateLabel.font = UIFont.italicSystemFontOfSize(11)
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
        
        mainView.addSubview(titleLabel)
        mainView.addSubview(sizeLabel)
        
        mainView.addSubview(dateLabel)
        mainView.addSubview(statusView)
        
        mainView.addSubview(fileIcon)
        mainView.addSubview(progress)
        
        self.contentInsets = UIEdgeInsetsMake(0, 0, 0, 0)
        
        self.bubble.userInteractionEnabled = true
        self.bubble.addGestureRecognizer(UITapGestureRecognizer(target: self, action: "documentDidTap"))
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Bind
    
    override func bind(message: ACMessage, reuse: Bool, cellLayout: CellLayout, setting: CellSetting) {

        self.bindedLayout = cellLayout as! DocumentCellLayout
        
        let document = message.content as! ACDocumentContent
        
        self.bubbleInsets = UIEdgeInsets(
            top: setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop,
            left: 10 + (isIPad ? 16 : 0),
            bottom: setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom,
            right: 10 + (isIPad ? 16 : 0))
        
        if (!reuse) {
            if (isOut) {
                bindBubbleType(.MediaOut, isCompact: false)
                dateLabel.textColor = MainAppTheme.bubbles.textDateOut
                self.statusView.hidden = false
            } else {
                bindBubbleType(.MediaIn, isCompact: false)
                dateLabel.textColor = MainAppTheme.bubbles.textDateIn
                self.statusView.hidden = true
            }
            
            titleLabel.text = bindedLayout.fileName
            sizeLabel.text = bindedLayout.fileSize
            
            // Reset progress
            self.progress.hideButton()
            UIView.animateWithDuration(0, animations: { () -> Void in
                self.progress.alpha = 0
                self.fileIcon.alpha = 0
            })
            
            // Bind file
            fileBind(message, autoDownload: document.getSource().getSize() < 1024 * 1025 * 1024)
        }
        
        // Always update date and state
        dateLabel.text = cellLayout.date
        
        // Setting message status
        if (isOut) {
            switch(UInt(message.messageState.ordinal())) {
            case ACMessageState.PENDING.rawValue:
                self.statusView.image = Resources.iconClock
                self.statusView.tintColor = MainAppTheme.bubbles.statusSending
                break
            case ACMessageState.SENT.rawValue:
                self.statusView.image = Resources.iconCheck1
                self.statusView.tintColor = MainAppTheme.bubbles.statusSent
                break
            case ACMessageState.RECEIVED.rawValue:
                self.statusView.image = Resources.iconCheck2
                self.statusView.tintColor = MainAppTheme.bubbles.statusReceived
                break
            case ACMessageState.READ.rawValue:
                self.statusView.image = Resources.iconCheck2
                self.statusView.tintColor = MainAppTheme.bubbles.statusRead
                break
            case ACMessageState.ERROR.rawValue:
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
    
    func documentDidTap() {
        let content = bindedMessage!.content as! ACDocumentContent
        if let fileSource = content.getSource() as? ACFileRemoteSource {
            Actor.requestStateWithFileId(fileSource.getFileReference().getFileId(), withCallback: CocoaDownloadCallback(
                notDownloaded: { () -> () in
                    Actor.startDownloadingWithReference(fileSource.getFileReference())
                }, onDownloading: { (progress) -> () in
                    Actor.cancelDownloadingWithFileId(fileSource.getFileReference().getFileId())
                }, onDownloaded: { (reference) -> () in
                    let docController = UIDocumentInteractionController(URL: NSURL(fileURLWithPath: CocoaFiles.pathFromDescriptor(reference)))
                    docController.delegate = self
                    docController.presentPreviewAnimated(true)
            }))
        } else if let fileSource = content.getSource() as? ACFileLocalSource {
            let rid = bindedMessage!.rid
            Actor.requestUploadStateWithRid(rid, withCallback: CocoaUploadCallback(
                notUploaded: { () -> () in
                    Actor.resumeUploadWithRid(rid)
                }, onUploading: { (progress) -> () in
                    Actor.pauseUploadWithRid(rid)
                }, onUploadedClosure: { () -> () in
                    let docController = UIDocumentInteractionController(URL: NSURL(fileURLWithPath: CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor())))
                    docController.delegate = self
                    docController.presentPreviewAnimated(true)
            }))
        }
    }
    
    override func fileUploadPaused(reference: String, selfGeneration: Int) {
        self.runOnUiThread(selfGeneration) { () -> () in
            self.fileIcon.hideView()
            
            self.progress.showView()
            self.progress.hideProgress()
            self.progress.setButtonType(FlatButtonType.buttonUpBasicType, animated: true)
        }
    }
    
    override func fileUploading(reference: String, progress: Double, selfGeneration: Int) {
        self.runOnUiThread(selfGeneration) { () -> () in
            self.fileIcon.hideView()
            
            self.progress.showView()
            self.progress.setProgress(progress)
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
        }
    }
    
    override func fileDownloadPaused(selfGeneration: Int) {
        self.runOnUiThread(selfGeneration) { () -> () in
            self.fileIcon.hideView()
            
            self.progress.showView()
            self.progress.hideProgress()
            self.progress.setButtonType(FlatButtonType.buttonDownloadType, animated: true)
        }
    }
    
    override func fileDownloading(progress: Double, selfGeneration: Int) {
        self.runOnUiThread(selfGeneration) { () -> () in
            self.fileIcon.hideView()
            
            self.progress.showView()
            self.progress.setProgress(progress)
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
        }
    }
    
    override func fileReady(reference: String, selfGeneration: Int) {
        self.runOnUiThread(selfGeneration) { () -> () in
            
            self.fileIcon.image = self.bindedLayout.icon
            self.fileIcon.showView()
            
            self.progress.hideView()
            self.progress.setProgress(1)
        }
    }
    
    override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        let insets = fullContentInsets
        
        let contentWidth = self.contentView.frame.width
        let top = insets.top - 2
//        let contentHeight = self.contentView.frame.height
        
        layoutBubble(200, contentHeight: 66)
        
        let contentLeft = self.isOut ? contentWidth - 200 - insets.right - contentInsets.left : insets.left
        
        // Content
        self.titleLabel.frame = CGRectMake(contentLeft + 62, 16 + top, 200 - 64, 22)
        self.sizeLabel.frame = CGRectMake(contentLeft + 62, 16 + 22 + top, 200 - 64, 22)
        
        // Progress state
        let progressRect = CGRectMake(contentLeft + 8, 12 + top, 48, 48)
        self.progress.frame = progressRect
        self.fileIcon.frame = CGRectMake(contentLeft + 16, 20 + top, 32, 32)
        
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

    func documentInteractionControllerViewControllerForPreview(controller: UIDocumentInteractionController) -> UIViewController {
        return self.controller
    }
}

class AABubbleDocumentCellLayout: AABubbleLayouter {
    
    func isSuitable(message: ACMessage) -> Bool {
        return message.content is ACDocumentContent
    }
    
    func buildLayout(peer: ACPeer, message: ACMessage) -> CellLayout {
        return DocumentCellLayout(message: message)
    }
    
    func cellClass() -> AnyClass {
        return AABubbleDocumentCell.self
    }
}

class DocumentCellLayout: CellLayout {
    
    let fileName: String
    let fileExt: String
    let fileSize: String
    
    let icon: UIImage
    let fastThumb: NSData?
    
    let autoDownload: Bool
    
    init(fileName: String, fileExt: String, fileSize: Int, fastThumb: ACFastThumb?, date: Int64, autoDownload: Bool) {
        
        // File metadata
        self.fileName = fileName
        self.fileExt = fileExt.lowercaseString
        self.fileSize = Actor.getFormatter().formatFileSize(jint(fileSize))
        
        // Auto download flag
        self.autoDownload = autoDownload
        
        // Fast thumb
        self.fastThumb = fastThumb?.getImage().toNSData()
        
        // File icon
        var fileName = "file_unknown"
        if (FileTypes[self.fileExt] != nil) {
            switch(FileTypes[self.fileExt]!) {
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
        self.icon = UIImage(named: fileName)!
        
        super.init(height: 66, date: date, key: "document")
    }
    
    convenience init(document: ACDocumentContent, date: Int64) {
        self.init(fileName: document.getName(), fileExt: document.getExt(), fileSize: Int(document.getSource().getSize()), fastThumb: document.getFastThumb(), date: date, autoDownload: (document.getSource().getSize() < 1024 * 1025 * 1024))
    }
    
    convenience init(message: ACMessage) {
        self.init(document: message.content as! ACDocumentContent, date: Int64(message.date))
    }
}