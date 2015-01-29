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
public final class InputValidator {

	private static final String VALID_CHARACTERS = "[^-_a-zA-Z0-9]+$";

    private InputValidator() {
        // no-op
    }

    public static boolean partnerNameIsValid(final String partnerName) {
    	return partnerName != null && partnerName.matches(VALID_CHARACTERS);
    }

    public static boolean projectKeyIsValid(final String projectKey) {
        return projectKey != null && projectKey.matches(VALID_CHARACTERS);
    }

    public static boolean repoSlugIsValid(final String repoSlug) {
        return repoSlug != null && repoSlug.matches(VALID_CHARACTERS);
    }
}
