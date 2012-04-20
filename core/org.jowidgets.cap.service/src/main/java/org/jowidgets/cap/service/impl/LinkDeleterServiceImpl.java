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

package org.jowidgets.cap.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.filter.ArithmeticFilter;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.api.filter.BooleanFilter;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IArithmeticFilterBuilder;
import org.jowidgets.cap.common.api.filter.IBooleanFilterBuilder;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.filter.IFilterFactory;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.tools.execution.SyncResultCallback;
import org.jowidgets.util.Assert;

final class LinkDeleterServiceImpl implements ILinkDeleterService {

	private final IReaderService<Void> linkReaderService;
	private final IDeleterService sourceDeleterService;
	private final IDeleterService linkDeleterService;
	private final IDeleterService linkedDeleterService;
	private final IEntityLinkProperties sourceProperties;
	private final IEntityLinkProperties destinationProperties;

	LinkDeleterServiceImpl(
		final IReaderService<Void> linkReaderService,
		final IDeleterService sourceDeleterService,
		final IDeleterService linkDeleterService,
		final IDeleterService linkedDeleterService,
		final IEntityLinkProperties sourceProperties,
		final IEntityLinkProperties destinationProperties) {

		Assert.paramNotNull(sourceProperties, "sourceProperties");
		Assert.paramNotNull(destinationProperties, "destinationProperties");

		this.linkReaderService = linkReaderService;
		this.sourceDeleterService = sourceDeleterService;
		this.linkDeleterService = linkDeleterService;
		this.linkedDeleterService = linkedDeleterService;
		this.sourceProperties = sourceProperties;
		this.destinationProperties = destinationProperties;
	}

	@Override
	public void delete(
		final IResultCallback<Void> result,
		final Collection<? extends ILinkDeletion> linksDeletions,
		final IExecutionCallback executionCallback) {
		try {
			deleteSyncImpl(linksDeletions, executionCallback);
			result.finished(null);
		}
		catch (final Exception exception) {
			result.exception(exception);
		}
	}

	private void deleteSyncImpl(
		final Collection<? extends ILinkDeletion> linksDeletions,
		final IExecutionCallback executionCallback) {

		final List<IBeanKey> sourceKeys = new LinkedList<IBeanKey>();
		final List<IBeanKey> destinationKeys = new LinkedList<IBeanKey>();

		final IFilterFactory filterFactory = CapCommonToolkit.filterFactory();
		final IBooleanFilterBuilder linkReaderFilterBuilder = filterFactory.booleanFilterBuilder();
		linkReaderFilterBuilder.setOperator(BooleanOperator.OR);

		for (final ILinkDeletion linkDeletion : linksDeletions) {

			final IBeanKey sourceKey = linkDeletion.getSourceKey();
			final IBeanKey destinationKey = linkDeletion.getDestinationKey();

			if (linkDeletion.deleteSource()) {
				sourceKeys.add(sourceKey);
			}
			if (linkDeletion.deleteDestination() || linkDeleterService == linkedDeleterService) {
				destinationKeys.add(destinationKey);
			}
			if (linkDeleterService != linkedDeleterService) {
				linkReaderFilterBuilder.addFilter(createLinkFilter(sourceKey, destinationKey));
			}
		}

		if (linkReaderFilterBuilder.isEmpty()) {
			deleteLinks(linkReaderFilterBuilder.build(), linksDeletions.size(), executionCallback);
		}

		deleteKeys(sourceKeys, sourceDeleterService, executionCallback);
		deleteKeys(destinationKeys, linkedDeleterService, executionCallback);
	}

	private void deleteLinks(final IFilter filter, final int maxRows, final IExecutionCallback executionCallback) {
		final List<IBeanDto> links = readLinks(filter, maxRows, executionCallback);
		deleteKeys(createLinkKeys(links), linkDeleterService, executionCallback);
	}

	private void deleteKeys(
		final List<IBeanKey> keys,
		final IDeleterService deleterService,
		final IExecutionCallback executionCallback) {
		if (keys.size() > 0) {
			final SyncResultCallback<Void> syncResultCallback = new SyncResultCallback<Void>();
			deleterService.delete(syncResultCallback, keys, executionCallback);
			syncResultCallback.getResultSynchronious();
		}
	}

	private List<IBeanKey> createLinkKeys(final List<IBeanDto> links) {
		final List<IBeanKey> result = new LinkedList<IBeanKey>();
		for (final IBeanDto beanDto : links) {
			result.add(CapCommonToolkit.beanKeyBuilder().setBeanDto(beanDto).build());
		}
		return result;
	}

	private List<IBeanDto> readLinks(final IFilter filter, final int maxRows, final IExecutionCallback executionCallback) {
		final List<ISort> sorting = Collections.emptyList();
		final List<IBeanKey> parentBeanKeys = Collections.emptyList();
		final SyncResultCallback<List<IBeanDto>> syncResultCallback = new SyncResultCallback<List<IBeanDto>>();
		linkReaderService.read(syncResultCallback, parentBeanKeys, filter, sorting, 0, maxRows, null, executionCallback);
		return syncResultCallback.getResultSynchronious();
	}

	private IFilter createLinkFilter(final IBeanKey sourceKey, final IBeanKey destinationKey) {
		final IBooleanFilterBuilder builder = BooleanFilter.builder();
		builder.setOperator(BooleanOperator.AND);
		builder.addFilter(createKeyFilter(sourceKey, sourceProperties));
		builder.addFilter(createKeyFilter(destinationKey, destinationProperties));
		return builder.build();
	}

	private IFilter createKeyFilter(final IBeanKey key, final IEntityLinkProperties linkProperties) {
		final IArithmeticFilterBuilder builder = ArithmeticFilter.builder();
		builder.setPropertyName(linkProperties.getForeignKeyPropertyName());
		builder.setOperator(ArithmeticOperator.EQUAL);
		builder.setParameter(key.getId());
		return builder.build();
	}

}
