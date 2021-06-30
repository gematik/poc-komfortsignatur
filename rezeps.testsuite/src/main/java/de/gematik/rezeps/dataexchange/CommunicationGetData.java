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

import de.gematik.test.erezept.fd.fhir.adapter.CommunicationType;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class CommunicationGetData implements Serializable {
  private static final long serialVersionUID = -6618749494672814720L;
  private int statusCode;
  private List<CommunicationData> communications;

  public CommunicationGetData(int statusCode) {
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public List<CommunicationData> getCommunications() {
    return communications;
  }

  public void setCommunications(List<CommunicationData> communications) {
    this.communications = communications;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CommunicationGetData that = (CommunicationGetData) o;

    return statusCode == that.statusCode && communications == that.communications;
  }

  @Override
  public int hashCode() {
    return Objects.hash(statusCode) + (communications != null ? communications.hashCode() : 0);
  }

  /**
   * check StatusCode 200 and communication list has one {@link CommunicationData} and is {@link
   * CommunicationType} given parameter type
   *
   * @param type {@link CommunicationType}
   * @return true, if StatusCode is 200, list size is 1 and List Object CommunicationType is given
   *     parameter type
   */
  public boolean isValidResponse(CommunicationType type) {
    return (this.statusCode == 200)
        && (this.communications.size() == 1 && this.communications.get(0).getType() == type);
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
