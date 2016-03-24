//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import AVFoundation

class AARecordAudioController: UIViewController,UIViewControllerTransitioningDelegate {
    
    ////////////////////////////////
    
    var buttonClose : UIButton!
    var recorderView : UIView!
    var timerLabel : UILabel!
    var chatController : ConversationViewController!
    
    var startRecButton  : UIButton!
    var stopRecButton   : UIButton!
    var playRecButton   : UIButton!
    var sendRecord      : UIButton!
    var cleanRecord     : UIButton!
    
    //
    
    var filePath : String!
    var fileDuration : NSTimeInterval!
    
    var recorded : Bool! = false
    
    private let audioRecorder: AAAudioRecorder! = AAAudioRecorder()
    private var audioPlayer: AAModernConversationAudioPlayer!
    
    var meterTimer:NSTimer!
    var soundFileURL:NSURL?
    
    ////////////////////////////////
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)!
        
        self.commonInit()
        
    }
    
    override init(nibName nibNameOrNil: String!, bundle nibBundleOrNil: NSBundle!)  {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        
        self.commonInit()
        
    }
    
    func commonInit() {
        self.modalPresentationStyle = .Custom
        self.transitioningDelegate = self
    }
    
    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
    
    
    // ---- UIViewControllerTransitioningDelegate methods
    
    func presentationControllerForPresentedViewController(presented: UIViewController, presentingViewController presenting: UIViewController, sourceViewController source: UIViewController) -> UIPresentationController? {
        
        if presented == self {
            return AACustomPresentationController(presentedViewController: presented, presentingViewController: presenting)
        }
        
        return nil
    }
    
    func animationControllerForPresentedController(presented: UIViewController, presentingController presenting: UIViewController, sourceController source: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        
        if presented == self {
            return AACustomPresentationAnimationController(isPresenting: true)
        }
        else {
            return nil
        }
    }
    
    func animationControllerForDismissedController(dismissed: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        
        if dismissed == self {
            return AACustomPresentationAnimationController(isPresenting: false)
        }
        else {
            return nil
        }
    }
    
    override func loadView() {
        super.loadView()
        
        self.recorderView = UIView()
        self.recorderView.frame = CGRectMake(self.view.frame.width/2 - 120, self.view.frame.height/2 - 80, 240, 160)
        self.recorderView.backgroundColor = UIColor.whiteColor()
        self.recorderView.layer.cornerRadius = 10
        self.recorderView.layer.masksToBounds = true
        self.view.addSubview(self.recorderView)
        
        self.buttonClose = UIButton(type: UIButtonType.System)
        self.buttonClose.addTarget(self, action: #selector(AARecordAudioController.closeController), forControlEvents: UIControlEvents.TouchUpInside)
        self.buttonClose.tintColor = UIColor.whiteColor()
        self.buttonClose.setImage(UIImage.bundled("aa_closerecordbutton"), forState: UIControlState.Normal)
        self.buttonClose.frame = CGRectMake(205, 5, 25, 25)
        self.recorderView.addSubview(self.buttonClose)
        
        let separatorView = UIView()
        separatorView.frame = CGRectMake(0, 80, 240, 0.5)
        separatorView.backgroundColor = UIColor.grayColor()
        self.recorderView.addSubview(separatorView)
        
        self.timerLabel = UILabel()
        self.timerLabel.text = "00:00"
        self.timerLabel.font = UIFont(name: "HelveticaNeue-Medium", size: 17)!
        self.timerLabel.textColor = ActorSDK.sharedActor().style.vcHintColor
        self.timerLabel.frame = CGRectMake(70, 5, 100, 40)
        self.timerLabel.textAlignment = .Center
        self.timerLabel.backgroundColor = UIColor.clearColor()
        self.recorderView.addSubview(self.timerLabel)
        
        self.startRecButton = UIButton(type: UIButtonType.System)
        self.startRecButton.tintColor = UIColor.redColor()
        self.startRecButton.setImage(UIImage.bundled("aa_startrecordbutton"), forState: UIControlState.Normal)
        self.startRecButton.addTarget(self, action: #selector(AARecordAudioController.startRec), forControlEvents: UIControlEvents.TouchUpInside)
        self.startRecButton.frame = CGRectMake(100, 110, 40, 40)
        
        self.recorderView.addSubview(self.startRecButton)
        
        self.stopRecButton = UIButton(type: UIButtonType.System)
        self.stopRecButton.tintColor = UIColor.redColor()
        self.stopRecButton.setImage(UIImage.bundled("aa_pauserecordbutton"), forState: UIControlState.Normal)
        self.stopRecButton.addTarget(self, action: #selector(AARecordAudioController.stopRec), forControlEvents: UIControlEvents.TouchUpInside)
        self.stopRecButton.frame = CGRectMake(100, 110, 40, 40)
        
        self.recorderView.addSubview(self.stopRecButton)
        
        self.stopRecButton.hidden = true
        
        self.playRecButton = UIButton(type: UIButtonType.System)
        self.playRecButton.tintColor = UIColor.greenColor()
        self.playRecButton.setImage(UIImage.bundled("aa_playrecordbutton"), forState: UIControlState.Normal)
        self.playRecButton.addTarget(self, action: #selector(AARecordAudioController.play), forControlEvents: UIControlEvents.TouchUpInside)
        self.playRecButton.frame = CGRectMake(100, 110, 40, 40)
        
        self.recorderView.addSubview(self.playRecButton)
        
        self.playRecButton.hidden = true
        
        self.sendRecord = UIButton(type: UIButtonType.System)
        self.sendRecord.tintColor = UIColor.greenColor()
        self.sendRecord.setImage(UIImage.bundled("aa_sendrecord"), forState: UIControlState.Normal)
        self.sendRecord.addTarget(self, action: #selector(AARecordAudioController.sendRecordMessage), forControlEvents: UIControlEvents.TouchUpInside)
        self.sendRecord.frame = CGRectMake(190, 115, 40, 40)
        self.sendRecord.enabled = false
        
        self.recorderView.addSubview(self.sendRecord)
        
        
        self.cleanRecord = UIButton(type: UIButtonType.System)
        self.cleanRecord.tintColor = UIColor.redColor()
        self.cleanRecord.setImage(UIImage.bundled("aa_deleterecord"), forState: UIControlState.Normal)
        self.cleanRecord.addTarget(self, action: #selector(AARecordAudioController.sendRecordMessage), forControlEvents: UIControlEvents.TouchUpInside)
        self.cleanRecord.frame = CGRectMake(10, 120, 30, 30)
        self.cleanRecord.enabled = false
        
        self.recorderView.addSubview(self.cleanRecord)
        
        //cx_deleterecord

    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
        
    }
    
    // actions
    
    func closeController() {
        
        self.audioRecorder.cancel()
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    
    
    func startRec() {
        
        //log.debug("recording. recorder nil")
        
        playRecButton.hidden    = true
        stopRecButton.hidden    = false
        startRecButton.hidden   = true
        
    
        startTimer()
        recordWithPermission()
        
    }
    
    func stopRec() {
        
        //log.debug("stop")
        
        meterTimer.invalidate()
        
        playRecButton.hidden    = false
        startRecButton.hidden   = true
        stopRecButton.hidden    = true
        
        audioRecorder.finish({ (path: String!, duration: NSTimeInterval) -> Void in
            if (nil == path) {
                print("onAudioRecordingFinished: empty path")
                return
            }
            
            self.filePath = path
            self.fileDuration = duration
            
            dispatch_async(dispatch_get_main_queue(), { () -> Void in
                self.sendRecord.enabled = true
                self.cleanRecord.enabled = true
            })
            
        })
        
    }
    
    func stopAudioRecording()
    {
        if (audioRecorder != nil)
        {
            audioRecorder.delegate = nil
            audioRecorder.cancel()
        }
    }
    
    func play() {
        
        self.audioPlayer = AAModernConversationAudioPlayer(filePath:self.filePath)
        self.audioPlayer.play(0)
        
        playRecButton.hidden    = true
        startRecButton.hidden   = true
        stopRecButton.hidden    = false
        
    }
    
    // setup record    
    func recordWithPermission() {
        let session:AVAudioSession = AVAudioSession.sharedInstance()
        // ios 8 and later
        if (session.respondsToSelector(#selector(AVAudioSession.requestRecordPermission(_:)))) {
            AVAudioSession.sharedInstance().requestRecordPermission({(granted: Bool)-> Void in
                if granted {
                    print("Permission to record granted")
                    self.setSessionPlayAndRecord()
                    self.meterTimer = NSTimer.scheduledTimerWithTimeInterval(0.1,
                        target:self,
                        selector:#selector(AARecordAudioController.updateAudioMeter(_:)),
                        userInfo:nil,
                        repeats:true)
                } else {
                    print("Permission to record not granted")
                }
            })
        } else {
            print("requestRecordPermission unrecognized")
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        
    }
    
    func startTimer() {
        self.meterTimer = NSTimer.scheduledTimerWithTimeInterval(0.1,
            target:self,
            selector:#selector(AARecordAudioController.updateAudioMeter(_:)),
            userInfo:nil,
            repeats:true)
    }
    
    func updateAudioMeter(timer:NSTimer) {
        
        if (self.audioRecorder != nil) {
            
            let dur = self.audioRecorder.currentDuration()
            
            let min = Int(dur / 60)
            let sec = Int(dur % 60)
            let s = String(format: "%02d:%02d", min, sec)
            
            self.timerLabel.text = s
            
        }
    }
    
    func setSessionPlayback() {
        
        let session:AVAudioSession = AVAudioSession.sharedInstance()
        
        do {
            try! session.setCategory(AVAudioSessionCategoryPlayback)
        }
        
        
        do {
            try! session.setActive(true)
        }
        
    }
    
    
    func setSessionPlayAndRecord() {
        
        let session:AVAudioSession = AVAudioSession.sharedInstance()
        
        do {
            try! session.setCategory(AVAudioSessionCategoryPlayAndRecord)
        }
        
        do {
            try! session.setActive(true)
        }
        
        self.audioRecorder.start()
        
    }
    
    func sendRecordMessage() {
        
        self.dismissViewControllerAnimated(true, completion: nil)
        //self.chatController.sendVoiceMessage(self.filePath, duration: self.fileDuration)
        
    }
    
}
