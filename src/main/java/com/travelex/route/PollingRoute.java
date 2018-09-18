package com.travelex.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.log4j.Logger;

public class PollingRoute extends RouteBuilder {

    private static final Logger LOGGER = Logger.getLogger(PollingRoute.class);

    @Override
    public void configure() throws Exception {
        LOGGER.info("In Polling route");

        onException(Throwable.class).handled(true).to("direct:error");

        // Main route
        from("{{pollCurrentStatusTimerEndpoint}}").routeId("pollingRoute")
                        .to("bean:pollingServiceDao?cache=true&method=read").split(body())
                        .parallelProcessing().executorServiceRef("splitterThreadPoolProfile")
                        .to("bean:duedilService?method=checkStatus");

        // Error route
        from("direct:error").process(new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                Exception ex = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                LOGGER.error("Error occurred: " + ex);
            }
        });


    }


}
