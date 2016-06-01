package im.actor.sdk.controllers.calls;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import im.actor.core.entity.PeerType;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallState;
import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.android.webrtc.AndroidMediaStream;
import im.actor.runtime.android.webrtc.AndroidPeerConnection;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCPeerConnectionCallback;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.calls.view.CallAvatarLayerAnimator;
import im.actor.sdk.controllers.calls.view.TimerActor;
import im.actor.sdk.controllers.fragment.BaseFragment;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.TintImageView;
import im.actor.sdk.view.adapters.HolderAdapter;
import im.actor.sdk.view.adapters.RecyclerListView;
import im.actor.sdk.view.adapters.ViewHolder;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.view.avatar.CallBackgroundAvatarView;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class CallFragment extends BaseFragment {
    private static final int PERMISSIONS_REQUEST_FOR_CALL = 147;
    private static final int NOTIFICATION_ID = 2;
    private static final int TIMER_ID = 1;
    long callId = -1;
    Peer peer;

    private Vibrator v;
    private View answerContainer;
    private Ringtone ringtone;
    private CallVM call;
    private ActorRef timer;
    private TextView statusTV;
    private NotificationManager manager;
    private CallState currentState;
    private ImageButton endCall;
    private View endCallContainer;
    private boolean speakerOn = false;
    private AudioManager audioManager;
    private AvatarView avatarView;
    private RecyclerListView membersList;
    private CallAvatarLayerAnimator animator;
    private View[] avatarLayers;
    private View layer1;
    private View layer2;
    private View layer3;
    private VideoSource source;
    private EglBase rootEglBase;
    private VideoRenderer localRender;
    private VideoRenderer remoteRender;
    private SurfaceViewRenderer localVideoView;
    private SurfaceViewRenderer remoteVideoView;

    private TextView muteCallTv;
    private TextView videoTv;
    private ViewGroup container;
    private TintImageView speaker;
    private TintImageView muteCall;
    private TextView speakerTV;
    private boolean sourceIsStopped = false;
    private WebRTCPeerConnectionCallback webRTCPeerConnectionCallback;
    float dX, dY;
    private TintImageView videoIcon;
    private boolean remoteRendererBinded = false;

    public CallFragment() {

        manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public CallFragment(long callId) {
        this.callId = callId;
        this.call = messenger().getCall(callId);
        if(call == null){
            this.peer = Peer.user(myUid());
        }else{
            this.peer = call.getPeer();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.container = container;
        manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        FrameLayout cont = (FrameLayout) inflater.inflate(R.layout.fragment_call, container, false);
        v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        CallBackgroundAvatarView backgroundAvatarView = (CallBackgroundAvatarView) cont.findViewById(R.id.background);

//        animator = new CallAvatarLayerAnimator(cont.findViewById(R.id.layer),
//                cont.findViewById(R.id.layer1),
//                cont.findViewById(R.id.layer2),
//                cont.findViewById(R.id.layer3),
//                cont.findViewById(R.id.layer4)
//                );

        layer1 = cont.findViewById(R.id.layer1);
        layer2 = cont.findViewById(R.id.layer2);
        layer3 = cont.findViewById(R.id.layer3);
        avatarLayers = new View[]{
//                cont.findViewById(R.id.layer),
                layer1,
                layer2,
                layer3,
//                cont.findViewById(R.id.layer4)
        };

        //TODO disabled while working on video, enable later!
//        showView(layer1);
//        showView(layer2);
//        showView(layer3);
//        wave(avatarLayers, 1.135f ,1900, -2f);

        for (int i = 0; i<avatarLayers.length; i++){
            View layer = avatarLayers[i];
            ((GradientDrawable)layer.getBackground()).setColor(Color.WHITE);
            ((GradientDrawable)layer.getBackground()).setAlpha(50);
        }

        endCallContainer = cont.findViewById(R.id.end_call_container);
        answerContainer = cont.findViewById(R.id.answer_container);
        ImageButton answer = (ImageButton) cont.findViewById(R.id.answer);
        answer.setOnClickListener(v1 -> onAnswer());
        ImageButton notAnswer = (ImageButton) cont.findViewById(R.id.notAnswer);
        endCall = (ImageButton) cont.findViewById(R.id.end_call);
        notAnswer.setOnClickListener(v1 -> doEndCall());
        endCall.setOnClickListener(v1 -> doEndCall());

        //
        //Avatar/Name bind
        //
        avatarView = (AvatarView) cont.findViewById(R.id.avatar);
        avatarView.init(Screen.dp(130), 50);

        TextView nameTV = (TextView) cont.findViewById(R.id.name);
        nameTV.setTextColor(ActorSDK.sharedActor().style.getProfileTitleColor());
        if(peer.getPeerType() == PeerType.PRIVATE){
            UserVM user = users().get(peer.getPeerId());
            avatarView.bind(user);
            backgroundAvatarView.bind(user);
            bind(nameTV, user.getName());
        }else if(peer.getPeerType() == PeerType.GROUP){
            GroupVM g = groups().get(peer.getPeerId());
            avatarView.bind(g);
            backgroundAvatarView.bind(g);
            bind(nameTV, g.getName());
        }

        nameTV.setSelected(true);

        //
        // Members list
        //
        membersList = (RecyclerListView) cont.findViewById(R.id.members_list);
        if(call!=null){
            CallMembersAdapter membersAdapter = new CallMembersAdapter(getActivity(), call.getMembers());
            membersList.setAdapter(membersAdapter);
        }

        //
        // Members list/ avatar switch
        //
        View.OnClickListener listener = v1 -> switchAvatarMembers();
        avatarView.setOnClickListener(listener);
//        cont.findViewById(R.id.background).setOnClickListener(listener);

        membersList.setOnItemClickListener((parent, view, position, id) -> switchAvatarMembers());



        statusTV = (TextView) cont.findViewById(R.id.status);
//        statusTV.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());


        // Check permission
        //
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Permissions", "call - no permission :c");
            CallFragment.this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.VIBRATE, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.WAKE_LOCK},
                    PERMISSIONS_REQUEST_FOR_CALL);

        }

        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);




        audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        speaker = (TintImageView) cont.findViewById(R.id.speaker);
        speaker.setResource(R.drawable.ic_volume_up_white_24dp);
        speakerTV = (TextView) cont.findViewById(R.id.speaker_tv);
        cont.findViewById(R.id.speaker_btn).setOnClickListener(v1 -> toggleSpeaker(speaker, speakerTV));
        checkSpeaker(speaker, speakerTV);

        muteCall = (TintImageView) cont.findViewById(R.id.mute);
        muteCallTv = (TextView) cont.findViewById(R.id.mute_tv);
        muteCall.setResource(R.drawable.ic_mic_off_white_24dp);
        cont.findViewById(R.id.mute_btn).setOnClickListener(v1 -> messenger().toggleCallMute(callId));

        videoIcon = (TintImageView) cont.findViewById(R.id.video);
        videoIcon.setResource(R.drawable.ic_videocam_white_24dp);
        videoTv = (TextView) cont.findViewById(R.id.video_tv);
        videoTv.setTextColor(getResources().getColor(R.color.picker_grey));
        videoIcon.setTint(getResources().getColor(R.color.picker_grey));
        cont.findViewById(R.id.video_btn).setOnClickListener(v1 -> messenger().toggleVideoEnabled(callId));
        final TintImageView back = (TintImageView) cont.findViewById(R.id.back);
        back.setResource(R.drawable.ic_message_white_24dp);
        cont.findViewById(R.id.back_btn).setOnClickListener(v1 -> getActivity().startActivity(Intents.openDialog(peer, false, getActivity())));

        final TintImageView add = (TintImageView) cont.findViewById(R.id.add);
        add.setResource(R.drawable.ic_person_add_white_24dp);
        TextView addTv = (TextView) cont.findViewById(R.id.add_user_tv);
        addTv.setTextColor(getResources().getColor(R.color.picker_grey));
        add.setTint(getResources().getColor(R.color.picker_grey));

        if (peer.getPeerType() == PeerType.PRIVATE && ActorSDK.sharedActor().isVideoCallsEnabled()) {
            rootEglBase = EglBase.create();

            remoteVideoView = (SurfaceViewRenderer) cont.findViewById(R.id.remote_renderer);
            remoteVideoView.init(rootEglBase.getEglBaseContext(), null);

            localVideoView = new SurfaceViewRenderer(getActivity());
            localVideoView.setVisibility(View.INVISIBLE);
            localVideoView.setZOrderMediaOverlay(true);
            localVideoView.init(rootEglBase.getEglBaseContext(), null);

            localVideoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:
                            dX = localVideoView.getX() - event.getRawX();
                            dY = localVideoView.getY() - event.getRawY();
                            break;

                        case MotionEvent.ACTION_MOVE:
                            localVideoView.animate()
                                    .x(event.getRawX() + dX)
                                    .y(event.getRawY() + dY)
                                    .setDuration(0)
                                    .start();

                        default:
                            return false;

                    }
                    return true;
                }
            });

            int margin = Screen.dp(20);
            int localVideoWidth = Screen.getWidth() / 3 - margin;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(localVideoWidth, Math.round(localVideoWidth / 1.5f), Gravity.TOP | Gravity.LEFT);

            int statusBarHeight = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }

            params.setMargins(margin, margin + statusBarHeight, 0, 0);
            cont.addView(localVideoView, params);


            webRTCPeerConnectionCallback = new WebRTCPeerConnectionCallback() {
                @Override
                public void onCandidate(int label, String id, String candidate) {

                }

                @Override
                public void onStreamAdded(WebRTCMediaStream remoteStream) {
                    AndroidMediaStream stream = (AndroidMediaStream) remoteStream;
                    if (stream.getVideoTrack() != null) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!remoteRendererBinded) {
                                    try {
                                        remoteVideoView.init(rootEglBase.getEglBaseContext(), null);
                                    } catch (IllegalStateException e) {
                                        //Already inited, it's ok here
                                    }

                                    remoteRender = new VideoRenderer(remoteVideoView);
                                    stream.getVideoTrack().addRenderer(remoteRender);
                                    remoteVideoView.setVisibility(View.VISIBLE);
                                    avatarView.setVisibility(View.INVISIBLE);
                                    nameTV.setVisibility(View.INVISIBLE);
                                }

                            }
                        });
                    }
                }

                @Override
                public void onStreamRemoved(WebRTCMediaStream stream) {

                }

                @Override
                public void onOwnStreamAdded(WebRTCMediaStream ownStream) {
                    AndroidMediaStream stream = (AndroidMediaStream) ownStream;
                    if (stream.getVideoTrack() != null) {


                        getActivity().runOnUiThread(() -> {
                            try {
                                localVideoView.init(rootEglBase.getEglBaseContext(), null);
                            } catch (IllegalStateException e) {
                                //Already inited, it's ok here
                            }
                            source = stream.getVideoSource();
                            localRender = new VideoRenderer(localVideoView);
                            stream.getVideoTrack().addRenderer(localRender);
                            localVideoView.setVisibility(View.VISIBLE);

                        });
                    }
                }

                @Override
                public void onOwnStreamRemoved(WebRTCMediaStream stream) {

                }

                @Override
                public void onRenegotiationNeeded() {

                }

                @Override
                public void onDisposed() {

                }
            };
        } else {
            if (call != null) {
                if (call.getIsVideoEnabled().get()) {
                    messenger().toggleVideoEnabled(callId);
                }
            }
        }


        return cont;
    }

    public void toggleSpeaker(TintImageView speaker, TextView speakerTV) {
        toggleSpeaker(speaker, speakerTV, !speakerOn);
    }

    public void toggleSpeaker(TintImageView speaker, TextView speakerTV, boolean speakerOn) {
        this.speakerOn = speakerOn;
        audioManager.setSpeakerphoneOn(speakerOn);
        checkSpeaker(speaker, speakerTV);
    }

    public void checkSpeaker(TintImageView speaker, TextView speakerTV) {
        if (speakerOn) {
            speaker.setTint(Color.WHITE);
            speakerTV.setTextColor(Color.WHITE);
        } else {
            speaker.setTint(getResources().getColor(R.color.picker_grey));
            speakerTV.setTextColor(getResources().getColor(R.color.picker_grey));
        }
    }

    public void switchAvatarMembers() {
        if(peer.getPeerType() == PeerType.GROUP){
            if(avatarView.getVisibility() == View.VISIBLE){
                hideView(avatarView);
                showView(membersList);
            }else{
                hideView(membersList);
                showView(avatarView);
            }
        }
    }

    private void startTimer() {

        final DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        if(timer == null){
            timer = ActorSystem.system().actorOf(Props.create(() -> new TimerActor(300)), "calls/timer");

            timer.send(new TimerActor.Register((currentTime, timeFromRegister) -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (currentState == CallState.IN_PROGRESS) {
                            statusTV.setText(formatter.format(new Date(timeFromRegister)));
                        }
                    });
                }
            }, TIMER_ID));

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_FOR_CALL) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission  granted
            } else {
                messenger().endCall(callId);
            }
        }
    }

    private void initIncoming() {

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        answerContainer.setVisibility(View.VISIBLE);
        endCallContainer.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                Thread.sleep(1100);
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                ringtone = RingtoneManager.getRingtone(getActivity(), notification);
                if (getActivity() != null & answerContainer.getVisibility() == View.VISIBLE && currentState == CallState.RINGING) {
                    if (ringtone != null) {
                        ringtone.play();
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private void onAnswer() {

        endCallContainer.setVisibility(View.VISIBLE);
        answerContainer.setVisibility(View.GONE);
        if (ringtone != null) {
            ringtone.stop();
        }

        messenger().answerCall(callId);
    }

    private void doEndCall() {
        messenger().endCall(callId);
        onCallEnd();
    }

    //
    // Vibrate/tone/wakelock
    //
    boolean vibrate = true;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private int field = 0x00000020;


    public void enableWakeLock() {
        powerManager = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(field, getActivity().getLocalClassName());

        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }


    public void onConnected() {
        vibrate = false;
        v.cancel();
        v.vibrate(200);

    }

    public void onCallEnd() {
        audioManager.setSpeakerphoneOn(false);
        vibrate = false;
        if (ringtone != null) {
            ringtone.stop();
        }
        if (v != null) {
            v.cancel();
        }

        if(timer!=null){
            timer.send(PoisonPill.INSTANCE);
        }

        manager.cancel(NOTIFICATION_ID);
        if(getActivity()!=null){
            getActivity().finish();
        }

    }

    public void disableWakeLock() {
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.members){
            switchAvatarMembers();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPause() {
        super.onPause();

        if (peer.getPeerType() == PeerType.PRIVATE && ActorSDK.sharedActor().isVideoCallsEnabled()) {
            if (source != null) {
                source.stop();
                sourceIsStopped = true;
            }


//            if (localVideoView != null) {
//                localVideoView.release();
//            }
//
//            if (remoteVideoView != null) {
//                remoteVideoView.release();
//            }


            if (call != null) {
                ArrayList<WebRTCPeerConnection> webRTCPeerConnections = call.getPeerConnection().get();

                for (WebRTCPeerConnection webRTCPeerConnection : webRTCPeerConnections) {

                    webRTCPeerConnection.removeCallback(webRTCPeerConnectionCallback);

                    HashMap<MediaStream, AndroidMediaStream> mediaStreams = ((AndroidPeerConnection) webRTCPeerConnection).getStreams();
                    for (MediaStream mediaStream : mediaStreams.keySet()) {
                        mediaStreams.get(mediaStream).removeRenderer(remoteRender);
                    }
                    remoteRendererBinded = false;

                    AndroidMediaStream stream = ((AndroidPeerConnection) webRTCPeerConnection).getLocalStream();
                    if (stream != null) {
                        stream.removeRenderer(localRender);
                    }
                }

            }

        }

        if(call!=null && call.getState().get()!=CallState.ENDED){
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_app_notify);
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
            builder.setContentTitle(getActivity().getString(R.string.call_notification));

            Intent intent = new Intent(getActivity(), CallActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("callId", callId);

            builder.setContentIntent(PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            Notification n = builder.build();

            n.flags += Notification.FLAG_ONGOING_EVENT;


            manager.notify(NOTIFICATION_ID, n);
        }


        disableWakeLock();

    }

    @Override
    public void onResume() {
        super.onResume();
        enableWakeLock();

        if (source != null) {
            source.restart();
            sourceIsStopped = false;
        }

        manager.cancel(NOTIFICATION_ID);
//        animator.popAnimation(true);
        if (call != null) {

            if (peer.getPeerType() == PeerType.PRIVATE && ActorSDK.sharedActor().isVideoCallsEnabled()) {
                bind(call.getPeerConnection(), (val, valueModel) -> {
                    for (WebRTCPeerConnection webRTCPeerConnection : val) {
                        webRTCPeerConnection.addCallback(webRTCPeerConnectionCallback);
                    }
                });

                bind(call.getIsVideoEnabled(), (val, valueModel) -> {
                    if (getActivity() != null) {
                        if (val) {
                            videoTv.setTextColor(Color.WHITE);
                            videoIcon.setTint(Color.WHITE);
                            if (localRender != null) {
                                localVideoView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            videoTv.setTextColor(getResources().getColor(R.color.picker_grey));
                            videoIcon.setTint(getResources().getColor(R.color.picker_grey));
                            localVideoView.setVisibility(View.INVISIBLE);
                        }
                    }
                });

            }

            bind(call.getIsMuted(), (val, valueModel) -> {
                if (getActivity() != null) {
                    if (val) {

                        muteCallTv.setTextColor(getResources().getColor(R.color.picker_grey));
                        muteCall.setTint(getResources().getColor(R.color.picker_grey));
                    } else {
                        muteCallTv.setTextColor(Color.WHITE);
                        muteCall.setTint(Color.WHITE);
                    }
                }
            });


            bind(call.getState(), (val, valueModel) -> {
                if (currentState != val) {
                    currentState = val;
                    switch (val) {

                        case RINGING:
                            if (call.isOutgoing()) {
                                statusTV.setText(R.string.call_outgoing);
                            } else {
                                statusTV.setText(R.string.call_incoming);
                                toggleSpeaker(speaker, speakerTV, true);
                                initIncoming();
                            }
                            break;

                        case CONNECTING:
                            statusTV.setText(R.string.call_connecting);
                            break;

                        case IN_PROGRESS:
                            toggleSpeaker(speaker, speakerTV, false);
                            onConnected();
                            startTimer();
                            break;

                        case ENDED:
                            statusTV.setText(R.string.call_ended);
                            onCallEnd();
                            break;

                    }
                }
            });

        }
    }

    class CallMembersAdapter extends HolderAdapter<CallMember>{


        private ArrayList<CallMember> members;

        protected CallMembersAdapter(Context context, final ValueModel<ArrayList<CallMember>> members) {
            super(context);
            this.members = members.get();
            members.subscribe((val, valueModel) -> {
                CallMembersAdapter.this.members = val;
                notifyDataSetChanged();
                Log.d("STATUS CHANGED", val.toString());
            });
        }

        @Override
        public int getCount() {
            return members.size();
        }

        @Override
        public CallMember getItem(int position) {
            return members.get(position);
        }

        @Override
        public long getItemId(int position) {
            return members.get(position).getUid();
        }

        @Override
        protected ViewHolder<CallMember> createHolder(CallMember obj) {
            return new MemberHolder();
        }

        private class MemberHolder extends ViewHolder<CallMember>{

            CallMember data;
            private TextView userName;
            private TextView status;
            private AvatarView avatarView;

            @Override
            public View init(final CallMember data, ViewGroup viewGroup, Context context) {
                View res = ((Activity) context).getLayoutInflater().inflate(R.layout.fragment_call_member_item, viewGroup, false);

                userName = (TextView) res.findViewById(R.id.name);
                userName.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
                status = (TextView) res.findViewById(R.id.status);
                status.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
                avatarView = (AvatarView) res.findViewById(R.id.avatar);
                avatarView.init(Screen.dp(35), 18);
                this.data = data;

                return res;
            }

            @Override
            public void bind(CallMember data, int position, Context context) {
                UserVM user = users().get(data.getUid());
                this.data = data;
                avatarView.bind(user);
                userName.setText(user.getName().get());
                status.setText(data.getState().name());
            }


            @Override
            public void unbind(boolean full) {
                if (full) {
                    avatarView.unbind();
                }
            }
        }
    }
}
