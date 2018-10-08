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
package ch.mimo.netty.handler.codec.icap;

import io.netty.buffer.ByteBuf;

/**
 * Decoder State that reads one huge chunk into many smaller chunks
 * 
 * @author Michael Mimo Moratti (mimo@mimo.ch)
 *
 * @see IcapMessageDecoder
 * @see StateEnum
 */
public class ReadChunkedContentAsChunksState extends State<Object> {

	public ReadChunkedContentAsChunksState(String name) {
		super(name);
	}
	
	@Override
	public void onEntry(ByteBuf buffer, IcapMessageDecoder icapMessageDecoder) throws DecodingException {
	}

	@Override
	public StateReturnValue execute(ByteBuf buffer, IcapMessageDecoder icapMessageDecoder) throws DecodingException {
		IcapChunk chunk = null;
		if(icapMessageDecoder.currentChunkSize > icapMessageDecoder.maxChunkSize) {
			chunk = new DefaultIcapChunk(buffer.readBytes(icapMessageDecoder.maxChunkSize));
			icapMessageDecoder.currentChunkSize -= icapMessageDecoder.maxChunkSize;
		} else {
			chunk = new DefaultIcapChunk(buffer.readBytes(icapMessageDecoder.currentChunkSize));
			icapMessageDecoder.currentChunkSize = 0;
		}
		chunk.setPreviewChunk(icapMessageDecoder.message.isPreviewMessage());
		if(chunk.isLast()) {
			icapMessageDecoder.currentChunkSize = 0;
			return StateReturnValue.createRelevantResult(new Object[]{chunk,new DefaultIcapChunkTrailer()}); 
		}
		return StateReturnValue.createRelevantResult(chunk);
	}

	@Override
	public StateEnum onExit(ByteBuf buffer, IcapMessageDecoder icapMessageDecoder, Object decisionInformation) throws DecodingException {
		if(icapMessageDecoder.currentChunkSize == 0) {
			return StateEnum.READ_CHUNK_DELIMITER_STATE;
		}
		return StateEnum.READ_CHUNKED_CONTENT_AS_CHUNKS_STATE;
	}

}
