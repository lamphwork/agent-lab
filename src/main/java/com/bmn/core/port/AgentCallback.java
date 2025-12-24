package com.bmn.core.port;

import com.bmn.core.model.AgentOutput;

public interface AgentCallback {

    void next(AgentOutput output);
}
