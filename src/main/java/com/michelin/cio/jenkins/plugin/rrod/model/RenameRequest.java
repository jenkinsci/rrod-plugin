/*
 * The MIT License
 *
 * Copyright (c) 2011-2012, Manufacture Francaise des Pneumatiques Michelin, Daniel Petisme
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.michelin.cio.jenkins.plugin.rrod.model;

import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a renaming request sent by a user to the administrator.
 * 
 * @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>
 */
public class RenameRequest extends Request {

    private String newName;

    public RenameRequest(String username, String project, String newName) {
        super(username, project);
        this.newName = newName;
    }

    @Override
    public String getMessage() {
        return Messages.RenameRequest_message(project, newName).toString();
    }

    public String getNewName() {
        return newName;
    }

    @Override
    public boolean execute(Job job) {
        boolean success = false;

        if (Hudson.getInstance().hasPermission(Item.DELETE) && Hudson.getInstance().hasPermission(Item.CREATE)) {
            try {
                job.renameTo(newName);
                success = true;
                LOGGER.log(Level.INFO, "The jobs {0} has been properly renamed in {1}", new Object[]{job.getName(), newName});
            } catch (IOException e) {
                errorMessage = e.getMessage();
                LOGGER.log(Level.SEVERE, "Impossible to rename the job " + job.getName(), e);
            } catch (IllegalArgumentException e) {
                errorMessage = e.getMessage();
                LOGGER.log(Level.SEVERE, "Impossible to rename the job " + job.getName(), e);
            }
        } else {
            errorMessage = "The current user " + username + " has no permission to rename the job";
            LOGGER.log(Level.FINE, "The current user {0} has no permission to RENAME the job", new Object[]{username});
        }

        return success;
    }
    private static final Logger LOGGER = Logger.getLogger(RenameRequest.class.getName());
}
