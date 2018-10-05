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

import java.io.UnsupportedEncodingException;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
import org.junit.Test;

public class IcapResponseEncoderTest extends AbstractEncoderTest {

	private EmbeddedChannel embeddedChannel;
	
	@Before
	public void setUp() {
	    embeddedChannel = new EmbeddedChannel(new IcapResponseEncoder());
	}
	
	@Test
	public void encode100ContinueResponse() throws UnsupportedEncodingException {
	    embeddedChannel.writeOutbound(DataMockery.create100ContinueIcapResponse());
		String response = getBufferContent(embeddedChannel.readOutbound());
		assertResponse(DataMockery.create100ContinueResponse(),response);
	}

	@Test
	public void encodeOPTIONSResponse() throws UnsupportedEncodingException {
		embeddedChannel.writeOutbound(DataMockery.createOPTIONSIcapResponse());
		String response = getBufferContent(embeddedChannel.readOutbound());
		assertResponse(DataMockery.createOPTIONSResponse(),response);
	}

	@Test
	public void encodeOPTIONSResponseWithBody() throws UnsupportedEncodingException {
		embeddedChannel.writeOutbound(DataMockery.createOPTIONSIcapResponseWithBody());
		String response = getBufferContent(embeddedChannel.readOutbound());
		assertResponse(DataMockery.createOPTIONSResponseWithBody(),response);
		embeddedChannel.writeOutbound(DataMockery.createOPTIONSIcapChunk());
		String chunk1 = getBufferContent(embeddedChannel.readOutbound());
		assertResponse(DataMockery.createOPTIONSChunk(),chunk1);
		embeddedChannel.writeOutbound(DataMockery.createOPTIONSLastIcapChunk());
		String lastChunk = getBufferContent(embeddedChannel.readOutbound());
		assertResponse(DataMockery.createOPTIONSLastChunk(),lastChunk);
	}
	
	@Test
	public void encodeREQMODResponseWithoutBody() throws UnsupportedEncodingException {
		embeddedChannel.writeOutbound(DataMockery.createREQMODWithGetRequestNoBodyIcapResponse());
		String response = getBufferContent(embeddedChannel.readOutbound());
		assertResponse(DataMockery.createREQMODWithGetRequestResponse(),response);
	}
	
	@Test
	public void encodeRESPMODResponseWithoutBody() throws UnsupportedEncodingException {
		embeddedChannel.writeOutbound(DataMockery.createRESPMODWithGetRequestNoBodyIcapResponse());
		String response = getBufferContent(embeddedChannel.readOutbound());
		assertResponse(DataMockery.createRESPMODWithGetRequestNoBodyResponse(),response);
	}
	
	@Test
	public void encodeREQMODResponseWithTwoChunkBody() throws UnsupportedEncodingException {
		embeddedChannel.writeOutbound(DataMockery.createREQMODWithTwoChunkBodyIcapResponse());
		String response = getBufferContent(embeddedChannel.readOutbound());
		assertResponse(DataMockery.createREQMODWithTwoChunkBodyResponse(),response);
		embeddedChannel.writeOutbound(DataMockery.createREQMODWithTwoChunkBodyIcapChunkOne());
		String chunk1 = getBufferContent(embeddedChannel.readOutbound());
		assertResponse(DataMockery.createREQMODWithTowChunkBodyChunkOne(),chunk1);
		embeddedChannel.writeOutbound(DataMockery.createREQMODWithTwoChunkBodyIcapChunkTwo());
		String chunk2 = getBufferContent(embeddedChannel.readOutbound());
		assertResponse(DataMockery.createREQMODWithTwoChunkBodyChunkTwo(),chunk2);
		embeddedChannel.writeOutbound(DataMockery.createREQMODWithTwoChunkBodyIcapChunkThree());
		String chunk3 = getBufferContent(embeddedChannel.readOutbound());
		assertResponse(DataMockery.createREQMODWithTwoChunkBodyChunkThree(),chunk3);
	}
	
	@Test
	public void encodeREQMODResponseWithHttpResponse() throws UnsupportedEncodingException {
		embeddedChannel.writeOutbound(DataMockery.createREQMODResponseContainingHttpResponseIcapResponse());
		String response = getBufferContent(embeddedChannel.readOutbound());
		assertResponse(DataMockery.createREQMODResponseContainingHttpResponse(),response);
	}
}
