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

package org.jowidgets.cap.sample2.app.service.reader;

import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.sample2.app.common.entity.IUser;
import org.jowidgets.cap.sample2.app.service.bean.User;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;
import org.jowidgets.cap.service.jpa.api.IJpaServiceFactory;
import org.jowidgets.cap.service.jpa.api.JpaServiceToolkit;

public final class UserServiceFactory {

	private UserServiceFactory() {}

	public static IReaderService<Void> createReaderService() {
		final IJpaServiceFactory serviceFactory = JpaServiceToolkit.serviceFactory();
		final IReaderService<Void> readerService = serviceFactory.readerService(User.class, IUser.ALL_PROPERTIES);
		return new JpaReaderServiceDecorator<Void>(readerService);
	}

	public static IUpdaterService createUpdaterService() {
		final IJpaServiceFactory serviceFactory = JpaServiceToolkit.serviceFactory();
		final IBeanAccess<User> beanAccess = serviceFactory.beanAccess(User.class);
		final IUpdaterServiceBuilder<User> builder = CapServiceToolkit.updaterServiceBuilder(beanAccess);
		builder.setBeanDtoFactoryAndBeanModifier(IUser.ALL_PROPERTIES);
		return new JpaUpdaterServiceDecorator(builder.build());
	}

	public static ICreatorService createCreatorService() {
		final IJpaServiceFactory serviceFactory = JpaServiceToolkit.serviceFactory();
		final ICreatorService creatorService = serviceFactory.creatorService(User.class, IUser.ALL_PROPERTIES);
		return new JpaCreatorServiceDecorator(creatorService);
	}

	public static IDeleterService createDeleterService() {
		final IJpaServiceFactory serviceFactory = JpaServiceToolkit.serviceFactory();
		final IDeleterService deleterService = serviceFactory.deleterService(User.class);
		return new JpaDeleterServiceDecorator(deleterService);
	}
}
