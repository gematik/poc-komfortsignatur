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
import java.security.SecureRandom;

public class CommonUtils {

  private CommonUtils() {
    throw new IllegalStateException("CommonUtils class");
  }

  /**
   * @param value The value to test.
   * @param trim true, if a non null string should trimmed.
   * @return true, if the given string is not null or empty.
   */
  public static boolean isNotEmpty(String value, boolean trim) {
    if (value == null) {
      return false;
    }
    if (trim) {
      return !"".equals(value.trim());
    }
    return !"".equals(value);
  }
  /**
   * @param value The value to test.
   * @return true, if the given string is not null or empty.
   */
  public static boolean isNotEmpty(String value) {
    return isNotEmpty(value, false);
  }

  /**
   * @param value The value to test.
   * @param trim true, if a non null string should trimmed.
   * @return true, if the given string is null or empty.
   */
  public static boolean isNullOrEmpty(String value, boolean trim) {
    boolean hasvalue = isNotEmpty(value, trim);
    return !hasvalue;
  }

  /**
   * @param value The value to test.
   * @return true, if the given string is null or empty.
   */
  public static boolean isNullOrEmpty(String value) {
    return isNullOrEmpty(value, false);
  }

  /**
   * generiert eine 128-bit Zahlenfolge Die Verwendung von
   *
   * <pre>new BigInteger(128,new SecureRandom());</pre>
   *
   * stellt dies leider nicht immer sicher. Es wird also 100x der Versuch gestartet.
   *
   * @return Zufälligen {@link BigInteger} 128-bit Zahlenfolge
   */
  public static BigInteger generateRNDIDNumber() {
    BigInteger result = new BigInteger(128, new SecureRandom());
    int count = 0;
    do {
      if ((result.bitLength() != 128)) {
        result = new BigInteger(128, new SecureRandom());
      }
      count++;
    } while ((result.bitLength() != 128) && (count <= 100));
    return result;
  }

  /**
   * generiert eine 128-bit Zahlenfolge
   *
   * @return einen zufälligen 128-bit Zahlenfolge als {@link String}
   */
  public static String generateRNDUserID() {
    BigInteger res = CommonUtils.generateRNDIDNumber();
    return res.toString();
  }
}
