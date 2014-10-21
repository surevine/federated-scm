/*
 * Copyright (C) 2008-2014 Surevine Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package com.surevine.gateway.scm;

import com.surevine.gateway.scm.util.PropertyUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.text.SimpleDateFormat;
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
    private Distributor distributor;
    private static final String CONTEXT_KEY = "scmExporter";

    public FederateSCMContextListener() {
        distributor = new DistributorImpl();
    }

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        logger.info("Initialising SCM export timer");
        int interval = PropertyUtil.getExportInterval();
        boolean exportAtStart = PropertyUtil.isExportAtStart();
        Timer timer = new Timer();
        Calendar cal = Calendar.getInstance();
        
        if (!exportAtStart) {
            cal.add(Calendar.SECOND, interval);
        }
        
        Date start = cal.getTime();
        TimerTask federateTask = new TimerTask() {
            @Override
            public void run() {
                logger.info("Running SCM export");
                distributor.distributeAll();
            }
        };

        timer.scheduleAtFixedRate(federateTask, start, interval * 1000);
        servletContextEvent.getServletContext().setAttribute(CONTEXT_KEY, timer);
        
        if (exportAtStart) {
            logger.info("SCM export timer initialised");
        } else {
            String startTimeString = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(start);
            logger.info("SCM export timer initialised with delayed start. First export will run in " + interval + " seconds at " + startTimeString);
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        logger.info("Shutting down SCM export timer");
        Timer timer = (Timer) servletContextEvent.getServletContext().getAttribute(CONTEXT_KEY);

        if (timer != null) {
            timer.cancel();
        }

        servletContextEvent.getServletContext().removeAttribute(CONTEXT_KEY);
        logger.info("SCM export timer shutdown complete");
    }
}
