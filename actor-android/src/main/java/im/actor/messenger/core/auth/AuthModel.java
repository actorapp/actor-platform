package im.actor.messenger.core.auth;

import android.app.Application;
import android.content.Context;

import com.droidkit.actors.ActorRef;
import com.droidkit.engine.event.NotificationCenter;

import im.actor.messenger.core.Core;
import im.actor.messenger.storage.AuthStorage;
import im.actor.messenger.util.io.ContextPersistence;

import java.io.Serializable;

import static com.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 30.08.14.
 */
public class AuthModel {
    private AuthProcessState authProcessState;
    private ActorRef authActor;

    public AuthModel(Application context, AuthStorage storage) {
        this.authProcessState = new AuthProcessState(context);
        if (storage.isLoggedIn()) {
            authProcessState.forceLoggedIn();
        }
        this.authActor = system().actorOf(AuthActor.auth(this, storage));
    }

    public void requestCode(long phone) {
        authActor.send(new AuthActor.AuthRequestCode(phone));
    }

    public void sendCode(int code) {
        authActor.send(new AuthActor.AuthSendCode(code));
    }

    public void resetAuth() {
        authActor.send(new AuthActor.AuthReset());
    }

    public void resetCodeSend() {
        authActor.send(new AuthActor.ResetCodeSend());
    }

    public void tryAgainCodeSend() {
        authActor.send(new AuthActor.TryAgainCodeSend());
    }

    public void resetCodeRequest() {
        authActor.send(new AuthActor.ResetCodeRequest());
    }

    public void tryAgainCodeRequest() {
        authActor.send(new AuthActor.TryAgainCodeRequest());
    }

    public void sendSignUp(String name, String avatarPath) {
        authActor.send(new AuthActor.PerformSignup(name, avatarPath));
    }

    public void tryAgainSignup() {
        authActor.send(new AuthActor.TryAgainSignup());
    }

    public void resetSignup() {
        authActor.send(new AuthActor.ResetSignup());
    }

    public AuthProcessState getAuthProcessState() {
        return authProcessState;
    }

    public boolean isAuthorized() {
        return authProcessState.getStateId() == AuthProcessState.STATE_SIGNED;
    }

    /**
     * State for auth process
     */
    public static class AuthProcessState extends ContextPersistence implements Serializable {

        private static final long TIMEOUT = 60 * 60 * 1000; // 1 Hour

        public static final int STATE_START = 0;
        public static final int STATE_REQUESTING_SMS = 1;
        public static final int STATE_REQUESTING_SMS_ERROR = 2;
        public static final int STATE_REQUESTED_SMS = 3;
        public static final int STATE_CODE_SENDING = 4;
        public static final int STATE_CODE_SEND_ERROR = 5;
        public static final int STATE_SIGN_UP = 6;
        public static final int STATE_SIGNING = 8;
        public static final int STATE_SIGNING_ERROR = 9;
        public static final int STATE_SIGNED = 7;

        private String smsHash;
        private long phoneNumber;
        private int stateId;
        private int smsCode;
        private String name;
        private String avatarImage;
        private long saveTime;

        public AuthProcessState(Context context) {
            super(context);
            tryLoad();

            if (System.currentTimeMillis() < saveTime || System.currentTimeMillis() - saveTime > TIMEOUT) {
                if (stateId == STATE_SIGNED) {
                    return;
                }
                stateId = STATE_START;
                smsHash = null;
                phoneNumber = 0;
                smsCode = 0;
            } else {
                if (stateId == STATE_REQUESTING_SMS ||
                        stateId == STATE_REQUESTING_SMS_ERROR) {
                    stateId = STATE_START;
                } else if (stateId == STATE_CODE_SENDING ||
                        stateId == STATE_CODE_SEND_ERROR) {
                    stateId = STATE_REQUESTED_SMS;
                } else if (stateId == STATE_SIGNING_ERROR || stateId == STATE_SIGNING) {
                    stateId = STATE_SIGN_UP;
                }
            }
        }

        @Override
        public boolean trySave() {
            saveTime = System.currentTimeMillis();
            return super.trySave();
        }

        // For persistence
        public AuthProcessState() {
            super(null);
        }

        public long getPhoneNumber() {
            return phoneNumber;
        }

        public String getSmsHash() {
            return smsHash;
        }

        public int getStateId() {
            return stateId;
        }

        public void setSmsHash(String smsHash) {
            this.smsHash = smsHash;
            trySave();
        }

        public void setPhoneNumber(long phoneNumber) {
            this.phoneNumber = phoneNumber;
            trySave();
        }

        public int getSmsCode() {
            return smsCode;
        }

        public void setSmsCode(int smsCode) {
            this.smsCode = smsCode;
            trySave();
        }

        public void setStateId(int stateId) {
            this.stateId = stateId;
            trySave();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
            trySave();
        }

        public String getAvatarImage() {
            return avatarImage;
        }

        public void setAvatarImage(String avatarImage) {
            this.avatarImage = avatarImage;
            trySave();
        }

        public synchronized boolean changeStateError(int nState, Throwable t) {
            if (check(getStateId(), nState)) {
                setStateId(nState);
                NotificationCenter.getInstance().fireEvent(Core.AUTH_STATE, new Object[]{nState, t});
                return true;
            } else {
                return false;
            }
        }

        public synchronized boolean changeState(int nState) {
            if (check(getStateId(), nState)) {
                setStateId(nState);
                NotificationCenter.getInstance().fireEvent(Core.AUTH_STATE, new Object[]{nState});
                return true;
            } else {
                return false;
            }
        }

        private boolean check(int os, int ns) {
            if (ns == STATE_START) {
                return true;
            }
            if (ns == STATE_REQUESTING_SMS) {
                if (os == STATE_START || os == STATE_REQUESTING_SMS_ERROR) {
                    return true;
                }
            }
            if (ns == STATE_REQUESTING_SMS_ERROR) {
                if (os == STATE_REQUESTING_SMS) {
                    return true;
                }
            }
            if (ns == STATE_REQUESTED_SMS) {
                if (os == STATE_REQUESTING_SMS || os == STATE_CODE_SEND_ERROR) {
                    return true;
                }
            }
            if (ns == STATE_CODE_SENDING) {
                if (os == STATE_REQUESTED_SMS || os == STATE_CODE_SEND_ERROR) {
                    return true;
                }
            }

            if (ns == STATE_CODE_SEND_ERROR) {
                if (os == STATE_CODE_SENDING) {
                    return true;
                }
            }

            if (ns == STATE_SIGN_UP) {
                if (os == STATE_CODE_SENDING) {
                    return true;
                }
            }

            if (ns == STATE_SIGNING) {
                if (os == STATE_SIGN_UP || os == STATE_SIGNING_ERROR) {
                    return true;
                }
            }

            if (ns == STATE_SIGNING_ERROR) {
                if (os == STATE_SIGNING) {
                    return true;
                }
            }

            if (ns == STATE_SIGNED) {
                if (os == STATE_SIGNING || os == STATE_CODE_SENDING) {
                    return true;
                }
            }

            return false;
        }

        public void forceLoggedIn() {
            stateId = STATE_SIGNED;
            smsHash = null;
            phoneNumber = 0;
            smsCode = 0;
        }
    }
}