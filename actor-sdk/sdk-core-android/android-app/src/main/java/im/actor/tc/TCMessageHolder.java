package im.actor.tc;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import im.actor.Application;
import im.actor.core.entity.Message;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;
import im.actor.sdk.R;
import im.actor.sdk.controllers.conversation.messages.BaseJsonHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.PreprocessedData;

public class TCMessageHolder extends BaseJsonHolder {

    private final LinearLayout messageBubble;
    TextView text;
    protected static ActorRef tcActor;
    ProgressBar progress;

    public TCMessageHolder(MessagesAdapter adapter, ViewGroup viewGroup, int id, boolean isFullSize) {
        super(adapter, viewGroup, id, isFullSize);
        text = (TextView) itemView.findViewById(R.id.tv_text);
        progress = (ProgressBar) itemView.findViewById(R.id.progress);
        messageBubble = (LinearLayout) itemView.findViewById(R.id.bubbleContainer);
        messageBubble.setBackgroundResource(R.drawable.conv_bubble_media_in);

    }

    @Override
    protected void bindData(Message message, JSONObject data, boolean isUpdated, PreprocessedData preprocessedData) {

        String render = "";
        try {
            int id = data.getInt("id");
            render += "id: " + id + "\n";
            String state = data.getString("state");
            render += "state: " + state;
            int pr = data.optInt("percentageComplete", 0);
            if (state.equals("running")) {
                render += ", progress: " + pr + "%\n";
                progress.setProgress(pr);
                progress.setVisibility(View.VISIBLE);
            } else {
                progress.setVisibility(View.GONE);
            }
            final String usr = data.getString("url");
            if (tcActor == null) {
                tcActor = ActorSystem.system().actorOf(Props.create(new ActorCreator() {
                    @Override
                    public TCActor create() {
                        return new TCActor(usr);
                    }
                }), "actor/tc");
                Log.d("TC", "create tc actor");

            }

            if (!data.getString("state").equals("finished")) {
                tcActor.send(new TCActor.Bind(id, message.getRid(), getPeer()));
            }

        } catch (JSONException e) {
            e.printStackTrace();
//            progress.setVisibility(View.GONE);
        }

        text.setText(render);

    }
}
