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

import com.surevine.gateway.scm.api.Distributor;
import com.surevine.gateway.scm.api.impl.DistributorImpl;
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
    private Distributor distributor;
    private static final String CONTEXT_KEY = "scmExporter";

    public FederateSCMContextListener() {
        distributor = new DistributorImpl();
    }

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        logger.info("Initialising SCM export timer");
        Timer timer = new Timer();
        Calendar cal = Calendar.getInstance();
        Date start = cal.getTime();
        TimerTask federateTask = new TimerTask() {
            @Override
            public void run() {
                logger.info("Running SCM export");
                distributor.distributeAll();
            }
        };

        timer.scheduleAtFixedRate(federateTask, start, PropertyUtil.getExportInterval() * 1000);
        servletContextEvent.getServletContext().setAttribute(CONTEXT_KEY, timer);
        logger.info("SCM export timer initialised");
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
