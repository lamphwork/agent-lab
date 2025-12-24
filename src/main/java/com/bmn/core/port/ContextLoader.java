package com.bmn.core.port;

import com.bmn.core.model.Context;

public interface ContextLoader {

    Context load(String id);

    void save(Context context);
}
