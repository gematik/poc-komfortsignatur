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

package de.gematik.rezeps.util;

import java.math.BigInteger;
import java.text.MessageFormat;
import org.junit.Assert;
import org.junit.Test;

public class CommonUtilsTest {

  public static final int BYTES_IN_256_BITS = 32;

  @Test
  public void isNullOrEmptyGivenNullStringTest() {
    String expected = null;
    Assert.assertTrue(CommonUtils.isNullOrEmpty(expected));
  }

  @Test
  public void isNullOrEmptyGivenEmptyStringTest() {
    String expected = "";
    Assert.assertTrue(CommonUtils.isNullOrEmpty(expected));
  }

  @Test
  public void isNullOrEmptyWithTrimGivenEmptyStringTest() {
    String expected = "";
    Assert.assertTrue(CommonUtils.isNullOrEmpty(expected, true));
  }

  @Test
  public void shouldFailIsNullOrEmptyWithTrimGivenStringTest() {
    String expected = "s a mple";
    Assert.assertFalse(CommonUtils.isNullOrEmpty(expected, false));
  }

  @Test
  public void isNullOrEmptyWithTrimGivenStringTest() {
    String expected = "s a mple";
    Assert.assertFalse(CommonUtils.isNullOrEmpty(expected, true));
  }

  @Test
  public void isNotEmptyWithTrimGivenStringTest() {
    String expected = "s a mple";
    Assert.assertTrue(CommonUtils.isNotEmpty(expected));
  }

  @Test
  public void isNotEmptyWithTrimGivenEmptyStringTest() {
    String expected = "";
    Assert.assertFalse(CommonUtils.isNotEmpty(expected));
  }

  @Test
  public void isNotEmptyWithTrimGivenEmptyStringAndTrimTest() {
    String expected = "";
    Assert.assertFalse(CommonUtils.isNotEmpty(expected, true));
  }

  @Test
  public void isNotEmptyWithTrimGivenStringAndTrimTest() {
    String expected = "Sam ple";
    Assert.assertTrue(CommonUtils.isNotEmpty(expected, true));
  }

  @Test
  public void isNotEmptyWithTrimGivenStringAndNoTrimTest() {
    String expected = "Sam ple";
    Assert.assertTrue(CommonUtils.isNotEmpty(expected, false));
  }

  @Test
  public void isNotEmptyWithTrimGivenStringAndNoTrimNoBlanksTest() {
    String expected = "Sample";
    Assert.assertTrue(CommonUtils.isNotEmpty(expected, false));
  }

  @Test
  public void generateRNDUserIDWithLenFiveTimesTest() {
    for (int i = 0; i < 5; i++) {
      long time = -System.currentTimeMillis();
      BigInteger bigInteger = CommonUtils.generateRNDIDNumber();
      long difference = time + System.currentTimeMillis();
      Assert.assertTrue(bigInteger.bitLength() == 128);
      System.out.println(
          MessageFormat.format(
              "Zum Erzeugen der Benutzer-ID \"{0}\" wurden {1} ms. benötigt. (Typ {2})",
              bigInteger, difference, bigInteger.getClass().getSimpleName()));
    }
  }

  @Test
  public void generateRNDUserIDAsStringWithLenFiveTimesTest() {
    for (int i = 0; i < 5; i++) {
      long time = -System.currentTimeMillis();
      String rndUserID = CommonUtils.generateRNDUserID();
      long difference = time + System.currentTimeMillis();
      Assert.assertTrue(rndUserID.matches("[0-9]+"));
      System.out.println(
          MessageFormat.format(
              "Zum Erzeugen der Benutzer-ID \"{0}\" wurden {1} ms. benötigt. (Typ {2})",
              rndUserID, difference, rndUserID.getClass().getSimpleName()));
    }
  }

  @Test
  public void generateRNDUserIDAsStringOnlyNumbersTest() {
    String text = CommonUtils.generateRNDUserID();
    Assert.assertTrue(text.matches("[0-9]+"));
  }

  @Test
  public void shouldGenerateRandomBytes() {
    byte[] randomBytes = CommonUtils.generateRandomBytes(BYTES_IN_256_BITS);
    Assert.assertNotNull("randomBytes sollte nicht null sein", randomBytes);
    Assert.assertEquals(
        "randomBytes hat nicht die erwartete Länge", BYTES_IN_256_BITS, randomBytes.length);
  }
}
