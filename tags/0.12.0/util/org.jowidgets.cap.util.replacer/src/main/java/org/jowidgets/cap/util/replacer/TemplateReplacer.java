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

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.Tuple;

public final class TemplateReplacer {

	private TemplateReplacer() {}

	public static void main(final String[] args) throws Exception {
		copyAndReplace(args, createConfig());
	}

	private static ReplacementConfig createConfig() {
		final ReplacementConfig config = new ReplacementConfig();

		final Set<Tuple<String, String>> replacements = new HashSet<Tuple<String, String>>();
		replacements.add(new Tuple<String, String>("Sample0", "MyApp"));
		replacements.add(new Tuple<String, String>("sample0", "myapp"));
		replacements.add(new Tuple<String, String>("<vendor>jowidgets.org</vendor>", "<vendor>myorg.com</vendor>"));

		config.setReplacements(replacements);

		final Set<Tuple<String[], String[]>> packageReplacements = new HashSet<Tuple<String[], String[]>>();
		final String[] packageReplacementSource = new String[] {"org", "jowidgets", "cap", "sample0"};
		final String[] packageReplacementDestination = new String[] {"com", "myorg", "myapp"};
		packageReplacements.add(new Tuple<String[], String[]>(packageReplacementSource, packageReplacementDestination));
		config.setPackageReplacements(packageReplacements);

		final IOFileFilter modifyFilesFilter = new SuffixFileFilter(new String[] {
				"java", "project", "xml", "MF", "htm", "html", "jnlp", "template.vm",
				"org.jowidgets.cap.ui.api.login.ILoginService", "org.jowidgets.security.api.IAuthenticationService",
				"org.jowidgets.service.api.IServiceProviderHolder"}, IOCase.INSENSITIVE);
		config.setModififyFilesFilter(modifyFilesFilter);

		//		final IOFileFilter checkstyleFilter = new SuffixFileFilter(new String[] {"checkstyle"}, IOCase.INSENSITIVE);
		//		final String checkstyleText = "New checkstyle config";
		//		final Tuple<IOFileFilter, String> checkstyleReplacement = new Tuple<IOFileFilter, String>(
		//			checkstyleFilter,
		//			checkstyleText);
		//		config.setFileReplacements(Collections.singleton(checkstyleReplacement));

		config.setJavaHeader("/* \n * Copyright (c) 2011 \n */");

		config.setParentPomVersion("0.0.1-SNAPSHOT");
		return config;
	}

	public static void copyAndReplace(final String[] args, final ReplacementConfig config) throws Exception {

		if (args.length != 2) {
			//CHECKSTYLE:OFF
			System.out.println("Usage: " + TemplateReplacer.class.getSimpleName() + " <sourceDirectory> <destinationDirectory>");
			//CHECKSTYLE:ON
			return;
		}

		final File source = new File(args[0]);
		if (!source.exists() || !source.isDirectory()) {
			//CHECKSTYLE:OFF
			System.out.println("Source directory doesn't exist: " + source);
			//CHECKSTYLE:ON
			return;
		}

		final File destination = new File(args[1]);
		if (destination.exists()) {
			//CHECKSTYLE:OFF
			System.out.println("Destination directory is not empty: " + destination);
			//CHECKSTYLE:ON
			return;
		}

		final String encoding = config.getEncoding();

		Collection<Tuple<String, String>> replacements = new HashSet<Tuple<String, String>>(config.getReplacements());
		final Collection<Tuple<String[], String[]>> packageReplacements = config.getPackageReplacements();

		replacements.addAll(getReplacementsFromPackageReplacements(packageReplacements));
		replacements = getSortedReplacements(replacements);

		Collection<Tuple<String, String>> pathReplacements = getPathReplacementsFromPackageReplacements(packageReplacements);
		pathReplacements.addAll(replacements);
		pathReplacements = getSortedReplacements(pathReplacements);

		final IOFileFilter directoryFilter = FileFilterUtils.makeSVNAware(FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter("target")));
		final IOFileFilter pomFileFilter = FileFilterUtils.nameFileFilter("pom.xml", IOCase.INSENSITIVE);
		final IOFileFilter javaFileFilter = FileFilterUtils.suffixFileFilter("java", IOCase.INSENSITIVE);

		final int sourcePathLength = source.getPath().length();

		for (final File file : list(source, directoryFilter)) {
			String subPath = replace(file.getPath().substring(sourcePathLength), pathReplacements);
			subPath = replace(subPath, replacements);
			final File destinationFile = new File(destination.getPath() + subPath);
			if (file.isDirectory()) {
				if (!destinationFile.exists()) {
					if (!ignorePath(destinationFile, packageReplacements)) {
						if (!destinationFile.mkdirs()) {
							throw new IllegalStateException("Failed to create directory: " + destinationFile);
						}
					}
				}
			}
			else {
				final Tuple<IOFileFilter, String> fileReplacement = getFileReplacement(
						destinationFile,
						config.getFileReplacements());
				if (fileReplacement != null) {
					if (!EmptyCheck.isEmpty(fileReplacement.getSecond())) {
						FileUtils.writeStringToFile(destinationFile, fileReplacement.getSecond(), encoding);
					}
				}
				else if (config.getJavaHeader() != null && javaFileFilter.accept(destinationFile)) {
					String text = FileUtils.readFileToString(file, encoding);
					if (config.getModififyFilesFilter().accept(destinationFile)) {
						text = replace(text, replacements);
					}

					final int startIndex = text.indexOf("/*");
					int endIndex = text.indexOf("*/");
					if (startIndex != -1 && endIndex != -1) {
						endIndex += 2;
						text = text.substring(0, startIndex) + config.getJavaHeader() + text.substring(endIndex, text.length());
					}
					FileUtils.writeStringToFile(destinationFile, text, encoding);
				}
				else if (config.getParentPomVersion() != null && pomFileFilter.accept(destinationFile)) {
					String text = FileUtils.readFileToString(file, encoding);
					if (config.getModififyFilesFilter().accept(destinationFile)) {
						text = replace(text, replacements);
					}
					int parentStartIndex = text.indexOf("<parent>");
					final int parentEndIndex = text.indexOf("</parent>");
					if (parentStartIndex != -1 && parentEndIndex != -1) {
						parentStartIndex += 8;
						String parentText = text.substring(parentStartIndex, parentEndIndex);
						final String replacement = "<version>" + config.getParentPomVersion() + "</version>";
						parentText = parentText.replaceAll("<version>.*</version>", replacement);
						text = text.substring(0, parentStartIndex) + parentText + text.substring(parentEndIndex, text.length());
					}
					FileUtils.writeStringToFile(destinationFile, text, encoding);
				}
				else if (config.getModififyFilesFilter().accept(destinationFile)) {
					String text = FileUtils.readFileToString(file, encoding);
					text = replace(text, replacements);
					FileUtils.writeStringToFile(destinationFile, text, encoding);
				}
				else {
					FileUtils.copyFile(file, destinationFile);
				}
			}
		}

	}

	private static Tuple<IOFileFilter, String> getFileReplacement(
		final File file,
		final Collection<Tuple<IOFileFilter, String>> fileReplacements) {

		for (final Tuple<IOFileFilter, String> fileReplacement : fileReplacements) {
			if (fileReplacement.getFirst().accept(file)) {
				return fileReplacement;
			}
		}
		return null;
	}

	private static Collection<Tuple<String, String>> getSortedReplacements(final Collection<Tuple<String, String>> replacements) {
		final List<Tuple<String, String>> result = new LinkedList<Tuple<String, String>>(replacements);
		Collections.sort(result, new Comparator<Tuple<String, String>>() {
			@Override
			public int compare(final Tuple<String, String> tuple1, final Tuple<String, String> tuple2) {
				return tuple2.getFirst().length() - tuple1.getFirst().length();
			}
		});
		return result;
	}

	private static String replace(final String source, final Collection<Tuple<String, String>> replacements) {
		String result = source;
		for (final Tuple<String, String> replacement : replacements) {
			result = result.replace(replacement.getFirst(), replacement.getSecond());
		}
		return result;
	}

	private static Collection<Tuple<String, String>> getReplacementsFromPackageReplacements(
		final Collection<Tuple<String[], String[]>> packageReplacements) {
		final Set<Tuple<String, String>> result = new HashSet<Tuple<String, String>>();
		for (final Tuple<String[], String[]> packageReplacement : packageReplacements) {
			result.add(new Tuple<String, String>(
				getPackage(packageReplacement.getFirst()),
				getPackage(packageReplacement.getSecond())));
		}
		return result;
	}

	private static Collection<Tuple<String, String>> getPathReplacementsFromPackageReplacements(
		final Collection<Tuple<String[], String[]>> packageReplacements) {
		final Set<Tuple<String, String>> result = new HashSet<Tuple<String, String>>();
		for (final Tuple<String[], String[]> packageReplacement : packageReplacements) {
			result.add(new Tuple<String, String>(getPath(packageReplacement.getFirst()), getPath(packageReplacement.getSecond())));
		}
		return result;
	}

	private static boolean ignorePath(final File file, final Collection<Tuple<String[], String[]>> packageReplacements) {
		final StringBuilder builder = new StringBuilder();
		final String path = file.getPath();
		for (final Tuple<String[], String[]> packageReplacement : packageReplacements) {
			final String[] pathParts = packageReplacement.getFirst();
			for (int i = 0; i < pathParts.length - 1; i++) {
				final String pathPart = pathParts[i];
				builder.append(File.separator);
				builder.append(pathPart);
				if (path.endsWith(builder.toString())) {
					return true;
				}
			}
		}
		return false;
	}

	private static String getPackage(final String[] pathParts) {
		final StringBuilder builder = new StringBuilder();
		for (final String pathPart : pathParts) {
			builder.append(pathPart);
			builder.append(".");
		}
		if (pathParts.length > 0) {
			builder.setLength(builder.length() - 1);
		}
		return builder.toString();
	}

	private static String getPath(final String[] pathParts) {
		final StringBuilder builder = new StringBuilder();
		for (final String pathPart : pathParts) {
			builder.append(File.separator);
			builder.append(pathPart);
		}
		return builder.toString();
	}

	private static Collection<File> list(final File dir, final IOFileFilter dirFilter) {
		final Collection<File> result = new LinkedList<File>();
		final IOFileFilter noDirsFilter = FileFilterUtils.notFileFilter(DirectoryFileFilter.INSTANCE);
		listRecursive(result, dir, FileFilterUtils.or(noDirsFilter, dirFilter));
		return result;
	}

	private static void listRecursive(final Collection<File> resultFiles, final File directory, final IOFileFilter filter) {
		final File[] files = directory.listFiles((FileFilter) filter);
		if (files != null) {
			for (final File file : files) {
				resultFiles.add(file);
				if (file.isDirectory()) {
					listRecursive(resultFiles, file, filter);
				}
			}
		}
	}
}
