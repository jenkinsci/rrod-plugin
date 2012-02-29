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

import com.google.common.base.Preconditions;
import hudson.model.Hudson;
import hudson.model.Job;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang.Validate;

/**
 * @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>
 */
public abstract class Request {

    protected static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat(Messages.Request_dateFormat());
    protected String username;
    protected String project;
    protected String errorMessage;
    private String creationDate;

    public Request(String username, String project) {
        this.username = username;
        this.project = project;
        this.creationDate = DATE_FORMATER.format(new Date());
    }

    public String getProject() {
        return project;
    }

    public String getUsername() {
        return username;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public abstract String getMessage();

    public boolean process() {
        boolean success = false;

        Job job = (Job) Hudson.getInstance().getItem(project);

        if (job != null) {
            success = execute(job);
        } else {
            errorMessage = "The job " + project + " doesn't exist";
        }

        return success;
    }

    public abstract boolean execute(Job job);

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Request other = (Request) obj;
        if ((this.username == null) ? (other.username != null) : !this.username.equals(other.username)) {
            return false;
        }
        if ((this.project == null) ? (other.project != null) : !this.project.equals(other.project)) {
            return false;
        }
        return true;
    }
}
