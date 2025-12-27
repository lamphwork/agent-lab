package com.bmn.core.nodes;

import com.bmn.core.model.Context;
import com.bmn.core.model.LLMMessage;
import com.bmn.core.model.ToolInfo;
import com.bmn.core.port.AgentCallback;
import com.bmn.core.port.ExecuteNode;

import java.util.List;

public class ToolExecuteNode implements ExecuteNode {

    @Override
    public Context execute(Context context, AgentCallback callback) {
        List<ToolInfo> tools = context.getToolsToExecute();
        if (tools == null) {
            System.out.println("Tools empty");
            return context;
        }


        for (ToolInfo tool : tools) {
            System.out.println("Executing tool: " + tool.name());
            System.out.println("Params: " + tool.args());
            String result = "Done";
            context.getHistories().add(LLMMessage.toolMessage(result, tool));
        }

        return context;
    }
}
