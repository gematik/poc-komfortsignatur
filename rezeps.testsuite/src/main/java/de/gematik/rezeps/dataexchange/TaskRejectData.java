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

package de.gematik.rezeps.dataexchange;

import java.io.Serializable;
import java.util.Objects;

public class TaskRejectData implements Serializable {

  private static final long serialVersionUID = 4842570682956351703L;
  private int statusCode;

  public TaskRejectData(int statusCode) {
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaskRejectData that = (TaskRejectData) o;
    return statusCode == that.statusCode;
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(statusCode);
    result = 31 * result;
    return result;
  }

  /**
   * check StatusCode
   *
   * @param expectedStatusCode Statuscode that is expected
   * @return true, if StatusCode is exact the expected
   */
  public boolean isValidStatusCode(int expectedStatusCode) {
    return (this.statusCode == expectedStatusCode);
  }
}
