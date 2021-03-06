// $Id: Task.java,v 1.14 2006-02-22 22:47:51 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center 
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center 
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.mskcc.pathdb.task;


/**
 * Wrapper for Background Threads.
 *
 * @author Ethan Cerami
 */
public abstract class Task extends Thread {
    private String taskName;
    private Throwable throwable;
    private ProgressMonitor pMonitor;

    /**
     * Constructor.
     *
     * @param taskName    Task Name.
     * @param consoleFlag Console Mode Flag.
     */
    public Task(String taskName, boolean consoleFlag) {
        GlobalTaskList taskList = GlobalTaskList.getInstance();
        taskList.addTask(this);
        this.taskName = taskName;
        this.pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(consoleFlag);
    }

    /**
     * Gets the Task Name.
     *
     * @return Task Name.
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Detects if an error has occurred.
     *
     * @return true or false.
     */
    public boolean errorOccurred() {
        return (throwable != null) ? true : false;
    }

    /**
     * Gets the Error Message.
     *
     * @return Error Message.
     */
    public String getErrorMessage() {
        return throwable.getMessage();
    }

    /**
     * Gets the Throwable Object.
     *
     * @return throwable Object.
     */
    public Throwable getThrowable() {
        return this.throwable;
    }

    /**
     * Sets the Throwable Object.
     *
     * @param e Exception Object.
     */
    public void setThrowable(Throwable e) {
        this.throwable = e;
    }

    /**
     * Gets the Progress Monitor.
     *
     * @return Progress Monitor object.
     */
    public ProgressMonitor getProgressMonitor() {
        return pMonitor;
    }
}
