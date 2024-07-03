package de.qStivi;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class ChatMessageTest {

    private GenericCommandInteractionEvent eventMock;
    private Message messageMock;

    @Before
    public void setUp() {
        eventMock = mock(GenericCommandInteractionEvent.class);
        messageMock = mock(Message.class);
        Mockito.when(eventMock.getHook()).thenReturn(mock(InteractionHook.class));
        Mockito.when(eventMock.getHook().retrieveOriginal()).thenReturn(mock(RestAction.class));
        Mockito.when(eventMock.getHook().retrieveOriginal().complete()).thenReturn(messageMock);
        Mockito.when(messageMock.editMessage(Mockito.anyString())).thenReturn(mock(MessageEditAction.class));
        Mockito.when(messageMock.delete()).thenReturn(mock(AuditableRestAction.class));
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
