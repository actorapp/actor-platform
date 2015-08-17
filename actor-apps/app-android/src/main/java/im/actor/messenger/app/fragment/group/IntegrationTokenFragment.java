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

import com.afollestad.materialdialogs.MaterialDialog;

import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.messenger.R;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.messenger.app.view.HolderAdapter;
import im.actor.messenger.app.view.ViewHolder;

import static im.actor.messenger.app.core.Core.groups;
import static im.actor.messenger.app.core.Core.messenger;
import static im.actor.messenger.app.core.Core.myUid;

/**
 * Created by korka on 25.05.15.
 */
public class IntegrationTokenFragment extends BaseFragment {

    private static final String EXTRA_GROUP_ID = "GROUP_ID";

    private int chatId;
    private ListView listView;
    private IntegrationTokenActionsAdapter adapter;
    private String integrationToken;
    private GroupVM groupInfo;
    private boolean isAdmin;

    private TextView emptyView;

    public static IntegrationTokenFragment create(int gid) {
        IntegrationTokenFragment res = new IntegrationTokenFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(EXTRA_GROUP_ID, gid);
        res.setArguments(arguments);
        return res;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        chatId = getArguments().getInt(EXTRA_GROUP_ID);

        groupInfo = groups().get(chatId);
        isAdmin = groupInfo.getCreatorId() == myUid();
        Command<String> cmd = messenger().requestIntegrationToken(chatId);
        if (cmd != null) cmd.start(new CommandCallback<String>() {
            @Override
            public void onResult(String res) {
                if (res != null && !res.isEmpty()) {
                    integrationToken = res;
                    adapter.notifyDataSetChanged();
                    hideView(emptyView);
                    showView(listView);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getActivity(), getString(R.string.integration_token_error_get_token), Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }
        });


        final ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        View res = inflater.inflate(R.layout.fragment_list, container, false);
        listView = (ListView) res.findViewById(R.id.listView);
        emptyView = (TextView) res.findViewById(R.id.emptyView);
        emptyView.setText(getString(R.string.integration_token_empty_view));
        adapter = new IntegrationTokenActionsAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (integrationToken != null && !integrationToken.isEmpty()) {
                    switch (position) {
                        case 0:
                            //Token itself
                            clipboard.setPrimaryClip(ClipData.newPlainText(null, integrationToken));
                            Toast.makeText(getActivity(), getString(R.string.integration_token_copied), Toast.LENGTH_SHORT).show();
                            break;

                        case 1:
                            //Hint
                            break;

                        case 2:
                            //Copy
                            clipboard.setPrimaryClip(ClipData.newPlainText(null, integrationToken));
                            Toast.makeText(getActivity(), getString(R.string.integration_token_copied), Toast.LENGTH_SHORT).show();
                            break;

                        case 3:
                            //Revoke
                            if (isAdmin) {
                                new MaterialDialog.Builder(getActivity())
                                        .content(R.string.alert_revoke_integration_token_message)
                                        .positiveText(R.string.alert_revoke_integration_token_yes)
                                        .negativeText(R.string.dialog_cancel)
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog materialDialog1) {
                                                execute(messenger().revokeIntegrationToken(chatId), R.string.integration_token_action_revoke, new CommandCallback<String>() {
                                                    @Override
                                                    public void onResult(String res) {
                                                        integrationToken = res;
                                                        adapter.notifyDataSetChanged();
                                                    }

                                                    @Override
                                                    public void onError(Exception e) {
                                                        Toast.makeText(getActivity(), getString(R.string.integration_token_error_revoke_link), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        })
                                        .show();
                            } else {
                                Toast.makeText(getActivity(), getString(R.string.integration_token_error_revoke_link_not_admin), Toast.LENGTH_LONG).show();
                            }


                            break;

                        case 4:
                            //Share
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("text/plain");
                            i.putExtra(Intent.EXTRA_TEXT, integrationToken);
                            Intent chooser = Intent.createChooser(i, getString(R.string.integration_token_chooser_title));
                            if (i.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(chooser);
                            }
                            break;
                    }
                }
            }
        });

        View footer = inflater.inflate(R.layout.fragment_link_item_footer, listView, false);
        listView.addFooterView(footer, null, false);


        return res;
    }

    class IntegrationTokenActionsAdapter extends HolderAdapter<Void> {

        protected IntegrationTokenActionsAdapter(Context context) {
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
            container = (FrameLayout) res.findViewById(R.id.linksActionContainer);
            topShadow = res.findViewById(R.id.top_shadow);
            botShadow = res.findViewById(R.id.bot_shadow);
            divider = res.findViewById(R.id.divider);
            return res;
        }

        @Override
        public void bind(Void data, int position, Context context) {

            switch (position) {
                case 0:
                    action.setText(integrationToken);
                    break;

                case 1:
                    action.setText(getString(R.string.integration_token_hint));
                    break;

                case 2:
                    action.setText(getString(R.string.integration_token_action_copy));
                    break;

                case 3:
                    action.setText(getString(R.string.integration_token_action_revoke));
                    break;

                case 4:
                    action.setText(getString(R.string.integration_token_action_share));
                    break;
            }

            //Hint styling
            if (position == 1) {
                container.setBackgroundColor(getActivity().getResources().getColor(R.color.bg_backyard));
                topShadow.setVisibility(View.VISIBLE);
                botShadow.setVisibility(View.VISIBLE);
                divider.setVisibility(View.INVISIBLE);
                action.setTextColor(getActivity().getResources().getColor(R.color.text_hint));
                action.setTextSize(14);
            } else {
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