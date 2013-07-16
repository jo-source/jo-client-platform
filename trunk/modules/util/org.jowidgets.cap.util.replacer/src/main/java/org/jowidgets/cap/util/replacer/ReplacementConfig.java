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

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.jowidgets.util.Tuple;

public final class ReplacementConfig {

	private String encoding;
	private String javaHeader;
	private Collection<Tuple<String, String>> replacements;
	private Collection<Tuple<String[], String[]>> packageReplacements;
	private Collection<Tuple<IOFileFilter, String>> fileReplacements;
	private IOFileFilter modififyFilesFilter;
	private String parentPomVersion;

	public ReplacementConfig() {
		this.encoding = "UTF-8";
		this.replacements = new HashSet<Tuple<String, String>>();
		this.packageReplacements = new HashSet<Tuple<String[], String[]>>();
		this.fileReplacements = new HashSet<Tuple<IOFileFilter, String>>();
		this.modififyFilesFilter = FileFilterUtils.fileFileFilter();
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	public void setReplacements(final Collection<Tuple<String, String>> replacements) {
		this.replacements = replacements;
	}

	public void setPackageReplacements(final Collection<Tuple<String[], String[]>> packageReplacements) {
		this.packageReplacements = packageReplacements;
	}

	public void setModififyFilesFilter(final IOFileFilter modififyFilesFilter) {
		this.modififyFilesFilter = modififyFilesFilter;
	}

	public Collection<Tuple<String, String>> getReplacements() {
		return replacements;
	}

	public Collection<Tuple<IOFileFilter, String>> getFileReplacements() {
		return fileReplacements;
	}

	public void setFileReplacements(final Collection<Tuple<IOFileFilter, String>> fileReplacements) {
		this.fileReplacements = fileReplacements;
	}

	public Collection<Tuple<String[], String[]>> getPackageReplacements() {
		return packageReplacements;
	}

	public IOFileFilter getModififyFilesFilter() {
		return modififyFilesFilter;
	}

	public String getParentPomVersion() {
		return parentPomVersion;
	}

	public void setParentPomVersion(final String parentPomVersion) {
		this.parentPomVersion = parentPomVersion;
	}

	public String getJavaHeader() {
		return javaHeader;
	}

	public void setJavaHeader(final String javaHeader) {
		this.javaHeader = javaHeader;
	}

}
