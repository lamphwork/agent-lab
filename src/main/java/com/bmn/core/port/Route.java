package com.bmn.core.port;

import com.bmn.core.model.Context;

@FunctionalInterface
public interface Route {

    Context.State route(Context context);
}
