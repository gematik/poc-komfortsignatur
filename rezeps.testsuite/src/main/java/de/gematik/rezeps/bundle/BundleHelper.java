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

package de.gematik.rezeps.bundle;

import de.gematik.rezeps.dataexchange.TaskCreateData;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Befüllt ein Bundle mit Daten. */
public class BundleHelper { // NOSONAR

  private static final String ATTRIBUTE_NAME_VALUE = "value";

  private static final String NO_INDENT = "no";
  public static final String OMIT_XML_DECLARATION = "yes";

  private final Document bundle;
  protected Properties bundleTemplateProperties;

  public BundleHelper() throws IOException, ParserConfigurationException, SAXException {
    this.bundle = convertStringToXmlDocument(initializeBundle());
  }

  private String initializeBundle() throws IOException {
    InputStream inputStream =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("bundle_template.properties");
    bundleTemplateProperties = new Properties();
    if (inputStream != null) {
      bundleTemplateProperties.load(inputStream);
    }

    inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("bundle_template.xml");
    if (inputStream != null) {
      return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    } else {
      throw new IOException("cannot read bundle_template.xml");
    }
  }

  /**
   * Initialisiert die Patienten-Daten in einem Bundle.
   *
   * @param patient Enthält die zu initialisierenden Patient-Daten.
   * @return Das Bundle mit den initialisierten Daten.
   */
  public String initializePatientData(Patient patient)
      throws XPathExpressionException, TransformerException {

    initializeName(patient.getGivenName(), patient.getSurname());
    initializeGivenName(patient.getGivenName());
    initializeSurname(patient.getSurname());
    initializeKvnr(patient.getKvnr());
    initializeStreetAndHouseNumber(patient.getStreet(), patient.getHouseNumber());
    initializeStreet(patient.getStreet());
    initializeHouseNumber(patient.getHouseNumber());
    initializePostalCode(patient.getPostalCode());
    initializeCity(patient.getCity());
    initializeBirthday(patient.getBirthday());

    return xmlDocumentToString();
  }

  /**
   * Initialisiert die Coverage-Daten in einem Bundle.
   *
   * @param coverage Enthält die zu initialisierenden Coverage-Daten.
   * @return Das Bundle mit den initialisierten Daten.
   */
  public String initializeCoverageData(Coverage coverage)
      throws TransformerException, XPathExpressionException {
    initializeIknr(coverage.getIknr());
    initializeCoverageName(coverage.getName());
    initializeStatus(coverage.getStatus());

    return xmlDocumentToString();
  }

  /**
   * Initialisiert die Medication-Daten in einem Bundle.
   *
   * @param medication Enthält die zu initialisierenden Medication-Daten.
   * @return Das Bundle mit den initialisierten Daten.
   */
  public String initializeMedicationData(Medication medication)
      throws TransformerException, XPathExpressionException {
    initializePznValue(medication.getPznValue());
    initializePznText(medication.getPznText());

    return xmlDocumentToString();
  }

  /**
   * Initialisiert die Daten aus dem Task create im Bundle.
   *
   * @param taskCreateData Enthält die zu initialisierenden Daten.
   */
  public void initializeTaskCreateData(TaskCreateData taskCreateData)
      throws TransformerException, XPathExpressionException {
    initializePrescriptionId(taskCreateData.getPrescriptionId());
    xmlDocumentToString();
  }

  /**
   * Liest den aktuellen Zustand des Bundles als String aus.
   *
   * @return Aktueller Zustand des Bundles.
   */
  public String readBundle() throws TransformerException {
    return xmlDocumentToString();
  }

  private Document convertStringToXmlDocument(String xmlString)
      throws ParserConfigurationException, IOException, SAXException {
    Document document;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); // NOSONAR
    DocumentBuilder documentBuilder = factory.newDocumentBuilder();
    document = documentBuilder.parse(new InputSource(new StringReader(xmlString)));
    return document;
  }

  private Node getElementByXPath(String xPathString) throws XPathExpressionException {
    XPath xPath = XPathFactory.newInstance().newXPath();
    return (Node) xPath.compile(xPathString).evaluate(bundle, XPathConstants.NODE);
  }

  private String xmlDocumentToString() throws TransformerException {
    String documentAsString;
    TransformerFactory transformerFactory = TransformerFactory.newInstance(); // NOSONAR
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, NO_INDENT);
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, OMIT_XML_DECLARATION);
    StringWriter writer = new StringWriter();
    transformer.transform(new DOMSource(bundle), new StreamResult(writer));
    documentAsString = writer.getBuffer().toString();
    return documentAsString;
  }

  private void initializeName(String givenName, String surname) throws XPathExpressionException {
    Node nameNode = getElementByXPath(bundleTemplateProperties.getProperty("XPATH_NAME"));
    String name = givenName + " " + surname;
    updateValueAttribute(nameNode, name);
  }

  private void updateValueAttribute(Node node, String updateString) {
    NamedNodeMap attributes = node.getAttributes();
    attributes.getNamedItem(ATTRIBUTE_NAME_VALUE).setNodeValue(updateString);
  }

  private void initializeGivenName(String givenName) throws XPathExpressionException {
    Node givenNameNode =
        getElementByXPath(bundleTemplateProperties.getProperty("XPATH_GIVEN_NAME"));
    updateValueAttribute(givenNameNode, givenName);
  }

  private void initializeSurname(String surname) throws XPathExpressionException {
    Node surnameNode = getElementByXPath(bundleTemplateProperties.getProperty("XPATH_SURNAME"));
    updateValueAttribute(surnameNode, surname);
  }

  private void initializeKvnr(String kvnr) throws XPathExpressionException {
    Node kvnrNode = getElementByXPath(bundleTemplateProperties.getProperty("XPATH_KVNR"));
    updateValueAttribute(kvnrNode, kvnr);
  }

  private void initializeStreetAndHouseNumber(String street, String houseNumber)
      throws XPathExpressionException {
    Node streetAndHouseNumberNode =
        getElementByXPath(bundleTemplateProperties.getProperty("XPATH_STREET_AND_HOUSE_NUMBER"));
    String streetAndHouseNumber = street + " " + houseNumber;
    updateValueAttribute(streetAndHouseNumberNode, streetAndHouseNumber);
  }

  private void initializeStreet(String street) throws XPathExpressionException {
    Node streetNode = getElementByXPath(bundleTemplateProperties.getProperty("XPATH_STREET"));
    updateValueAttribute(streetNode, street);
  }

  private void initializeHouseNumber(String houseNumber) throws XPathExpressionException {
    Node houseNumberNode =
        getElementByXPath(bundleTemplateProperties.getProperty("XPATH_HOUSE_NUMBER"));
    updateValueAttribute(houseNumberNode, houseNumber);
  }

  private void initializePostalCode(String postalCode) throws XPathExpressionException {
    Node postalCodeNode =
        getElementByXPath(bundleTemplateProperties.getProperty("XPATH_POSTAL_CODE"));
    updateValueAttribute(postalCodeNode, postalCode);
  }

  private void initializeCity(String city) throws XPathExpressionException {
    Node cityNode = getElementByXPath(bundleTemplateProperties.getProperty("XPATH_CITY"));
    updateValueAttribute(cityNode, city);
  }

  private void initializeBirthday(String birthday) throws XPathExpressionException {
    Node birthdayNode = getElementByXPath(bundleTemplateProperties.getProperty("XPATH_BIRTHDAY"));
    updateValueAttribute(birthdayNode, birthday);
  }

  private void initializeIknr(String iknr) throws XPathExpressionException {
    Node iknrNode = getElementByXPath(bundleTemplateProperties.getProperty("XPATH_IKNR"));
    updateValueAttribute(iknrNode, iknr);
  }

  private void initializeCoverageName(String name) throws XPathExpressionException {
    Node nameNode = getElementByXPath(bundleTemplateProperties.getProperty("XPATH_COVERAGE_NAME"));
    updateValueAttribute(nameNode, name);
  }

  private void initializeStatus(String status) throws XPathExpressionException {
    Node statusNode = getElementByXPath(bundleTemplateProperties.getProperty("XPATH_STATUS"));
    updateValueAttribute(statusNode, status);
  }

  private void initializePznValue(String pznValue) throws XPathExpressionException {
    Node pznValueNode = getElementByXPath(bundleTemplateProperties.getProperty("XPATH_PZN_VALUE"));
    updateValueAttribute(pznValueNode, pznValue);
  }

  private void initializePznText(String pznText) throws XPathExpressionException {
    Node pznTextNode = getElementByXPath(bundleTemplateProperties.getProperty("XPATH_PZN_TEXT"));
    updateValueAttribute(pznTextNode, pznText);
  }

  private void initializePrescriptionId(String prescriptionId) throws XPathExpressionException {
    Node prescriptionIdNode =
        getElementByXPath(bundleTemplateProperties.getProperty("XPATH_PRESCRIPTION_ID"));
    updateValueAttribute(prescriptionIdNode, prescriptionId);
  }
}
