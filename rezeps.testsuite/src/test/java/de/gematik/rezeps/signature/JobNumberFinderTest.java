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

package de.gematik.rezeps.signature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.signatureservice.v7.GetJobNumberResponse;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class JobNumberFinderTest {

  private static final String MANDANT = "mandant001";
  private static final String CLIENT_SYSTEM = "client_system001";
  private static final String WORKPLACE = "workplace001";
  private static final String JOB_NUMBER = "4711";

  @Test
  public void shouldPerformGetJobNumber() throws IOException {
    JobNumberFinder jobNumberFinder = new JobNumberFinder();

    PerformGetJobNumber performGetJobNumber = mock(PerformGetJobNumber.class);
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    when(performGetJobNumber.performGetJobNumber(invocationContext))
        .thenReturn(determineGetJobNumberResponse());
    jobNumberFinder.performGetJobNumber = performGetJobNumber;

    String jobNumber = jobNumberFinder.performGetJobNumber(invocationContext);
    Assert.assertEquals(JOB_NUMBER, jobNumber);
  }

  private GetJobNumberResponse determineGetJobNumberResponse() {
    GetJobNumberResponse getJobNumberResponse = new GetJobNumberResponse();
    getJobNumberResponse.setJobNumber(JOB_NUMBER);
    return getJobNumberResponse;
  }
}
