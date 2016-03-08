package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;

public class AuthStartRes {

    @NotNull
    @Property("readonly, nonatomic")
    private final String transactionHash;
    @NotNull
    @Property("readonly, nonatomic")
    private final AuthMode authMode;
    @Property("readonly, nonatomic")
    private final boolean isRegistered;

    public AuthStartRes(@NotNull String transactionHash, @NotNull AuthMode authMode, boolean isRegistered) {
        this.transactionHash = transactionHash;
        this.authMode = authMode;
        this.isRegistered = isRegistered;
    }

    @NotNull
    public String getTransactionHash() {
        return transactionHash;
    }

    @NotNull
    public AuthMode getAuthMode() {
        return authMode;
    }

    public boolean isRegistered() {
        return isRegistered;
    }
}
