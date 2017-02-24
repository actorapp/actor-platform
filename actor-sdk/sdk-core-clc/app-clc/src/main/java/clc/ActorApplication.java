package clc;


import im.actor.core.*;
import im.actor.core.api.rpc.RequestEditAbout;
import im.actor.core.api.rpc.ResponseSeq;
import im.actor.core.api.updates.UpdateUserAboutChanged;
import im.actor.core.entity.*;
import im.actor.core.entity.content.TextContent;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.providers.NotificationProvider;
import im.actor.core.providers.PhoneBookProvider;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.clc.ClcJavaPreferenceStorage;
import im.actor.runtime.generic.mvvm.AndroidListUpdate;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.generic.mvvm.DisplayList;
import im.actor.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.prefs.BackingStoreException;


public class ActorApplication {


    private String username;
    private String nickname;
    private String password;
    int myNumber = 1;
    public ConfigurationBuilder builder;
    private static final Logger logger = LoggerFactory.getLogger(ClcApplication.class);
    private static int randomSeed;
    static ClcMessenger messenger;
    private ActorApplicationCallback callback;


    public ActorApplication(String username,String nickname ,String password,ConfigurationBuilder builder,ActorApplicationCallback callback)
    {

        this.username = username;
        this.password = password;
        this.nickname = nickname;


        this.builder = builder;
        this.callback = callback;



        builder.setDeviceCategory(DeviceCategory.DESKTOP);
        builder.setPlatformType(PlatformType.GENERIC);
        builder.setApiConfiguration(new ApiConfiguration(
                "cli",
                1,
                "4295f9666fad3faf2d04277fe7a0c40ff39a85d313de5348ad8ffa650ad71855",
                "najva00000000000000000123-" + myNumber,
                "najva00000000000000000000-v1-" + myNumber));

        messenger = new ClcMessenger(builder.build(),Integer.toString(myNumber));




//        messenger.resetAuth();
        sendUserName(username,password);
    }

    private void sendUserName(String username,String password)
    {
        messenger.requestStartUserNameAuth(username).start(new CommandCallback<AuthState>() {
            @Override
            public void onResult(AuthState res) {
                logger.info(res.toString());
                sendPassword(password);
            }

            @Override
            public void onError(Exception e) {
                logger.error(e.getMessage(),e);
            }
        });
    }

    private void sendPassword(String password) {
        try {
            messenger.validatePassword(password).start(new CommandCallback<AuthState>() {
                @Override
                public void onResult(AuthState res) {
                    randomSeed = new Random().nextInt();
                    if (res == AuthState.SIGN_UP) {
                        logger.info("SIGN_UP");
                        signUp(username, Sex.MALE,null, password);
                    } else if(res == AuthState.LOGGED_IN){
                        logger.info("LOGGED_IN");
                        callback.SignInFinished(messenger);
                    }

                }

                @Override
                public void onError(Exception e) {
                    RpcException rpcE = (RpcException)e;
                    if(rpcE.getTag().equals("USERNAME_UNOCCUPIED"))
                    {
                        signUp(username, Sex.MALE,null, password);
                    }
                    else
                    {
                        callback.Error(e);
                    }

                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    private void signUp(final String name, Sex sex, String avatarPath, String password) {
        messenger.signUp(nickname, sex, avatarPath,password).start(new CommandCallback<AuthState>() {
            @Override
            public void onResult(AuthState res) {
                if (res == AuthState.LOGGED_IN){
                    logger.info("LOGGED_IN");
                    callback.SignInFinished(messenger);
//                    sendMessage("+989150000" + (myNumber + 20), "seed: " + randomSeed + "," + myNumber);
                }else if(res == AuthState.SIGN_UP){
                    logger.info("SIGN_UP");
                    callback.SignInFinished(messenger);
                }
            }

            @Override
            public void onError(Exception e) {
                RpcException rpcE = (RpcException)e;
                if(rpcE.getTag().equals("NICKNAME_BUSY")) {
                    callback.SignUpFinished(messenger);
                }

            }
        });
    }
}