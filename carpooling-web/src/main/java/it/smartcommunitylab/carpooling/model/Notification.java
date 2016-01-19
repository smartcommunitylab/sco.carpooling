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

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 
 * @author nawazk
 *
 */
@Document
public class Notification {

	@Id
	private String id;
	/** receiver of this notification.**/
	private String targetUserId;
	/** type[Chat,ParticipationRequest,ParticipationResponse,TripAvailability].**/
	private String type;
	/** data map. **/
	private Map<String, String> data = new HashMap<String, String>();
	/** read/unread flag.**/
	private boolean status;
	/** travelId.**/
	private String travelId;
	/** timestamp.(server side time of creation) **/
	private long timestamp;

	public Notification() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Notification(String targetUserId, String type, Map<String, String> data, boolean status, String travelId,
			long timestamp) {
		super();
		this.targetUserId = targetUserId;
		this.type = type;
		this.data = data;
		this.status = status;
		this.travelId = travelId;
		this.timestamp = timestamp;
	}

	public String getId() {
		return id;
	}

	public String getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(String targetUserId) {
		this.targetUserId = targetUserId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getTravelId() {
		return travelId;
	}

	public void setTravelId(String travelId) {
		this.travelId = travelId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
