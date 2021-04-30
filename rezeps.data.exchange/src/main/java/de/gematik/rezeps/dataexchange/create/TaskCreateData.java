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
import java.util.Objects;

public class TaskCreateData implements Serializable {

  private int statusCode;
  private String taskId;
  private String prescriptionId;
  private String accessCode;
  private String status;

  public TaskCreateData(String taskId, String prescriptionId, String accessCode) {
    this.taskId = taskId;
    this.prescriptionId = prescriptionId;
    this.accessCode = accessCode;
  }

  public TaskCreateData(int statusCode, String taskId, String prescriptionId, String accessCode, String status) {
    this.statusCode = statusCode;
    this.taskId = taskId;
    this.prescriptionId = prescriptionId;
    this.accessCode = accessCode;
    this.status = status;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaskCreateData taskCreateData = (TaskCreateData) o;
    return statusCode == taskCreateData.statusCode && Objects.equals(taskId, taskCreateData.taskId)
        && Objects.equals(prescriptionId, taskCreateData.prescriptionId) && Objects
        .equals(accessCode, taskCreateData.accessCode) && Objects.equals(status, taskCreateData.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(statusCode, taskId, prescriptionId, accessCode, status);
  }
}
