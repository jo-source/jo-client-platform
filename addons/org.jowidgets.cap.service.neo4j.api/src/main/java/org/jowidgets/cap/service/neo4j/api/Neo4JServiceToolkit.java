/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.service.neo4j.api;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.jowidgets.util.Assert;

public final class Neo4JServiceToolkit {

	private static INeo4JServiceToolkit instance;

	private Neo4JServiceToolkit() {}

	public static synchronized void initialize(final INeo4JServiceToolkit instance) {
		Assert.paramNotNull(instance, "instance");
		if (instance != null) {
			Neo4JServiceToolkit.instance = instance;
		}
		else {
			throw new IllegalStateException("The INeo4JServiceToolkit is already initialized");
		}
	}

	public static INeo4JServiceToolkit getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	public static INeo4JServiceFactory serviceFactory() {
		return getInstance().serviceFactory();
	}

	public static IGraphDBConfigBuilder graphDBConfigBuilder() {
		return getInstance().graphDBConfigBuilder();
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			final ServiceLoader<INeo4JServiceToolkit> serviceLoader = ServiceLoader.load(INeo4JServiceToolkit.class);
			final Iterator<INeo4JServiceToolkit> iterator = serviceLoader.iterator();

			if (!iterator.hasNext()) {
				throw new IllegalStateException("No implementation found for '" + INeo4JServiceToolkit.class.getName() + "'");
			}
			else {
				instance = iterator.next();
				if (iterator.hasNext()) {
					throw new IllegalStateException("More than one implementation found for '"
						+ INeo4JServiceToolkit.class.getName()
						+ "'");
				}
			}
		}
	}

}
