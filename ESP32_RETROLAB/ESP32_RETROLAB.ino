#include <Wire.h>
#include <U8g2lib.h>
#include <Adafruit_ADS1X15.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>


// PINOUT

// I2C, shared
#define SDA_PIN 27
#define SCL_PIN 14
// DS18B20
#define TEMP_PIN 32
// Buzzer
#define BUZZER_PIN 25
// Encoder
#define ENCODER_SW  23
#define ENCODER_CLK 17
#define ENCODER_DT  5
// ADS1115
#define VOLTAGE_CHANNEL    0
#define RESISTANCE_CHANNEL 1

//RETROLAB NETWORK SETTINGS

// CHANGE THIS
const char* WIFI_SSID = "YOUR_SSID";
const char* WIFI_PASSWORD = "YOUR_PASSWORD";
// CHANGE THIS, check IPv4 --> CMD and ipconfig
const char* API_BASE_URL = "YOUR_URL";

// CHANGE THIS, this is the same as in your.env
const char* API_EMAIL = "YOUR_ADMIN_EMAIL";
const char* API_PASSWORD = "YOUR_ADMIN_PASSWORD";

// JWT returned by backend, automatically do not need to input
String jwtToken = "";

//Network state constants
bool wifiConnected = false;
bool apiAuthenticated = false;

//OLED CONFIG

U8G2_SSD1306_128X64_NONAME_F_HW_I2C u8g2(
  U8G2_R0,
  U8X8_PIN_NONE
);

//ADS, analogic to digital config
Adafruit_ADS1115 ads;

//voltage:
// V/Ohm --> DPDT --> 40k--> A0 --> 10k --> GND, same as in schema

const float VOLTAGE_DIVIDER_RATIO = 5.0;

// Resistance:
// 3.3V --> RREF --> A1 --> DPDT --> V/Ohm

const float RREF = 10000.0;
const float VCC_RESISTANCE = 3.288;
const float RESISTANCE_CALIBRATION = 1.1666;

//INA3221 config, current

uint8_t inaAddress = 0;
#define INA3221_REG_SHUNT_CH1 0x01
#define INA3221_REG_BUS_CH1   0x02

const float SHUNT_RESISTANCE = 0.1;

const float CURRENT_CALIBRATION = 0.9448;

// DS18B20, onewire temperature
OneWire oneWire(TEMP_PIN);
DallasTemperature tempSensor(&oneWire);
bool tempFound = false;

//CONTINUITY, if it falls below, there is not
const float CONTINUITY_THRESHOLD = 18.0;

//menu
enum Mode {
  CONSOLE_MENU,
  REPAIR_CASE_MENU,
  COMPONENT_TEST_MENU,
  NO_TESTS_SCREEN,
  MENU,
  VOLTAGE_MODE,
  CURRENT_MODE,
  RESISTANCE_MODE,
  CONTINUITY_MODE,
  TEMPERATURE_MODE
};

Mode currentMode = CONSOLE_MENU;
const char* menuItems[] = {
  "Voltage",
  "Current",
  "Resistance",
  "Continuity",
  "Temperature"
};

const int MENU_COUNT = 5;
int selectedMenuItem = 0;

//RetroLab data, fixed size arrays are used to keep memory usage predictable

const int MAX_CONSOLES = 50;
const int MAX_REPAIR_CASES = 50;
const int MAX_COMPONENT_TESTS = 100;


struct ConsoleItem {
  long consoleId;
  String consoleModelName;
  String serialNumber;
};

struct RepairCaseItem {
  long repairCaseId;
  String consoleName;
  String title;
  String status;
};

struct ComponentTestItem {
  long componentTestId;
  long repairCaseId;
  long componentId;
  String componentName;

  float measuredVoltage;
  bool hasVoltage;

  float measuredCurrent;
  bool hasCurrent;

  float measuredResistance;
  bool hasResistance;

  float temperature;
  bool hasTemperature;

  bool continuity;
  bool hasContinuity;

  String result;
  String notes;
};

ConsoleItem consoles[MAX_CONSOLES];
RepairCaseItem repairCases[MAX_REPAIR_CASES];
ComponentTestItem componentTests[MAX_COMPONENT_TESTS];

//initialization

int consoleCount = 0;
int repairCaseCount = 0;
int componentTestCount = 0;

int selectedConsole = 0;
int selectedRepairCase = 0;
int selectedComponentTest = 0;

long activeConsoleId = -1;
long activeRepairCaseId = -1;
long activeComponentTestId = -1;
long activeComponentId = -1;

String activeRepairCaseTitle = "";
String activeComponentName = "";
String activeTestResult = "";
String activeTestNotes = "";

float activeMeasuredVoltage = 0;
bool activeHasVoltage = false;

float activeMeasuredCurrent = 0;
bool activeHasCurrent = false;

float activeMeasuredResistance = 0;
bool activeHasResistance = false;

float activeTemperature = 0;
bool activeHasTemperature = false;

bool activeContinuity = false;
bool activeHasContinuity = false;

//long press saves measurement, short to go back
const unsigned long SAVE_HOLD_TIME = 1000;
bool buttonWasDown = false;
unsigned long buttonDownAt = 0;

//ENCODER

int lastCLKState;

unsigned long lastEncoderMove = 0;
unsigned long lastButtonPress = 0;

const unsigned long ENCODER_DEBOUNCE = 3;
const unsigned long BUTTON_DEBOUNCE = 250;

//BUZZER

void buzzerOn() {
  digitalWrite(BUZZER_PIN, LOW);
}
void buzzerOff() {
  digitalWrite(BUZZER_PIN, HIGH);
}

//NETWORK STATUS SCREEN

void drawNetworkStatus(
  const char* title,
  const char* status
) {

  u8g2.clearBuffer();

  u8g2.setFont(
    u8g2_font_6x10_tf
  );

  u8g2.drawStr(
    0,
    9,
    "RETROLAB NETWORK"
  );

  u8g2.drawHLine(
    0,
    12,
    128
  );

  u8g2.drawStr(
    0,
    31,
    title
  );

  u8g2.drawStr(
    0,
    47,
    status
  );

  u8g2.sendBuffer();
}

//WIFI

bool connectWiFi() {

  Serial.println();
  Serial.println("============================");
  Serial.println("       RetroLab WIFI");
  Serial.println("============================");

  drawNetworkStatus(
    "WiFi",
    "Connecting..."
  );

  WiFi.mode(WIFI_STA);
  WiFi.begin(
    WIFI_SSID,
    WIFI_PASSWORD
  );

  unsigned long startTime =
    millis();

  // cannot be waiting for more than 20sec
  while (
    WiFi.status() != WL_CONNECTED &&
    millis() - startTime < 20000
  ) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();

  //connection fails
  if (
    WiFi.status() != WL_CONNECTED
  ) {
    Serial.println(
      "WiFi connection failed"
    );

    wifiConnected = false;
    drawNetworkStatus(
      "WiFi",
      "FAILED"
    );

    delay(1500);
    return false;
  }

  wifiConnected = true;
  Serial.println(
    "WiFi connected"
  );
  Serial.print(
    "ESP32 IP: "
  );

  Serial.println(
    WiFi.localIP()
  );

  //signal strength in dBm
  Serial.print(
    "Signal strength: "
  );
  Serial.print(
    WiFi.RSSI()
  );
  Serial.println(
    " dBm"
  );

  drawNetworkStatus(
    "WiFi",
    "CONNECTED"
  );

  delay(1000);
  return true;
}

//LOGIN
// authenticate against RetroLab and store JWT

bool loginRetroLab() {

  // if wifi is disconnected, does not make any sense to attempt the request
  if (
    WiFi.status() != WL_CONNECTED
  ) {

    Serial.println(
      "Cannot login: WiFi disconnected"
    );

    wifiConnected = false;
    apiAuthenticated = false;

    return false;
  }

  drawNetworkStatus(
    "RetroLab API",
    "Logging in..."
  );

  // manage HTTP connection between ESP32 and REST API
  HTTPClient http;
  String url =
    String(API_BASE_URL) +
    "/api/v1/auth/login";

  Serial.println();

  // print request endpoint, debugging
  Serial.print(
    "POST "
  );

  Serial.println(
    url
  );

  //initialize HTTP connection using the login URL. http.begin does not perform POST yet
  if (!http.begin(url)) {

    Serial.println(
      "Could not initialize HTTP"
    );
    apiAuthenticated = false;

    drawNetworkStatus(
      "RetroLab API",
      "HTTP ERROR"
    );
    delay(1500);

    return false;
  }

  // media type declared in the Content-Type header
  http.addHeader(
    "Content-Type",
    "application/json"
  );
  //create json document, then add credentials
  JsonDocument request;
  request["email"] =
    API_EMAIL;
  request["password"] =
    API_PASSWORD;

  // convert arduino json into json string
  String requestBody;

  serializeJson(
    request,
    requestBody
  );

  Serial.println(
    "Sending login request..."
  );

  // send actual POST request to Spring Boot, returned value is the status
  int responseCode =
    http.POST(
      requestBody
    );

  Serial.print(
    "HTTP status: "
  );

  Serial.println(
    responseCode
  );

  // negative HTTPClient values represent connection errors
  if (responseCode < 0) {

    Serial.print(
      "HTTP connection error: "
    );

    //convert into string
    Serial.println(
      http.errorToString(
        responseCode
      )
    );
    http.end();

    apiAuthenticated = false;

    drawNetworkStatus(
      "RetroLab API",
      "CONNECTION ERROR"
    );
    delay(1500);

    return false;
  }

  // read body, then release resources
  String response =
    http.getString();

  http.end();

  // if login is not succesful, return fail
  if (
    responseCode != 200
  ) {

    Serial.println(
      "Login failed"
    );

    //print backend response, debug
    Serial.print(
      "Backend response: "
    );
    Serial.println(
      response
    );

    apiAuthenticated = false;

    drawNetworkStatus(
      "RetroLab API",
      "LOGIN FAILED"
    );
    delay(1500);

    return false;
  }

  // backend response is still a string, use ArduinoJson to convert into JSON doc
  JsonDocument document;

  DeserializationError error =
    deserializeJson(
      document,
      response
    );

  // even if 200, w need to make sure that it is a valid JSON
  if (error) {

    Serial.print(
      "JSON error: "
    );

    Serial.println(
      error.c_str()
    );

    apiAuthenticated = false;

    drawNetworkStatus(
      "RetroLab API",
      "JSON ERROR"
    );

    delay(1500);

    return false;
  }

  // successful authentication response must have a JWT, prevent the program from marking the ESP32 as authenticated if no token
  if (
    !document["token"].is<const char*>()
  ) {

    Serial.println(
      "JWT missing from response"
    );

    apiAuthenticated = false;

    drawNetworkStatus(
      "RetroLab API",
      "TOKEN ERROR"
    );
    delay(1500);

    return false;
  }

  // extract jJWT and store globally
  jwtToken =
    document["token"].as<String>();

  // 0 used as feault
  long expiresIn =
    document["expiresIn"] | 0;

  // finally can consider it authenticated
  apiAuthenticated = true;

  Serial.println(
    "Login successful"
  );
  Serial.println(
    "JWT received successfully"
  );
  // never print JWT itself
  Serial.print(
    "JWT length: "
  );

  Serial.println(
    jwtToken.length()
  );

  Serial.print(
    "JWT expires in: "
  );

  Serial.print(
    expiresIn
  );

  Serial.println(
    " ms"
  );

  //give visual feedback
  drawNetworkStatus(
    "RetroLab API",
    "LOGIN OK"
  );

  delay(1500);

  return true;
}

//CONNECTION

//return true only if both connection and API login succeed
bool connectRetroLab() {

  Serial.println();
  Serial.println(
    "Starting RetroLab connection..."
  );

  if (!connectWiFi()) {

    Serial.println(
      "RetroLab unavailable: no WiFi"
    );

    return false;
  }

  if (!loginRetroLab()) {

    Serial.println(
      "RetroLab unavailable: login failed"
    );

    return false;
  }

  Serial.println();
  Serial.println("============================");
  Serial.println("     RETROLAB CONNECTED");
  Serial.println("============================");

  return true;
}

void drawRepairCaseMenu();

//API DATA

// returns whether a test should remain selectable. String& to pass by reference, avoiding a copy, only read
bool isPendingTestResult(const String& result) {
  return result == "NOT_TESTED" ||
         result == "WARNING" ||
         result == "FAIL";
}

//shorten text for it to fit
String fitText(const String& text, int maxCharacters) {
  if (text.length() <= (unsigned int)maxCharacters) {
    return text;
  }
  if (maxCharacters <= 3) {
    return text.substring(0, maxCharacters);
  }
  return text.substring(0, maxCharacters - 3) + "...";
}

// make sure an API request can be made
bool ensureApiAuthentication() {
  if (WiFi.status() != WL_CONNECTED) {
    wifiConnected = false;
    apiAuthenticated = false;

    if (!connectWiFi()) {
      return false;
    }
  }
  if (!apiAuthenticated || jwtToken.length() == 0) {
    return loginRetroLab();
  }

  return true;
}

// reusable authenticated GET request, avoids repeating the same HTTP/JWT logic in every loader
bool authenticatedGet(
  const String& url,
  String& response,
  const char* resourceName
) {
  if (!ensureApiAuthentication()) {
    return false;
  }

  for (int attempt = 0; attempt < 2; attempt++) {
    HTTPClient http;

    Serial.println();
    Serial.print("GET ");
    Serial.println(url);

    if (!http.begin(url)) {
      Serial.println(
        "Could not initialize HTTP"
      );
      return false;
    }

    // Bearer scheme
    http.addHeader(
      "Authorization",
      "Bearer " + jwtToken
    );

    int responseCode = http.GET();

    Serial.print(resourceName);
    Serial.print(" HTTP status: ");
    Serial.println(responseCode);

    // 401 means the JWT is no longer accepted, new login is performed
    if (responseCode == 401) {
      http.end();
      apiAuthenticated = false;

      if (attempt == 0 && loginRetroLab()) {
        continue;
      }

      return false;
    }

    response = http.getString();
    http.end();

    if (responseCode != 200) {
      Serial.print(resourceName);
      Serial.print(" error: ");
      Serial.println(response);
      return false;
    }

    return true;
  }

  return false;
}


// get all available consoles, store in consoles[]
bool loadConsoles() {
  consoleCount = 0;
  selectedConsole = 0;

  drawNetworkStatus(
    "Consoles",
    "Loading..."
  );

  String url =
    String(API_BASE_URL) +
    "/api/v1/consoles";

  String response;

  if (!authenticatedGet(
        url,
        response,
        "Consoles"
      )) {
    drawNetworkStatus(
      "Consoles",
      "LOAD FAILED"
    );

    delay(1500);
    return false;
  }

  JsonDocument document;

  DeserializationError error =
    deserializeJson(document, response);

  if (error) {
    Serial.print("Consoles JSON error: ");
    Serial.println(error.c_str());

    drawNetworkStatus(
      "Consoles",
      "JSON ERROR"
    );

    delay(1500);
    return false;
  }

  JsonArray consoleArray =
    document.as<JsonArray>();

  for (JsonObject console : consoleArray) {
    if (consoleCount >= MAX_CONSOLES) {
      break;
    }

    // reference next free array element directly, & avoids copying ConsoleItem while its fields are populated
    ConsoleItem& item =
      consoles[consoleCount];

    item.consoleId =
      console["consoleId"] | -1;
    item.consoleModelName =
      console["consoleModelName"] | "";
    item.serialNumber =
      console["serialNumber"] | "";
    if (item.consoleId >= 0) {
      consoleCount++;
    }
  }

  Serial.print("Consoles loaded: ");
  Serial.println(consoleCount);

  return true;
}

// get only repair cases belonging to the selected console
bool loadRepairCasesForConsole(long consoleId) {
  repairCaseCount = 0;
  selectedRepairCase = 0;

  drawNetworkStatus(
    "Repair cases",
    "Loading..."
  );

  String url =
    String(API_BASE_URL) +
    "/api/v1/repair-cases/consoles/" +
    String(consoleId);

  String response;

  if (!authenticatedGet(
        url,
        response,
        "Repair cases"
      )) {
    drawNetworkStatus(
      "Repair cases",
      "LOAD FAILED"
    );

    delay(1500);
    return false;
  }

  JsonDocument document;

  DeserializationError error =
    deserializeJson(document, response);

  if (error) {
    Serial.print(
      "Repair cases JSON error: "
    );
    Serial.println(error.c_str());

    drawNetworkStatus(
      "Repair cases",
      "JSON ERROR"
    );

    delay(1500);
    return false;
  }

  JsonArray cases =
    document.as<JsonArray>();

  for (JsonObject repairCase : cases) {
    if (
      repairCaseCount >=
      MAX_REPAIR_CASES
    ) {
      break;
    }

    //same as before, reference next free item
    RepairCaseItem& item =
      repairCases[repairCaseCount];

    item.repairCaseId =
      repairCase["repairCaseId"] | -1;
    item.consoleName =
      repairCase["consoleName"] | "";
    item.title =
      repairCase["title"] | "";
    item.status =
      repairCase["status"] | "";

    if (item.repairCaseId >= 0) {
      repairCaseCount++;
    }
  }

  Serial.print(
    "Repair cases for console loaded: "
  );
  Serial.println(repairCaseCount);

  return true;
}

// get complete repair case list from backend, unfiltered alterative for loadRepairCasesForConsole()
// This alternative loader is no longer required by the console-first flow.
// Empty repair-case lists are reloaded for the active console instead.

//get component tests belonging to the selected repair case, only the ones that are not PASS
//existing values and null flags preserved, as we do not do patch
bool loadComponentTests(long repairCaseId) {
  componentTestCount = 0;
  selectedComponentTest = 0;

  drawNetworkStatus(
    "Component tests",
    "Loading..."
  );

  String url =
    String(API_BASE_URL) +
    "/api/v1/component-tests/repair-case/" +
    String(repairCaseId);

  String response;

  if (!authenticatedGet(
        url,
        response,
        "Component tests"
      )) {
    drawNetworkStatus(
      "Component tests",
      "LOAD FAILED"
    );

    delay(1500);
    return false;
  }

  JsonDocument document;

  DeserializationError error =
    deserializeJson(document, response);

  if (error) {
    Serial.print("Component tests JSON error: ");
    Serial.println(error.c_str());

    drawNetworkStatus(
      "Component tests",
      "JSON ERROR"
    );

    delay(1500);
    return false;
  }

  JsonArray tests = document.as<JsonArray>();

  //tests that are PASS ignored
  for (JsonObject test : tests) {
    String result = test["result"] | "";

    if (!isPendingTestResult(result)) {
      continue;
    }
    if (componentTestCount >= MAX_COMPONENT_TESTS) {
      break;
    }

    //reference the next free array
    ComponentTestItem& item =
      componentTests[componentTestCount];

    item.componentTestId =
      test["componentTestId"] | -1;
    item.repairCaseId =
      test["repairCaseId"] | -1;
    item.componentId =
      test["componentId"] | -1;
    item.componentName =
      test["componentName"] | "";
    item.hasVoltage =
      !test["measuredVoltage"].isNull();

    if (item.hasVoltage) {
      item.measuredVoltage =
        test["measuredVoltage"].as<float>();
    }

    item.hasCurrent =
      !test["measuredCurrent"].isNull();

    if (item.hasCurrent) {
      item.measuredCurrent =
        test["measuredCurrent"].as<float>();
    }

    item.hasResistance =
      !test["measuredResistance"].isNull();

    if (item.hasResistance) {
      item.measuredResistance =
        test["measuredResistance"].as<float>();
    }

    item.hasTemperature =
      !test["temperature"].isNull();

    if (item.hasTemperature) {
      item.temperature =
        test["temperature"].as<float>();
    }

    item.hasContinuity =
      !test["continuity"].isNull();

    if (item.hasContinuity) {
      item.continuity =
        test["continuity"].as<bool>();
    }

    item.result = result;
    item.notes =
      test["notes"] | "";

    if (item.componentTestId >= 0) {
      componentTestCount++;
    }
  }

  Serial.print("Pending component tests loaded: ");
  Serial.println(componentTestCount);

  return true;
}

// SAVE MEASUREMENTS PART

// round measurements to two decimals before sending to backend
float roundToTwoDecimals(float value) {
  return roundf(value * 100.0f) / 100.0f;
}

// reusable oled screen, similar to before
void drawSaveStatus(
  const char* status,
  const char* detail
) {
  buzzerOff();
  u8g2.clearBuffer();
  u8g2.setFont(u8g2_font_6x10_tf);
  u8g2.drawStr(0, 9, "RETROLAB SAVE");
  u8g2.drawHLine(0, 12, 128);
  u8g2.drawStr(0, 31, status);
  u8g2.drawStr(0, 48, detail);
  u8g2.sendBuffer();
}

// build the complete ComponentTest JSON, existing values are preserved
bool saveActiveComponentTest() {
  if (
    activeComponentTestId < 0 ||
    activeRepairCaseId < 0 ||
    activeComponentId < 0
  ) {
    drawSaveStatus(
      "SAVE FAILED",
      "No active test"
    );
    delay(1200);
    return false;
  }

  if (!ensureApiAuthentication()) {
    drawSaveStatus(
      "SAVE FAILED",
      "No connection"
    );
    delay(1200);
    return false;
  }

  // this doc will be converted into the JSON body of the PUT
  JsonDocument request;

  //ids for RepairCase and Component associated with this test
  request["repairCaseId"] = activeRepairCaseId;
  request["componentId"] = activeComponentId;

  if (activeHasVoltage) {
    request["measuredVoltage"] =
      roundToTwoDecimals(
        activeMeasuredVoltage
      );
  } else {
    request["measuredVoltage"] = nullptr;
  }

  if (activeHasCurrent) {
    request["measuredCurrent"] =
      roundToTwoDecimals(
        activeMeasuredCurrent
      );
  } else {
    request["measuredCurrent"] = nullptr;
  }

  if (activeHasResistance) {
    request["measuredResistance"] =
      roundToTwoDecimals(
        activeMeasuredResistance
      );
  } else {
    request["measuredResistance"] = nullptr;
  }

  if (activeHasTemperature) {
    request["temperature"] =
      roundToTwoDecimals(
        activeTemperature
      );
  } else {
    request["temperature"] = nullptr;
  }

  if (activeHasContinuity) {
    request["continuity"] =
      activeContinuity;
  } else {
    request["continuity"] = nullptr;
  }

  request["result"] =
    activeTestResult;

  if (activeTestNotes.length() > 0) {
    request["notes"] =
      activeTestNotes;
  } else {
    request["notes"] = nullptr;
  }

  //HTTPCLiente sends text as request body, so serializeJson() converts it into a JSON formatted Arduino string
  String requestBody;
  serializeJson(request, requestBody);

  HTTPClient http;

  String url =
    String(API_BASE_URL) +
    "/api/v1/component-tests/" +
    String(activeComponentTestId);

  Serial.println();
  Serial.print("PUT ");
  Serial.println(url);
  Serial.print("Body: ");
  Serial.println(requestBody);

  if (!http.begin(url)) {
    drawSaveStatus(
      "SAVE FAILED",
      "HTTP error"
    );
    delay(1500);
    return false;
  }

  //same as above, tell that it has JSON
  http.addHeader(
    "Content-Type",
    "application/json"
  );

  http.addHeader(
    "Authorization",
    "Bearer " + jwtToken
  );

  int responseCode =
    http.PUT(requestBody);

  Serial.print("Save HTTP status: ");
  Serial.println(responseCode);

  String response =
    http.getString();

  http.end();

  if (
    responseCode >= 200 &&
    responseCode < 300
  ) {
    Serial.println("Measurement saved");
    drawSaveStatus(
      "SAVED",
      activeComponentName.c_str()
    );
    delay(1000);
    return true;
  }

  Serial.print("Save error: ");
  Serial.println(response);

  drawSaveStatus(
    "SAVE FAILED",
    "Backend rejected"
  );

  delay(1500);
  return false;
}

// read exactly one measurement according to the currently active mode, reutrning true means
//the value is valid and then saveCurrentMeasurement() may continue with the HTTP save
bool captureCurrentMeasurement() {
  switch (currentMode) {
    case VOLTAGE_MODE: {
      float value = readVoltage();

      //rejects negative voltage.
      if (value < 0) {
        drawSaveStatus(
          "NOT SAVED",
          "Negative voltage"
        );
        delay(1500);
        return false;
      }
      activeMeasuredVoltage = value;
      activeHasVoltage = true;
      return true;
    }

    case CURRENT_MODE: {
      //store in mA
      float value =readCurrent() * 1000.0;
      if (value < 0) {
        drawSaveStatus(
          "NOT SAVED",
          "Negative current"
        );
        delay(1200);
        return false;
      }

      activeMeasuredCurrent = value;
      activeHasCurrent = true;
      return true;
    }

    case RESISTANCE_MODE: {
      float value = readResistance();

      if (
        isinf(value) ||
        isnan(value) ||
        value < 0
      ) {
        drawSaveStatus(
          "NOT SAVED",
          "Invalid resistance"
        );
        delay(1500);
        return false;
      }
      activeMeasuredResistance = value;
      activeHasResistance = true;
      return true;
    }

    case CONTINUITY_MODE: {
      float resistance =
        readResistance();

      activeContinuity =
        !isinf(resistance) &&
        resistance <
          CONTINUITY_THRESHOLD;

      activeHasContinuity = true;
      return true;
    }

    case TEMPERATURE_MODE: {
      float value =
        readTemperature();

      if (
        value == DEVICE_DISCONNECTED_C
      ) {
        drawSaveStatus(
          "NOT SAVED",
          "Sensor error"
        );
        delay(1500);
        return false;
      }

      activeTemperature = value;
      activeHasTemperature = true;
      return true;
    }

    default:
      return false;
  }
}

//cordinate the save operation the long encoder press triggers, the ComponentTest is still active to measure more values
void saveCurrentMeasurement() {
  Mode measurementMode =
    currentMode;

  drawSaveStatus(
    "SENDING...",
    activeComponentName.c_str()
  );

  if (captureCurrentMeasurement()) {
    saveActiveComponentTest();
  }
  currentMode =MENU;

  drawMenu();

  //the user remains on the selected Component Test.
  Serial.print("Active component test: ");
  Serial.println(activeComponentTestId);
}

// CONSOLE SCREEN
void drawConsoleMenu() {
  buzzerOff();
  u8g2.clearBuffer();

  u8g2.setFont(u8g2_font_6x10_tf);
  u8g2.drawStr(0, 9, "SELECT CONSOLE");
  u8g2.drawHLine(0, 12, 128);

  if (consoleCount == 0) {
    u8g2.drawStr(15, 31, "NO CONSOLES");
    u8g2.drawStr(8, 49, "Press to reload");
    u8g2.sendBuffer();
    return;
  }

  const int visibleItems = 4;
  int firstItem = selectedConsole - 1;

  if (firstItem < 0) {
    firstItem = 0;
  }

  if (
    firstItem >
    consoleCount - visibleItems
  ) {
    firstItem =
      consoleCount - visibleItems;
  }

  if (firstItem < 0) {
    firstItem = 0;
  }

  for (
    int row = 0;
    row < visibleItems;
    row++
  ) {
    int index = firstItem + row;

    if (index >= consoleCount) {
      break;
    }

    int y = 23 + row * 10;

    //serial number
    String label =
      consoles[index].serialNumber;

    label = fitText(label, 20);

    if (index == selectedConsole) {
      u8g2.drawBox(
        0,
        y - 8,
        128,
        10
      );
      u8g2.setDrawColor(0);
      u8g2.drawStr(
        3,
        y,
        label.c_str()
      );
      u8g2.setDrawColor(1);
    } else {
      u8g2.drawStr(
        3,
        y,
        label.c_str()
      );
    }
  }

  u8g2.sendBuffer();
}

// confirm console highlighted by the encoder
void selectConsole() {
  if (consoleCount == 0) {
    loadConsoles();
    drawConsoleMenu();
    return;
  }

  ConsoleItem& selected =
    consoles[selectedConsole];

  activeConsoleId =
    selected.consoleId;

  Serial.println();
  Serial.print("Selected console: ");
  Serial.print(selected.consoleModelName);
  Serial.print(" | Serial: ");
  Serial.print(selected.serialNumber);
  Serial.print(" | ID: ");
  Serial.println(activeConsoleId);

  if (
    !loadRepairCasesForConsole(
      activeConsoleId
    )
  ) {
    currentMode = CONSOLE_MENU;
    drawConsoleMenu();
    return;
  }

  currentMode = REPAIR_CASE_MENU;
  drawRepairCaseMenu();
}

//REPAIR CASE SCREEN

//render repair cases associated with console, scrolling equivalent to selectedRepairCase
void drawRepairCaseMenu() {
  buzzerOff();
  u8g2.clearBuffer();

  u8g2.setFont(u8g2_font_6x10_tf);
  u8g2.drawStr(0, 9, "SELECT REPAIR CASE");
  u8g2.drawHLine(0, 12, 128);

  if (repairCaseCount == 0) {
    u8g2.drawStr(8, 31, "NO REPAIR CASES");
    u8g2.drawStr(8, 49, "Press to reload");
    u8g2.sendBuffer();
    return;
  }

  const int visibleItems = 4;

  int firstItem =
    selectedRepairCase - 1;

  if (firstItem < 0) {
    firstItem = 0;
  }

  if (firstItem > repairCaseCount - visibleItems) {
    firstItem = repairCaseCount - visibleItems;
  }

  if (firstItem < 0) {
    firstItem = 0;
  }

  for (int row = 0; row < visibleItems; row++) {
    int index = firstItem + row;

    if (index >= repairCaseCount) {
      break;
    }

    int y = 23 + row * 10;

    String label =
      repairCases[index].title;

    if (label.length() == 0) {
      label =
        repairCases[index].consoleName;
    }

    label = fitText(label, 20);
    if (index == selectedRepairCase) {
      u8g2.drawBox(0, y - 8, 128, 10);
      u8g2.setDrawColor(0);
      u8g2.drawStr(3, y, label.c_str());
      u8g2.setDrawColor(1);
    } else {
      u8g2.drawStr(3, y, label.c_str());
    }
  }

  u8g2.sendBuffer();
}

//COMPONENT TEST SCREEN

//render componentTests for the active RepairCase, PASS tests already removed
void drawComponentTestMenu() {
  buzzerOff();
  u8g2.clearBuffer();

  u8g2.setFont(u8g2_font_6x10_tf);
  u8g2.drawStr(0, 9, "SELECT COMPONENT TEST");
  u8g2.drawHLine(0, 12, 128);

  const int visibleItems = 4;

  int firstItem =selectedComponentTest - 1;

  if (firstItem < 0) {
    firstItem = 0;
  }

  if (firstItem > componentTestCount - visibleItems) {
    firstItem = componentTestCount - visibleItems;
  }

  if (firstItem < 0) {
    firstItem = 0;
  }

  for (int row = 0; row < visibleItems; row++) {
    int index = firstItem + row;

    if (index >= componentTestCount) {
      break;
    }

    int y = 23 + row * 10;

    String label =
      componentTests[index].componentName;

    if (label.length() == 0) {
      label =
        "Test " +
        String(
          componentTests[index].componentTestId
        );
    }

    label = fitText(label, 20);

    if (index == selectedComponentTest) {
      u8g2.drawBox(0, y - 8, 128, 10);
      u8g2.setDrawColor(0);
      u8g2.drawStr(3, y, label.c_str());
      u8g2.setDrawColor(1);
    } else {
      u8g2.drawStr(3, y, label.c_str());
    }
  }

  u8g2.sendBuffer();
}

void drawNoTestsScreen() {
  buzzerOff();
  u8g2.clearBuffer();

  u8g2.setFont(u8g2_font_6x10_tf);
  u8g2.drawStr(0, 9, "COMPONENT TESTS");
  u8g2.drawHLine(0, 12, 128);

  u8g2.drawStr(10, 31, "NO PENDING TESTS");
  u8g2.drawStr(12, 49, "Press to return");
  u8g2.sendBuffer();
}

//RETROLAB SELECTION

//confirm selected RepairCase and request its ComponentTests, if no tests, directed to NO_TESTS_SCREEN
void selectRepairCase() {
  if (repairCaseCount == 0) {
    // reload only the repair cases belonging to the already selected console
    loadRepairCasesForConsole(activeConsoleId);
    drawRepairCaseMenu();
    return;
  }

  RepairCaseItem& repairCase =
    repairCases[selectedRepairCase];

  activeRepairCaseId =
    repairCase.repairCaseId;

  activeRepairCaseTitle =
    repairCase.title;

  Serial.println();
  Serial.print("Selected repair case: ");
  Serial.print(activeRepairCaseId);
  Serial.print(" - ");
  Serial.println(activeRepairCaseTitle);

  if (!loadComponentTests(activeRepairCaseId)) {
    currentMode = REPAIR_CASE_MENU;
    drawRepairCaseMenu();
    return;
  }

  if (componentTestCount == 0) {
    currentMode = NO_TESTS_SCREEN;
    drawNoTestsScreen();
    return;
  }
  currentMode = COMPONENT_TEST_MENU;
  drawComponentTestMenu();
}

//copy selected ComponentTests into the active variables, working copy of the backend object
void selectComponentTest() {
  if (componentTestCount == 0) {
    currentMode = NO_TESTS_SCREEN;
    drawNoTestsScreen();
    return;
  }

  ComponentTestItem& test =
    componentTests[selectedComponentTest];
  activeComponentTestId =
    test.componentTestId;
  activeComponentId =
    test.componentId;
  activeComponentName =
    test.componentName;
  activeTestResult =
    test.result;
  activeTestNotes =
    test.notes;
  activeMeasuredVoltage =
    test.measuredVoltage;
  activeHasVoltage =
    test.hasVoltage;
  activeMeasuredCurrent =
    test.measuredCurrent;
  activeHasCurrent =
    test.hasCurrent;
  activeMeasuredResistance =
    test.measuredResistance;
  activeHasResistance =
    test.hasResistance;
  activeTemperature =
    test.temperature;
  activeHasTemperature =
    test.hasTemperature;
  activeContinuity =
    test.continuity;
  activeHasContinuity =
    test.hasContinuity;

  Serial.println();
  Serial.print("Selected component test: ");
  Serial.print(activeComponentTestId);
  Serial.print(" - ");
  Serial.print(activeComponentName);
  Serial.print(" [");
  Serial.print(activeTestResult);
  Serial.println("]");

  selectedMenuItem = 0;
  currentMode = MENU;
  drawMenu();
}

//I2C SCANNER

void scanI2C() {

  Serial.println();
  Serial.println("============================");
  Serial.println("      RETROLAB I2C SCAN");
  Serial.println("============================");

  inaAddress = 0;

  int devices = 0;

  for (
    uint8_t address = 1;
    address < 127;
    address++
  ) {

    Wire.beginTransmission(
      address
    );

    uint8_t error =
      Wire.endTransmission();

    if (error == 0) {
      devices++;

      Serial.print(
        "Encontrado: 0x"
      );

      if (address < 16) {
        Serial.print("0");
      }
      Serial.print(
        address,
        HEX
      );

      if (
        address == 0x3C ||
        address == 0x3D
      ) {

        Serial.print(
          " --> OLED"
        );

      } else if (
        address >= 0x40 &&
        address <= 0x43
      ) {

        inaAddress =
          address;

        Serial.print(
          " --> INA3221"
        );

      } else if (
        address >= 0x48 &&
        address <= 0x4B
      ) {
        Serial.print(
          " --> ADS1115"
        );
      }

      Serial.println();
    }
  }

  Serial.print(
    "Total I2C: "
  );

  Serial.println(
    devices
  );

  Serial.println(
    "============================"
  );
}

// ======================================================
// INA3221 REGISTER
// ======================================================

// read one 15-bit register directly from INA3221 over I2C. address + repeated-start read of the two bytes from the sensor
uint16_t readINARegister(
  uint8_t reg
) {

  if (inaAddress == 0) {
    return 0;
  }

  Wire.beginTransmission(
    inaAddress
  );
  Wire.write(
    reg
  );
  if (
    Wire.endTransmission(false) != 0
  ) {

    return 0;
  }

  Wire.requestFrom(
    inaAddress,
    (uint8_t)2
  );
  if (
    Wire.available() < 2
  ) {
    return 0;
  }

  uint16_t value =
    Wire.read() << 8;
  value |=
    Wire.read();

  return value;
}

//CURRENT

//convert INA3221 CH1 shunt-register value into volts
// there are status and reserved lower bits, so shift to the right
float readShuntVoltage() {

  int16_t raw =
    (int16_t)readINARegister(
      INA3221_REG_SHUNT_CH1
    );

  raw >>= 3;
  return raw * 0.000040;
}

// calculate current using Ohms law I = V/R from voltage drop across the 0.1 ohm shunt, apply callibration
float readCurrent() {

  float shuntVoltage =
    readShuntVoltage();

  float current =
    shuntVoltage /
    SHUNT_RESISTANCE;

  current *=CURRENT_CALIBRATION;
  return current;
}

//VOLTAGE

//read the ADS1115 voltage channel, as there is a tension divider we need to reconstruct
float readVoltage() {

  int16_t raw =
    ads.readADC_SingleEnded(
      VOLTAGE_CHANNEL
    );

  float adcVoltage =
    ads.computeVolts(raw);

  float voltage =
    adcVoltage *
    VOLTAGE_DIVIDER_RATIO;

  //supress noise around 0v
  if (
    voltage > -0.02 &&
    voltage < 0.02
  ) {
    voltage = 0;
  }

  return voltage;
}

//RESISTANCE

//several samples are collected and sorted, so to avoid dirty readings
//divider equation derives the unknown resistance from the measured node voltage, RREF and VCC_RESISTANCE
//second filter stabilizes the displayed value
float readResistance() {

  const int sampleCount = 15;

  float samples[
    sampleCount
  ];

  //discard first conversion
  ads.readADC_SingleEnded(
    RESISTANCE_CHANNEL
  );

  //take several samples
  for (
    int i = 0;
    i < sampleCount;
    i++
  ) {

    int16_t raw =
      ads.readADC_SingleEnded(
        RESISTANCE_CHANNEL
      );

    samples[i] =
      ads.computeVolts(raw);

    delay(3);
  }
  //order samples
  for (
    int i = 0;
    i < sampleCount - 1;
    i++
  ) {

    for (
      int j = i + 1;
      j < sampleCount;
      j++
    ) {

      if (
        samples[j] <
        samples[i]
      ) {
        float temporary =
          samples[i];
        samples[i] =
          samples[j];
        samples[j] =
          temporary;
      }
    }
  }

  float v =
    samples[
      sampleCount / 2
    ];

  static float filteredResistance =
    -1.0;

  if (v < 0) {
    v = 0;
  }
  //short or very small resistor
  if (v < 0.005) {

    filteredResistance = 0;
    return 0;
  }

  //open circuit
  if (
    v >=
    VCC_RESISTANCE - 0.10
  ) {
    filteredResistance =-1.0;

    return INFINITY;
  }

  float resistance =
    RREF *
    (
      v /
      (
        VCC_RESISTANCE - v
      )
    );

  resistance *=RESISTANCE_CALIBRATION;

  //reload filter when there is a clear change
  if (
    filteredResistance < 0 ||
    filteredResistance == 0 ||
    resistance >
      filteredResistance * 1.20 ||
    resistance <
      filteredResistance * 0.80
  ) {

    filteredResistance =
      resistance;

  } else {
    const float alpha =
      0.20;

    filteredResistance =
      alpha * resistance +
      (1.0 - alpha) *
      filteredResistance;
  }

  return filteredResistance;
}

//TEMPERATURE

float readTemperature() {

  if (!tempFound) {
    return
      DEVICE_DISCONNECTED_C;
  }

  tempSensor.requestTemperatures();
  return
    tempSensor.getTempCByIndex(0);
}

//OLED HEADER

//draw common header
void drawHeader(
  const char* title
) {

  u8g2.setFont(
    u8g2_font_6x10_tf
  );

  u8g2.drawStr(
    0,
    9,
    "RETROLAB"
  );

  u8g2.drawHLine(
    0,
    12,
    128
  );

  u8g2.drawStr(
    0,
    23,
    title
  );
}

//MENU

//draw measurement options
void drawMenu() {

  buzzerOff();

  u8g2.clearBuffer();

  u8g2.setFont(
    u8g2_font_6x10_tf
  );

  u8g2.drawStr(
    0,
    9,
    "RETROLAB MULTIMETER"
  );

  u8g2.drawHLine(
    0,
    12,
    128
  );

  for (
    int i = 0;
    i < MENU_COUNT;
    i++
  ) {

    int y =
      22 + i * 9;

    if (
      i ==
      selectedMenuItem
    ) {

      u8g2.drawBox(
        0,
        y - 7,
        128,
        9
      );

      u8g2.setDrawColor(0);
      u8g2.drawStr(
        4,
        y,
        menuItems[i]
      );

      u8g2.setDrawColor(1);
    } else {
      u8g2.drawStr(
        4,
        y,
        menuItems[i]
      );
    }
  }

  u8g2.sendBuffer();
}

//VOLTAGE SCREEN

void drawVoltageMode() {

  float voltage =
    readVoltage();

  char buffer[32];

  u8g2.clearBuffer();
  drawHeader(
    "VOLTAGE"
  );

  u8g2.setFont(
    u8g2_font_logisoso24_tf
  );

  snprintf(
    buffer,
    sizeof(buffer),
    "%.3f",
    voltage
  );

  u8g2.drawStr(
    3,
    52,
    buffer
  );

  u8g2.setFont(
    u8g2_font_6x10_tf
  );

  u8g2.drawStr(
    103,
    52,
    "V"
  );

  u8g2.sendBuffer();

  Serial.print(
    "Voltage A0: "
  );

  Serial.print(
    voltage,
    3
  );
  Serial.println(
    " V"
  );
}

//CURRENT SCREEN

void drawCurrentMode() {

  float currentMA =
    readCurrent() *
    1000.0;

  char buffer[32];

  u8g2.clearBuffer();

  drawHeader(
    "CURRENT"
  );

  u8g2.setFont(
    u8g2_font_logisoso24_tf
  );

  snprintf(
    buffer,
    sizeof(buffer),
    "%.1f",
    currentMA
  );

  u8g2.drawStr(
    3,
    52,
    buffer
  );

  u8g2.setFont(
    u8g2_font_6x10_tf
  );
  u8g2.drawStr(
    92,
    52,
    "mA"
  );

  u8g2.sendBuffer();

  Serial.print(
    "Current: "
  );

  Serial.print(
    currentMA,
    2
  );
  Serial.println(
    " mA"
  );
}

//RESISTANCE SCREEN

void drawResistanceMode() {

  float resistance =
    readResistance();

  char buffer[32];

  u8g2.clearBuffer();

  drawHeader(
    "RESISTANCE"
  );

  if (
    isinf(resistance)
  ) {
    u8g2.setFont(
      u8g2_font_logisoso20_tf
    );

    u8g2.drawStr(
      20,
      50,
      "OPEN"
    );

  } else if (
    resistance >= 1000
  ) {

    u8g2.setFont(
      u8g2_font_logisoso20_tf
    );

    snprintf(
      buffer,
      sizeof(buffer),
      "%.2f",
      resistance / 1000.0
    );

    u8g2.drawStr(
      4,
      50,
      buffer
    );
    u8g2.setFont(
      u8g2_font_6x10_tf
    );

    u8g2.drawStr(
      82,
      50,
      "kOhm"
    );

  } else {

    u8g2.setFont(
      u8g2_font_logisoso20_tf
    );

    snprintf(
      buffer,
      sizeof(buffer),
      "%.1f",
      resistance
    );

    u8g2.drawStr(
      4,
      50,
      buffer
    );

    u8g2.setFont(
      u8g2_font_6x10_tf
    );

    u8g2.drawStr(
      90,
      50,
      "Ohm"
    );
  }

  u8g2.sendBuffer();

  Serial.print(
    "Resistance A1: "
  );

  if (
    isinf(resistance)
  ) {

    Serial.println(
      "OPEN"
    );

  } else {

    Serial.print(
      resistance,
      1
    );

    Serial.println(
      " Ohm"
    );
  }
}

//CONTINUITY SCREEN

//uses resistance to determine continuity, if below threshold activate buzzer and BEEP
void drawContinuityMode() {

  float resistance =
    readResistance();

  bool continuity =
    !isinf(resistance) &&
    resistance <
      CONTINUITY_THRESHOLD;

  if (continuity) {
    buzzerOn();

  } else {
    buzzerOff();
  }

  char buffer[32];

  u8g2.clearBuffer();

  drawHeader(
    "CONTINUITY"
  );

  if (continuity) {

    u8g2.setFont(
      u8g2_font_logisoso20_tf
    );

    u8g2.drawStr(
      22,
      48,
      "BEEP"
    );

  } else {

    u8g2.setFont(
      u8g2_font_logisoso20_tf
    );

    u8g2.drawStr(
      17,
      48,
      "OPEN"
    );
  }

  u8g2.setFont(
    u8g2_font_5x8_tf
  );

  if (
    isinf(resistance)
  ) {

    snprintf(
      buffer,
      sizeof(buffer),
      "R: OPEN"
    );

  } else {

    snprintf(
      buffer,
      sizeof(buffer),
      "R: %.1f ohm",
      resistance
    );
  }

  u8g2.drawStr(
    0,
    60,
    buffer
  );
  u8g2.sendBuffer();
}

//TEMPERATURE SCREEN

void drawTemperatureMode() {

  float temperature =
    readTemperature();

  char buffer[32];

  u8g2.clearBuffer();
  drawHeader(
    "TEMPERATURE"
  );

  if (
    temperature ==
    DEVICE_DISCONNECTED_C
  ) {

    u8g2.setFont(
      u8g2_font_6x10_tf
    );

    u8g2.drawStr(
      8,
      43,
      "SENSOR ERROR"
    );

  } else {
    u8g2.setFont(
      u8g2_font_logisoso24_tf
    );

    snprintf(
      buffer,
      sizeof(buffer),
      "%.1f",
      temperature
    );

    u8g2.drawStr(
      3,
      52,
      buffer
    );

    u8g2.setFont(
      u8g2_font_6x10_tf
    );
    u8g2.drawStr(
      88,
      52,
      "C"
    );
  }

  u8g2.sendBuffer();

  Serial.print(
    "Temperature: "
  );

  Serial.print(
    temperature,
    1
  );
  Serial.println(
    " C"
  );
}

//SELECT MODE

//translate menu index into coresponding mode, loop() will then begin refreshing
void enterSelectedMode() {

  switch (
    selectedMenuItem
  ) {

    case 0:
      currentMode =
        VOLTAGE_MODE;
      break;
    case 1:
      currentMode =
        CURRENT_MODE;
      break;
    case 2:
      currentMode =
        RESISTANCE_MODE;
      break;
    case 3:
      currentMode =
        CONTINUITY_MODE;
      break;
    case 4:
      currentMode =
        TEMPERATURE_MODE;
      break;
  }
}

//ENCODER

//central input handler, button behavior depends on context, refer to TFG memory
void handleEncoder() {

  int currentCLKState =
    digitalRead(
      ENCODER_CLK
    );

  // ROTATION
  if (
    currentCLKState != lastCLKState &&
    currentCLKState == LOW
  ) {

    if (
      millis() -
      lastEncoderMove >
      ENCODER_DEBOUNCE
    ) {

      int direction;

      if (
        digitalRead(
          ENCODER_DT
        ) != currentCLKState
      ) {
        direction = 1;
      } else {
        direction = -1;
      }

      if (
        currentMode == CONSOLE_MENU &&
        consoleCount > 0
      ) {
        selectedConsole += direction;

        if (selectedConsole >= consoleCount) {
          selectedConsole = 0;
        }

        if (selectedConsole < 0) {
          selectedConsole =
            consoleCount - 1;
        }
        drawConsoleMenu();

      } else if (
        currentMode == REPAIR_CASE_MENU &&
        repairCaseCount > 0
      ) {
        selectedRepairCase += direction;
        if (
          selectedRepairCase >=
          repairCaseCount
        ) {
          selectedRepairCase = 0;
        }
        if (selectedRepairCase < 0) {
          selectedRepairCase =
            repairCaseCount - 1;
        }

        drawRepairCaseMenu();

      } else if (
        currentMode ==
          COMPONENT_TEST_MENU &&
        componentTestCount > 0
      ) {
        selectedComponentTest += direction;

        if (
          selectedComponentTest >=
          componentTestCount
        ) {
          selectedComponentTest = 0;
        }
        if (selectedComponentTest < 0) {
          selectedComponentTest =
            componentTestCount - 1;
        }

        drawComponentTestMenu();

      } else if (
        currentMode == MENU
      ) {
        selectedMenuItem += direction;

        if (selectedMenuItem >= MENU_COUNT) {
          selectedMenuItem = 0;
        }
        if (selectedMenuItem < 0) {
          selectedMenuItem =
            MENU_COUNT - 1;
        }
        drawMenu();
      }

      lastEncoderMove = millis();
    }
  }

  lastCLKState = currentCLKState;

  // BUTTON
  bool buttonDown =
    digitalRead(ENCODER_SW) == LOW;

  if (
    buttonDown &&
    !buttonWasDown
  ) {
    buttonWasDown = true;
    buttonDownAt = millis();
  }

  if (
    !buttonDown &&
    buttonWasDown
  ) {
    buttonWasDown = false;

    unsigned long pressDuration =
      millis() - buttonDownAt;

    if (
      millis() -
      lastButtonPress >
      BUTTON_DEBOUNCE
    ) {

      bool measurementMode =
        currentMode == VOLTAGE_MODE ||
        currentMode == CURRENT_MODE ||
        currentMode == RESISTANCE_MODE ||
        currentMode == CONTINUITY_MODE ||
        currentMode == TEMPERATURE_MODE;

      //distinguish short from long ppress
      if (
        measurementMode &&
        pressDuration >= SAVE_HOLD_TIME
      ) {
        saveCurrentMeasurement();

      } else {
        switch (currentMode) {
          case CONSOLE_MENU:
            selectConsole();
            break;
          case REPAIR_CASE_MENU:
            selectRepairCase();
            break;
          case COMPONENT_TEST_MENU:
            selectComponentTest();
            break;
          case NO_TESTS_SCREEN:
            currentMode =
              REPAIR_CASE_MENU;
            drawRepairCaseMenu();
            break;
          case MENU:
            enterSelectedMode();
            break;

          case VOLTAGE_MODE:
          case CURRENT_MODE:
          case RESISTANCE_MODE:
          case CONTINUITY_MODE:
          case TEMPERATURE_MODE:
            currentMode = MENU;
            buzzerOff();
            drawMenu();
            break;
        }
      }

      lastButtonPress = millis();
    }
  }
}

//SETUP

//runs once the ESP32 starts, initializes Serial, I2C, OLED, ADS1115, DS18B20, buzzer and encoder
void setup() {

  Serial.begin(
    115200
  );

  delay(700);

  //I2C
  Wire.begin(
    SDA_PIN,
    SCL_PIN
  );

  Wire.setClock(
    100000
  );

  //OLED
  u8g2.begin();

  //Scan
  scanI2C();

  //ADS1115
  if (
    !ads.begin(
      0x48,
      &Wire
    )
  ) {
    Serial.println(
      "ERROR: ADS1115"
    );
    u8g2.clearBuffer();
    u8g2.setFont(
      u8g2_font_6x10_tf
    );
    u8g2.drawStr(
      0,
      30,
      "ADS1115 ERROR"
    );

    u8g2.sendBuffer();

    while (true) {
      delay(1000);
    }
  }
  ads.setGain(
    GAIN_ONE
  );

  //Temperature
  tempSensor.begin();

  tempFound =
    tempSensor.getDeviceCount() >
    0;

  Serial.print(
    "DS18B20 GPIO32: "
  );

  Serial.println(
    tempFound
      ? "OK"
      : "NOT FOUND"
  );

  //Buzzer
  pinMode(
    BUZZER_PIN,
    OUTPUT
  );

  buzzerOff();

  //Encoder
  pinMode(
    ENCODER_CLK,
    INPUT_PULLUP
  );

  pinMode(
    ENCODER_DT,
    INPUT_PULLUP
  );

  pinMode(
    ENCODER_SW,
    INPUT_PULLUP
  );

  lastCLKState =
    digitalRead(
      ENCODER_CLK
    );

  bool retroLabConnected =
    connectRetroLab();

  // Console --> Repair Case --> Component Test.
  if (
    retroLabConnected &&
    loadConsoles()
  ) {
    currentMode = CONSOLE_MENU;
    drawConsoleMenu();
  } else {
    currentMode = CONSOLE_MENU;
    consoleCount = 0;
    drawConsoleMenu();
  }

  Serial.println();

  Serial.println(
    "RETROLAB READY"
  );
  Serial.println(
    "Voltage    --> ADS1115 A0"
  );
  Serial.println(
    "Resistance --> ADS1115 A1"
  );
  Serial.print(
    "WiFi: "
  );

  Serial.println(
    wifiConnected
      ? "OK"
      : "OFFLINE"
  );

  Serial.print(
    "RetroLab API: "
  );

  Serial.println(
    apiAuthenticated
      ? "AUTHENTICATED"
      : "OFFLINE"
  );
}

//loop, delays balances responsiveness against sensor and display update frequency
void loop() {

  handleEncoder();

  switch (
    currentMode
  ) {

    case CONSOLE_MENU:
    case REPAIR_CASE_MENU:
    case COMPONENT_TEST_MENU:
    case NO_TESTS_SCREEN:
    case MENU:
      break;

    case VOLTAGE_MODE:

      buzzerOff();
      drawVoltageMode();
      delay(100);
      break;

    case CURRENT_MODE:

      buzzerOff();
      drawCurrentMode();
      delay(100);
      break;

    case RESISTANCE_MODE:

      buzzerOff();
      drawResistanceMode();
      delay(100);
      break;

    case CONTINUITY_MODE:

      drawContinuityMode();
      delay(50);
      break;

    case TEMPERATURE_MODE:

      buzzerOff();
      drawTemperatureMode();
      delay(250);
      break;
  }
}