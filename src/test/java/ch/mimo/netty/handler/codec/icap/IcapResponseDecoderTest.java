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

public class IcapResponseDecoderTest extends AbstractIcapTest {

	private EmbeddedChannel embeddedChannel;

	@Before
	public void setUp() throws UnsupportedEncodingException {
		embeddedChannel = new EmbeddedChannel(new IcapResponseDecoder());
	}
	
	@Test
	public void decodeOPTIONSResponse() throws UnsupportedEncodingException {
		embeddedChannel.writeInbound(DataMockery.createOPTIONSResponse());
		IcapResponse response = embeddedChannel.readInbound();
		doOutput(response.toString());
		DataMockery.assertOPTIONSResponse(response);
	}
	
	@Test
	public void decodeREQMODResponseWithGetRequestNoBody() throws UnsupportedEncodingException {
		embeddedChannel.writeInbound(DataMockery.createREQMODWithGetRequestResponse());
		IcapResponse response = embeddedChannel.readInbound();
		doOutput(response.toString());
		DataMockery.assertREQMODWithGetRequestResponse(response);
	}
	
	@Test
	public void decodeRESPMODWithGetRequestNoBody() throws UnsupportedEncodingException {
		embeddedChannel.writeInbound(DataMockery.createRESPMODWithGetRequestNoBodyResponse());
		IcapResponse response = embeddedChannel.readInbound();
		doOutput(response.toString());
		DataMockery.assertRESPMODWithGetRequestNoBodyResponse(response);
	}

	@Test
	public void decodeREQMODResponseWithGetRequestAndBody() throws UnsupportedEncodingException {
		embeddedChannel.writeInbound(DataMockery.createREQMODWithImplicitTwoChunkBodyResponse());
		IcapResponse response = embeddedChannel.readInbound();
		doOutput(response.toString());
		DataMockery.assertCreateREQMODWithImplicitTwoChunkBodyResponse(response);
		IcapChunk chunk1 = embeddedChannel.readInbound();
		DataMockery.assertCreateREQMODWithTwoChunkBodyFirstChunk(chunk1);
		IcapChunk chunk2 = embeddedChannel.readInbound();
		DataMockery.assertCreateREQMODWithTwoChunkBodySecondChunk(chunk2);
		IcapChunk chunk3 = embeddedChannel.readInbound();
		DataMockery.assertCreateREQMODWithTwoChunkBodyThirdChunk(chunk3);
	}
	
	@Test 
	public void decodeRESPMODWithGetRequestAndPreview() throws UnsupportedEncodingException {
		embeddedChannel.writeInbound(DataMockery.createRESPMODWithGetRequestAndPreviewResponse());
		IcapResponse response = embeddedChannel.readInbound();
		doOutput(response.toString());
		DataMockery.assertCreateRESPMODWithGetRequestAndPreviewResponse(response);
		IcapChunk previewChunk = embeddedChannel.readInbound();
		doOutput(previewChunk.toString());
		DataMockery.assertCreateRESPMODWithGetRequestAndPreviewChunk(previewChunk);
		IcapChunk lastChunk = embeddedChannel.readInbound();
		doOutput(lastChunk.toString());
		DataMockery.assertCreateRESPMODWithGetRequestAndPreviewLastChunk(lastChunk);
	}
	
	@Test
	public void decode100Continue() throws UnsupportedEncodingException {
		embeddedChannel.writeInbound(DataMockery.create100ContinueResponse());
		IcapResponse result = embeddedChannel.readInbound();
		assertNotNull("The decoded icap request instance is null",result);
		assertEquals("wrong response status code",IcapResponseStatus.CONTINUE,result.getStatus());
	}
	
	@Test
	public void decode100ContineFollowedBy204NoContent() throws UnsupportedEncodingException {
		embeddedChannel.writeInbound(DataMockery.create100ContinueResponse());
		IcapResponse result = embeddedChannel.readInbound();
		assertNotNull("The decoded icap request instance is null",result);
		assertEquals("wrong response status code",IcapResponseStatus.CONTINUE,result.getStatus());
		embeddedChannel.writeInbound(DataMockery.create204NoContentResponse());
		result = embeddedChannel.readInbound();
		assertNotNull("The decoded icap request instance is null",result);
		assertEquals("wrong response status code",IcapResponseStatus.NO_CONTENT,result.getStatus());
	}
	
	@Test
	public void decodeRESPMODWithPreviewFollowedByREQPMODFollwedBy100Continue() throws UnsupportedEncodingException {
		embeddedChannel.writeInbound(DataMockery.createRESPMODWithGetRequestAndPreviewResponse());
		Object object = embeddedChannel.readInbound();
		assertNotNull("RESPMOD request was null",object);
		assertTrue("wrong object type",object instanceof IcapResponse);
		object = embeddedChannel.readInbound();
		assertNotNull("RESPMOD preview chunk was null",object);
		assertTrue("wrong object type",object instanceof IcapChunk);
		IcapChunk chunk = (IcapChunk)object;
		assertTrue("chunk is not preview",chunk.isPreviewChunk());
		object = embeddedChannel.readInbound();
		assertNotNull("preview chunk trailer is null",object);
		assertTrue("wrong object type",object instanceof IcapChunkTrailer);
		IcapChunkTrailer trailer = (IcapChunkTrailer)object;
		assertTrue("chunk trailer is not marked as preview",trailer.isPreviewChunk());
		embeddedChannel.writeInbound(DataMockery.createREQMODWithGetRequestResponse());
		object = embeddedChannel.readInbound();
		assertNotNull("REQMOD request was null",object);
		assertTrue("wrong object type",object instanceof IcapResponse);
		embeddedChannel.writeInbound(DataMockery.create100ContinueResponse());
		IcapResponse result = embeddedChannel.readInbound();
		assertNotNull("The decoded icap request instance is null",result);
		assertEquals("wrong response status code",IcapResponseStatus.CONTINUE,result.getStatus());
	}
	
	@Test
	public void decodeREQMODResponseWithHttpResponse() throws UnsupportedEncodingException {
		embeddedChannel.writeInbound(DataMockery.createREQMODResponseContainingHttpResponse());
		IcapResponse response = embeddedChannel.readInbound();
		DataMockery.assertREQMODResponseContainingHttpResponse(response);
	}
	
	@Test
	public void decode204ResponseWithoutEncapsulatedHeader() throws UnsupportedEncodingException {
		embeddedChannel.writeInbound(DataMockery.create204ResponseWithoutEncapsulatedHeader());
		IcapResponse response = embeddedChannel.readInbound();
		assertNotNull("The decoded icap response instance is null",response);
	}
	
	@Test
	public void decode100ContinueWithoutEncapsulatedHeader() throws UnsupportedEncodingException {
		embeddedChannel.writeInbound(DataMockery.create100ResponseWithoutEncapsulatedHeader());
		IcapResponse response = embeddedChannel.readInbound();
		assertNotNull("The decoded icap response instance is null",response);
	}
}
