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

package org.jowidgets.cap.service.hibernate.api;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.jowidgets.classloading.api.SharedClassLoader;
import org.jowidgets.util.Assert;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;

public final class KillSessionSupport {

	private static IMaybe<IKillSessionSupport> instance;

	private KillSessionSupport() {}

	public static synchronized void initialize(final IKillSessionSupport instance) {
		Assert.paramNotNull(instance, "instance");
		if (instance != null) {
			KillSessionSupport.instance = new Some<IKillSessionSupport>(instance);
		}
		else {
			throw new IllegalStateException("Already initialized");
		}
	}

	public static IKillSessionSupport getInstance() {
		if (instance == null) {
			createInstance();
		}
		if (instance.isSomething()) {
			return instance.getValue();
		}
		else {
			return null;
		}
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			final ServiceLoader<IKillSessionSupport> serviceLoader = ServiceLoader.load(
					IKillSessionSupport.class,
					SharedClassLoader.getCompositeClassLoader());
			final Iterator<IKillSessionSupport> iterator = serviceLoader.iterator();

			if (iterator.hasNext()) {
				instance = new Some<IKillSessionSupport>(iterator.next());
				if (iterator.hasNext()) {
					throw new IllegalStateException(
						"More than one implementation found for '" + IKillSessionSupport.class.getName() + "'");
				}
			}
			else {
				instance = Nothing.getInstance();
			}
		}
	}
}
