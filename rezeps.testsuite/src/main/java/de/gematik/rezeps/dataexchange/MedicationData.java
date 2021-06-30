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

public class MedicationData implements Serializable {

  private static final long serialVersionUID = -886060966312334335L;
  private String kvnr;
  private String pznValue;
  private String pznText;
  private String prescriptionId;

  public MedicationData(String kvnr, String pznValue, String pznText, String prescriptionId) {
    this.kvnr = kvnr;
    this.pznValue = pznValue;
    this.pznText = pznText;
    this.prescriptionId = prescriptionId;
  }

  public String getKvnr() {
    return kvnr;
  }

  public void setKvnr(String kvnr) {
    this.kvnr = kvnr;
  }

  public String getPznValue() {
    return pznValue;
  }

  public void setPznValue(String pznValue) {
    this.pznValue = pznValue;
  }

  public String getPznText() {
    return pznText;
  }

  public void setPznText(String pznText) {
    this.pznText = pznText;
  }

  public String getPrescriptionId() {
    return prescriptionId;
  }

  public void setPrescriptionId(String prescriptionId) {
    this.prescriptionId = prescriptionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MedicationData other = (MedicationData) o;
    return this.kvnr == other.kvnr
        && this.pznText == other.pznText
        && this.pznValue == other.pznValue
        && this.prescriptionId == other.prescriptionId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(kvnr, pznText, pznValue, prescriptionId);
  }
}
