package im.actor.messenger.app.fragment.group;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.messenger.app.view.HolderAdapter;
import im.actor.messenger.app.view.ViewHolder;
import im.actor.model.entity.GroupMember;
import im.actor.model.viewmodel.GroupVM;

import static im.actor.messenger.app.Core.groups;

/**
 * Created by korka on 25.05.15.
 */
public class InviteLinkFragment extends BaseFragment {

    private static final String EXTRA_GROUP_ID = "GROUP_ID";

    private int chatId;
    private GroupVM groupInfo;
    private ListView listView;

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

        View res = inflater.inflate(R.layout.fragment_invite_link, container, false);
        listView = (ListView) res.findViewById(R.id.inviteLinkActionsList);
        listView.setAdapter(new InviteLincActionsAdapter(getActivity()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Toast.makeText(getActivity(), "1", Toast.LENGTH_SHORT).show();
                        break;

                    case 1:
                        Toast.makeText(getActivity(), "2", Toast.LENGTH_SHORT).show();
                        break;

                    case 2:
                        Toast.makeText(getActivity(), "3", Toast.LENGTH_SHORT).show();
                        break;

                    case 3:
                        Toast.makeText(getActivity(), "4", Toast.LENGTH_SHORT).show();
                        break;

                    case 4:
                        Toast.makeText(getActivity(), "5", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

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
        LinearLayout container;
        @Override
        public View init(Void data, ViewGroup viewGroup, Context context) {
            View res = ((Activity) context).getLayoutInflater().inflate(R.layout.fragment_invite_link_item, viewGroup, false);
            action = (TextView) res.findViewById(R.id.action);
            container = (LinearLayout) res.findViewById(R.id.inviteLinksActionContainer);
            return res;
        }

        @Override
        public void bind(Void data, int position, Context context) {
            switch (position){
                case 0:
                    action.setText("http://".concat(getActivity().getString(R.string.messenger_domain)).concat("/token"));
                    container.setBackgroundColor(Color.TRANSPARENT);
                    break;

                case 1:
                    container.setBackgroundColor(getActivity().getResources().getColor(R.color.bg_backyard));
                    action.setText("Бла бла бла");
                    break;

                case 2:
                    action.setText("action 1");
                    container.setBackgroundColor(Color.TRANSPARENT);
                    break;

                case 3:
                    action.setText("action 2");
                    container.setBackgroundColor(Color.TRANSPARENT);
                    break;

                case 4:
                    action.setText("action 3");
                    container.setBackgroundColor(Color.TRANSPARENT);
                    break;
            }
        }
    }
}