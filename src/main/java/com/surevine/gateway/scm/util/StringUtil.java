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

/**
 * @author nick.leaver@surevine.com
 */
public final class StringUtil {
    private StringUtil() {
        // no-op
    }

    /**
     * Cleans up an input String for use in file paths by removing trailing and leading whitespace, removing any chars 
     * that aren't a-z, A-Z or 0-9, replacing whitespace with underscores, and dropping the string to lowercase.
     * @param dirty the dirty user input string
     * @return a cleaner string that can be used in file paths
     */
    public static String cleanStringForFilePath(final String dirty) {
        String clean = dirty;
        if (clean != null) {
            clean = clean.replaceAll("\\s+", " "); // collapse all whitespace (spaces used here instead of _ to allow trim)
            clean = clean.replaceAll("[^a-zA-Z0-9_ ]", ""); // remove any unwanted characters
            clean = clean.trim(); // remove any leading or trailing whitespace
            clean = clean.toLowerCase(); // drop to lower case
            clean = clean.replaceAll("\\s+", "_"); // replace any remaining mid-string spaces with underscores
        }
        return clean;
    }

    /**
     * Simple test to check if a String contains a-z, A-Z, 0-9 and spaces only. A restrictive and basic test 
     * but the expected input is mostly for use in file paths, arguments to other services, or as part of a URL.
     * @param dirty potentially dirty string
     * @return true if the string only contains a-z A-Z 0-9 spaces or underscores
     */
    public static boolean isClean(final String dirty) {
        return dirty == null || dirty.matches("[a-zA-Z0-9 _]+");
    }
}
