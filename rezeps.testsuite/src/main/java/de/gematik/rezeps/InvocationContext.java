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

package de.gematik.rezeps;

import de.gematik.rezeps.util.CommonUtils;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import java.io.Serializable;

/** Repräsentiert einen Aufrufkontext */
public class InvocationContext implements Serializable {

  private static final long serialVersionUID = -3118553804984148873L;
  private String mandant;
  private String clientSystem;
  private String workplace;
  private String user;

  /**
   * Konstruktor
   *
   * @param mandant mandant
   * @param clientSystem ClientSystem
   * @param workplace Arbeitsplatz
   */
  public InvocationContext(String mandant, String clientSystem, String workplace) {
    this.mandant = mandant;
    this.clientSystem = clientSystem;
    this.workplace = workplace;
  }

  /**
   * Konstruktor
   *
   * @param mandant Mandant
   * @param clientSystem ClientSystem
   * @param workplace Arbeitsplatz
   * @param user BenutzerID
   */
  public InvocationContext(String mandant, String clientSystem, String workplace, String user) {
    this.mandant = mandant;
    this.clientSystem = clientSystem;
    this.workplace = workplace;
    this.user = user;
  }

  public String getMandant() {
    return mandant;
  }

  public void setMandant(String mandant) {
    this.mandant = mandant;
  }

  public String getClientSystem() {
    return clientSystem;
  }

  public void setClientSystem(String clientSystem) {
    this.clientSystem = clientSystem;
  }

  public String getWorkplace() {
    return workplace;
  }

  public void setWorkplace(String workplace) {
    this.workplace = workplace;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  /**
   * Konvertiert den Aufrufkontext in die für die SOAP-Schnittstelle benötigte Repräsentation.
   *
   * @return Aufrufkontext in der für die SOAP-Schnittstelle benötigten Repräsentation.
   */
  public ContextType convertToContextType() {
    ContextType contextType = new ContextType();
    contextType.setMandantId(mandant);
    contextType.setClientSystemId(clientSystem);
    contextType.setWorkplaceId(workplace);
    contextType.setUserId(user);
    return contextType;
  }
  /**
   * Prüft grob den Aufrufkontext. <b>Eine Inhaltliche prüfung findet nicht statt.</b>
   *
   * @return true wenn {@link InvocationContext} is scheinbar valid (Mandant, Clientsystem und
   *     Arbeitsplatz sind gesetzt.
   */
  public boolean isValidInvocationContext() {
    return (null != this.convertToContextType()
        && !CommonUtils.isNullOrEmpty(this.getMandant())
        && !CommonUtils.isNullOrEmpty(this.getClientSystem())
        && !CommonUtils.isNullOrEmpty(this.getWorkplace()));
  }
}
