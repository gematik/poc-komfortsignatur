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
import de.gematik.rezeps.util.CommonUtils;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CardHandleFinder {

  private static final Logger LOGGER = LoggerFactory.getLogger(CardHandleFinder.class);
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
   * Bestimmt das Handle eines <b>bestimmten</b> HBAs.
   *
   * @param invocationContext Aufrufkontext für GetCards-Request
   * @param iccsn ICCSN der Karte
   * @return Gefundenes HBA-Handle.
   */
  public String determineHbaHandle(InvocationContext invocationContext, String iccsn)
      throws IOException {
    return determineCardHandle(invocationContext, CardTypeType.HBA, iccsn);
  }

  /**
   * Bestimmt das Handle einer SMC-B
   *
   * @param invocationContext Aufrufkontext für GetCards-Request
   * @return Gefundenes SMC-B-Handle.
   */
  public String determineSmcBHandle(InvocationContext invocationContext) throws IOException {
    return determineCardHandle(invocationContext, CardTypeType.SMC_B, null);
  }

  /**
   * Bestimmt das Handle einer ZOD_2_0
   *
   * @param invocationContext Aufrufkontext für GetCards-Request
   * @return Gefundenes ZOD_2_0.
   */
  public String determineZOD20Handle(InvocationContext invocationContext) throws IOException {
    return determineCardHandle(invocationContext, CardTypeType.ZOD_2_0, null);
  }

  /**
   * Bestimmt das Handle einer ZOD_2_0
   *
   * @param invocationContext Aufrufkontext für GetCards-Request
   * @param iccsn ICCSN der Karte
   * @return Gefundenes ZOD_2_0.
   */
  public String determineZOD20Handle(InvocationContext invocationContext, String iccsn)
      throws IOException {
    return determineCardHandle(invocationContext, CardTypeType.ZOD_2_0, iccsn);
  }

  /**
   * Bestimmt das Handle einer HBA_Q_SIG
   *
   * @param invocationContext Aufrufkontext für GetCards-Request
   * @return Gefundenes HBA_Q_SIG.
   */
  public String determineHBAQSigHandle(InvocationContext invocationContext) throws IOException {
    return determineCardHandle(invocationContext, CardTypeType.HBA_Q_SIG, null);
  }

  /**
   * Bestimmt das Handle einer <b>bestimmten</b> HBA_Q_SIG
   *
   * @param invocationContext Aufrufkontext für GetCards-Request
   * @param iccsn ICCSN der Karte
   * @return Gefundenes HBA_Q_SIG.
   */
  public String determineHBAQSigHandle(InvocationContext invocationContext, String iccsn)
      throws IOException {
    return determineCardHandle(invocationContext, CardTypeType.HBA_Q_SIG, iccsn);
  }

  /**
   * Bestimmt das Handle einer <b>bestimmten</b> SMC-B
   *
   * @param invocationContext Aufrufkontext für GetCards-Request
   * @param iccsn ICCSN der Karte
   * @return Gefundenes SMC-B-Handle.
   */
  public String determineSmcBHandle(InvocationContext invocationContext, String iccsn)
      throws IOException {
    return determineCardHandle(invocationContext, CardTypeType.SMC_B, iccsn);
  }

  private String determineCardHandle(InvocationContext invocationContext, CardTypeType cardType)
      throws IOException {
    return determineCardHandle(invocationContext, cardType, null);
  }

  private void logCardData(CardInfoType card) {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          MessageFormat.format(
              "[{0}] von \"{1}\" ICCSN: {2} in Slot {3}",
              card.getCardType().name(),
              card.getCardHolderName(),
              card.getIccsn(),
              card.getSlotId()));
    }
  }

  private String determineCardHandle(
      InvocationContext invocationContext, CardTypeType cardType, String iccsn) throws IOException {
    String cardHandle = null;
    GetCardsResponse response = performGetCards.performGetCards(invocationContext);
    List<CardInfoType> cards = response.getCards().getCard();
    if (!CommonUtils.isNullOrEmpty(iccsn)) {
      CardInfoType card =
          cards.stream()
              .filter(c -> iccsn.equals(c.getIccsn()))
              .filter(type -> cardType.equals(type.getCardType()))
              .findFirst()
              .orElse(null);
      if (card != null) {
        logCardData(card);
        cardHandle = card.getCardHandle();
      }
    } else {
      CardInfoType card =
          cards.stream()
              .filter(type -> cardType.equals(type.getCardType()))
              .findFirst()
              .orElse(null);
      if (card != null) {
        logCardData(card);
        cardHandle = card.getCardHandle();
      }
    }
    return cardHandle;
  }
}
