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

package de.gematik.rezeps.cardterminal;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardterminalservice.v1.RequestCardResponse;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CardRequestor {

  @Autowired PerformRequestCard performRequestCard;

  /**
   * Ruft die Operation RequestCard des Konnektors auf.
   *
   * @param invocationContext Kontext, der für den Aufruf verwendet werden soll.
   * @param ctID ID des Terminals für das die Karte angefordert werden soll.
   * @param slot Nummer des Slots für den die Karte angefordert werden soll.
   * @return Das Ergebnis des Aufrufs für die weitere Verarbeitung.
   */
  public RequestCardResult performRequestCard(
      InvocationContext invocationContext, String ctID, int slot) throws IOException {
    RequestCardResponse requestCardResponse =
        performRequestCard.performRequestCard(invocationContext, ctID, slot);

    RequestCardResult requestCardResult = new RequestCardResult();
    if (requestCardResponse != null) {
      Status status = requestCardResponse.getStatus();
      if (status != null) {
        requestCardResult.setStatus(status.getResult());
      }
      CardInfoType card = requestCardResponse.getCard();
      if (card != null) {
        requestCardResult.setCardHandle(card.getCardHandle());
      }
    }
    return requestCardResult;
  }
}
