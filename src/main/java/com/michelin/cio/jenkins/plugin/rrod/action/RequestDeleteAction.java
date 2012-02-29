/*
 * The MIT License
 *
 * Copyright (c) 2011-2012, Manufacture Francaise des Pneumatiques Michelin, Daniel Petisme,
 * Romain Seguy
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

import hudson.Functions;
import java.util.logging.Level;
import hudson.model.Item;
import com.michelin.cio.jenkins.plugin.rrod.model.DeleteRequest;
import com.michelin.cio.jenkins.plugin.rrod.RequestRenameOrDeletePlugin;
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
 * Represents the "Ask for deletion" action appearing on a given project's page.
 * 
 * @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>
 */
public class RequestDeleteAction implements Action {

    private AbstractProject<?, ?> project;

    public RequestDeleteAction(AbstractProject<?, ?> project) {
        this.project = project;
    }

    public HttpResponse doCreateDeleteRequest(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
        if (isIconDisplayed()) {
            LOGGER.log(FINE, "Deletion request");

            final String username = request.getParameter("username");

            RequestRenameOrDeletePlugin plugin = Hudson.getInstance().getPlugin(RequestRenameOrDeletePlugin.class);
            plugin.addRequest(new DeleteRequest(username, project.getName()));
            LOGGER.log(Level.INFO, "The request to delete the jobs {0} has been sent to the administrator", project.getName());
        }

        return new HttpRedirect(request.getContextPath() + '/' + project.getUrl());
    }

    public String getDisplayName() {
        if (isIconDisplayed()) {
            return Messages.RequestDeleteAction_DisplayName().toString();
        }
        return null;
    }

    public String getIconFileName() {
        if (isIconDisplayed()) {
            return "/images/24x24/edit-delete.png";
        }
        return null;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }

    public String getUrlName() {
        return "request-delete";
    }

    /**
     * Displays the icon when the user can configure and !delete.
     */
    private boolean isIconDisplayed() {
        boolean isDisplayed = false;
        try {
            isDisplayed = hasConfigurePermission() && !hasDeletePermission();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Impossible to know if the icon has to be displayed", e);
        }

        return isDisplayed;
    }

    private boolean hasConfigurePermission() throws IOException, ServletException {
        return Functions.hasPermission(project, Item.CONFIGURE);
    }

    private boolean hasDeletePermission() throws IOException, ServletException {
        return Functions.hasPermission(project, Item.DELETE);
    }
    private static final Logger LOGGER = Logger.getLogger(RequestDeleteAction.class.getName());
}
