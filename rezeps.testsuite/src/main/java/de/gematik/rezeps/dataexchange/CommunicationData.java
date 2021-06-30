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
import java.util.Map;
import java.util.Objects;

public class CommunicationData implements Serializable {
  private static final long serialVersionUID = 6784668833636913083L;
  private String taskReference;
  private String message;
  private CommunicationType type = null;
  private String sender;
  private String hrPickupCode = "";
  private String dmcCode = "";

  public String getHrPickupCode() {
    return hrPickupCode;
  }

  public void setHrPickupCode(String hrPickupCode) {
    this.hrPickupCode = hrPickupCode;
  }

  public String getDmcCode() {
    return dmcCode;
  }

  public void setDmcCode(String dmcCode) {
    this.dmcCode = dmcCode;
  }

  public CommunicationData() {
    // empty Constructor cause it`s a simple PoJo
  }

  public String getTaskReference() {
    return taskReference;
  }

  public void setTaskReference(String taskReference) {
    this.taskReference = taskReference;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CommunicationData that = (CommunicationData) o;

    return (this.type.equals(that.type))
        && (this.taskReference.equals(that.taskReference))
        && (this.message.equals(that.message));
  }

  @Override
  public int hashCode() {
    return Objects.hash(message)
        + (taskReference != null ? taskReference.hashCode() : 0)
        + (type != null ? type.hashCode() : 0);
  }

  /**
   * Setzt den Communications {@link CommunicationType}
   *
   * @param type {@link CommunicationType}
   */
  public void setType(CommunicationType type) {
    this.type = type;
  }

  public CommunicationType getType() {
    return this.type;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getSender() {
    return this.sender;
  }

  public Map<String, String> getReplayContent() {
    return Map.of("info_text", message, "pickUpCodeHR", hrPickupCode, "pickUpCodeDMC", dmcCode);
  }
}
