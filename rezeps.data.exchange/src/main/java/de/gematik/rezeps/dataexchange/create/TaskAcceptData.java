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

package de.gematik.rezeps.dataexchange.create;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class TaskAcceptData implements Serializable {

  private int statusCode;
  private String status;
  private String secret;
  private byte[] signedPrescription;

  public TaskAcceptData(int statusCode, String status) {
    this.status = status;
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public byte[] getSignedPrescription() {
    return signedPrescription;
  }

  public void setSignedPrescription(byte[] signedPrescription) {
    this.signedPrescription = signedPrescription;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaskAcceptData that = (TaskAcceptData) o;
    return statusCode == that.statusCode && Objects.equals(status, that.status)
        && Objects.equals(secret, that.secret) && Arrays
        .equals(signedPrescription, that.signedPrescription);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(statusCode, status, secret);
    result = 31 * result + Arrays.hashCode(signedPrescription);
    return result;
  }
}
