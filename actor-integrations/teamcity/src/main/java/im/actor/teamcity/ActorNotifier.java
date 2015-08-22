package im.actor.teamcity;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import jetbrains.buildServer.notification.NotificatorAdapter;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.UserPropertyInfo;
import jetbrains.buildServer.users.NotificatorPropertyKey;
import jetbrains.buildServer.users.PropertyKey;
import jetbrains.buildServer.users.SUser;

public class ActorNotifier extends NotificatorAdapter {

    private static final String TYPE = "actorNotifier";
    private static final String TYPE_NAME = "Actor Notifier";

    private static final String PROPERTY_TOKEN = "group_token";

    private static final PropertyKey PROPERTY_TOKEN_ID = new NotificatorPropertyKey(TYPE, PROPERTY_TOKEN);

    private static OkHttpClient client = new OkHttpClient();

    public ActorNotifier(NotificatorRegistry notificatorRegistry) throws IOException {
        ArrayList<UserPropertyInfo> userProps = new ArrayList<UserPropertyInfo>();
        userProps.add(new UserPropertyInfo(PROPERTY_TOKEN, "Group Integration Token"));
        notificatorRegistry.register(this, userProps);
    }

    @Override
    public void notifyBuildSuccessful(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
        String message = "**#" + build.getBuildNumber() + " " + build.getFullName() + " (`" + getBranchName(build.getBranch()) + "`) Successful**";
        doNotify(message, users);
    }

    @Override
    public void notifyBuildStarted(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
        String message = " #" + build.getBuildNumber() + " " + build.getFullName() + " (`" + getBranchName(build.getBranch()) + "`) Started";
        doNotify(message, users);
    }

    @Override
    public void notifyBuildFailed(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
        String message = "**#" + build.getBuildNumber() + " " + build.getFullName() + " (`" + getBranchName(build.getBranch()) + "`) Failed**";
        doNotify(message, users);
    }

    @Override
    public void notifyBuildFailing(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
        String message = "**#" + build.getBuildNumber() + " " + build.getFullName() + " (`" + getBranchName(build.getBranch()) + "`) Failing**";
        doNotify(message, users);
    }

    @Override
    public void notifyBuildFailedToStart(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
        String message = "**#" + build.getBuildNumber() + " " + build.getFullName() + " (`" + getBranchName(build.getBranch()) + "`) Failed To Start**";
        doNotify(message, users);
    }

    @NotNull
    @Override
    public String getNotificatorType() {
        return TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return TYPE_NAME;
    }

    private void doNotify(@NotNull String text, @NotNull Set<SUser> users) {
        String jsonString = text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        for (SUser user : users) {
            String token = user.getPropertyValue(PROPERTY_TOKEN_ID);
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{\"text\": \"" + jsonString + "\"}");
            Request request = new Request.Builder()
                    .url(token)
                    .post(body)
                    .build();
            try {
                client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getBranchName(Branch branch) {
        return branch == null ? "<default>" : branch.getDisplayName();
    }
}
