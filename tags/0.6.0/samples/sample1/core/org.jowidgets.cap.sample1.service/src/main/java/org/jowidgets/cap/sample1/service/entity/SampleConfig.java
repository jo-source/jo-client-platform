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

package org.jowidgets.cap.sample1.service.entity;

import java.util.Date;

import org.jowidgets.cap.sample1.common.entity.ISampleConfig;

public class SampleConfig extends AbstractSampleBean implements ISampleConfig {

	private String applicationName;
	private Integer maxSessionCount;
	private Integer maxClientCount;
	private String hostName;
	private String ip;
	private Integer port;
	private String rootHostName;
	private String rootIp;
	private Integer rootPort;
	private final Date serverStarted;

	public SampleConfig(final Long id) {
		super(id);
		this.serverStarted = new Date();
	}

	@Override
	public String getApplicationName() {
		return applicationName;
	}

	@Override
	public void setApplicationName(final String applicationName) {
		increaseVersion();
		this.applicationName = applicationName;
	}

	@Override
	public Integer getMaxSessionCount() {
		return maxSessionCount;
	}

	@Override
	public void setMaxSessionCount(final Integer maxSessionCount) {
		increaseVersion();
		this.maxSessionCount = maxSessionCount;
	}

	@Override
	public Integer getMaxClientCount() {
		return maxClientCount;
	}

	@Override
	public void setMaxClientCount(final Integer maxClientCount) {
		increaseVersion();
		this.maxClientCount = maxClientCount;
	}

	@Override
	public String getHostName() {
		return hostName;
	}

	@Override
	public void setHostName(final String hostName) {
		increaseVersion();
		this.hostName = hostName;
	}

	@Override
	public String getIp() {
		return ip;
	}

	@Override
	public void setIp(final String ip) {
		increaseVersion();
		this.ip = ip;
	}

	@Override
	public Integer getPort() {
		return port;
	}

	@Override
	public void setPort(final Integer port) {
		increaseVersion();
		this.port = port;
	}

	@Override
	public String getRootHostName() {
		return rootHostName;
	}

	@Override
	public void setRootHostName(final String rootHostName) {
		increaseVersion();
		this.rootHostName = rootHostName;
	}

	@Override
	public String getRootIp() {
		return rootIp;
	}

	@Override
	public void setRootIp(final String rootIp) {
		increaseVersion();
		this.rootIp = rootIp;
	}

	@Override
	public Integer getRootPort() {
		return rootPort;
	}

	@Override
	public void setRootPort(final Integer rootPort) {
		increaseVersion();
		this.rootPort = rootPort;
	}

	@Override
	public Date getServerStarted() {
		return serverStarted;
	}

	@Override
	public Long getServerUptime() {
		return new Date().getTime() - serverStarted.getTime();
	}
}
