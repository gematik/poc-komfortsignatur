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

import com.google.common.base.Objects;
import de.gematik.rezeps.Status;
import java.io.Serializable;
import java.util.Arrays;

public class TaskAcceptData extends Status implements Serializable {

  private static final long serialVersionUID = 4534881106020606538L;

  private String secret;
  private byte[] signedPrescription;
  private String taskId;

  public TaskAcceptData(int statusCode, String status) {
    super(statusCode, status);
  }

  public void setTaskID(String taskId) {
    this.taskId = taskId;
  }

  public String getTaskId() {
    return this.taskId;
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TaskAcceptData)) {
      return false;
    }
    TaskAcceptData that = (TaskAcceptData) obj;
    return Objects.equal(secret, that.secret)
        && Arrays.equals(signedPrescription, that.signedPrescription)
        && Objects.equal(taskId, that.taskId)
        && Objects.equal(statusCode, that.statusCode)
        && Objects.equal(statusName, that.statusName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(secret, signedPrescription, taskId, statusCode, statusName);
  }
}
