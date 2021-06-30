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

package de.gematik.rezeps.dataexchange;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class TaskCloseData implements Serializable {

  private static final long serialVersionUID = 4309535994521204782L;
  private int statusCode;
  private byte[] signature;
  private SignedReceipt signedReceipt;

  public TaskCloseData(int statusCode, byte[] signature) {
    this.statusCode = statusCode;
    this.signature = signature;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public byte[] getSignature() {
    return signature;
  }

  public void setSignature(byte[] signature) {
    this.signature = signature;
  }

  public SignedReceipt getSignedReceipt() {
    return signedReceipt;
  }

  public void setSignedPrescription(final SignedReceipt signedReceipt) {
    this.signedReceipt = signedReceipt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaskCloseData that = (TaskCloseData) o;
    return statusCode == that.statusCode && Arrays.equals(signature, that.signature);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(statusCode);
    result = 31 * result + Arrays.hashCode(signature);
    return result;
  }
}
