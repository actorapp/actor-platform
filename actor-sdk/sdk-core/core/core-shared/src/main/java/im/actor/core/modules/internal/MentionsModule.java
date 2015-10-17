/*
 * Copyright (C) 2014-2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import im.actor.core.entity.Group;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.MentionFilterResult;
import im.actor.core.entity.User;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.util.StringMatch;
import im.actor.core.util.StringMatcher;

public class MentionsModule extends AbsModule {

    private static final int SEARCH_LIMIT = 30;

    public MentionsModule(ModuleContext context) {
        super(context);
    }

    public List<MentionFilterResult> findMentions(int gid, String query) {
        query = query.trim().toLowerCase();

        ArrayList<MentionFilterResult> results = new ArrayList<MentionFilterResult>();
        final Group group = groups().getValue(gid);
        GroupMember[] members = group.getMembers().toArray(new GroupMember[group.getMembers().size()]);
        Arrays.sort(members, new Comparator<GroupMember>() {
            @Override
            public int compare(GroupMember a, GroupMember b) {
                User ua = users().getValue(a.getUid());
                User ub = users().getValue(b.getUid());
                return ua.getName().compareToIgnoreCase(ub.getName());
            }
        });

        for (GroupMember member : members) {
            if (member.getUid() == myUid()) {
                continue;
            }
            
            User user = users().getValue(member.getUid());

            boolean isNick = user.getNick() != null;
            String mention;
            String secondName;

            if (isNick) {
                mention = user.getNick();
                secondName = user.getName();
            } else {
                if (user.getLocalName() != null) {
                    mention = user.getServerName();
                    secondName = user.getLocalName();
                } else {
                    mention = user.getName();
                    secondName = null;
                }
            }

            if (query.length() == 0) {
                results.add(new MentionFilterResult(user.getUid(),
                        user.getAvatar(),
                        isNick ? "@" + mention : mention,
                        new ArrayList<StringMatch>(),
                        secondName,
                        new ArrayList<StringMatch>(), isNick));
            } else {
                List<StringMatch> mentionMatches = StringMatcher.findMatches(mention, query);
                if (secondName != null) {
                    List<StringMatch> secondNameMatches = StringMatcher.findMatches(secondName, query);
                    if (mentionMatches.size() > 0 || secondNameMatches.size() > 0) {
                        if (isNick) {
                            List<StringMatch> nickMatches = new ArrayList<StringMatch>();
                            for (StringMatch m : mentionMatches) {
                                nickMatches.add(new StringMatch(m.getStart() + 1, m.getLength()));
                            }
                            mentionMatches = nickMatches;
                        }
                        results.add(new MentionFilterResult(user.getUid(),
                                user.getAvatar(),
                                isNick ? "@" + mention : mention,
                                mentionMatches,
                                secondName,
                                secondNameMatches, isNick));
                    }
                } else {
                    if (mentionMatches.size() > 0) {
                        results.add(new MentionFilterResult(user.getUid(),
                                user.getAvatar(),
                                mention,
                                mentionMatches,
                                null,
                                null, false));
                    }
                }
            }
        }

        if (results.size() > SEARCH_LIMIT) {
            results.clear();
        }

        return results;
    }
}