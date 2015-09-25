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

/**
 * 
 * @author nawazk
 *
 */
public class Recurrency {

	/** travel times(mins of day)-reqd.**/
	private int time;
	/** days of week when route is due [0,1,2,3,4,5,6].**/
	private int[] days = new int[] { 0, 1, 2, 3, 4, 5, 6 };
	/** days of month when route is due.**/
	private List<Integer> dates = new ArrayList<Integer>();

	public Recurrency() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Recurrency(int time, int[] days, List<Integer> dates) {
		super();
		this.time = time;
		this.days = days;
		this.dates = dates;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int[] getDays() {
		return days;
	}

	public void setDays(int[] days) {
		this.days = days;
	}

	public List<Integer> getDates() {
		return dates;
	}

	public void setDates(List<Integer> dates) {
		this.dates = dates;
	}

}
