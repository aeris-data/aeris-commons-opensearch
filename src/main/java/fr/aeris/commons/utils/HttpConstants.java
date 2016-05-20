/**
 * Copyright 2008 - 2009 Pro-Netics S.P.A. 
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License"); 
 *    you may not use this file except in compliance with the License. 
 *    You may obtain a copy of the License at 
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0 
 * 
 *    Unless required by applicable law or agreed to in writing, software 
 *    distributed under the License is distributed on an "AS IS" BASIS, 
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    See the License for the specific language governing permissions and 
 *    limitations under the License. 
 */
package fr.aeris.commons.utils;

/**
 * Constants used in the server module for interacting through HTTP requests.
 * 
 * @author Sergio Bossa
 */
public interface HttpConstants {

	// Request parameters:
	public static final String TERMS_PARAMETER = "q";
	public static final String PARENTIDENTIFIER_PARAMETER = "parentIdentifier";
	public static final String START_PARAMETER = "sd";
	public static final String END_PARAMETER = "ed";
	// Default values:
	public static final int PAGE_DEFAULT_VALUE = 10;
	public static final int MAX_DEFAULT_VALUE = 10;

	// Query parameters
	public static final String QUERY_PARENTIDENTIFIER_LN = "eo:parentIdentifier";
	public static final String QUERY_START_LN = "time:start";
	public static final String QUERY_END_LN = "time:end";
}