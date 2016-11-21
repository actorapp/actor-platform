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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import im.actor.core.entity.PeerType;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallState;
import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.android.webrtc.AndroidVideoTrack;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.calls.view.TimerActor;
import im.actor.sdk.controllers.ActorBinder;
import im.actor.sdk.controllers.BaseFragment;
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

    protected static final int PERMISSIONS_REQUEST_FOR_CALL = 147;
    protected static final int NOTIFICATION_ID = 2;
    protected static final int TIMER_ID = 1;

    protected final ActorBinder ACTIVITY_BINDER = new ActorBinder();

    protected long callId = -1;
    protected Peer peer;

    protected Vibrator v;
    protected View answerContainer;
    protected Ringtone ringtone;
    protected CallVM call;

    protected AvatarView avatarView;
    protected TextView nameTV;
    protected ActorRef timer;
    protected TextView statusTV;
    protected View[] avatarLayers;
    protected View layer1;
    protected View layer2;
    protected View layer3;

    protected NotificationManager manager;
    protected CallState currentState;
    protected ImageButton endCall;
    protected View endCallContainer;
    protected boolean speakerOn = false;
    protected AudioManager audioManager;

    protected RecyclerListView membersList;

    protected float dX, dY;

    protected TintImageView muteCall;
    protected TextView muteCallTv;
    protected TintImageView speaker;
    protected TextView speakerTV;
    protected TintImageView videoIcon;
    protected TextView videoTv;

    //
    // Video References
    //
    protected EglBase eglContext;

    protected SurfaceViewRenderer localVideoView;
    protected VideoRenderer localRender;
    protected boolean isLocalViewConfigured;
    protected VideoTrack localTrack;

    protected SurfaceViewRenderer remoteVideoView;
    protected VideoRenderer remoteRender;
    protected boolean isRemoteViewConfigured;
    protected VideoTrack remoteTrack;


    //
    // Vibrate/tone/wakelock
    //
    boolean vibrate = true;
    protected PowerManager powerManager;
    protected PowerManager.WakeLock wakeLock;
    protected int field = 0x00000020;


    //
    // Constructor
    //

    static CallFragment create(long callId) {
        CallFragment res = new CallFragment();
        Bundle args = new Bundle();
        args.putLong("call_id", callId);
        res.setArguments(args);
        return res;
    }

    public CallFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.callId = getArguments().getLong("call_id");
        this.call = messenger().getCall(callId);
        if (call == null) {
            this.peer = Peer.user(myUid());
        } else {
            this.peer = call.getPeer();
        }

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

        for (int i = 0; i < avatarLayers.length; i++) {
            View layer = avatarLayers[i];
            ((GradientDrawable) layer.getBackground()).setColor(Color.WHITE);
            ((GradientDrawable) layer.getBackground()).setAlpha(50);
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

        nameTV = (TextView) cont.findViewById(R.id.name);
        nameTV.setTextColor(ActorSDK.sharedActor().style.getProfileTitleColor());
        if (peer.getPeerType() == PeerType.PRIVATE) {
            UserVM user = users().get(peer.getPeerId());
            avatarView.bind(user);
            backgroundAvatarView.bind(user);
            bind(nameTV, user.getName());
        } else if (peer.getPeerType() == PeerType.GROUP) {
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
        if (call != null) {
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
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Permissions", "call - no permission :c");
            CallFragment.this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.VIBRATE, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.WAKE_LOCK},
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

        if (peer.getPeerType() == PeerType.PRIVATE) {

            eglContext = EglBase.create();

            remoteVideoView = (SurfaceViewRenderer) cont.findViewById(R.id.remote_renderer);

            localVideoView = new SurfaceViewRenderer(getActivity()) {
                private boolean aspectFixed = false;

                @Override
                public void renderFrame(VideoRenderer.I420Frame frame) {
                    if (!aspectFixed) {
                        aspectFixed = true;
                        int maxWH = Screen.getWidth() / 3 - Screen.dp(20);
                        float scale = Math.min(maxWH / (float) frame.width, maxWH / (float) frame.height);

                        int destW = (int) (scale * frame.width);
                        int destH = (int) (scale * frame.height);

                        boolean turned = frame.rotationDegree % 90 % 2 == 0;

                        localVideoView.post(new Runnable() {
                            @Override
                            public void run() {
                                localVideoView.getLayoutParams().height = turned ? destW : destH;
                                localVideoView.getLayoutParams().width = turned ? destH : destW;
                            }
                        });
                    }
                    super.renderFrame(frame);
                }
            };
            localVideoView.setVisibility(View.INVISIBLE);
            localVideoView.setZOrderMediaOverlay(true);

            localVideoView.setOnTouchListener((v1, event) -> {

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
        if (peer.getPeerType() == PeerType.GROUP) {
            if (avatarView.getVisibility() == View.VISIBLE) {
                hideView(avatarView);
                showView(membersList);
            } else {
                hideView(membersList);
                showView(avatarView);
            }
        }
    }

    protected void startTimer() {

        final DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (timer == null) {
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

    protected void initIncoming() {

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

    protected void onAnswer() {

        endCallContainer.setVisibility(View.VISIBLE);
        answerContainer.setVisibility(View.GONE);
        if (ringtone != null) {
            ringtone.stop();
        }

        messenger().answerCall(callId);
    }

    protected void doEndCall() {
        messenger().endCall(callId);
        onCallEnd();
    }


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

        if (timer != null) {
            timer.send(PoisonPill.INSTANCE);
        }

        manager.cancel(NOTIFICATION_ID);
        if (getActivity() != null) {
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
    public void onResume() {
        super.onResume();

        enableWakeLock();


        //
        // Bind State
        //
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


        //
        // Is Muted
        //
        bind(call.getIsAudioEnabled(), (val, valueModel) -> {
            if (getActivity() != null) {
                if (!val) {
                    muteCallTv.setTextColor(getResources().getColor(R.color.picker_grey));
                    muteCall.setTint(getResources().getColor(R.color.picker_grey));
                } else {
                    muteCallTv.setTextColor(Color.WHITE);
                    muteCall.setTint(Color.WHITE);
                }
            }
        });

        //
        // Bind Video Streams
        //
        if (peer.getPeerType() == PeerType.PRIVATE) {

            //
            // Video Button
            //
            bind(call.getIsVideoEnabled(), (val, valueModel) -> {
                if (val) {
                    videoTv.setTextColor(Color.WHITE);
                    videoIcon.setTint(Color.WHITE);
                } else {
                    videoTv.setTextColor(getResources().getColor(R.color.picker_grey));
                    videoIcon.setTint(getResources().getColor(R.color.picker_grey));
                }
            });


            //
            // Bind Own Stream
            //

            ACTIVITY_BINDER.bind(call.getOwnVideoTracks(), (videoTracks, valueModel) -> {
                boolean isNeedUnbind = true;

                if (videoTracks.size() > 0) {

                    if (!isLocalViewConfigured) {
                        localVideoView.init(eglContext.getEglBaseContext(), null);
                        isLocalViewConfigured = true;
                    }

                    VideoTrack videoTrack = ((AndroidVideoTrack) videoTracks.get(0)).getVideoTrack();

                    if (videoTrack != localTrack) {
                        if (localTrack != null) {
                            localTrack.removeRenderer(localRender);
                            localRender.dispose();
                        }

                        localTrack = videoTrack;
                        localRender = new VideoRenderer(localVideoView);
                        localTrack.addRenderer(localRender);
                        localVideoView.setVisibility(View.VISIBLE);
                    }
                    isNeedUnbind = false;

                }

                if (isNeedUnbind) {
                    if (localTrack != null) {
                        localTrack.removeRenderer(localRender);
                        localTrack = null;
                        localRender.dispose();
                        localRender = null;
                    }
                    if (isLocalViewConfigured) {
                        localVideoView.release();
                        isLocalViewConfigured = false;
                    }
                    localVideoView.setVisibility(View.INVISIBLE);
                }
            });

            //
            // Bind Their Stream
            //

            ACTIVITY_BINDER.bind(call.getTheirVideoTracks(), (videoTracks, valueModel) -> {
                boolean isNeedUnbind = true;

                if (videoTracks.size() > 0) {

                    if (!isRemoteViewConfigured) {
                        remoteVideoView.init(eglContext.getEglBaseContext(), null);
                        isRemoteViewConfigured = true;
                    }

                    VideoTrack videoTrack = ((AndroidVideoTrack) videoTracks.get(0)).getVideoTrack();

                    if (videoTrack != remoteTrack) {
                        if (remoteTrack != null) {
                            remoteTrack.removeRenderer(remoteRender);
                            remoteRender.dispose();
                        }

                        remoteTrack = videoTrack;
                        remoteRender = new VideoRenderer(remoteVideoView);
                        remoteTrack.addRenderer(remoteRender);
                        remoteVideoView.setVisibility(View.VISIBLE);
                        avatarView.setVisibility(View.INVISIBLE);
                        nameTV.setVisibility(View.INVISIBLE);
                    }
                    isNeedUnbind = false;

                }

                if (isNeedUnbind) {
                    if (remoteTrack != null) {
                        remoteTrack.removeRenderer(remoteRender);
                        remoteTrack = null;
                        remoteRender.dispose();
                        remoteRender = null;
                    }
                    if (isRemoteViewConfigured) {
                        remoteVideoView.release();
                        isRemoteViewConfigured = false;
                    }
                    remoteVideoView.setVisibility(View.INVISIBLE);
                    avatarView.setVisibility(View.VISIBLE);
                    nameTV.setVisibility(View.VISIBLE);
                }
            });

        } else {
            videoTv.setTextColor(getResources().getColor(R.color.picker_grey));
            videoIcon.setTint(getResources().getColor(R.color.picker_grey));
        }


        //
        // Hide "in progress" notification
        //
        manager.cancel(NOTIFICATION_ID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.members) {
            switchAvatarMembers();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();

        disableWakeLock();

        if (peer.getPeerType() == PeerType.PRIVATE) {

            // Release Local Viewport

            if (localTrack != null) {
                localTrack.removeRenderer(localRender);
                localRender.dispose();
                localRender = null;
                localTrack = null;
            }
            if (isLocalViewConfigured) {
                localVideoView.release();
                isLocalViewConfigured = false;
            }

            // Release Remote Viewport

            if (remoteTrack != null) {
                remoteTrack.removeRenderer(remoteRender);
                remoteRender.dispose();
                remoteRender = null;
                remoteTrack = null;
            }
            if (isRemoteViewConfigured) {
                remoteVideoView.release();
                isRemoteViewConfigured = false;
            }
        }


        //
        // Unbind call streams
        //
        ACTIVITY_BINDER.unbindAll();


        //
        // Show In Progress
        //
        if (call != null && call.getState().get() != CallState.ENDED) {
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
    }


    class CallMembersAdapter extends HolderAdapter<CallMember> {


        protected ArrayList<CallMember> members;

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

        protected class MemberHolder extends ViewHolder<CallMember> {

            CallMember data;
            protected TextView userName;
            protected TextView status;
            protected AvatarView avatarView;

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
