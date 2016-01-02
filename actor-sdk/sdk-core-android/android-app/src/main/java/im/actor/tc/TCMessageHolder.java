package im.actor.tc;

import android.view.View;
import android.view.ViewGroup;
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
import im.actor.sdk.controllers.conversation.messages.BaseCustomHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.PreprocessedData;
import im.actor.sdk.core.audio.AudioPlayerActor;

public class TCMessageHolder extends BaseCustomHolder {

    TextView text;
    protected static ActorRef tcActor;
    ProgressBar progress;

    public TCMessageHolder(MessagesAdapter adapter, ViewGroup viewGroup, int id, boolean isFullSize) {
        super(adapter, viewGroup, id, isFullSize);
        text = (TextView) itemView.findViewById(R.id.tv_text);
        progress = (ProgressBar) itemView.findViewById(R.id.progress);
    }

    @Override
    protected void bindData(Message message, boolean isUpdated, PreprocessedData preprocessedData) {
        String render = "";
        JSONObject json = ((Application.TCBotMesaage) message.getContent()).getJson();
        try {
            final JSONObject data = json.getJSONObject("data");


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
                tcActor = ActorSystem.system().actorOf(Props.create(TCActor.class, new ActorCreator<TCActor>() {
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
