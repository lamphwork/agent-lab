package com.bmn.adapter;

import com.bmn.core.model.Context;
import com.bmn.core.port.ContextLoader;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class InMemContextLoader implements ContextLoader {

    private final Map<String, Context> contextMap = new HashMap<String, Context>();

    @Override
    public Context load(String id) {
        return contextMap.get(id);
    }

    @Override
    public void save(Context context) {
        contextMap.put(context.getId(), context);
    }
}
