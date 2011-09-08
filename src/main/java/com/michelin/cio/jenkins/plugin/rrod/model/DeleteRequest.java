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

package com.michelin.cio.jenkins.plugin.rrod.model;

import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a deletion request sent by a user to Jenkins' administrator.
 * 
 * @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>
 */
public class DeleteRequest extends Request {

    public DeleteRequest(String username, String project) {
        super(username, project);
    }

    @Override
    public String getMessage() {
        return Messages.DeleteRequest_message(project).toString();
    }

    @Override
    public boolean process() {
        boolean success = false;

        if (Hudson.getInstance().hasPermission(Item.DELETE)) {
            Job job = (Job) Hudson.getInstance().getItem(project);
            try {
                job.delete();
                success = true;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to delete the job " + job.getName(), e);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Unable to delete the job " + job.getName(), e);
            }
        } else {
            LOGGER.log(Level.FINE, "The current user does not have the DELETE permission");
        }

        return success;
    }

    private static final Logger LOGGER = Logger.getLogger(DeleteRequest.class.getName());

}
