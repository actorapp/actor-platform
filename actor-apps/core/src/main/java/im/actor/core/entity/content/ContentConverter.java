package im.actor.core.entity.content;

import im.actor.core.entity.content.internal.AbsContentContainer;

public interface ContentConverter {
    AbsContent convert(AbsContentContainer container);
}
