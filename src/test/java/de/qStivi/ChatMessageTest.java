package de.qStivi;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;

public class ChatMessageTest {

    private GenericCommandInteractionEvent eventMock;
    private Message messageMock;

    @Before
    public void setUp() {
        eventMock = Mockito.mock(GenericCommandInteractionEvent.class);
        messageMock = Mockito.mock(Message.class);
        Mockito.when(eventMock.getHook()).thenReturn(Mockito.mock(InteractionHook.class));
        Mockito.when(eventMock.getHook().retrieveOriginal()).thenReturn(Mockito.mock(RestAction.class));
        Mockito.when(eventMock.getHook().retrieveOriginal().complete()).thenReturn(messageMock);
    }

    @Test
    public void testGetInstance() {
        ChatMessage instance = ChatMessage.getInstance(eventMock);
        assertNotNull(instance);
    }

    @Test(expected = IllegalStateException.class)
    public void testEditWithoutInstance() {
        ChatMessage instance = ChatMessage.getInstance();
        instance.edit("Test message");
    }

    @Test
    public void testEditWithInstance() {
        ChatMessage.getInstance(eventMock);
        ChatMessage.getInstance().edit("Test message");
        Mockito.verify(messageMock).editMessage("Test message");
    }

    @Test(expected = IllegalStateException.class)
    public void testDeleteWithoutInstance() {
        ChatMessage instance = ChatMessage.getInstance();
        instance.delete();
    }

    @Test
    public void testDeleteWithInstance() {
        ChatMessage.getInstance(eventMock);
        ChatMessage.getInstance().delete();
        Mockito.verify(messageMock).delete();
    }
}
