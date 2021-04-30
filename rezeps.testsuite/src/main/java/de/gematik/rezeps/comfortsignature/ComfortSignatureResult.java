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

package de.gematik.rezeps.comfortsignature;

/** Repräsentiert das Ergebnis eines Aufrufs von ActivateComfortSignature beim Konnektor. */
public class ComfortSignatureResult {

  public static final String STATUS_OK = "OK";
  public static final String SIGNATURE_MODE_COMFORT = "COMFORT";

  private String status;
  private String signatureMode;

  public ComfortSignatureResult() {}

  public ComfortSignatureResult(String status, String signatureMode) {
    this.status = status;
    this.signatureMode = signatureMode;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getSignatureMode() {
    return signatureMode;
  }

  public void setSignatureMode(String signatureMode) {
    this.signatureMode = signatureMode;
  }

  /**
   * Prüft, ob die Komfortsignatur aktiviert ist. Dazu muss der Status "OK" und der Signatur-Modus
   * "COMFORT" sein.
   *
   * @return true, falls die Komfortsignatur aktiviert ist, andernfalls false.
   */
  public boolean isComfortSignatureActivated() {
    boolean isComfortSignatureActivated = false;
    if (status != null
        && status.equals(STATUS_OK)
        && signatureMode != null
        && signatureMode.equals(SIGNATURE_MODE_COMFORT)) {
      isComfortSignatureActivated = true;
    }
    return isComfortSignatureActivated;
  }

  /**
   * Prüft, ob die Komfortsignatur erfolgreich deaktiviert werden konnte.
   *
   * @return true, falls die Komfortsignatur erfolgreich deaktiviert werden konnte, andernfalls
   *     false.
   */
  public boolean isDeactivateComfortSignature() {
    return ((status != null)
        && (!SIGNATURE_MODE_COMFORT.equalsIgnoreCase(signatureMode))
        && (status.equals(STATUS_OK)));
  }
}
