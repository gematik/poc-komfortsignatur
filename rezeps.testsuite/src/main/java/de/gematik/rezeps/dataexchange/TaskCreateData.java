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

public class TaskCreateData extends Status implements Serializable {

  private static final long serialVersionUID = -1134117006939320250L;

  private String taskId;
  private String prescriptionId;
  private String accessCode;

  public TaskCreateData(String taskId, String prescriptionId, String accessCode) {
    super();
    this.taskId = taskId;
    this.prescriptionId = prescriptionId;
    this.accessCode = accessCode;
  }

  public TaskCreateData(
      int statusCode, String taskId, String prescriptionId, String accessCode, String status) {
    super(statusCode, status);

    this.taskId = taskId;
    this.prescriptionId = prescriptionId;
    this.accessCode = accessCode;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getPrescriptionId() {
    return prescriptionId;
  }

  public void setPrescriptionId(String prescriptionId) {
    this.prescriptionId = prescriptionId;
  }

  public String getAccessCode() {
    return accessCode;
  }

  public void setAccessCode(String accessCode) {
    this.accessCode = accessCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TaskCreateData)) {
      return false;
    }
    TaskCreateData that = (TaskCreateData) obj;
    return Objects.equal(taskId, that.taskId)
        && Objects.equal(prescriptionId, that.prescriptionId)
        && Objects.equal(accessCode, that.accessCode)
        && Objects.equal(statusCode, that.statusCode)
        && Objects.equal(statusName, that.statusName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(taskId, prescriptionId, accessCode, statusCode, statusName);
  }
}
