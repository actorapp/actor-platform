package im.actor.messenger.app.fragment.help;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import im.actor.messenger.BuildConfig;
import im.actor.messenger.R;
import im.actor.messenger.app.fragment.BaseFragment;

/**
 * Created by ex3ndr on 30.09.14.
 */
public class HelpFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.help_main, container, false);
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

        final String versionName = BuildConfig.VERSION_TITLE;
        ((TextView) res.findViewById(R.id.version)).setText(versionName);
        res.findViewById(R.id.versionItem).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("App version", versionName);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(), R.string.help_version_copy, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        return res;
    }
}
