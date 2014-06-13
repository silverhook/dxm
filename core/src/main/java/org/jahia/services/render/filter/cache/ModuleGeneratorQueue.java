/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.render.filter.cache;

import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * @author : rincevent
 * @since JAHIA 6.5
 * Created : 12 oct. 2010
 */
public class ModuleGeneratorQueue implements InitializingBean {
    private Map<String, String> notCacheableModule = new ConcurrentHashMap<String, String>(2503);
    private Map<String, CountDownLatch> generatingModules;
    private int maxModulesToGenerateInParallel = 50;
    private long moduleGenerationWaitTime = 5000;
    private volatile Semaphore availableProcessings = null;
    private long minimumIntervalAfterLastAutoThreadDump = 60000; // in milliseconds
    private boolean useLatchOnlyForPages = false;
    private boolean threadDumpToSystemOut = true;
    private boolean threadDumpToFile = true;

    public Map<String, String> getNotCacheableModule() {
        return notCacheableModule;
    }

    public Map<String, CountDownLatch> getGeneratingModules() {
        return generatingModules;
    }

    public Semaphore getAvailableProcessings() {
        // Double-checked locking only works with volatile for Java 5+
        // result variable is used to avoid accessing the volatile field multiple times to increase performance per Effective Java 2nd Ed.
        Semaphore result = availableProcessings;
        if (result == null) {
            synchronized (this) {
                result = availableProcessings;
                if (result == null) {
                    availableProcessings = result = new Semaphore(getMaxModulesToGenerateInParallel(), true);
                }
            }
        }
        return result;
    }

    public int getMaxModulesToGenerateInParallel() {
        return maxModulesToGenerateInParallel;
    }

    public long getModuleGenerationWaitTime() {
        return moduleGenerationWaitTime;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        generatingModules = new HashMap<String, CountDownLatch>(maxModulesToGenerateInParallel);
    }

    public void setMaxModulesToGenerateInParallel(int maxModulesToGenerateInParallel) {
        this.maxModulesToGenerateInParallel = maxModulesToGenerateInParallel;
        availableProcessings = null;
    }

    public void setModuleGenerationWaitTime(long moduleGenerationWaitTime) {
        this.moduleGenerationWaitTime = moduleGenerationWaitTime;
    }

    public void setMinimumIntervalAfterLastAutoThreadDump(long minimumIntervalAfterLastAutoThreadDump) {
        this.minimumIntervalAfterLastAutoThreadDump = minimumIntervalAfterLastAutoThreadDump;
    }

    public long getMinimumIntervalAfterLastAutoThreadDump() {
        return minimumIntervalAfterLastAutoThreadDump;
    }

    public boolean isThreadDumpToSystemOut() {
        return threadDumpToSystemOut;
    }

    public void setThreadDumpToSystemOut(boolean threadDumpToSystemOut) {
        this.threadDumpToSystemOut = threadDumpToSystemOut;
    }

    public boolean isThreadDumpToFile() {
        return threadDumpToFile;
    }

    public void setThreadDumpToFile(boolean threadDumpToFile) {
        this.threadDumpToFile = threadDumpToFile;
    }
}
