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

import io.netty.handler.codec.http.HttpContent;

/**
 * This is the main ICAP Chunk. In essence it is a @see {@link HttpChunk} with the addition of 
 * Preview controls and members.
 * 
 * @author Michael Mimo Moratti (mimo@mimo.ch)
 *
 */
public interface IcapChunk extends HttpContent {

	/**
	 * Toggles whether this chunk belongs to a preview message.
	 * 
	 * @param preview boolean true to indicate it is a preview chunk
	 */
	void setPreviewChunk(boolean preview);
	
	/**
	 * Gets whether this chunk belongs to a preview message.
	 * 
	 * @return boolean true if this chunk is preview.
	 */
	boolean isPreviewChunk();
	
	/**
	 * Toggles whether this chunk belongs to a early terminated preview message.
	 * 
	 * @param earlyTermination boolean true if the preview message is early terminated.
	 */
	void setEarlyTermination(boolean earlyTermination);
	
	/**
	 * Gets whether this chunk belongs to a early terminated preview message.
	 * 
	 * @return boolean true if the preview message is early terminated.
	 */
	boolean isEarlyTerminated();

	boolean isLast();
}
