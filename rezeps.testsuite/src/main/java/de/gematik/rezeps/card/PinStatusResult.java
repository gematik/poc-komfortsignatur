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

import de.gematik.ws.conn.cardservice.v8.PinStatusEnum;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * POJO PinStatusResult enthält Informationen zum PinStatus einer Card
 *
 * <pre>
 *     Status {@link Status}
 *     leftTries {@link BigInteger}
 *     pinStatusEnum {@link PinStatusEnum}
 * </pre>
 */
public class PinStatusResult implements Serializable {

  private static final long serialVersionUID = 4920119976436215464L;

  public PinStatusResult(Status status, BigInteger leftTries, PinStatusEnum pinStatusEnum) {
    this.status = status;
    this.leftTries = leftTries;
    this.pinStatusEnum = pinStatusEnum;
  }
  /**
   * Setzt den Status der Response
   *
   * @param status {@link Status}
   */
  public void setStatus(Status status) {
    this.status = status;
  }

  /**
   * Setzt die Anzahl der verbleibenden Versuche
   *
   * @param leftTries {@link BigInteger}
   */
  public void setLeftTries(BigInteger leftTries) {
    this.leftTries = leftTries;
  }

  /**
   * Setzt den PinStatus
   *
   * @param pinStatusEnum {@link PinStatusEnum}
   */
  public void setPinStatusEnum(PinStatusEnum pinStatusEnum) {
    this.pinStatusEnum = pinStatusEnum;
  }

  transient Status status;

  /**
   * Gibt den {@link Status} zurück
   *
   * @return Status
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Gibt die Anzahl der verbleibenden Versuche zurück
   *
   * @return Anzahl Versuche
   */
  public BigInteger getLeftTries() {
    return leftTries;
  }

  /**
   * Gibt den aktuellen Pin Status zurück
   *
   * @return PinStatus {@link PinResultEnum}
   */
  public PinStatusEnum getPinStatusEnum() {
    return pinStatusEnum;
  }

  BigInteger leftTries;
  PinStatusEnum pinStatusEnum;
}
