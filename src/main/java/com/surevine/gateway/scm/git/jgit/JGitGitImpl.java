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
package com.surevine.gateway.scm.git.jgit;

import com.surevine.gateway.scm.git.Git;
import com.surevine.gateway.scm.git.GitException;
import com.surevine.gateway.scm.util.PropertyUtil;
import org.eclipse.jgit.api.CloneCommand;

import java.io.File;

/**
 * @author nick.leaver@surevine.com
 */
public class JGitGitImpl extends Git {
    private File gitDirectory;
    
    public JGitGitImpl() {
        this.gitDirectory = PropertyUtil.getGitDir();
    }
    
    @Override
    public void clone(final String repoURI) throws GitException {
        CloneCommand cloneCommand = new CloneCommand();
        cloneCommand.setDirectory(gitDirectory);
        cloneCommand.setURI(repoURI);
        try {
            cloneCommand.call();
        } catch (Exception e) {
            throw new GitException(e);
        }
    }
}
