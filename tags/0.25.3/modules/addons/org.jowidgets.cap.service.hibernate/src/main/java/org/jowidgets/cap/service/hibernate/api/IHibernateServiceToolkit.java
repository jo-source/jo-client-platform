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

import org.jowidgets.util.IDecorator;

public interface IHibernateServiceToolkit {

	/**
	 * Gets a exception decorator that converts persistence exceptions to service exceptions.
	 * To set the bean id's the hibernate exceptions will be unwrapped and evaluated.
	 * 
	 * Remark: When using hibernate, the entity field of some persistence exceptions (e.g. OptimisticLockExeception)
	 * will not be filled by hibernate, but the id field of the causing stale state exception is filled.
	 * So to get more convenience to the user, this exception decorator could be used without adding a hibernate
	 * dependency to the main service layer modules.
	 * 
	 * @return A hibernate exception decorator
	 */
	IDecorator<Throwable> exceptionDecorator();

	/**
	 * Gets a default builder to decorate jpa services for given persistence unit.
	 * 
	 * decorated services will cancel the underlying hibernate session, if the execution callback gets
	 * a cancel event.
	 * 
	 * Remark: If you want to use more that one persistence unit in an application,
	 * you may use more than one IServiceProviderHolder, one for each persistence unit,
	 * and decorate it manually (without java services injection).
	 * TODO MG must be verified, if this is possible.
	 * 
	 * @param persistenceUnitName The persistence unit to get the decorator builder for
	 * @return The builder
	 */
	ICancelServicesDecoratorProviderBuilder cancelServiceDecoratorProviderBuilder(String persistenceUnitName);

}
