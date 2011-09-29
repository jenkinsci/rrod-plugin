/*
 * The MIT License
 *
 * Copyright (c) 2011, Manufacture Francaise des Pneumatiques Michelin, Daniel Petisme
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

package com.michelin.cio.jenkins.plugin.rrod.action;

import hudson.model.Item;
import com.michelin.cio.jenkins.plugin.rrod.RequestRenameOrDeletePlugin;
import com.michelin.cio.jenkins.plugin.rrod.model.RenameRequest;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Hudson;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;

/**
 * Represents the "Ask for renaming" action appearing on a given project's page.
 * 
 * @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>
 */
public class RequestRenameAction implements Action {

    private AbstractProject<?, ?> project;

    public RequestRenameAction(AbstractProject<?, ?> project) {
        this.project = project;
    }

    public HttpResponse doCreateRenameRequest(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
        Hudson.getInstance().checkPermission(Item.CONFIGURE);
        LOGGER.log(FINE, "Renaming request");

        final String newName = request.getParameter("new-name");
        final String username = request.getParameter("username");

        RequestRenameOrDeletePlugin plugin = Hudson.getInstance().getPlugin(RequestRenameOrDeletePlugin.class);
        plugin.addRequest(new RenameRequest(username, project.getName(), newName));

        return new HttpRedirect(request.getContextPath() + '/' + project.getUrl());
    }

    public String getDisplayName() {
        if (isIconDisplayed()) {
            return Messages.RequestRenameAction_DisplayName().toString();
        }
        return null;
    }

    public String getIconFileName() {
        if (isIconDisplayed()) {
            return "/images/24x24/setting.png";
        }
        return null;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }

    public String getUrlName() {
        return "request-rename";
    }

    /*
     * Permission computing
     *  1: The user has the permission
     *  0: The user has not the permission
     *  *: it doesn't matter
     * 
     *           
     * Create    | 1 | 0 | 0 | 
     * Delete    | 0 | 1 | 0 |
     * Configure | * | * | 1 |
     * 
     * So display the icon whenthe user can:
     * Create AND !Delete OR
     * Delet AND !Create OR
     * Configure AND !Create AND !Delete
     */
    private boolean isIconDisplayed() {
        return (hasConfigurePermission() && !(hasCreatePermission() && hasDeletePermission()))
                || (hasCreatePermission() && !hasDeletePermission())
                || (!hasCreatePermission() && hasDeletePermission());
    }

    private boolean hasConfigurePermission() {
        return Hudson.getInstance().hasPermission(Item.CONFIGURE);
    }

    private boolean hasCreatePermission() {
        return Hudson.getInstance().hasPermission(Item.CREATE);
    }

    private boolean hasDeletePermission() {
        return Hudson.getInstance().hasPermission(Item.DELETE);
    }
    
    private static final Logger LOGGER = Logger.getLogger(RequestRenameAction.class.getName());

}
