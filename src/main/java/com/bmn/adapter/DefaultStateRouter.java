package com.bmn.adapter;

import com.bmn.core.model.Context;
import com.bmn.core.port.StateRouter;

public class DefaultStateRouter implements StateRouter {

    @Override
    public Context.State route(Context ctx) {

        if (ctx.isUserCanceled()) return Context.State.USER_CANCELED;

        return switch (ctx.getState()) {

            case INTENT_DETECT -> switch (ctx.getIntent()) {
                case "reply" -> ctx.getResumState();
                case "new_task" -> Context.State.PLANNING;
                case "cancel" -> Context.State.USER_CANCELED;
                default -> Context.State.GREETING;
            };

            case GREETING -> Context.State.INTENT_DETECT;

            case PLANNING -> Context.State.EXECUTE_STEP;

            case EXECUTE_STEP -> {
                if (ctx.getCurrentStepIndex() >= ctx.getPlan().size()) {
                    yield Context.State.DONE_GOAL;
                }
                yield Context.State.EXECUTE_STEP;
            }

            case PAUSE_ASK_USER -> Context.State.PAUSE_ASK_USER;

            case DONE_GOAL, USER_CANCELED -> ctx.getState();

            case RESPONDER -> Context.State.DONE_GOAL;
        };
    }

}
