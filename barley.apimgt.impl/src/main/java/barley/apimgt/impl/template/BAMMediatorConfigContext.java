package barley.apimgt.impl.template;

import org.apache.velocity.VelocityContext;

import barley.apimgt.api.model.API;
import barley.apimgt.impl.utils.APIUtil;

public class BAMMediatorConfigContext extends ConfigContextDecorator {

    private API api;

    public BAMMediatorConfigContext(ConfigContext context, API api) {
        super(context);
        this.api = api;
    }

    public VelocityContext getContext() {
        VelocityContext context = super.getContext();
        boolean enabledStats = APIUtil.isAnalyticsEnabled();
        if (enabledStats) {
            context.put("statsEnabled", Boolean.TRUE);
        } else {
            context.put("statsEnabled", Boolean.FALSE);
        }

        return context;
    }

}
