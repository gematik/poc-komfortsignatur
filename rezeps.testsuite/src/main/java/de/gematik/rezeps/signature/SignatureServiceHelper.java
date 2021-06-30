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

/** Unterstützt den Zugriff auf den SignatureService. */
public class SignatureServiceHelper {

  public static final String SIGNATURE_TYPE = "urn:ietf:rfc:5652";
  public static final String TV_MODE_NONE = "NONE";
  public static final boolean INCLUDE_REVOCATION_INFO = true;

  private SignatureServiceHelper() {}
}
