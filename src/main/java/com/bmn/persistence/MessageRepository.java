package com.bmn.persistence;

import com.bmn.model.Message;

import java.util.List;

public interface MessageRepository {

    List<Message> getMessages(String contextId, int limit);

    void pushMessage(Message message);
}
