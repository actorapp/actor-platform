//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

class AAVoiceRecorderView: UIView {
    
    //////////////////////////////////

    var timeLabel:UILabel!
    
    var sliderLabel: UILabel!
    var sliderArrow: UIImageView!
    
    var recorderImageCircle:UIImageView!
    
    //////////////////////////////////
    
    var trackTouchPoint: CGPoint!
    var firstTouchPoint: CGPoint!
    
    //////////////////////////////////
    
    weak var binedController : ConversationViewController!
    var meterTimer:NSTimer!
    
    //////////////////////////////////
    
    var appStyle: ActorStyle { get { return ActorSDK.sharedActor().style } }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.createSubviews()
        
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func createSubviews() {
        //////////////////////////////////
        //          init
        //////////////////////////////////
        
        self.timeLabel = UILabel()
        
        self.sliderLabel = UILabel()
        self.sliderArrow = UIImageView()
        
        self.recorderImageCircle = UIImageView()
        
        //////////////////////////////////
        //      add as subviews
        //////////////////////////////////
        
        self.addSubview(self.timeLabel)
        self.addSubview(self.sliderLabel)
        self.addSubview(self.sliderArrow)
        self.addSubview(self.recorderImageCircle)
        
        //////////////////////////////////
        //        customize
        //////////////////////////////////
        
        self.backgroundColor = appStyle.chatInputFieldBgColor
        
        self.timeLabel.text = "0:00"
        self.timeLabel.font = UIFont.systemFontOfSize(15)
        self.timeLabel.textColor = appStyle.vcHintColor
        self.timeLabel.frame = CGRectMake(29, 12, 50, 20)
        
        self.sliderLabel.text = "Slide to cancel"
        self.sliderLabel.font = UIFont.systemFontOfSize(14)
        self.sliderLabel.textAlignment = .Left
        self.sliderLabel.frame = CGRectMake(140,12,100,20)
        self.sliderLabel.textColor = appStyle.vcHintColor
        
        self.sliderArrow.image = UIImage.tinted("aa_recorderarrow", color: appStyle.vcHintColor)
        self.sliderArrow.frame = CGRectMake(110,12,20,20)
        
        self.recorderImageCircle.image = UIImage.tinted("aa_recordercircle", color: UIColor(red: 0.7287, green: 0.7252, blue: 0.7322, alpha: 1.0))
        self.recorderImageCircle.frame = CGRectMake(10, 15, 14, 14)
        
        //
        
    }
    
    // update location views from track touch position
    func updateLocation(offset:CGFloat,slideToRight:Bool) {
        
        var sliderLabelFrame = self.sliderLabel.frame
        sliderLabelFrame.origin.x += offset
        self.sliderLabel.frame = sliderLabelFrame
        
        var sliderArrowImageFrame = self.sliderArrow.frame
        sliderArrowImageFrame.origin.x += offset
        self.sliderArrow.frame = sliderArrowImageFrame
        
        if (slideToRight == true) {
            
            if (self.timeLabel.frame.minX < 29) {
                
                var timeLabelFrame = self.timeLabel.frame
                timeLabelFrame.origin.x += offset
                self.timeLabel.frame = timeLabelFrame
                
                var circleFrame = self.recorderImageCircle.frame
                circleFrame.origin.x += offset
                self.recorderImageCircle.frame = circleFrame
                
            }
            
        } else {
            
            if (self.timeLabel.frame.maxX-5 > sliderArrowImageFrame.minX) {
                
                var timeLabelFrame = self.timeLabel.frame
                timeLabelFrame.origin.x += offset
                self.timeLabel.frame = timeLabelFrame
                
                var circleFrame = self.recorderImageCircle.frame
                circleFrame.origin.x += offset
                self.recorderImageCircle.frame = circleFrame
                
            }
            
        }
    
    }
    
    func startAnimation() {
        
        self.timeLabel.frame = CGRectMake(-129, 12, 50, 20)
        self.sliderLabel.frame = CGRectMake(440,12,100,20)
        self.sliderArrow.frame = CGRectMake(310,12,20,20)
        self.recorderImageCircle.frame = CGRectMake(-110, 15, 14, 14)
        
        UIView.animateWithDuration(1.5, delay: 0.0, usingSpringWithDamping: 0.5, initialSpringVelocity: 1.0, options: UIViewAnimationOptions.CurveLinear, animations: { () -> Void in
            
            self.timeLabel.frame = CGRectMake(29, 12, 50, 20)
            self.sliderLabel.frame = CGRectMake(140,12,100,20)
            self.sliderArrow.frame = CGRectMake(110,12,20,20)
            self.recorderImageCircle.frame = CGRectMake(10, 15, 14, 14)
            
            }, completion: { (complite) -> Void in
                
                // animation complite
                
        })
        
    }
    
    func recordingStarted() {
        
        UIView.animateWithDuration(0.3, animations: { () -> Void in
            
                self.recorderImageCircle.alpha = 0
            
            }) { (comp) -> Void in
                
                self.recorderImageCircle.image = UIImage.bundled("aa_recordercircle")
                
                UIView.animateWithDuration(0.3, animations: { () -> Void in
                    self.recorderImageCircle.alpha = 1
                })
                
                self.addAnimationsOnRecorderCircle()
                self.startUpdateTimer()
                
        }
        
    }
    
    func recordingStoped() {
        meterTimer?.invalidate()
        self.timeLabel.text = "0:00"
        self.recorderImageCircle.layer.removeAllAnimations()
        self.recorderImageCircle.image = UIImage.tinted("aa_recordercircle", color: UIColor(red: 0.7287, green: 0.7252, blue: 0.7322, alpha: 1.0))
        
        self.timeLabel.frame = CGRectMake(29, 12, 50, 20)
        self.sliderLabel.frame = CGRectMake(140,12,100,20)
        self.sliderArrow.frame = CGRectMake(110,12,20,20)
        self.recorderImageCircle.frame = CGRectMake(10, 15, 14, 14)
    }
    
    
    func startUpdateTimer() {
        self.meterTimer = NSTimer.scheduledTimerWithTimeInterval(0.1,
            target:self,
            selector:#selector(AAVoiceRecorderView.updateAudioMeter(_:)),
            userInfo:nil,
            repeats:true)
    }
    
    func updateAudioMeter(timer:NSTimer) {
        
        if let recorder = self.binedController?.audioRecorder {
            
            let dur = recorder.currentDuration()
            
            let minutes = Int(dur / 60)
            let seconds = Int(dur % 60)
            
            if seconds < 10 {
                self.timeLabel.text = "\(minutes):0\(seconds)"
            } else {
                self.timeLabel.text = "\(minutes):\(seconds)"
            }
            
        }
    }
    
    
    func addAnimationsOnRecorderCircle() {
        
        let circleAnimation = CABasicAnimation(keyPath: "opacity")
        circleAnimation.repeatCount = 100000000
        circleAnimation.duration = 1.0
        circleAnimation.autoreverses = true
        circleAnimation.fromValue = 1.0
        circleAnimation.toValue = 0.1
        self.recorderImageCircle.layer.addAnimation(circleAnimation, forKey: nil)
        
    }
    
    func closeRecording() {
        
    }
    

}
