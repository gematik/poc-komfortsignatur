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

package de.gematik.rezeps.bundle;

import java.io.Serializable;

public class Medication implements Serializable {

  private static final long serialVersionUID = -5058085280525379689L;
  private String pznValue;
  private String pznText;

  public Medication(String pznValue, String pznText) {
    this.pznValue = pznValue;
    this.pznText = pznText;
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
}
