/**
 * Copyright 2015 Smart Community Lab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.smartcommunitylab.carpooling.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 
 * @author nawazk.
 *
 */
@Document
public class TravelRequest {
	@Id
	private String id;
	/** From zone (reqd).**/
	private Zone from;
	/** To zone (reqd).**/
	private Zone to;
	/** requested date/time of trip - reqd.**/
	private long when;
	/** who (reqd).**/
	private String userId;
	/** whether to enable match notifications.**/
	private boolean monitored;
	/** no of post required. **/
	private int nrOfPost;

	public TravelRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TravelRequest(Zone from, Zone to, long when, String userId, boolean monitored, int nrOfPost) {
		super();
		this.from = from;
		this.to = to;
		this.when = when;
		this.userId = userId;
		this.monitored = monitored;
		this.nrOfPost = nrOfPost;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Zone getFrom() {
		return from;
	}

	public void setFrom(Zone from) {
		this.from = from;
	}

	public Zone getTo() {
		return to;
	}

	public void setTo(Zone to) {
		this.to = to;
	}

	public long getWhen() {
		return when;
	}

	public void setWhen(long when) {
		this.when = when;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean isMonitored() {
		return monitored;
	}

	public void setMonitored(boolean monitored) {
		this.monitored = monitored;
	}

	public int getNrOfPost() {
		return nrOfPost;
	}

	public void setNrOfPost(int nrOfPost) {
		this.nrOfPost = nrOfPost;
	}

	@Override
	public String toString() {
		return "TravelRequest [id=" + id + ", from=" + from + ", to=" + to + ", timestamp=" + when + ", userId="
				+ userId + ", monitored=" + monitored + "]";
	}

}
