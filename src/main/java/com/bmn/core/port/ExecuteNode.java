package com.bmn.core.port;

import com.bmn.core.model.Context;

@FunctionalInterface
public interface ExecuteNode {

    Context execute(Context context, AgentCallback callback);
}
