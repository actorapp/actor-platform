package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;

public class AuthCodeRes {

    @NotNull
    @Property("readonly, nonatomic")
    private String transactionHash;
    @NotNull
    @Property("readonly, nonatomic")
    private AuthRes result;
    @Property("readonly, nonatomic")
    private boolean needToSignup;

    public AuthCodeRes(@NotNull AuthRes result) {
        this.result = result;
        this.needToSignup = false;
    }

    public AuthCodeRes(@NotNull String transactionHash) {
        this.transactionHash = transactionHash;
        this.needToSignup = true;
    }

    @NotNull
    public String getTransactionHash() {
        return transactionHash;
    }

    @NotNull
    public AuthRes getResult() {
        return result;
    }

    public boolean isNeedToSignup() {
        return needToSignup;
    }
}
