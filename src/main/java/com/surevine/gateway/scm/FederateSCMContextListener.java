package com.surevine.gateway.scm;

import com.surevine.gateway.scm.util.PropertyUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Exports SCM project updates at regular intervals
 * @author nick.leaver@surevine.com
 */
public class FederateSCMContextListener implements ServletContextListener {
    private Logger logger = Logger.getLogger(FederateSCMContextListener.class);
    private static final String CONTEXT_KEY = "scmExporter";

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("Initialising SCM export timer");
        Timer timer = new Timer();
        Calendar cal = Calendar.getInstance();
        Date start = cal.getTime();
        TimerTask federateTask = new TimerTask() {
            @Override
            public void run() {
                logger.debug("Running SCM export");
                
            }
        };

        timer.scheduleAtFixedRate(federateTask, start, PropertyUtil.getExportInterval());
        servletContextEvent.getServletContext().setAttribute (CONTEXT_KEY, timer);
        logger.info("SCM export timer initialised");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("Shutting down SCM export timer");
        Timer timer = (Timer) servletContextEvent.getServletContext().getAttribute(CONTEXT_KEY);

        if (timer != null) {
            timer.cancel();
        }

        servletContextEvent.getServletContext().removeAttribute(CONTEXT_KEY);
        logger.info("SCM export timer shutdown complete");
    }
}
