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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author nick.leaver@surevine.com
 */
public class StringUtilTest {
    @Test
    public void testCleanStringForFilePath() {
        String dirty1 = "\t   ThIS $§string NEEDS\n\n\tto be cleaned    \n\t*&&^%%";
        String expected1 = "this_string_needs_to_be_cleaned";
        
        String dirty2 = "/etc/passwd";
        String expected2 = "etcpasswd";

        String dirty3 = "scp /etc/secretfile root@127.0.0.1; useradd badguy";
        String expected3 = "scp_etcsecretfile_root127001_useradd_badguy";
        
        String dirty4 = "C:\\Program Files\\AdminProgram.exe";
        String expected4 = "cprogram_filesadminprogramexe";
        
        assertEquals(expected1, StringUtil.cleanStringForFilePath(dirty1));
        assertEquals(expected2, StringUtil.cleanStringForFilePath(dirty2));
        assertEquals(expected3, StringUtil.cleanStringForFilePath(dirty3));
        assertEquals(expected4, StringUtil.cleanStringForFilePath(dirty4));
    }
    
    @Test
    public void testIsClean() {
        String dirty1 = "has a\ttab";
        String dirty2 = "has a  tab and a \nnewline";
        String dirty3 = "h$s som£ ûñù§ual characters";
        
        String clean1 = "this is OK";
        String clean2 = "this_is_OK_too";
        String clean3 = "12345";
        String clean4 = " spaces are ok ";
        
        assertFalse(StringUtil.isClean(dirty1));
        assertFalse(StringUtil.isClean(dirty2));
        assertFalse(StringUtil.isClean(dirty3));
        
        assertTrue(StringUtil.isClean(clean1));
        assertTrue(StringUtil.isClean(clean2));
        assertTrue(StringUtil.isClean(clean3));
        assertTrue(StringUtil.isClean(clean4));
    }
}
