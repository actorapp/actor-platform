package im.actor.cli;

public class Main {

    private static void printUsage() {
        System.out.println("USAGE:");
        System.out.println("actor-cli demo send [-e <endpoint>] <phone_number> <activation_code> <destination_number> [<count>]");
    }

    public static void main(String[] args) {

        System.out.println("Actor CLI v0.1");
        System.out.println();

        // Parsing arguments
        if (args.length < 2) {
            printUsage();
            return;
        }
        if (!args[0].equals("demo") || !args[1].equals("send")) {
            printUsage();
            return;
        }

        String endpoint = "tls://front1-mtproto-api-rev2.actor.im";
        int argsOffset = 2;
        if (args[argsOffset].equals("-e")) {
            endpoint = args[argsOffset + 1];
            argsOffset += 2;
        }

        final long phoneNumber = Long.parseLong(args[argsOffset++]);
        final String activationCode = args[argsOffset++];
        final String destNumber = args[argsOffset++];
        final int count;
        if (args.length > argsOffset) {
            count = Integer.parseInt(args[argsOffset++]);
        } else {
            count = 1;
        }

        System.out.println("Starting Send Demo");
        System.out.println("From Account: " + phoneNumber + " with code " + activationCode);
        System.out.println("To Account: " + destNumber);
        System.out.println("Messages Count: " + (count >= 0 ? "" + count : "infinite"));
        System.out.println("With Endpoint: " + endpoint);
        System.out.println();

        startDemo(new DemoConfig(phoneNumber, activationCode, destNumber, count, endpoint));

        while (true) ;
    }

    private static void startDemo(final DemoConfig config) {
//        final Messenger messenger = CliConfiguration.createMessenger(config.getEndpoint());
//        System.out.print("Authenticating...");
//        messenger.requestStartPhoneAuth(config.getPhoneNumber()).start(new CommandCallback<AuthState>() {
//            @Override
//            public void onResult(AuthState res) {
//                messenger.validateCode(config.getActivationCode()).start(new CommandCallback<AuthState>() {
//                    @Override
//                    public void onResult(AuthState res) {
//                        System.out.println("Logged In.");
//                        demoSearchStep(messenger, config);
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        System.out.println("LogIn Error... Stopping demo.");
//                    }
//                });
//            }
//
//            @Override
//            public void onError(Exception e) {
//                System.out.println("LogIn Error. Stopping demo.");
//            }
//        });
    }
//
//    private static void demoSearchStep(final Messenger messenger, final DemoConfig config) {
//        System.out.print("Searching for " + config.getDestAccount() + "...");
//
//        messenger.findUsers(config.getDestAccount()).start(new CommandCallback<UserVM[]>() {
//            @Override
//            public void onResult(UserVM[] res) {
//                if (res.length == 0) {
//                    System.out.println("Unable to find user. Stopping Demo.");
//                } else {
//                    int uid = res[0].getId();
//                    System.out.println("Found User with uid #" + (uid));
//                    doSendStep(messenger, uid, config);
//                }
//            }
//
//            @Override
//            public void onError(Exception e) {
//                System.out.println("Unable to find user. Stopping Demo.");
//            }
//        });
//    }

//    private static void doSendStep(final Messenger messenger, int uid, final DemoConfig config) {
//        System.out.println("Sending messages...");
//
//        if (config.getCount() <= 0) {
//            int index = 0;
//            while (true) {
//                messenger.sendMessage(Peer.user(uid), "Test Message: " + index);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    return;
//                }
//            }
//        } else {
//            for (int i = 0; i < config.getCount(); i++) {
//                messenger.sendMessage(Peer.user(uid), "Test Message: " + i);
//            }
//        }
//
//        System.out.println("All messages are schedulled.");
//    }

    private static class DemoConfig {
        private long phoneNumber;
        private String activationCode;
        private String destAccount;
        private int count;
        private String endpoint;

        public DemoConfig(long phoneNumber, String activationCode, String destAccount, int count, String endpoint) {
            this.phoneNumber = phoneNumber;
            this.activationCode = activationCode;
            this.destAccount = destAccount;
            this.count = count;
            this.endpoint = endpoint;
        }

        public long getPhoneNumber() {
            return phoneNumber;
        }

        public String getActivationCode() {
            return activationCode;
        }

        public String getDestAccount() {
            return destAccount;
        }

        public int getCount() {
            return count;
        }

        public String getEndpoint() {
            return endpoint;
        }
    }
}
