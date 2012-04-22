/*
 * Copyright (c) 2011, grossmann
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * * Neither the name of the jo-widgets.org nor the
 *   names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL jo-widgets.org BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.jowidgets.cap.sample1.common.entity;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jowidgets.cap.common.api.bean.IBean;

public interface ISampleConfig extends IBean {

	String APPLICATION_NAME_PROPERTY = "applicationName";
	String MAX_SESSION_COUNT_PROPERTY = "maxSessionCount";
	String MAX_CLIENT_COUNT_PROPERTY = "maxClientCount";
	String HOST_NAME_PROPERTY = "hostName";
	String IP_PROPERTY = "ip";
	String PORT_PROPERTY = "port";
	String ROOT_HOST_NAME_PROPERTY = "rootHostName";
	String ROOT_IP_PROPERTY = "rootIp";
	String ROOT_PORT_PROPERTY = "rootPort";
	String SERVER_STARTED_PROPERTY = "serverStarted";
	String SERVER_UPTIME_PROPERTY = "serverUptime";

	List<String> ALL_PROPERTIES = new LinkedList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add(APPLICATION_NAME_PROPERTY);
			add(MAX_SESSION_COUNT_PROPERTY);
			add(MAX_CLIENT_COUNT_PROPERTY);
			add(HOST_NAME_PROPERTY);
			add(IP_PROPERTY);
			add(PORT_PROPERTY);
			add(ROOT_HOST_NAME_PROPERTY);
			add(ROOT_IP_PROPERTY);
			add(ROOT_PORT_PROPERTY);
			add(SERVER_STARTED_PROPERTY);
			add(SERVER_UPTIME_PROPERTY);
			add(IBean.ID_PROPERTY);
			add(IBean.VERSION_PROPERTY);
		}
	};

	@NotNull
	@Size(min = 1, max = 50)
	String getApplicationName();

	void setApplicationName(final String name);

	Integer getMaxSessionCount();

	void setMaxSessionCount(Integer sessionCount);

	Integer getMaxClientCount();

	void setMaxClientCount(Integer sessionCount);

	String getHostName();

	void setHostName(String hostName);

	String getIp();

	void setIp(String ip);

	Integer getPort();

	void setPort(Integer port);

	String getRootHostName();

	void setRootHostName(String hostName);

	String getRootIp();

	void setRootIp(String ip);

	Integer getRootPort();

	void setRootPort(Integer port);

	Date getServerStarted();

	Long getServerUptime();

}
