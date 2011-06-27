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

package org.jowidgets.cap.service.impl;

import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.service.api.adapter.IAdapterFactoryProvider;
import org.jowidgets.cap.service.api.creator.ISyncCreatorService;
import org.jowidgets.cap.service.api.executor.ISyncExecutorService;
import org.jowidgets.cap.service.api.reader.ISyncReaderService;
import org.jowidgets.cap.service.api.updater.ISyncUpdaterService;
import org.jowidgets.util.IAdapterFactory;

@SuppressWarnings({"rawtypes", "unchecked"})
final class AdapterFactoryProviderImpl implements IAdapterFactoryProvider {

	private final ExecutorServiceAdapterFactory executorServiceAdapterFactory;
	private final ReaderServiceAdapterFactory readerServiceAdapterFactory;
	private final UpdaterServiceAdapterFactory updaterServiceAdapterFactory;
	private final CreatorServiceAdapterFactory creatorServiceAdapterFactory;

	AdapterFactoryProviderImpl() {
		this.executorServiceAdapterFactory = new ExecutorServiceAdapterFactory();
		this.readerServiceAdapterFactory = new ReaderServiceAdapterFactory();
		this.updaterServiceAdapterFactory = new UpdaterServiceAdapterFactory();
		this.creatorServiceAdapterFactory = new CreatorServiceAdapterFactory();
	}

	@Override
	public <PARAM_TYPE> IAdapterFactory<IExecutorService<PARAM_TYPE>, ISyncExecutorService<PARAM_TYPE>> executor() {
		return executorServiceAdapterFactory;
	}

	@Override
	public IAdapterFactory<IUpdaterService, ISyncUpdaterService> updater() {
		return updaterServiceAdapterFactory;
	}

	@Override
	public <PARAM_TYPE> IAdapterFactory<IReaderService<PARAM_TYPE>, ISyncReaderService<PARAM_TYPE>> reader() {
		return readerServiceAdapterFactory;
	}

	@Override
	public IAdapterFactory<ICreatorService, ISyncCreatorService> creator() {
		return creatorServiceAdapterFactory;
	}

}
