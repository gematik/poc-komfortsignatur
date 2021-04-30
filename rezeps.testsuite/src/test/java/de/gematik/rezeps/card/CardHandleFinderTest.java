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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservice.v8.Cards;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CardHandleFinderTest {

  private static final String EXPECTED_CARD_HANDLE = "card_handle_hba";

  @Test
  public void shouldDetermineCardHandle() throws IOException {
    PerformGetCards performGetCards = mock(PerformGetCards.class);
    InvocationContext invocationContext =
        new InvocationContext("mandant001", "clientSystem001", "workplace001");
    GetCardsResponse getCardsResponse = determineGetCardsResponse();
    when(performGetCards.performGetCards(invocationContext)).thenReturn(getCardsResponse);

    CardHandleFinder cardHandleFinder = new CardHandleFinder();
    cardHandleFinder.performGetCards = performGetCards;

    String cardHandle = cardHandleFinder.determineHbaHandle(invocationContext);
    Assert.assertEquals(EXPECTED_CARD_HANDLE, cardHandle);
  }

  private GetCardsResponse determineGetCardsResponse() {
    GetCardsResponse getCardsResponse = new GetCardsResponse();

    CardInfoType cardInfoTypeEgk = new CardInfoType();
    cardInfoTypeEgk.setCardType(CardTypeType.EGK);
    cardInfoTypeEgk.setCardHandle("card_handle_egk");

    CardInfoType cardInfoTypeHba = new CardInfoType();
    cardInfoTypeHba.setCardType(CardTypeType.HBA);
    cardInfoTypeHba.setCardHandle("card_handle_hba");

    List<CardInfoType> cardInfoTypeList = new ArrayList<>();
    cardInfoTypeList.add(cardInfoTypeEgk);
    cardInfoTypeList.add(cardInfoTypeHba);

    Cards cards = mock(Cards.class);
    when(cards.getCard()).thenReturn(cardInfoTypeList);
    getCardsResponse.setCards(cards);
    return getCardsResponse;
  }
}
