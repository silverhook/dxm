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
package org.jahia.services.channels.providers;

import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelProvider;
import org.jahia.services.channels.ChannelService;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A basic channel provider configured through Spring files
 */
public class UserAgentChannelProvider implements ChannelProvider, InitializingBean, BeanNameAware {

    public static final String USER_AGENT_HEADER_NAME = "user-agent";

    private int priority;
    private String beanName;

    private Map<String,Channel> channels = new HashMap<String,Channel>();
    private Map<Pattern,Channel> userAgentChannels = new HashMap<Pattern,Channel>();

    // the following list is used for Spring initialization, it is not used later on.
    List<Channel> channelList = new ArrayList<Channel>();

    private ChannelService channelService;

    public void setChannelList(List<Channel> channelList) {
        this.channelList = channelList;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public void afterPropertiesSet() throws Exception {
        for (Channel channel : channelList) {
            channels.put(channel.getIdentifier(), channel);
            if (channel.hasCapabilityValue("userAgentPattern")) {
                Pattern curPattern = Pattern.compile(channel.getCapability("userAgentPattern"));
                userAgentChannels.put(curPattern, channel);
            }
        }
        channelService.addProvider(this);
    }

    public Map<String, String> getChannelCapabilities(String identifier) {
        if (channels.containsKey(identifier)) {
            return channels.get(identifier).getCapabilities();
        }
        return null;
    }

    public String resolveChannel(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT_HEADER_NAME);
        if (userAgent != null) {
            for (Map.Entry<Pattern,Channel> entry : userAgentChannels.entrySet()) {
                Pattern curPattern = entry.getKey();
                Matcher m = curPattern.matcher(userAgent);
                if (m.matches()) {
                    return entry.getValue().getIdentifier();
                }
            }
        }
        return null;
    }

    public List<String> getAllChannels() {
        return Collections.unmodifiableList(new ArrayList<String>(channels.keySet()));
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getFallBack(String identifier) {
        return channels.get(identifier).getFallBack();
    }

    public boolean isVisible(String identifier) {
        return channels.get(identifier).isVisible();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (!(obj instanceof UserAgentChannelProvider)) return false;
        return ((UserAgentChannelProvider) obj).getBeanName().equals(beanName);    
    }

    @Override
    public int hashCode() {
        return beanName != null ? beanName.hashCode() : 0;
    }
}
