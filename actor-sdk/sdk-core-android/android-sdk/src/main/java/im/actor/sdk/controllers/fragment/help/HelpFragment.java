package im.actor.sdk.controllers.fragment.help;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;

public class HelpFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.help_main, container, false);
        res.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        res.findViewById(R.id.divider1).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
        res.findViewById(R.id.divider2).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
        res.findViewById(R.id.divider3).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());

        ((TextView) res.findViewById(R.id.help_about_title)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.help_faq_title)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.help_feedback_title)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.help_version_title)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        ((TextView) res.findViewById(R.id.help_about_hint)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        ((TextView) res.findViewById(R.id.help_faq_hint)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        ((TextView) res.findViewById(R.id.help_feedback_hint)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        ((TextView) res.findViewById(R.id.version)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        res.findViewById(R.id.openFaq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://actor.im")));
            }
        });
        res.findViewById(R.id.giveFeedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uriText = "mailto:support@actor.im" +
                        "?subject=" + Uri.encode(getString(R.string.help_email_subject));

                Uri uri = Uri.parse(uriText);
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                sendIntent.setData(uri);
                startActivity(Intent.createChooser(sendIntent, getString(R.string.help_email_title)));
            }
        });
        res.findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://actor.im")));
            }
        });

//        final String versionName = BuildConfig.VERSION_TITLE;
//        ((TextView) res.findViewById(R.id.version)).setText(versionName);
//        res.findViewById(R.id.versionItem).setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
//                android.content.ClipData clip = android.content.ClipData.newPlainText("App version", versionName);
//                clipboard.setPrimaryClip(clip);
//                Toast.makeText(getActivity(), R.string.help_version_copy, Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });
        return res;
    }
}
