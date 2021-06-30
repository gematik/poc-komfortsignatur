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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class CardHandleFinderTest {

  private static final String EXPECTED_CARD_HANDLE = "card_handle_hba";
  private InvocationContext invocationContext;

  private GetCardsResponse getCardsResponse;

  @Mock PerformGetCards performGetCards = mock(PerformGetCards.class);

  @Before
  public void prepare() {
    invocationContext = new InvocationContext("mandant001", "clientSystem001", "workplace001");
    getCardsResponse = determineGetCardsResponse();
  }

  @Test
  public void shouldDetermineCardHandleHba() throws IOException {
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
    cardInfoTypeEgk.setIccsn("80276883110000116873-EGK");
    cardInfoTypeEgk.setCardHolderName("Gustaf Caspar Orm Skarsgård");

    CardInfoType cardInfoTypeHba = new CardInfoType();
    cardInfoTypeHba.setCardType(CardTypeType.HBA);
    cardInfoTypeHba.setCardHandle("card_handle_hba");
    cardInfoTypeHba.setIccsn("80276883110000116873-Nimue-HBA");
    cardInfoTypeHba.setCardHolderName("Nimue");

    CardInfoType cardInfoTypeHba_TWO = new CardInfoType();
    cardInfoTypeHba_TWO.setCardType(CardTypeType.HBA);
    cardInfoTypeHba_TWO.setCardHandle("card_handle_hba_TWO");
    cardInfoTypeHba_TWO.setIccsn("80265883110021116873-HBA");
    cardInfoTypeHba_TWO.setCardHolderName("Merlin der Zauberer");

    CardInfoType cardTypeTypeSmcB = new CardInfoType();
    cardTypeTypeSmcB.setCardType(CardTypeType.SMC_B);
    cardTypeTypeSmcB.setCardHandle("card_handle_smc-b");
    cardTypeTypeSmcB.setIccsn("80276883110000116873-SMC-B");
    cardTypeTypeSmcB.setCardHolderName("Praxis Priv.-Doz. Dr. J. Schnaakeburgk");
    cardTypeTypeSmcB.setSlotId(BigInteger.TWO);

    CardInfoType cardTypeTypeSMC_KT = new CardInfoType();
    cardTypeTypeSMC_KT.setCardType(CardTypeType.SMC_KT);
    cardTypeTypeSMC_KT.setCardHandle("card_handle_smc-b");
    cardTypeTypeSMC_KT.setIccsn("80276883118899116873-SMC-KT");
    cardTypeTypeSMC_KT.setSlotId(BigInteger.valueOf(4L));

    CardInfoType cardTypeTypeSmcB_TWO = new CardInfoType();
    cardTypeTypeSmcB_TWO.setCardType(CardTypeType.SMC_B);
    cardTypeTypeSmcB_TWO.setCardHandle("card_handle2_smc-b");
    cardTypeTypeSmcB_TWO.setIccsn("80276883110000116876-SMC-B");
    cardTypeTypeSmcB_TWO.setSlotId(BigInteger.ONE);
    cardTypeTypeSmcB_TWO.setCardHolderName("Löwenapotheke im Zahlengrund");

    CardInfoType cardTypeTypeHBAQSIG = new CardInfoType();
    cardTypeTypeHBAQSIG.setCardType(CardTypeType.HBA_Q_SIG);
    cardTypeTypeHBAQSIG.setCardHandle("card_handle_HBA_Q_SIG");
    cardTypeTypeHBAQSIG.setIccsn("80276883110000116876-HBA_Q_SIG");
    cardTypeTypeHBAQSIG.setSlotId(BigInteger.ONE);
    cardTypeTypeHBAQSIG.setCardHolderName("HBA HBA_Q_SIG Card");

    CardInfoType cardTypeTypeZOD20 = new CardInfoType();
    cardTypeTypeZOD20.setCardType(CardTypeType.ZOD_2_0);
    cardTypeTypeZOD20.setCardHandle("card_handle_ZOD_2_0");
    cardTypeTypeZOD20.setIccsn("80276883110000116876-ZOD_2_0");
    cardTypeTypeZOD20.setSlotId(BigInteger.TEN);
    cardTypeTypeZOD20.setCardHolderName("ZOD`s CardHolder");

    List<CardInfoType> cardInfoTypeList = new ArrayList<>();
    cardInfoTypeList.add(cardInfoTypeEgk);
    cardInfoTypeList.add(cardInfoTypeHba);
    cardInfoTypeList.add(cardTypeTypeSmcB);
    cardInfoTypeList.add(cardTypeTypeSmcB_TWO);
    cardInfoTypeList.add(cardTypeTypeSMC_KT);
    cardInfoTypeList.add(cardTypeTypeHBAQSIG);
    cardInfoTypeList.add(cardTypeTypeZOD20);

    Cards cards = mock(Cards.class);
    when(cards.getCard()).thenReturn(cardInfoTypeList);
    getCardsResponse.setCards(cards);
    return getCardsResponse;
  }

  @Test
  public void shouldDetermineCardHandleWithICCSN() throws IOException {
    when(performGetCards.performGetCards(invocationContext)).thenReturn(getCardsResponse);
    CardHandleFinder cardHandleFinder = new CardHandleFinder();
    cardHandleFinder.performGetCards = performGetCards;

    String cardHandle =
        cardHandleFinder.determineHbaHandle(invocationContext, "80276883110000116873-Nimue-HBA");
    Assert.assertNotNull(cardHandle);
    Assert.assertEquals("card_handle_hba", cardHandle);
  }

  @Test
  public void shouldDetermineSmcBCardHandleWithICCSN() throws IOException {
    when(performGetCards.performGetCards(invocationContext)).thenReturn(getCardsResponse);
    CardHandleFinder cardHandleFinder = new CardHandleFinder();
    cardHandleFinder.performGetCards = performGetCards;

    String cardHandle =
        cardHandleFinder.determineSmcBHandle(invocationContext, "80276883110000116876-SMC-B");
    Assert.assertNotNull(cardHandle);
    Assert.assertEquals("card_handle2_smc-b", cardHandle);
  }

  @Test
  public void shouldDetermineCardHandleWithInvalidOrNotExistingICCSNByTypeSMCB()
      throws IOException {
    when(performGetCards.performGetCards(invocationContext)).thenReturn(getCardsResponse);

    CardHandleFinder cardHandleFinder = new CardHandleFinder();
    cardHandleFinder.performGetCards = performGetCards;

    String cardHandle = cardHandleFinder.determineSmcBHandle(invocationContext, "INVALID");
    Assert.assertNull(cardHandle);
  }

  @Test
  public void shouldDetermineCardHandleWithInvalidOrNotExistingICCSNByTypeHBA() throws IOException {
    when(performGetCards.performGetCards(invocationContext)).thenReturn(getCardsResponse);

    CardHandleFinder cardHandleFinder = new CardHandleFinder();
    cardHandleFinder.performGetCards = performGetCards;

    String cardHandle = cardHandleFinder.determineHbaHandle(invocationContext, "INVALID");
    Assert.assertNull(cardHandle);
  }

  @Test
  public void shouldDetermineZOD20CardHandle() throws IOException {
    when(performGetCards.performGetCards(invocationContext)).thenReturn(getCardsResponse);
    CardHandleFinder cardHandleFinder = new CardHandleFinder();
    cardHandleFinder.performGetCards = performGetCards;

    String cardHandle = cardHandleFinder.determineZOD20Handle(invocationContext);
    Assert.assertNotNull(cardHandle);
    Assert.assertEquals("card_handle_ZOD_2_0", cardHandle);
  }

  @Test
  public void shouldDetermineZOD20CardHandleWithICCSN() throws IOException {
    when(performGetCards.performGetCards(invocationContext)).thenReturn(getCardsResponse);
    CardHandleFinder cardHandleFinder = new CardHandleFinder();
    cardHandleFinder.performGetCards = performGetCards;

    String cardHandle =
        cardHandleFinder.determineZOD20Handle(invocationContext, "80276883110000116876-ZOD_2_0");
    Assert.assertNotNull(cardHandle);
    Assert.assertEquals("card_handle_ZOD_2_0", cardHandle);
  }

  @Test
  public void shouldDetermineHBAQSigHandleWithICCSN() throws IOException {
    when(performGetCards.performGetCards(invocationContext)).thenReturn(getCardsResponse);
    CardHandleFinder cardHandleFinder = new CardHandleFinder();
    cardHandleFinder.performGetCards = performGetCards;

    String cardHandle =
        cardHandleFinder.determineHBAQSigHandle(
            invocationContext, "80276883110000116876-HBA_Q_SIG");
    Assert.assertNotNull(cardHandle);
    Assert.assertEquals("card_handle_HBA_Q_SIG", cardHandle);
  }
}
