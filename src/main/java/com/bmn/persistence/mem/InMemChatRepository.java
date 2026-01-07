package com.bmn.persistence.mem;

import com.bmn.model.Message;
import com.bmn.persistence.MessageRepository;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;

@Repository
public class InMemChatRepository implements MessageRepository {

    private final LinkedList<Message> messages = new LinkedList<>();

    @Override
    public List<Message> getMessages(String contextId, int limit) {
        int length = messages.size();
        int offset = Math.max(0, length - limit);
        return messages.stream()
                .skip(offset)
                .limit(limit)
                .toList();
    }

    @Override
    public void pushMessage(Message message) {
        messages.addLast(message);
    }
}
