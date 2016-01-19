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

public class RecurrentBooking {

	/** traveller description(reqd). **/
	private Traveller traveller;
	/** whether booking has been requested/accepted/rejected by driver. **/
	private int accepted = 0; // {0,1,-1}

	public RecurrentBooking() {
	}

	public RecurrentBooking(Traveller traveller, int accepted) {
		super();
		this.traveller = traveller;
		this.accepted = accepted;
	}

	public Traveller getTraveller() {
		return traveller;
	}

	public void setTraveller(Traveller traveller) {
		this.traveller = traveller;
	}

	public int getAccepted() {
		return accepted;
	}

	public void setAccepted(int accepted) {
		this.accepted = accepted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((traveller == null) ? 0 : traveller.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecurrentBooking other = (RecurrentBooking) obj;
		if (traveller == null) {
			if (other.traveller != null)
				return false;
		} else if (!traveller.equals(other.traveller))
			return false;
		return true;
	}

}