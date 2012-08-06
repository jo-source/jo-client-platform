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

package org.jowidgets.cap.service.neo4J.impl;

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncDeleterService;
import org.jowidgets.cap.service.api.adapter.ISyncExecutorService;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.executor.IBeanExecutor;
import org.jowidgets.cap.service.api.executor.IExecutorServiceBuilder;
import org.jowidgets.cap.service.neo4j.api.GraphDBConfig;
import org.jowidgets.cap.service.neo4j.api.INodeBean;
import org.jowidgets.cap.service.neo4j.api.NodeAccess;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;

final class SyncNeo4JDeleterServiceImpl implements ISyncDeleterService {

	private final ISyncExecutorService<Void> executorService;
	private final Index<Node> nodeIndex;

	SyncNeo4JDeleterServiceImpl(
		final IBeanAccess<? extends IBean> beanAccess,
		final IExecutableChecker<? extends IBean> executableChecker,
		final boolean allowDeletedData,
		final boolean allowStaleData) {

		final IExecutorServiceBuilder<IBean, Void> executorServiceBuilder = CapServiceToolkit.executorServiceBuilder(beanAccess);
		if (executableChecker != null) {
			executorServiceBuilder.setExecutableChecker(executableChecker);
		}
		executorServiceBuilder.setAllowDeletedBeans(allowDeletedData).setAllowStaleBeans(allowStaleData);
		executorServiceBuilder.setExecutor(new IBeanExecutor<IBean, Void>() {
			@Override
			public IBean execute(final IBean data, final Void parameter, final IExecutionCallback executionCallback) {
				CapServiceToolkit.checkCanceled(executionCallback);
				if (data instanceof INodeBean) {
					deleteNode(((INodeBean) data).getNode());
				}
				else {
					final Node node = NodeAccess.findNode(beanAccess.getBeanTypeId(), data.getId());
					if (node != null) {
						deleteNode(node);
					}
					else if (!allowDeletedData) {
						throw new DeletedBeanException(data.getId());
					}
				}
				return null;
			}

			private void deleteNode(final Node node) {
				//TODO MG this is a aspect and should be configurable
				for (final Relationship relationship : node.getRelationships(Direction.OUTGOING)) {
					relationship.delete();
				}
				nodeIndex.remove(node);
				node.delete();
			}
		});
		this.executorService = executorServiceBuilder.buildSyncService();
		this.nodeIndex = GraphDBConfig.getNodeIndex();
	}

	@Override
	public void delete(final Collection<? extends IBeanKey> beanKeys, final IExecutionCallback executionCallback) {
		executorService.execute(beanKeys, null, executionCallback);
	}
}
