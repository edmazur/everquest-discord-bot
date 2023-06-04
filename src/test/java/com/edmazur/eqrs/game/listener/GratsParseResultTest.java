package com.edmazur.eqrs.game.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.FakeMessageBuilder;
import com.edmazur.eqrs.ValueOrError;
import com.edmazur.eqrs.game.Item;
import com.edmazur.eqrs.game.ItemDatabase;
import java.io.File;
import java.util.List;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

class GratsParseResultTest {

  private static final EqLogEvent EQ_LOG_EVENT = EqLogEvent.parseFromLine(
      "[Wed May 24 23:00:41 2023] Veriasse tells the guild, '!grats Resplendent Robe Veriasse 69'")
      .get();
  private static final String ITEM_NAME = "Resplendent Robe";
  private static final long CHANNEL_ID = 123;
  private static final File FILE = new File("somefile");

  private ItemDatabase itemDatabase;
  private List<Item> items;

  @BeforeEach
  void beforeEach() {
    MockitoAnnotations.openMocks(this);

    itemDatabase = new ItemDatabase();
    itemDatabase.initialize();
    items = itemDatabase.parse(ITEM_NAME);
  }

  @Test
  void prepareForCreate() {
    GratsParseResult gratsParseResult = new GratsParseResult(
        EQ_LOG_EVENT,
        items,
        ValueOrError.value("(loot command)"),
        ValueOrError.value(CHANNEL_ID),
        List.of(ValueOrError.value(FILE)));
    FakeMessageBuilder fakeMessageBuilder = new FakeMessageBuilder();
    assertEquals(
        "💰 ET: `[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'`\n"
        + "✅ **$loot command**: `(loot command)`\n"
        + "✅ **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n",
        gratsParseResult.prepareForCreate(fakeMessageBuilder).getStringBuilder().toString());
    assertEquals(FILE, fakeMessageBuilder.getAttachment());
  }

  @Test
  void prepareForCreate_lootCommandError() {
    GratsParseResult gratsParseResult = new GratsParseResult(
        EQ_LOG_EVENT,
        items,
        ValueOrError.error("(loot command error)"),
        ValueOrError.value(CHANNEL_ID),
        List.of(ValueOrError.value(FILE)));
    FakeMessageBuilder fakeMessageBuilder = new FakeMessageBuilder();
    assertEquals(
        "💰 ET: `[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'`\n"
        + "❌ **$loot command**: (loot command error)\n"
        + "✅ **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n",
        gratsParseResult.prepareForCreate(fakeMessageBuilder).getStringBuilder().toString());
    assertEquals(FILE, fakeMessageBuilder.getAttachment());
  }

  @Test
  void prepareForCreate_channelMatchError() {
    GratsParseResult gratsParseResult = new GratsParseResult(
        EQ_LOG_EVENT,
        items,
        ValueOrError.value("(loot command)"),
        ValueOrError.error("(channel match error)"),
        List.of(ValueOrError.value(FILE)));
    FakeMessageBuilder fakeMessageBuilder = new FakeMessageBuilder();
    assertEquals(
        "💰 ET: `[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'`\n"
        + "✅ **$loot command**: `(loot command)`\n"
        + "❌ **Channel match**: (channel match error)\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n",
        gratsParseResult.prepareForCreate(fakeMessageBuilder).getStringBuilder().toString());
    assertEquals(FILE, fakeMessageBuilder.getAttachment());
  }

  @Test
  void prepareForCreate_missingScreenshot() {
    GratsParseResult gratsParseResult = new GratsParseResult(
        EQ_LOG_EVENT,
        items,
        ValueOrError.value("(loot command)"),
        ValueOrError.value(CHANNEL_ID),
        List.of(ValueOrError.error("(item screenshot error)")));
    FakeMessageBuilder fakeMessageBuilder = new FakeMessageBuilder();
    assertEquals(
        "💰 ET: `[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'`\n"
        + "✅ **$loot command**: `(loot command)`\n"
        + "✅ **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n"
        + "(item screenshot error)\n",
        gratsParseResult.prepareForCreate(fakeMessageBuilder).getStringBuilder().toString());
    assertNull(fakeMessageBuilder.getAttachment());
  }

  @Test
  void fromMessage() {
    runFromMessageTest(
        "💰 ET: `[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'`\n"
        + "✅ **$loot command**: `(loot command)`\n"
        + "✅ **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe", true);
  }

  @Test
  void fromMessage_lootCommandError() {
    runFromMessageTest(
        "💰 ET: `[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'`\n"
        + "❌ **$loot command**: (loot command error)\n"
        + "✅ **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe", true);
  }

  @Test
  void fromMessage_channelMatchError() {
    runFromMessageTest(
        "💰 ET: `[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'`\n"
        + "✅ **$loot command**: `(loot command)`\n"
        + "❌ **Channel match**: (channel match error)\n"
        + "https://wiki.project1999.com/Resplendent_Robe", true);
  }

  @Test
  void fromMessage_missingScreenshot() {
    runFromMessageTest(
        "💰 ET: `[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'`\n"
        + "✅ **$loot command**: `(loot command)`\n"
        + "✅ **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n"
        + "(item screenshot error)", false);
  }

  private void runFromMessageTest(String message, boolean hasAttachment) {
    Message mockMessage = mock(Message.class);
    when(mockMessage.getContent()).thenReturn(message);
    when(mockMessage.getAttachments())
        .thenReturn(hasAttachment ? List.of(mock(MessageAttachment.class)) : List.of());
    GratsParseResult gratsParseResult =
        GratsParseResult.fromMessage(mockMessage, itemDatabase).get();
    FakeMessageBuilder fakeMessageBuilder = new FakeMessageBuilder();
    assertEquals(
        message + "\n",
        gratsParseResult.prepareForCreate(fakeMessageBuilder).getStringBuilder().toString());
  }

}
