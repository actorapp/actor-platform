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


public class ClcApplication {

    private static final Logger logger = LoggerFactory.getLogger(ClcApplication.class);

    static ClcMessenger messenger;
    static int rate = 1000;
    static int myNumber = 1;
    private static int counter = 0;

    private static ArrayList<Long[]> states = new ArrayList<Long[]>();
    private static Timer senderTimer;
    private static int messagesCount = 5;
    private static int randomSeed;

   public static void sendMessage(String number, final String message) {
        messenger.findUsers(number).start(new CommandCallback<UserVM[]>() {
            @Override
            public void onResult(UserVM[] users) {

                if (users.length == 0)
                    return;

                final UserVM user = users[0];
                final Peer peer = Peer.fromUniqueId(user.getId());
                if (!user.isContact().get()) {
                    Command<Boolean> addContact = messenger.addContact(user.getId());

                    assert addContact != null;
                    addContact.start(new CommandCallback<Boolean>() {
                        @Override
                        public void onResult(Boolean res) {
                            if (res) {
                		messenger.sendMessage(peer, message);
                            }
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                } else {
                    messenger.sendMessage(peer, message);
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public static void signUp(final String name, Sex sex, String avatarPath) {
        messenger.signUp(name, sex, avatarPath).start(new CommandCallback<AuthState>() {
            @Override
            public void onResult(AuthState res) {
                if (res == AuthState.LOGGED_IN){
                    logger.info("LOGGED_IN");
//                    sendMessage("+989150000" + (myNumber + 20), "seed: " + randomSeed + "," + myNumber);
                }else if(res == AuthState.SIGN_UP){
                    logger.info("SIGN_UP");
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public static void sendCode(String code) {
        try {
            messenger.validateCode(code).start(new CommandCallback<AuthState>() {
                @Override
                public void onResult(AuthState res) {
                    randomSeed = new Random().nextInt();
                    if (res == AuthState.SIGN_UP) {
                        logger.info("SIGN_UP");
                        signUp("75550000" + (myNumber), Sex.MALE, null);
                    } else if(res == AuthState.LOGGED_IN){
                        logger.info("LOGGED_IN");
                    }

                }

                @Override
                public void onError(Exception e) {

                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    public static void requestSms(int phone) {
            long res = Long.parseLong("75550000" + phone);
            messenger.requestStartPhoneAuth(res).start(new CommandCallback<AuthState>() {
                @Override
                public void onResult(AuthState res) {
                    logger.info(res.toString());
                    sendCode("0000");
                }

                @Override
                public void onError(Exception e) {
                    logger.error(e.getMessage(),e);
                }
            });
    }

    public static void main(String[] args) {

        ConfigurationBuilder builder = new ConfigurationBuilder();
        if (args.length > 0)
            builder.addEndpoint(args[0]);
        else
            builder.addEndpoint("tcp://localhost:9070");

        builder.setPhoneBookProvider(new PhoneBookProvider() {
            @Override
            public void loadPhoneBook(Callback callback) {
                callback.onLoaded(new ArrayList<PhoneBookContact>());
            }
        });

        builder.setNotificationProvider(new NotificationProvider() {
            @Override
            public void onMessageArriveInApp(Messenger messenger) {
            }

            @Override
            public void onNotification(Messenger messenger, List<Notification> topNotifications, int messagesCount, int conversationsCount) {

            }

            @Override
            public void onUpdateNotification(Messenger messenger, List<Notification> topNotifications, int messagesCount, int conversationsCount) {

            }

            @Override
            public void hideAllNotifications() {

            }
        });

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
        requestSms(myNumber);


    }

}
