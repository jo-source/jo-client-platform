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

package org.jowidgets.cap.security.ui.api;

import org.jowidgets.cap.ui.api.widgets.IBeanRelationTree;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFolder;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFolderBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.ISingleBeanForm;
import org.jowidgets.cap.ui.api.widgets.ISingleBeanFormBluePrint;

public interface ISecureControlMapperFactory<AUTHORIZATION_TYPE> {

	<BEAN_TYPE> ISecureControlMapper<IBeanTable<BEAN_TYPE>, IBeanTableBluePrint<BEAN_TYPE>, AUTHORIZATION_TYPE> beanTable();

	<BEAN_TYPE> ISecureControlMapper<IBeanRelationTree<BEAN_TYPE>, IBeanRelationTreeBluePrint<BEAN_TYPE>, AUTHORIZATION_TYPE> beanRelationTree();

	<BEAN_TYPE> ISecureControlMapper<IBeanTabFolder<BEAN_TYPE>, IBeanTabFolderBluePrint<BEAN_TYPE>, AUTHORIZATION_TYPE> beanTabFolder();

	<BEAN_TYPE> ISecureControlMapper<ISingleBeanForm<BEAN_TYPE>, ISingleBeanFormBluePrint<BEAN_TYPE>, AUTHORIZATION_TYPE> singleBeanForm();

}
