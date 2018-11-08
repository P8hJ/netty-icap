/*******************************************************************************
 * Copyright 2012 Michael Mimo Moratti
 * Modifications Copyright (c) 2018 eBlocker GmbH
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
package ch.mimo.netty.handler.codec.icap;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * Main ICAP message encoder. This encoder is based on @see {@link OneToOneEncoder}
 * 
 * @author Michael Mimo Moratti (mimo@mimo.ch)
 *
 * @see IcapRequestEncoder
 * @see IcapResponseEncoder
 */
public abstract class IcapMessageEncoder extends MessageToMessageEncoder<Object> {
	
	private final InternalLogger LOG;
	
	public IcapMessageEncoder() {
		LOG = InternalLoggerFactory.getInstance(getClass());
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
		LOG.debug("Encoding [" + msg.getClass().getName() + "]");
		if(msg instanceof IcapMessage) {
			IcapMessage message = (IcapMessage)msg;
            ByteBuf buffer = Unpooled.buffer();
			encodeInitialLine(buffer,message);
			encodeHeaders(buffer,message);
			ByteBuf httpRequestBuffer = encodeHttpRequestHeader(message.getHttpRequest());
			ByteBuf httpResponseBuffer = encodeHttpResponseHeader(message.getHttpResponse());
            int index = 0;
            Encapsulated encapsulated = new Encapsulated();
            if(httpRequestBuffer.readableBytes() > 0) {
            	encapsulated.addEntry(IcapMessageElementEnum.REQHDR,index);
            	httpRequestBuffer.writeBytes(IcapCodecUtil.CRLF);
            	index += httpRequestBuffer.readableBytes();
            }
            if(httpResponseBuffer.readableBytes() > 0) {
            	encapsulated.addEntry(IcapMessageElementEnum.RESHDR,index);
            	httpResponseBuffer.writeBytes(IcapCodecUtil.CRLF);
            	index += httpResponseBuffer.readableBytes();
            }
            if(message.getBodyType() != null) {
            	encapsulated.addEntry(message.getBodyType(),index);
            } else {
            	encapsulated.addEntry(IcapMessageElementEnum.NULLBODY,index);
            }
            encapsulated.encode(buffer);
            buffer.writeBytes(httpRequestBuffer);
            buffer.writeBytes(httpResponseBuffer);
            out.add(buffer);
		} else if(msg instanceof IcapChunk) {
			ByteBuf buffer = Unpooled.buffer();
			IcapChunk chunk = (IcapChunk)msg;
			if(chunk.isLast()) {
				if(chunk.isEarlyTerminated()) {
					buffer.writeBytes(IcapCodecUtil.NATIVE_IEOF_SEQUENCE);
					buffer.writeBytes(IcapCodecUtil.CRLF);
					buffer.writeBytes(IcapCodecUtil.CRLF);
				} else if(msg instanceof IcapChunkTrailer) {
					buffer.writeByte((byte) '0');
					buffer.writeBytes(IcapCodecUtil.CRLF);
					if (((IcapChunkTrailer)msg).getUseOriginalBody() != null) {
						buffer.writeBytes(";use-original-body=".getBytes(IcapCodecUtil.ASCII_CHARSET));
						buffer.writeBytes(Integer.toString(((IcapChunkTrailer)msg).getUseOriginalBody()).getBytes(IcapCodecUtil.ASCII_CHARSET));
					}
					encodeTrailingHeaders(buffer,(IcapChunkTrailer)msg);
					buffer.writeBytes(IcapCodecUtil.CRLF);
				} else {
					buffer.writeByte((byte) '0');
					buffer.writeBytes(IcapCodecUtil.CRLF);
					buffer.writeBytes(IcapCodecUtil.CRLF);
				}
			} else {
				ByteBuf chunkBuffer = chunk.content();
				int contentLength = chunkBuffer.readableBytes();
				buffer.writeBytes(Integer.toHexString(contentLength).getBytes(IcapCodecUtil.ASCII_CHARSET));
				buffer.writeBytes(IcapCodecUtil.CRLF);
				buffer.writeBytes(chunkBuffer);
				buffer.writeBytes(IcapCodecUtil.CRLF);
			}
			out.add(buffer);
		}
	}

	protected abstract int encodeInitialLine(ByteBuf buffer, IcapMessage message)  throws Exception;
	
	private ByteBuf encodeHttpRequestHeader(HttpRequest httpRequest) throws UnsupportedEncodingException {
		ByteBuf buffer = Unpooled.buffer();
		if(httpRequest != null) {
			buffer.writeBytes(httpRequest.getMethod().toString().getBytes(IcapCodecUtil.ASCII_CHARSET));
			buffer.writeByte(IcapCodecUtil.SPACE);
			buffer.writeBytes(httpRequest.getUri().getBytes(IcapCodecUtil.ASCII_CHARSET));
			buffer.writeByte(IcapCodecUtil.SPACE);
			buffer.writeBytes(httpRequest.getProtocolVersion().toString().getBytes(IcapCodecUtil.ASCII_CHARSET));
			buffer.writeBytes(IcapCodecUtil.CRLF);
            for (Map.Entry<String, String> h: httpRequest.headers()) {
                encodeHeader(buffer, h.getKey(), h.getValue());
            }
		}
		return buffer;
	}
	
	private ByteBuf encodeHttpResponseHeader(HttpResponse httpResponse) throws UnsupportedEncodingException {
		ByteBuf buffer = Unpooled.buffer();
		if(httpResponse != null) {
			buffer.writeBytes(httpResponse.getProtocolVersion().toString().getBytes(IcapCodecUtil.ASCII_CHARSET));
			buffer.writeByte(IcapCodecUtil.SPACE);
			buffer.writeBytes(httpResponse.getStatus().toString().getBytes(IcapCodecUtil.ASCII_CHARSET));
			buffer.writeBytes(IcapCodecUtil.CRLF);
            for (Map.Entry<String, String> h: httpResponse.headers()) {
                encodeHeader(buffer, h.getKey(), h.getValue());
            }
		}
		return buffer;
	}
	
    private int encodeTrailingHeaders(ByteBuf buffer, IcapChunkTrailer chunkTrailer) {
    	int index = buffer.readableBytes();
        for (Map.Entry<String, String> h: chunkTrailer.trailingHeaders()) {
            encodeHeader(buffer, h.getKey(), h.getValue());
        }
        return buffer.readableBytes() - index;
    }
	
    private int encodeHeaders(ByteBuf buffer, IcapMessage message) {
    	int index = buffer.readableBytes();
        for (Map.Entry<String, String> h: message.getHeaders()) {
            encodeHeader(buffer, h.getKey(), h.getValue());
        }
        return buffer.readableBytes() - index;
    }
    
    private void encodeHeader(ByteBuf buf, String header, String value) {
		buf.writeBytes(header.getBytes(IcapCodecUtil.ASCII_CHARSET));
		buf.writeByte(IcapCodecUtil.COLON);
		buf.writeByte(IcapCodecUtil.SPACE);
		buf.writeBytes(value.getBytes(IcapCodecUtil.ASCII_CHARSET));
		buf.writeBytes(IcapCodecUtil.CRLF);
    }
}
