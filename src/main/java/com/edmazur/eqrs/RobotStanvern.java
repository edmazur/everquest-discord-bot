package com.edmazur.eqrs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordUser;
import com.edmazur.eqrs.discord.listener.AnnouncementListener;
import com.edmazur.eqrs.discord.listener.TodListener;
import com.edmazur.eqrs.game.GameLog;
import com.edmazur.eqrs.game.GameLogEvent;
import com.edmazur.eqrs.game.RaidTargets;
import com.edmazur.eqrs.game.listener.FteListener;
import com.edmazur.eqrs.game.listener.GameLogListener;
import com.edmazur.eqrs.game.listener.HeartbeatListener;
import com.edmazur.eqrs.game.listener.RaidTargetSpawnListener;

public class RobotStanvern {

  private static final Logger LOGGER = new Logger();

  public static void main(String[] args) {

    Config config = new Config();
    Discord discord = new Discord(config);

    // Uncomment to send one-off images/messages.
//    File image = new File("/home/mazur/eclipse-workspace/RobotStanvern/img/angry-robot.gif");
//    if (image.exists()) {
//      discord.sendMessage(DiscordChannel.TOD, image);
//    }

    Database database = new Database(config);
    RaidTargets raidTargets = new RaidTargets(database);
    // TODO: Set this up more like how game log messages are received centrally
    // and passed out to listeners?
    new TodListener(discord, database, raidTargets);
    new AnnouncementListener(discord);

    List<GameLogListener> gameLogListeners = new ArrayList<>();

    // Add FTE listener.
    gameLogListeners.add(new FteListener(discord));

    // Add heartbeat listener.
    HeartbeatListener heartbeatListener = new HeartbeatListener(discord);
    ScheduledExecutorService scheduledExecutorService =
        Executors.newScheduledThreadPool(10);
    scheduledExecutorService.scheduleAtFixedRate(
        heartbeatListener, 1, 1, TimeUnit.SECONDS);
    gameLogListeners.add(heartbeatListener);

    // Add raid target spawn listener.
    RaidTargetSpawnListener raidTargetSpawnListener =
        new RaidTargetSpawnListener(discord);
    gameLogListeners.add(raidTargetSpawnListener);

    // Print configs for each listener.
    for (GameLogListener gameLogListener : gameLogListeners) {
      LOGGER.log("%s running (%s)",
          gameLogListener.getClass().getName(), gameLogListener.getConfig());
    }

    // Parse the log.
    // TODO: Automatically switch between logs as you change characters.
//    String character = "Stanvern";
    String character = "Holecreep";
    GameLog gameLog = new GameLog(character);
    LOGGER.log("Reading log from: " + character);
    for (GameLogEvent gameLogEvent : gameLog) {
      for (GameLogListener gameLogListener : gameLogListeners) {
        gameLogListener.onGameLogEvent(gameLogEvent);
      }
    }
  }

}