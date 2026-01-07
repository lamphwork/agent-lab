package com.bmn.langgraph.node;

import com.bmn.langgraph.GraphCallback;

public interface ProcessNode<T, O> {

    String execute(T state, GraphCallback<O> callback);

}
