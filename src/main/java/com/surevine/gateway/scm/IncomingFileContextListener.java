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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 * Monitors a directory for incoming files and hands them off for processing
 * @author nick.leaver@surevine.com
 */
public class IncomingFileContextListener implements ServletContextListener {
    private Logger logger = Logger.getLogger(IncomingFileContextListener.class);
    private IncomingProcessor incomingProcessor;
    private static Thread fileImporter;

    public IncomingFileContextListener() {
        incomingProcessor = new IncomingProcessorImpl();
    }

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        try {
            final Path incomingDir = Paths.get(PropertyUtil.getGatewayImportDir());
            final WatchService watchService = FileSystems.getDefault().newWatchService();
            incomingDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            
            fileImporter = new Thread() {
                @Override
                public void run() {
                    logger.debug("Monitoring for new imported files from the gateway");
                    while (true) {
                        try {
                            WatchKey key = watchService.take();

                            if (key != null) {
                                for (WatchEvent event : key.pollEvents()) {
                                    if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                                        WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                                        Path filename = pathEvent.context();
                                        Path newFilePath = incomingDir.resolve(filename);

                                        if (Files.exists(newFilePath)) {
                                            // TODO examine, unpack, parse metadata, send to processor
                                            logger.debug("Processing file " + newFilePath);
                                        }
                                    }
                                }
                            }
                        } catch (InterruptedException ie) {
                            logger.debug("File watcher thread interrupted");
                        }
                    }
                }
            };
    
            fileImporter.start();
            logger.info("Gateway import listener initialised");
        } catch (Exception e) {
            logger.error("Error occurred while monitoring for incoming files", e);
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        logger.debug("Stopping monitoring for import files");
        fileImporter.interrupt();
    }
}
