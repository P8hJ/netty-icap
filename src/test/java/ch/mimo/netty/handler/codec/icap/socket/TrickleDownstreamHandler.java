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

import static io.netty.channel.Channels.write;

import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.ChannelBuffers;
import io.netty.channel.ChannelDownstreamHandler;
import io.netty.channel.ChannelEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageEvent;

public class TrickleDownstreamHandler implements ChannelDownstreamHandler {

	private long latency;
	private int chunkSize;
	
	public TrickleDownstreamHandler(long latency, int chunkSize) {
		this.latency = latency;
		this.chunkSize = chunkSize;
	}
	
	@Override
	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if(e instanceof MessageEvent) {
			MessageEvent event = (MessageEvent)e;
			ChannelBuffer buffer = (ChannelBuffer)event.getMessage();
			while(buffer.readableBytes() > 0) {
				ChannelBuffer newBuffer = ChannelBuffers.dynamicBuffer();
				if(buffer.readableBytes() >= chunkSize) {
					newBuffer.writeBytes(buffer.readBytes(chunkSize));
				} else {
					newBuffer.writeBytes(buffer.readBytes(buffer.readableBytes()));
				}
				Thread.sleep(latency);
				write(ctx, e.getFuture(),newBuffer,event.getRemoteAddress());
			}
		} else {
			ctx.sendDownstream(e);
		}
	}

}
