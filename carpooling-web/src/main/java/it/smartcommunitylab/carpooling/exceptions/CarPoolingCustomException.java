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

package it.smartcommunitylab.carpooling.exceptions;

import it.smartcommunitylab.carpooling.model.Response;

public class CarPoolingCustomException extends Exception {

	public String erroMsg;
	public int errorCode;
	public Response body;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CarPoolingCustomException(String msg) {
		super(msg);
	}

	public CarPoolingCustomException(int errorCode, String msg) {
		super(msg);
		this.errorCode = errorCode;
		this.erroMsg = msg;
		this.body = new Response<Void>(errorCode, msg);
	}

	public Response getBody() {
		return body;
	}

}