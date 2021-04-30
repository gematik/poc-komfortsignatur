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

package de.gematik.rezeps.card;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CardHandleFinder {

  @Autowired PerformGetCards performGetCards;

  /**
   * Bestimmt das Handle eines HBAs.
   *
   * @param invocationContext Aufrufkontext für GetCards-Request
   * @return Gefundenes HBA-Handle.
   */
  public String determineHbaHandle(InvocationContext invocationContext) throws IOException {
    return determineCardHandle(invocationContext, CardTypeType.HBA);
  }

  /**
   * Bestimmt das Handle einer SMC-B
   *
   * @param invocationContext Aufrufkontext für GetCards-Request
   * @return Gefundenes SMC-B-Handle.
   */
  public String determineSmcBHandle(InvocationContext invocationContext) throws IOException {
    return determineCardHandle(invocationContext, CardTypeType.SMC_B);
  }

  private String determineCardHandle(InvocationContext invocationContext, CardTypeType cardType)
      throws IOException {
    String cardHandle = null;
    GetCardsResponse response = performGetCards.performGetCards(invocationContext);
    List<CardInfoType> cards = response.getCards().getCard();
    for (CardInfoType card : cards) {
      if (card.getCardType().value().equals(cardType.value())) {
        cardHandle = card.getCardHandle();
      }
    }
    return cardHandle;
  }
}
