/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.messenger.app.fragment.call;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.BaseFragment;

public class CallFragment extends BaseFragment {

    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_call, container, false);
        textView = (TextView) res.findViewById(R.id.phoneState);
        res.findViewById(R.id.cancelCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                CurrentCall currentCall = messenger().getCurrentCall().get();
//                if (currentCall != null) {
//                    messenger().endCall(currentCall.getRid());
//                }
            }
        });
        res.findViewById(R.id.answerCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                CurrentCall currentCall = messenger().getCurrentCall().get();
//                if (currentCall != null) {
//                    messenger().answerCall(currentCall.getRid());
//                }
            }
        });
        return res;
    }

    @Override
    public void onResume() {
        super.onResume();
//        CurrentCall call = messenger().getCurrentCall().get();
//        if (call != null && call.getCallState() == CallState.ENDED) {
//            getActivity().finish();
//            return;
//        }
//        bind(messenger().getCurrentCall(), new ValueChangedListener<CurrentCall>() {
//            @Override
//            public void onChanged(CurrentCall val, ValueModel<CurrentCall> valueModel) {
//                if (val == null) {
//                    textView.setText("Null");
//                } else {
//                    textView.setText("" + val.getCallState());
//                    if (val.getCallState() == CallState.ENDED) {
//                        textView.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (getActivity() != null) {
//                                    getActivity().finish();
//                                }
//                            }
//                        }, 1000);
//                    }
//                }
//
//            }
//        });
    }
}
