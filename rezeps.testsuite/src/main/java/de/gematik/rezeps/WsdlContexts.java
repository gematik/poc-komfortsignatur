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

/* Sammelt die Kontext-Pfade der Java-Klassen, die aus den WSDL-Dateien generiert werden.
 */
public class WsdlContexts {

  private WsdlContexts() {}

  public static final String CARD_SERVICE_CONTEXT = "de.gematik.ws.conn.cardservice.v8";
  public static final String CARD_TERMINAL_SERVICE_CONTEXT =
      "de.gematik.ws.conn.cardterminalservice.v1";
  public static final String CERTIFICATE_SERICE_CONTEXT =
      "de.gematik.ws.conn.certificateservice.v6";
  public static final String EVENT_SERVICE_CONTEXT = "de.gematik.ws.conn.eventservice.v7";
  public static final String SIGNATURE_SERVICE_CONTEXT = "de.gematik.ws.conn.signatureservice.v7";
}
