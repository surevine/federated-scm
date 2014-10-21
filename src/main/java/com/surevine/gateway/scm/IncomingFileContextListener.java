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

import com.surevine.gateway.scm.IncomingProcessor;
import com.surevine.gateway.scm.IncomingProcessorImpl;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Monitors a directory for incoming files and hands them off for processing
 * @author nick.leaver@surevine.com
 */
public class IncomingFileContextListener implements ServletContextListener {
    private Logger logger = Logger.getLogger(IncomingFileContextListener.class);
    private IncomingProcessor incomingProcessor;
    private static final String CONTEXT_KEY = "scmImportListener";
    
    public IncomingFileContextListener() {
        incomingProcessor = new IncomingProcessorImpl();
    }

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        // TODO
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        // TODO
    }
}
