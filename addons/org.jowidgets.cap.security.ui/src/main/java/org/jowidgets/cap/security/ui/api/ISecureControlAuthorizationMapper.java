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

import org.jowidgets.api.widgets.IControl;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.widgets.descriptor.IWidgetDescriptor;

public interface ISecureControlAuthorizationMapper<WIDGET_TYPE extends IControl, DESCRIPTOR_TYPE extends IWidgetDescriptor<? extends WIDGET_TYPE>, AUTHORIZATION_TYPE> {

	/**
	 * Gets the authorization for a descriptor
	 * 
	 * @param descriptor The descriptor to get the authorization for
	 * 
	 * @return The authorization or null, if widgets has no required authorization
	 */
	AUTHORIZATION_TYPE getAuthorization(DESCRIPTOR_TYPE descriptor);

	/**
	 * Gets the text that should be shown, if user has not the required authorization
	 * 
	 * @return The text to show on authorization fail or null
	 */
	String getAuthorizationFailedText(DESCRIPTOR_TYPE descriptor, WIDGET_TYPE widget);

	/**
	 * Gets the icon that should be shown, if user has not the required authorization
	 * 
	 * @return The icon to show on authorization fail or null
	 */
	IImageConstant getAuthorizationFailedIcon(DESCRIPTOR_TYPE descriptor, WIDGET_TYPE widget);

	Class<?> getWidgetType();

}
