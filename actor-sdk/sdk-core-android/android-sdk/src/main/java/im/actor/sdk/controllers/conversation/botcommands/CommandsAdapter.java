package im.actor.sdk.controllers.conversation.botcommands;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.BotCommand;
import im.actor.core.viewmodel.UserVM;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.adapters.HolderAdapter;
import im.actor.sdk.view.adapters.ViewHolder;
import im.actor.sdk.view.avatar.AvatarView;

import static im.actor.sdk.util.ActorSDKMessenger.users;

public class CommandsAdapter extends HolderAdapter<BotCommand> {

    private final int uid;
    int oldRowsCount = 0;
    private List<BotCommand> commands;
    private List<BotCommand> commandsToShow = new ArrayList<BotCommand>();
    private CommandsUpdatedCallback updatedCallback;
    private int highlightColor;
    private UserVM botUser;
    private String query = "";

    public CommandsAdapter(int uid, Context context, CommandsUpdatedCallback updatedCallback) {
        super(context);
        botUser = users().get(uid);
        highlightColor = context.getResources().getColor(R.color.primary);
        commands = users().get(uid).getBotCommands().get();
        commandsToShow = new ArrayList<>(commands);
        this.uid = uid;
        this.updatedCallback = updatedCallback;

    }

    public void setQuery(String q) {
        if (q == null || q.equals(query)) {
            return;
        }
        query = q;
        ArrayList<BotCommand> filterd = new ArrayList<BotCommand>();
        for (BotCommand command : commands) {
            if (command.getSlashCommand().toLowerCase().startsWith(q)) {
                filterd.add(command);
            }
        }
        commandsToShow.clear();
        commandsToShow.addAll(filterd);
        int newRowsCount = commandsToShow.size();
        updatedCallback.onMentionsUpdated(oldRowsCount, newRowsCount);
        oldRowsCount = newRowsCount;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return commandsToShow.size();
    }

    @Override
    public BotCommand getItem(int position) {
        return commandsToShow.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    protected ViewHolder<BotCommand> createHolder(BotCommand obj) {
        return new CommandHolder();
    }

    public interface CommandsUpdatedCallback {
        void onMentionsUpdated(int oldRowsCount, int newRowsCount);
    }

    public class CommandHolder extends ViewHolder<BotCommand> {

        BotCommand data;
        private TextView commandName;
        private TextView description;
        private AvatarView avatarView;

        @Override
        public View init(final BotCommand data, ViewGroup viewGroup, Context context) {
            View res = ((Activity) context).getLayoutInflater().inflate(R.layout.fragment_chat_mention_item, viewGroup, false);
            res.findViewById(R.id.divider).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());

            commandName = (TextView) res.findViewById(R.id.name);
            commandName.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
            description = (TextView) res.findViewById(R.id.mentionHint);
            description.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
            avatarView = (AvatarView) res.findViewById(R.id.avatar);
            avatarView.init(Screen.dp(35), 18);
            this.data = data;

            return res;
        }

        @Override
        public void bind(BotCommand data, int position, Context context) {
            this.data = data;
            avatarView.bind(botUser);
            String name = data.getSlashCommand();
            commandName.setText("/".concat(name));

            CharSequence hint = data.getDescription();
            description.setText(hint);
        }

        public BotCommand getCommand() {
            return data;
        }

        @Override
        public void unbind(boolean full) {
            if (full) {
                avatarView.unbind();
            }
        }
    }


}
