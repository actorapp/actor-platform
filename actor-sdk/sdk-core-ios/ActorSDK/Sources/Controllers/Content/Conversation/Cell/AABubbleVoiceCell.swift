//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import VBFPopFlatButton
import YYImage

public class AABubbleVoiceCell: AABubbleBaseFileCell,AAModernConversationAudioPlayerDelegate,AAModernViewInlineMediaContextDelegate {

    // Views
    
    private let progress = AAProgressView(size: CGSizeMake(44, 44))
    private let timeLabel = AttributedLabel()
    private let voiceTimeLabel = AttributedLabel()
    private let playPauseButton = UIButton()
    private let soundProgressSlider = UISlider()
    private let statusView = UIImageView()
    

    private let durationLabel = AttributedLabel()
    private let titleLabel = AttributedLabel()
    
    // Binded data
    
    var bindedLayout: VoiceMessageCellLayout!
    var thumbLoaded = false
    var contentLoaded = false
    
    // Constructors
    
    public init(frame: CGRect) {
        
        super.init(frame: frame, isFullSize: false)
        
        ////////////////////////////////////////////////////
        
        timeLabel.font = UIFont.italicSystemFontOfSize(11)
        timeLabel.textColor = appStyle.chatTextDateOutColor
        
        ////////////////////////////////////////////////////
        
        statusView.contentMode = UIViewContentMode.Center
        
        ////////////////////////////////////////////////////
        
        soundProgressSlider.tintColor = appStyle.chatStatusSending
        
        soundProgressSlider.userInteractionEnabled = false
        
        //soundProgressSlider.addTarget(self, action: "seekToNewAudioValue", forControlEvents: UIControlEvents.ValueChanged)
        
        let insets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 0)
        
        let trackLeftImage = UIImage.tinted("aa_voiceplaybackground", color: UIColor(red: 0.0, green: 0.761, blue: 0.9964, alpha: 1.0 ))
        let trackLeftResizable = trackLeftImage.resizableImageWithCapInsets(insets)
        soundProgressSlider.setMinimumTrackImage(trackLeftResizable, forState: .Normal)
        
        let trackRightImage = UIImage.tinted("aa_voiceplaybackground", color: UIColor(red: 0.0, green: 0.5856, blue: 0.9985, alpha: 1.0 ))
        let trackRightResizable = trackRightImage.resizableImageWithCapInsets(insets)
        soundProgressSlider.setMaximumTrackImage(trackRightResizable, forState: .Normal)
        
        let thumbImageNormal = UIImage.bundled("aa_thumbvoiceslider")
        soundProgressSlider.setThumbImage(thumbImageNormal, forState: .Normal)
        
        ////////////////////////////////////////////////////
        
        voiceTimeLabel.font = UIFont.italicSystemFontOfSize(11)
        voiceTimeLabel.textColor = appStyle.chatTextDateOutColor
        
        ////////////////////////////////////////////////////

        durationLabel.font = UIFont.systemFontOfSize(11.0)
        durationLabel.textColor = appStyle.chatTextDateOutColor
        durationLabel.text = " "
        durationLabel.sizeToFit()
        
        ////////////////////////////////////////////////////
        
        durationLabel.layer.drawsAsynchronously         = true
        progress.layer.drawsAsynchronously              = true
        playPauseButton.layer.drawsAsynchronously       = true
        soundProgressSlider.layer.drawsAsynchronously   = true
        voiceTimeLabel.layer.drawsAsynchronously        = true
        timeLabel.layer.drawsAsynchronously             = true
        statusView.layer.drawsAsynchronously            = true
        
        ////////////////////////////////////////////////////
        
        contentView.addSubview(durationLabel)
        contentView.addSubview(progress)
        contentView.addSubview(playPauseButton)
        contentView.addSubview(soundProgressSlider)
        contentView.addSubview(voiceTimeLabel)
        contentView.addSubview(timeLabel)
        contentView.addSubview(statusView)
        
        ////////////////////////////////////////////////////
        
        playPauseButton.setImage(UIImage.bundled("aa_playrecordbutton"), forState: UIControlState.Normal)
        playPauseButton.addTarget(self, action: #selector(AABubbleVoiceCell.mediaDidTap), forControlEvents: UIControlEvents.TouchUpInside)
        
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)
        
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: - Binding
    
    public override func bind(message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        self.bindedLayout = cellLayout as! VoiceMessageCellLayout
        
        let document = message.content as! ACVoiceContent
        
        bubbleInsets = UIEdgeInsets(
            top: setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop,
            left: 10 + (AADevice.isiPad ? 16 : 0),
            bottom: setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom,
            right: 10 + (AADevice.isiPad ? 16 : 0))
        
        
        if (!reuse) {
            
            // Bind bubble
            if (self.isOut) {
                bindBubbleType(BubbleType.MediaOut, isCompact: false)
            } else {
                bindBubbleType(BubbleType.MediaIn, isCompact: false)
            }
            
            titleLabel.text = AALocalized("ChatVoiceMessage")
            durationLabel.text = bindedLayout.voiceDuration
            
            // Reset progress
            self.progress.hideButton()
            UIView.animateWithDuration(0, animations: { () -> Void in
                self.progress.hidden = true
            })
            
            // Bind file
            fileBind(message, autoDownload: document.getSource().getSize() < 1024 * 1025 * 1024)
        
        }
        
        dispatchOnUi { () -> Void in
            
            //let content = bindedMessage!.content as! ACDocumentContent
            
            let content = self.bindedMessage!.content as! ACDocumentContent
            if let fileSource = content.getSource() as? ACFileRemoteSource {
                let fileID = fileSource.getFileReference().getFileId()
                
                if self.controller.currentAudioFileId != fileID {
                    self.soundProgressSlider.value = 0.0
                    self.playPauseButton.setImage(UIImage.bundled("aa_playrecordbutton"), forState: UIControlState.Normal)
                    self.controller.voiceContext?.removeDelegate(self)
                } else {
                    self.controller.voiceContext?.delegate = self
                    self.controller.voicePlayer?.delegate = self
                    
                    if self.controller.voicePlayer?.isPaused() == false {
                        self.playPauseButton.setImage(UIImage.bundled("aa_pauserecordbutton"), forState: UIControlState.Normal)
                    } else {
                        self.playPauseButton.setImage(UIImage.bundled("aa_playrecordbutton"), forState: UIControlState.Normal)
                    }
                    
                }
                
                if let progressVoice = self.controller.voicesCache[fileID] {
                    if progressVoice > 0.0 {
                        self.soundProgressSlider.value = progressVoice
                    }
                }
                
            }
            
        }
        
        
        // Update time
        timeLabel.text = cellLayout.date
        
        // Update status
        if (isOut) {
            statusView.hidden = false
            switch(message.messageState.toNSEnum()) {
            case .SENT:
                if message.sortDate <= readDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusRead
                } else if message.sortDate <= receiveDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusReceived
                } else {
                    self.statusView.image = appStyle.chatIconCheck1
                    self.statusView.tintColor = appStyle.chatStatusSent
                }
            case .ERROR:
                self.statusView.image = appStyle.chatIconError
                self.statusView.tintColor = appStyle.chatStatusError
                break
            default:
                self.statusView.image = appStyle.chatIconClock
                self.statusView.tintColor = appStyle.chatStatusSending
                break
            }
        } else {
            statusView.hidden = true
        }
    }
    
    
    //MARK: - File state binding
    
    public override func fileUploadPaused(reference: String, selfGeneration: Int) {
        
        runOnUiThread(selfGeneration) { () -> () in
            self.playPauseButton.hideView()
            
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonUpBasicType, animated: true)
            self.progress.hideProgress()
        }
    }
    
    public override func fileUploading(reference: String, progress: Double, selfGeneration: Int) {
        
        runOnUiThread(selfGeneration) { () -> () in
            self.playPauseButton.hideView()
            
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
            self.progress.setProgress(progress)
        }
    }
    
    public override func fileDownloadPaused(selfGeneration: Int) {
        
        runOnUiThread(selfGeneration) { () -> () in
            self.playPauseButton.hideView()
            
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonDownloadType, animated: true)
            self.progress.hideProgress()
        }
    }
    
    public override func fileDownloading(progress: Double, selfGeneration: Int) {
        
        runOnUiThread(selfGeneration) { () -> () in
            self.playPauseButton.hideView()
            
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
            self.progress.setProgress(progress)
        }
    }
    
    public override func fileReady(reference: String, selfGeneration: Int) {
        
        runOnUiThread(selfGeneration) { () -> () in
            self.playPauseButton.showView()
            
            self.progress.setProgress(1)
            self.progress.hideView()
        }
    }
    
    
    //MARK: - Media Action
    
    public func mediaDidTap() {
        let content = bindedMessage!.content as! ACDocumentContent
        if let fileSource = content.getSource() as? ACFileRemoteSource {
            Actor.requestStateWithFileId(fileSource.getFileReference().getFileId(), withCallback: AAFileCallback(
                notDownloaded: { () -> () in
                    Actor.startDownloadingWithReference(fileSource.getFileReference())
                }, onDownloading: { (progress) -> () in
                    Actor.cancelDownloadingWithFileId(fileSource.getFileReference().getFileId())
                }, onDownloaded: { (reference) -> () in
                    
                    dispatchOnUi({ () -> Void in
                        
                        let path = CocoaFiles.pathFromDescriptor(reference)
                        
                        let fileID = fileSource.getFileReference().getFileId()
                        
                        self.controller.playVoiceFromPath(path,fileId: fileID,position:self.soundProgressSlider.value)
                        
                        self.playPauseButton.setImage(UIImage.bundled("aa_pauserecordbutton"), forState: UIControlState.Normal)
                        
                        self.controller.voicePlayer.delegate = self
                        self.controller.voiceContext.delegate = self
                        
                    })

            }))
            
        } else if let fileSource = content.getSource() as? ACFileLocalSource {
            let rid = bindedMessage!.rid
            Actor.requestUploadStateWithRid(rid, withCallback: AAUploadFileCallback(
                notUploaded: { () -> () in
                    Actor.resumeUploadWithRid(rid)
                }, onUploading: { (progress) -> () in
                    Actor.pauseUploadWithRid(rid)
                }, onUploadedClosure: { () -> () in
                    
                    dispatchOnUi({ () -> Void in
                        
                        let path = CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor())
                        
                        let content = self.bindedMessage!.content as! ACDocumentContent
                        if let fileSource = content.getSource() as? ACFileRemoteSource {
                            let fileID = fileSource.getFileReference().getFileId()
                            
                            self.controller.playVoiceFromPath(path,fileId: fileID,position:self.soundProgressSlider.value)
                            
                            self.playPauseButton.setImage(UIImage.bundled("aa_pauserecordbutton"), forState: UIControlState.Normal)
                            
                            self.controller.voicePlayer.delegate = self
                            self.controller.voiceContext.delegate = self
                        }
                        

                    })
                    
                    
            }))
            
        }
    }
    
    public func seekToNewAudioValue() {
        
    }
    
    public func audioPlayerDidFinish() {
        
        dispatchOnUi { () -> Void in
            
            self.playPauseButton.setImage(UIImage.bundled("aa_playrecordbutton"), forState: UIControlState.Normal)
            self.soundProgressSlider.value = 0.0
            self.controller.voicesCache[self.controller.currentAudioFileId] = 0.0
            
        }
        
    }
    
    public func inlineMediaPlaybackStateUpdated(isPaused: Bool, playbackPosition: Float, timestamp: NSTimeInterval, preciseDuration: NSTimeInterval) {
        
        dispatchOnUi({ () -> Void in
        
            self.soundProgressSlider.value = playbackPosition
            
            if (isPaused == true) {
                self.playPauseButton.setImage(UIImage.bundled("aa_playrecordbutton"), forState: UIControlState.Normal)
            }
            
            self.controller.voicesCache[self.controller.currentAudioFileId] = playbackPosition
        
            
        })
        
    }
    
    //MARK: - Layouting
    
    public override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        
        layoutBubble(200, contentHeight: 55)
        
        let contentLeft = self.isOut ? contentWidth - 200 - insets.right - contentInsets.left : insets.left
        
        let top = insets.top - 2
        
        // Progress state
        let progressRect = CGRectMake(contentLeft + 7.5, 7.5 + top, 44, 44)
        self.progress.frame = progressRect
        self.playPauseButton.frame = progressRect
        
        timeLabel.frame = CGRectMake(0, 0, 1000, 1000)
        timeLabel.sizeToFit()
        
        // Content
        self.soundProgressSlider.frame = CGRectMake(contentLeft + 62, 16 + top, 200 - 70, 22)
        self.durationLabel.frame = CGRectMake(contentLeft + 62, 10 + 25 + top, 200 - 64, 22)
        
        // Message state
        if (self.isOut) {
            self.timeLabel.frame = CGRectMake(self.bubble.frame.maxX - 55 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
            self.statusView.frame = CGRectMake(self.bubble.frame.maxX - 24 - self.bubblePadding, self.bubble.frame.maxY - 24, 20, 26)
            self.statusView.hidden = false
        } else {
            self.timeLabel.frame = CGRectMake(self.bubble.frame.maxX - 47 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
            self.statusView.hidden = true
        }
        
    }
    
}

/**
 Voice cell layout
 */
public class VoiceMessageCellLayout: AACellLayout {
    
    public let contentSize: CGSize
    public let screenSize: CGSize
    public let autoDownload: Bool
    
    public let fileName: String
    public let fileExt: String
    public let fileSize: String
    public var voiceDuration: String!
    
    /**
     Creting layout for media bubble
     */
    public init(fileName: String, fileExt: String, fileSize: Int,id: Int64, date: Int64, autoDownload: Bool,duration:jint, layouter: AABubbleLayouter) {
        
        // Saving content size
        self.contentSize = CGSizeMake(200, 55)
        
        // Saving autodownload flag
        self.autoDownload = autoDownload
        
        // Calculating bubble screen size
        self.screenSize = CGSize(width: 200, height: 55)
        
        self.fileName = fileName
        self.fileExt = fileExt.lowercaseString
        self.fileSize = Actor.getFormatter().formatFileSize(jint(fileSize))
        
        // Creating layout
        super.init(height: self.screenSize.height + 2, date: date, key: "voice", layouter: layouter)
        
        self.voiceDuration = getTimeString(Int(duration))
    }
    
    func getTimeString(totalSeconds:Int) -> String {
        
        let seconds = Int(totalSeconds % 60)
        let minutes = Int((totalSeconds / 60) % 60)
        
        if minutes < 10 {
            if seconds < 10 {
                return "0\(minutes):0\(seconds)"
            } else {
                return "0\(minutes):\(seconds)"
            }
        } else {
            if seconds < 10 {
                return "\(minutes):0\(seconds)"
            } else {
                return "\(minutes):\(seconds)"
            }
        }
        
    }
    
    /**
     Creating layout for voice content
     */
    
    public convenience init(id: Int64, voiceContent: ACVoiceContent, date: Int64, layouter: AABubbleLayouter) {
        self.init(fileName: voiceContent.getName(), fileExt: voiceContent.getExt(), fileSize: Int(voiceContent.getSource().getSize()),id: id, date: date, autoDownload: true,duration:jint(voiceContent.getDuration()/1000), layouter: layouter)
    }
    
    
    /**
     Creating layout for message
     */
    public convenience init(message: ACMessage, layouter: AABubbleLayouter) {
        if let content = message.content as? ACVoiceContent {
            self.init(id: Int64(message.rid), voiceContent: content, date: Int64(message.date), layouter: layouter)
        } else {
            fatalError("Unsupported content for media cell")
        }
    }
}

/**
 Layouter for voice bubbles
 */
public class AABubbleVoiceCellLayouter: AABubbleLayouter {
    
    public func isSuitable(message: ACMessage) -> Bool {
        if message.content is ACVoiceContent {
            return true
        }
        
        return false
    }
    
    public func buildLayout(peer: ACPeer, message: ACMessage) -> AACellLayout {
        return VoiceMessageCellLayout(message: message, layouter: self)
    }
    
    public func cellClass() -> AnyClass {
        return AABubbleVoiceCell.self
    }
}
