/*
 * Copyright (c) 2011, H.Westphal
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

package org.jowidgets.message.impl.akka;

import java.util.concurrent.atomic.AtomicReference;

import org.jowidgets.message.api.IExceptionCallback;
import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.IMessageReceiver;
import org.jowidgets.util.Assert;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

// TODO HRW execution interception for setting e.g. the security context
public final class MessageBroker extends UntypedActor implements IMessageBroker, IMessageChannel {

	private final String brokerId;
	private final ActorRef destination;
	private ActorRef actorRef;
	private volatile IMessageReceiver receiver;

	private MessageBroker(final String brokerId, final ActorRef destination) {
		super();
		Assert.paramNotNull(brokerId, "brokerId");
		this.brokerId = brokerId;
		this.destination = destination;
	}

	public static IMessageBroker create(final String brokerId) {
		return create(brokerId, null);
	}

	public static IMessageBroker create(final String brokerId, final ActorRef destination) {
		final AtomicReference<MessageBroker> messageBrokerRef = new AtomicReference<MessageBroker>();
		final ActorRef actorRef = Actors.actorOf(new UntypedActorFactory() {
			@Override
			public Actor create() {
				messageBrokerRef.set(new MessageBroker(brokerId, destination));
				return messageBrokerRef.get();
			}
		});
		final MessageBroker messageBroker = messageBrokerRef.get();
		messageBroker.actorRef = actorRef;
		return messageBroker;
	}

	@Override
	public ActorRef getActorRef() {
		return actorRef;
	}

	@Override
	public Object getBrokerId() {
		return brokerId;
	}

	@Override
	public void setReceiver(final IMessageReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void onReceive(final Object message) throws Exception {
		if (receiver != null) {
			receiver.onMessage(message, new IMessageChannel() {
				@Override
				public void send(final Object message, final IExceptionCallback exceptionCallback) {
					try {
						context().getSender().get().sendOneWay(message, context());
					}
					catch (final Throwable t) {
						exceptionCallback.exception(t);
					}
				}
			});
		}
	}

	@Override
	public IMessageChannel getChannel() {
		return this;
	}

	@Override
	public void send(final Object message, final IExceptionCallback exceptionCallback) {
		if (destination != null) {
			try {
				destination.sendOneWay(message, context());
			}
			catch (final Throwable t) {
				exceptionCallback.exception(t);
			}
		}
	}

}
