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

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

/**
 * 
 * @author nawazk
 *
 */
public class TravelProfile {

	/** list of user usual routes.**/
	private List<ReqRoute> routes = new ArrayList<TravelProfile.ReqRoute>();

	public TravelProfile() {
		//		super();
		// TODO Auto-generated constructor stub
	}

	public TravelProfile(List<ReqRoute> routes) {
		super();
		this.routes = routes;
	}

	public List<ReqRoute> getRoutes() {
		return routes;
	}

	public void setRoutes(List<ReqRoute> routes) {
		this.routes = routes;
	}

	static class ReqRoute {

		/** id of route(reqd). **/
		@Id
		private String id;
		/** name of route(reqd). **/
		private String name;
		/** origin(reqd). **/
		private Zone from;
		/** destination(reqd). **/
		private Zone to;
		/** interval starttime (mins of day) -reqd. **/
		private int timeFrom;
		/** interval endtime (mins of day) - reqd). **/
		private int timeTo;
		/** whether route is active for monitoring. **/
		private boolean enabled = true;
		/** day of week when route is due. **/
		private int[] recurrency; 

		public ReqRoute() {
			super();
			// TODO Auto-generated constructor stub
		}

		public ReqRoute(String name, Zone origin, Zone destination, int timeFrom, int timeTo, boolean enabled,
				int[] recurrency) {
			super();
			this.name = name;
			this.from = origin;
			this.to = destination;
			this.timeFrom = timeFrom;
			this.timeTo = timeTo;
			this.enabled = enabled;
			this.recurrency = recurrency;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
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

		public int getTimeFrom() {
			return timeFrom;
		}

		public void setTimeFrom(int timeFrom) {
			this.timeFrom = timeFrom;
		}

		public int getTimeTo() {
			return timeTo;
		}

		public void setTimeTo(int timeTo) {
			this.timeTo = timeTo;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public int[] getRecurrency() {
			return recurrency;
		}

		public void setRecurrency(int[] recurrency) {
			this.recurrency = recurrency;
		}

	}

}
