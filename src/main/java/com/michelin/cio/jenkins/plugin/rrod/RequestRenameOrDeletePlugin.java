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
package com.michelin.cio.jenkins.plugin.rrod;

import com.michelin.cio.jenkins.plugin.rrod.model.DeleteRequest;
import com.michelin.cio.jenkins.plugin.rrod.model.RenameRequest;
import com.michelin.cio.jenkins.plugin.rrod.model.Request;
import hudson.Extension;
import hudson.Plugin;
import hudson.model.Hudson;
import hudson.model.ManagementLink;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.list.SetUniqueList;

/**
 * Manages pending renaming and deletion requests.
 *
 * @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>
 */
public class RequestRenameOrDeletePlugin extends Plugin {

    /**
     * The requests are unique (to avoid duplication problems like delete a job alreasy deleted)
     */
    private List<Request> requests = new ArrayList<Request>();
    
    private transient List<String> errors = new ArrayList<String>();

    public void addRequest(final Request request) {
        boolean alreadyRequested = CollectionUtils.exists(requests, new Predicate() {

            public boolean evaluate(Object object) {
                return request.equals(object);
            }
        });

        if (!alreadyRequested) {
            requests.add(request);
            persistPendingRequests();
        }
    }

    public HttpResponse doManageRequests(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
        Hudson.getInstance().checkPermission(Hudson.ADMINISTER);

        errors.clear();

        String[] selectedRequests = request.getParameterValues("selected");

        //Store the request once they have been applied
        List<Request> requestsToRemove = new ArrayList<Request>();

        if (selectedRequests != null && selectedRequests.length > 0) {
            for (String sindex : selectedRequests) {
                if (StringUtils.isNotBlank(sindex)) {
                    int index = Integer.parseInt(sindex);
                    Request currentRequest = requests.get(index);

                    if (StringUtils.isNotBlank(request.getParameter("apply"))) {

                        if (currentRequest.process()) {
                            //Store to remove
                            requestsToRemove.add(currentRequest);
                        } else {
                            errors.add(currentRequest.getErrorMessage());
                            LOGGER.log(Level.WARNING, "The request \"{0}\" can not be processed", currentRequest.getMessage());
                        }
                    } else {
                        requestsToRemove.add(currentRequest);
                        LOGGER.log(Level.INFO, "The request \"{0}\" has been discarded", currentRequest.getMessage());
                    }

                } else {
                    LOGGER.log(Level.WARNING, "The request index is not defined");
                }
            }
        } else {
            LOGGER.log(Level.FINE, "Nothing selected");
        }

        //Once it has done thr work, it removes the applied requests
        if (!requestsToRemove.isEmpty()) {
            removeAllRequests(requestsToRemove);
        }
        
        return new HttpRedirect(".");
    }

    public List<Request> getRequests() {
        return requests;
    }

    public List<String> getErrors() {
        return errors;
    }

    private void persistPendingRequests() {
        try {
            save();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to persist the pending requests");
        }
    }

    public void removeAllRequests(List<Request> requestsToRemove) {
        requests.removeAll(requestsToRemove);
        persistPendingRequests();
    }

    @Override
    public void start() throws Exception {
        super.start();

        Hudson.XSTREAM.alias("RenameRequest", RenameRequest.class);
        Hudson.XSTREAM.alias("DeleteRequest", DeleteRequest.class);
        Hudson.XSTREAM.alias("RequestRenameOrDeletePlugin", RequestRenameOrDeletePlugin.class);

        load();
    }

    @Extension
    public static final class RequestRenameOrDeleteManagementLink extends ManagementLink {

        @Override
        public String getDescription() {
            return Messages.RequestRenameOrDeleteManagementLink_Description().toString();
        }

        @Override
        public String getIconFileName() {
            return "/images/48x48/clipboard.png";
        }

        public String getDisplayName() {
            return Messages.RequestRenameOrDeleteManagementLink_DisplayName().toString();
        }

        @Override
        public String getUrlName() {
            return "plugin/rrod";
        }
    }
    private static final Logger LOGGER = Logger.getLogger(RequestRenameOrDeletePlugin.class.getName());
}
