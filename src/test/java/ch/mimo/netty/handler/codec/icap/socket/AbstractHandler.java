/*******************************************************************************
 * Copyright 2012 Michael Mimo Moratti
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package ch.mimo.netty.handler.codec.icap.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelStateEvent;
import io.netty.channel.ExceptionEvent;
import io.netty.channel.MessageEvent;
import io.netty.channel.SimpleChannelUpstreamHandler;

public abstract class AbstractHandler extends SimpleChannelUpstreamHandler implements Handler {

	private Channel channel;
	private boolean processed;
	private boolean exception;
	
	private Throwable cause;
	
	
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelOpen(ctx, e);
		this.channel = ctx.getChannel();
	}

	@Override
	public final void messageReceived(ChannelHandlerContext context, MessageEvent event) throws Exception {
		processed = doMessageReceived(context,event,channel);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		super.exceptionCaught(ctx, e);
		cause = e.getCause();
		exception = true;
	}
	
	public void close() {
		this.channel.close().awaitUninterruptibly();
	}
	
	public boolean isProcessed() {
		return processed;
	}
	
	public boolean hasException() {
		return exception;
	}
	
	public Throwable getExceptionCause() {
		return cause;
	}

	public abstract boolean doMessageReceived(ChannelHandlerContext context, MessageEvent event, Channel channel) throws Exception;
}
