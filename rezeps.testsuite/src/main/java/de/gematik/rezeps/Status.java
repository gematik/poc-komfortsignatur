/*
 * Copyright (c) 2021 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.rezeps;

/** PoJo Status that holds a StatusName and a StatusCode */
public abstract class Status {

  public Status() {}

  protected Status(int statusCode, String status) {
    this.statusCode = statusCode;
    this.statusName = status;
  }

  protected int statusCode;
  protected String statusName;

  /**
   * Get the Status code
   *
   * @return int Status code
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Set the Status code
   *
   * @param statusCode int status code
   */
  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  /**
   * gets Status
   *
   * @return {@link String} the status
   */
  public String getStatus() {
    return statusName;
  }

  /**
   * Set Status
   *
   * @param statusName String
   */
  public void setStatus(String statusName) {
    this.statusName = statusName;
  }
}
