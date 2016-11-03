/*
 * Copyright (c) 2016, grossmann
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

package org.jowidgets.cap.ui.api.widgets;

import org.jowidgets.util.Interval;

public interface IBeanListViewport {

	/**
	 * Scrolls the viewport to the first selected element.
	 * If nothing is selected or the first selected element is already visible, nothing happens.
	 * 
	 */
	void scrollToSelection();

	/**
	 * Scrolls the viewport to the last element
	 * If the view is empty, nothing happens.
	 */
	void scrollToEnd();

	/**
	 * Scrolls the viewport to the given element index, if the element is not shown in the viewport.
	 *
	 * If the given element is already visible in the viewport, nothing happens.
	 * 
	 * @param elementIndex The elementIndex to scroll to
	 */
	void scrollToElement(int elementIndex);

	/**
	 * Gets the interval of the visible elements (visible in viewport).
	 * 
	 * Remark: If the view is empty or now element is visible, the left and the right boundary is null.
	 * 
	 * @return The interval of the visible elements
	 */
	Interval<Integer> getVisibleElements();

}
