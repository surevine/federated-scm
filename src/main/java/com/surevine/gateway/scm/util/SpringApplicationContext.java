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
package com.surevine.gateway.scm.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Provides static access to Spring beans
 * @author nick.leaver@surevine.com
 */
public final class SpringApplicationContext implements ApplicationContextAware {
    private static ApplicationContext appContext;

    private SpringApplicationContext() {
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        appContext = applicationContext;
    }

    public static Object getBean(final String beanName) {
        return appContext.getBean(beanName);
    }
}
