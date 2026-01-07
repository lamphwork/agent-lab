package com.bmn.langgraph;

import com.bmn.langgraph.context.State;
import com.bmn.langgraph.node.ProcessNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public abstract class StateGraph<T extends State, O> {

    protected final Map<String, ProcessNode<T, O>> nodes;

    public StateGraph(Map<String, ProcessNode<T, O>> nodes) {
        this.nodes = nodes;
    }

    /**
     * process by context
     *
     * @param context context
     */
    public synchronized void process(T context, GraphCallback<O> callback) {
        while (!shouldStop(context)) {
            String node = getNextNode(context);
            log.info("Processing node {}", node);

            ProcessNode<T, O> nextNode = nodes.get(node);
            if (nextNode == null) {
                throw new IllegalStateException("node " + node + " not found");
            }

            context.setCurrentNode(node);
            nextNode.execute(context, callback);
        }
    }

    /**
     * define what next based on current context
     *
     * @param context current context
     * @return next node
     */
    abstract String getNextNode(T context);

    /**
     * graph should stop (human in the loop, cancel, ...)
     *
     * @param context current context
     * @return stop or not
     */
    abstract boolean shouldStop(T context);
}
