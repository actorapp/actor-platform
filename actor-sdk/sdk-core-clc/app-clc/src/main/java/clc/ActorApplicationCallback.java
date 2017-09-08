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

public interface ActorApplicationCallback
{
    public void SignInFinished(ClcMessenger messenger);
    public void SignUpFinished(ClcMessenger messenger);
    public void Error(Exception e);
}