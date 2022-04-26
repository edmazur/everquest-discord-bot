package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameTodDetector {

  private static final Pattern GUILD_CHAT_PATTERN =
      Pattern.compile("(?:\\p{Alpha}+ tells the guild|You say to your guild), '(.+)'");

  public boolean containsTod(EqLogEvent eqLogEvent) {
    Matcher matcher = GUILD_CHAT_PATTERN.matcher(eqLogEvent.getPayload());
    if (!matcher.matches()) {
      return false;
    }
    String guildChatText = matcher.group(1).toLowerCase();
    return guildChatText.contains("tod") && !guildChatText.contains("today");
  }

}
