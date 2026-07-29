package org.unitedlands.unitedchat.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.unitedlands.unitedchat.utils.Config;
import org.unitedlands.unitedchat.utils.Messages;

public class ChatMessageManager {

    public Component getMessage(Messages message) {
        return Config.get().getMessage(message.key);
    }

    public Component getMessage(Messages message, TagResolver.Single... resolvers) {
        return Config.get().getMessage(message.key, resolvers);
    }

}
