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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

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
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
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

    boolean incoming;
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

    public CallFragment() {

        manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public CallFragment(long callId, boolean incoming) {
        this.callId = callId;
        this.call = messenger().getCall(callId);
        if(call == null){
            this.peer = Peer.user(myUid());
        }else{
            this.peer = call.getPeer();
        }
        this.incoming = incoming;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


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

        showView(layer1);
        showView(layer2);
        showView(layer3);
        wave(avatarLayers, 1.135f ,1900, -2f);

        for (int i = 0; i<avatarLayers.length; i++){
            View layer = avatarLayers[i];
            ((GradientDrawable)layer.getBackground()).setColor(Color.WHITE);
            ((GradientDrawable)layer.getBackground()).setAlpha(50);
        }

        endCallContainer = cont.findViewById(R.id.end_call_container);
        answerContainer = cont.findViewById(R.id.answer_container);
        ImageButton answer = (ImageButton) cont.findViewById(R.id.answer);
        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAnswer();
            }
        });
        ImageButton notAnswer = (ImageButton) cont.findViewById(R.id.notAnswer);
        endCall = (ImageButton) cont.findViewById(R.id.end_call);
        notAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doEndCall();
            }
        });
        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doEndCall();
            }
        });

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
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchAvatarMembers();
            }
        };
        avatarView.setOnClickListener(listener);
//        cont.findViewById(R.id.background).setOnClickListener(listener);

        membersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switchAvatarMembers();
            }

        });



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
        final TintImageView speaker = (TintImageView) cont.findViewById(R.id.speaker);
        speaker.setResource(R.drawable.ic_volume_up_white_24dp);
        final TextView speakerTV = (TextView) cont.findViewById(R.id.speaker_tv);
        cont.findViewById(R.id.speaker_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSpeaker(speaker, speakerTV);
            }
        });
        checkSpeaker(speaker, speakerTV);

        final TintImageView muteCall = (TintImageView) cont.findViewById(R.id.mute);
        final TextView muteCallTv = (TextView) cont.findViewById(R.id.mute_tv);
        muteCall.setResource(R.drawable.ic_mic_off_white_24dp);
        cont.findViewById(R.id.mute_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messenger().toggleCallMute(callId);
            }
        });

        final TintImageView video = (TintImageView) cont.findViewById(R.id.video);
        video.setResource(R.drawable.ic_videocam_white_24dp);
        TextView videoTv = (TextView) cont.findViewById(R.id.video_tv);
        videoTv.setTextColor(getResources().getColor(R.color.picker_grey));
        video.setTint(getResources().getColor(R.color.picker_grey));

        final TintImageView back = (TintImageView) cont.findViewById(R.id.back);
        back.setResource(R.drawable.ic_message_white_24dp);
        cont.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        final TintImageView add = (TintImageView) cont.findViewById(R.id.add);
        add.setResource(R.drawable.ic_person_add_white_24dp);
        TextView addTv = (TextView) cont.findViewById(R.id.add_user_tv);
        addTv.setTextColor(getResources().getColor(R.color.picker_grey));
        add.setTint(getResources().getColor(R.color.picker_grey));

        if(call!=null){
            call.getIsMuted().subscribe(new ValueChangedListener<Boolean>() {
                @Override
                public void onChanged(Boolean val, Value<Boolean> valueModel) {
                    if(getActivity()!=null){
                        if(val){

                            muteCallTv.setTextColor(getResources().getColor(R.color.picker_grey));
                            muteCall.setTint(getResources().getColor(R.color.picker_grey));
                        }else{
                            muteCallTv.setTextColor(Color.WHITE);
                            muteCall.setTint(Color.WHITE);
                        }
                    }
                }
            });

            call.getState().subscribe(new ValueChangedListener<CallState>() {
                @Override
                public void onChanged(CallState val, Value<CallState> valueModel) {
                    if(currentState!=val){
                        currentState = val;
                        switch (val){

                            case RINGING:
                                if(call.isOutgoing()){
                                    statusTV.setText(R.string.call_outgoing);
                                }else{
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

                }
            }, true);
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
            timer = ActorSystem.system().actorOf(Props.create(new ActorCreator() {
                @Override
                public Actor create() {
                    return new TimerActor(300);
                }
            }), "calls/timer");

            timer.send(new TimerActor.Register(new TimerActor.TimerCallback() {
                @Override
                public void onTick(long currentTime, final long timeFromRegister) {
                    if(getActivity()!=null){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(currentState == CallState.IN_PROGRESS){
                                    statusTV.setText(formatter.format(new Date(timeFromRegister)));
                                }
                            }
                        });
                    }
                }
            } ,TIMER_ID));

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
        answerContainer.setVisibility(View.VISIBLE);
        endCallContainer.setVisibility(View.GONE);

        new Thread(new Runnable() {
            @Override
            public void run() {
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
        if(call!=null && call.getState().get()!=CallState.ENDED){
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_app_notify);
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
            builder.setContentTitle(getActivity().getString(R.string.call_notification));

            Intent intent = new Intent(getActivity(), CallActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("callId", callId);
            intent.putExtra("incoming", incoming);

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

        manager.cancel(NOTIFICATION_ID);
//        animator.popAnimation(true);

    }

    class CallMembersAdapter extends HolderAdapter<CallMember>{


        private ArrayList<CallMember> members;

        protected CallMembersAdapter(Context context, final ValueModel<ArrayList<CallMember>> members) {
            super(context);
            this.members = members.get();
            members.subscribe(new ValueChangedListener<ArrayList<CallMember>>() {
                @Override
                public void onChanged(ArrayList<CallMember> val, Value<ArrayList<CallMember>> valueModel) {
                    CallMembersAdapter.this.members = val;
                    notifyDataSetChanged();
                    Log.d("STATUS CHANGED", val.toString());
                }
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
