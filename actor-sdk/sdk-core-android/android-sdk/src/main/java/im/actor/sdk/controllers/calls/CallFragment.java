package im.actor.sdk.controllers.calls;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import im.actor.sdk.controllers.fragment.BaseFragment;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.SearchHighlight;
import im.actor.sdk.view.adapters.HolderAdapter;
import im.actor.sdk.view.adapters.ViewHolder;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.view.avatar.CoverAvatarView;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
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
    private TextView timerTV;
    private NotificationManager manager;

    public CallFragment() {

        manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public CallFragment(long callId, boolean incoming) {
        this.callId = callId;
        this.call = messenger().getCall(callId);
        this.peer = call.getPeer();
        this.incoming = incoming;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        FrameLayout cont = (FrameLayout) inflater.inflate(R.layout.fragment_call, container, false);
        v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        answerContainer = cont.findViewById(R.id.answer_container);
        ImageButton answer = (ImageButton) cont.findViewById(R.id.answer);
        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAnswer();
            }
        });
        ImageButton notAnswer = (ImageButton) cont.findViewById(R.id.notAnswer);
        ImageButton endCall = (ImageButton) cont.findViewById(R.id.end_call);
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
        //Avatar/Name
        //
        CoverAvatarView avatarView = (CoverAvatarView) cont.findViewById(R.id.avatar);
        avatarView.setBkgrnd((ImageView) cont.findViewById(R.id.avatar_bgrnd));
        TextView nameTV = (TextView) cont.findViewById(R.id.name);
        nameTV.setTextColor(ActorSDK.sharedActor().style.getProfileTitleColor());

        if(peer.getPeerType() == PeerType.PRIVATE){

            UserVM user = users().get(peer.getPeerId());
            bind(avatarView, user.getAvatar());
            bind(nameTV, user.getName());
        }else if(peer.getPeerType() == PeerType.GROUP){
            GroupVM g = groups().get(peer.getPeerId());
            bind(avatarView, g.getAvatar());
            bind(nameTV, g.getName());
        }

        ListView membersList  = (ListView) cont.findViewById(R.id.members_list);
        CallMembersAdapter membersAdapter = new CallMembersAdapter(getActivity(), call.getMembers());
        membersList.setAdapter(membersAdapter);

        timerTV = (TextView) cont.findViewById(R.id.timer);
        timerTV.setTextColor(ActorSDK.sharedActor().style.getProfileSubtitleColor());

        //
        // Check permission
        //
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Permissions", "call - no permission :c");
            CallFragment.this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.VIBRATE, Manifest.permission.WAKE_LOCK},
                    PERMISSIONS_REQUEST_FOR_CALL);

        }

        call.getState().subscribe(new ValueChangedListener<CallState>() {
            @Override
            public void onChanged(CallState val, Value<CallState> valueModel) {
                switch (val){
                    case ENDED:
                        onCallEnd();
                        break;
                    case IN_PROGRESS:
                        onConnected();
                        startTimer();
                        break;
                    case CALLING_INCOMING:
                        initIncoming();
                        break;
                    case CALLING_OUTGOING:
                        onConnecting();
                        break;
                }
            }
        }, true);


        return cont;
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
                                timerTV.setText(formatter.format(new Date(timeFromRegister)));
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1100);
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    ringtone = RingtoneManager.getRingtone(getActivity(), notification);
                    if (getActivity() != null & answerContainer.getVisibility() == View.VISIBLE) {
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

        onConnecting();
        answerContainer.setVisibility(View.INVISIBLE);
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


    public void onConnecting() {
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
        vibrate = false;
        if (ringtone != null) {
            ringtone.stop();
        }
        if (v != null) {
            v.cancel();
        }
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }

        if(timer!=null){
            timer.send(PoisonPill.INSTANCE);
        }

        manager.cancel(NOTIFICATION_ID);
        if(getActivity()!=null){
            getActivity().finish();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if(call.getState().get()!=CallState.ENDED){
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_app_notify);
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
            builder.setContentTitle("return to call");

            Intent intent = new Intent(getActivity(), CallActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("callId", callId);
            intent.putExtra("incoming", incoming);

            builder.setContentIntent(PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            Notification n = builder.build();

            n.flags += Notification.FLAG_ONGOING_EVENT;


            manager.notify(NOTIFICATION_ID, n);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        manager.cancel(NOTIFICATION_ID);
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
            public void unbind() {
                avatarView.unbind();
            }
        }
    }
}
