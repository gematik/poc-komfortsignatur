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

package de.gematik.rezeps.signature;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Repräsentiert das Ergebnis einer Signatur-Erstellung. */
public class SignDocumentResult implements Serializable {

  private static final Logger LOGGER = LoggerFactory.getLogger(SignDocumentResult.class);

  public static final String STATUS_OK = "OK";
  public static final String MIME_TYPE_BASE_64_DATA = "text/plain; charset=utf-8";
  public static final String TYPE_BASE_64_SIGNATURE = "urn:ietf:rfc:5652";

  private static final boolean RESPONSE_VALID = true;
  private static final long serialVersionUID = -7026986518818356677L;

  private String status;
  private String mimeTypeBase64Data;
  private String typeBase64Signature;
  private byte[] signedBundle;

  public SignDocumentResult(
      String status, String mimeTypeBase64Data, String typeBase64Signature, byte[] signedBundle) {
    this.status = status;
    this.mimeTypeBase64Data = mimeTypeBase64Data;
    this.typeBase64Signature = typeBase64Signature;
    this.signedBundle = signedBundle;
  }

  public byte[] getSignedBundle() {
    return signedBundle;
  }

  public void setSignedBundle(byte[] signedBundle) {
    this.signedBundle = signedBundle;
  }

  /**
   * Prüft, ob die Erstellung der Signatur erfolgreich war.
   *
   * @return true, wenn die Signatur erfolgreich erstellt werden konnte, andernfalls false.
   */
  public boolean isValidResponse() {

    LOGGER.info("Response status: {}", status);
    if (status == null || !status.equals(STATUS_OK)) {
      return false;
    }

    LOGGER.info("Mime-type of base64 data: {}", mimeTypeBase64Data);
    if (mimeTypeBase64Data != null && !mimeTypeBase64Data.equals(MIME_TYPE_BASE_64_DATA)) {
      return false;
    }

    LOGGER.info("Type of base64 signature: {}", typeBase64Signature);
    if (typeBase64Signature == null || !typeBase64Signature.equals(TYPE_BASE_64_SIGNATURE)) {
      return false;
    }

    if (signedBundle == null || signedBundle.length == 0) {
      LOGGER.info("Der signierte Verordnungsdatensatz ist leer");
      return false;
    }
    LOGGER.info("Länge des signierten Verordnungsdatensatzes: {}", signedBundle.length);

    return RESPONSE_VALID;
  }
}
