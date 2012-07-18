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

package org.jowidgets.cap.util.replacer;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jowidgets.util.Assert;
import org.jowidgets.util.Tuple;

public final class ReplacementConfigBuilder {

	private String encoding;
	private String javaHeader;
	private String parentPomVersion;
	private String domainName;
	private String orgName;
	private String applicationName;

	public ReplacementConfigBuilder() {
		this.parentPomVersion = "0.0.1-SNAPSHOT";
		this.domainName = "com";
		this.orgName = "myorg";
		this.applicationName = "MyApplication";
	}

	public ReplacementConfigBuilder setEncoding(final String encoding) {
		Assert.paramNotEmpty(encoding, "encoding");
		this.encoding = encoding;
		return this;
	}

	public ReplacementConfigBuilder setJavaHeader(final String javaHeader) {
		this.javaHeader = javaHeader;
		return this;
	}

	public ReplacementConfigBuilder setParentPomVersion(final String parentPomVersion) {
		Assert.paramNotEmpty(parentPomVersion, "parentPomVersion");
		this.parentPomVersion = parentPomVersion;
		return this;
	}

	public ReplacementConfigBuilder setDomainName(final String domainName) {
		Assert.paramNotEmpty(domainName, "domainName");
		this.domainName = domainName;
		return this;
	}

	public ReplacementConfigBuilder setOrgName(final String orgName) {
		Assert.paramNotEmpty(orgName, "orgName");
		this.orgName = orgName;
		return this;
	}

	public ReplacementConfigBuilder setApplicationName(final String applicationName) {
		Assert.paramNotEmpty(applicationName, "applicationName");
		this.applicationName = applicationName;
		return this;
	}

	public ReplacementConfig build() {
		final ReplacementConfig result = new ReplacementConfig();
		if (encoding != null) {
			result.setEncoding(encoding);
		}

		final Set<Tuple<String, String>> replacements = new HashSet<Tuple<String, String>>();
		replacements.add(new Tuple<String, String>("Sample0", applicationName));
		replacements.add(new Tuple<String, String>("sample0", applicationName.toLowerCase()));
		replacements.add(new Tuple<String, String>("<vendor>jowidgets.org</vendor>", "<vendor>"
			+ orgName
			+ "."
			+ domainName
			+ "</vendor>"));
		result.setReplacements(replacements);

		final Set<Tuple<String[], String[]>> packageReplacements = new HashSet<Tuple<String[], String[]>>();
		final String[] packageReplacementSource = new String[] {"org", "jowidgets", "cap", "sample0"};
		final String[] packageReplacementDestination = new String[] {
				domainName.toLowerCase(), orgName.toLowerCase(), applicationName.toLowerCase()};
		packageReplacements.add(new Tuple<String[], String[]>(packageReplacementSource, packageReplacementDestination));
		result.setPackageReplacements(packageReplacements);

		final IOFileFilter modifyFilesFilter = new SuffixFileFilter(new String[] {
				"java", "project", "xml", "MF", "htm", "html", "jnlp", "template.vm",
				"org.jowidgets.cap.ui.api.login.ILoginService", "org.jowidgets.security.api.IAuthenticationService",
				"org.jowidgets.service.api.IServiceProviderHolder"}, IOCase.INSENSITIVE);
		result.setModififyFilesFilter(modifyFilesFilter);

		result.setJavaHeader(javaHeader);
		result.setParentPomVersion(parentPomVersion);

		return result;
	}
}
