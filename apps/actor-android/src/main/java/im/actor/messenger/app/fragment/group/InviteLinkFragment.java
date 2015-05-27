package im.actor.messenger.app.fragment.group;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.messenger.app.view.HolderAdapter;
import im.actor.messenger.app.view.ViewHolder;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.entity.Peer;
import im.actor.model.viewmodel.GroupVM;

import static im.actor.messenger.app.Core.groups;
import static im.actor.messenger.app.Core.messenger;

/**
 * Created by korka on 25.05.15.
 */
public class InviteLinkFragment extends BaseFragment {

    private static final String EXTRA_GROUP_ID = "GROUP_ID";

    private int chatId;
    private GroupVM groupInfo;
    private ListView listView;
    private InviteLincActionsAdapter adapter;
    private String link = "Ooops";


    public static InviteLinkFragment create(int gid) {
        InviteLinkFragment res = new InviteLinkFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(EXTRA_GROUP_ID, gid);
        res.setArguments(arguments);
        return res;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        chatId = getArguments().getInt(EXTRA_GROUP_ID);

        groupInfo = groups().get(chatId);

        link = messenger().getGroupInviteLink(Peer.group(chatId));
        if(link ==null || link.isEmpty()){
            //messenger().requestInviteLink(chatId)
            execute(messenger().requestInviteLink(chatId), R.string.invite_link_title, new CommandCallback<String>() {
                @Override
                public void onResult(String res) {
                    link = res;
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(Exception e) {
                    //Ooops
                }
            });
        }

        final ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        View res = inflater.inflate(R.layout.fragment_invite_link, container, false);
        listView = (ListView) res.findViewById(R.id.inviteLinkActionsList);
        adapter = new InviteLincActionsAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        clipboard.setPrimaryClip(ClipData.newPlainText(null, link));
                        Toast.makeText(getActivity(), getString(R.string.invite_link_copied), Toast.LENGTH_SHORT).show();
                        break;

                    case 1:
                        //Nothing
                        break;

                    case 2:
                        clipboard.setPrimaryClip(ClipData.newPlainText(null, link));
                        Toast.makeText(getActivity(), getString(R.string.invite_link_copied), Toast.LENGTH_SHORT).show();
                        break;

                    case 3:
                        execute(messenger().revokeInviteLink(chatId), R.string.invite_link_title, new CommandCallback<String>() {
                            @Override
                            public void onResult(String res) {
                                link = res;
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Exception e) {
                                //Ooops
                            }
                        });
                        break;

                    case 4:
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("text/plain");
                        i.putExtra(Intent.EXTRA_TEXT, link);
                        Intent chooser = Intent.createChooser(i, getString(R.string.invite_link_chooser_title));
                        if (i.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(chooser);
                        }
                        break;
                }
            }
        });

        View footer = inflater.inflate(R.layout.fragment_invite_link_item_footer, listView, false);
        listView.addFooterView(footer, null, false);

        

        return  res;
    }

    class InviteLincActionsAdapter extends HolderAdapter<Void>{

        protected InviteLincActionsAdapter(Context context) {
            super(context);
        }

        @Override
        public Void getItem(int position) {
            return null;
        }
        @Override
        public int getCount() {
            return 5;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        protected ViewHolder<Void> createHolder(Void obj) {
            return new ActionHolder();
        }
    }

    private class ActionHolder extends ViewHolder<Void> {
        TextView action;
        FrameLayout container;
        View topShadow;
        View botShadow;
        View divider;
        @Override
        public View init(Void data, ViewGroup viewGroup, Context context) {
            View res = ((Activity) context).getLayoutInflater().inflate(R.layout.fragment_invite_link_item, viewGroup, false);
            action = (TextView) res.findViewById(R.id.action);
            container = (FrameLayout) res.findViewById(R.id.inviteLinksActionContainer);
            topShadow = res.findViewById(R.id.top_shadow);
            botShadow = res.findViewById(R.id.bot_shadow);
            divider = res.findViewById(R.id.divider);
            return res;
        }

        @Override
        public void bind(Void data, int position, Context context) {
            switch (position){
                case 0:
                    action.setText(link);
                    break;

                case 1:
                    action.setText(getString(R.string.invite_link_hint));
                    break;

                case 2:
                    action.setText(getString(R.string.invite_link_action_copy));
                    break;

                case 3:
                    action.setText(getString(R.string.invite_link_action_revoke));
                    break;

                case 4:
                    action.setText(getString(R.string.invite_link_action_share));
                    break;
            }

            if(position == 1){
                container.setBackgroundColor(getActivity().getResources().getColor(R.color.bg_backyard));
                topShadow.setVisibility(View.VISIBLE);
                botShadow.setVisibility(View.VISIBLE);
                divider.setVisibility(View.INVISIBLE);
                action.setTextColor(getActivity().getResources().getColor(R.color.text_hint));
                action.setTextSize(14);
            }else{
                container.setBackgroundColor(Color.TRANSPARENT);
                topShadow.setVisibility(View.INVISIBLE);
                botShadow.setVisibility(View.INVISIBLE);
                divider.setVisibility(View.VISIBLE);
                action.setTextColor(getActivity().getResources().getColor(R.color.text_primary));
                action.setTextSize(16);
            }
        }
    }
}