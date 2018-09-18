/*  Getting_BPM_to_Monitor prints the BPM to the Serial Monitor, using the least lines of code and PulseSensor Library.
    Tutorial Webpage: https://pulsesensor.com/pages/getting-advanced

  --------Use This Sketch To------------------------------------------
  1) Displays user's live and changing BPM, Beats Per Minute, in Arduino's native Serial Monitor.
  2) Print: "♥  A HeartBeat Happened !" when a beat is detected, live.
  2) Learn about using a PulseSensor Library "Object".
  4) Blinks LED on PIN 13 with user's Heartbeat.
  --------------------------------------------------------------------*/

#define USE_ARDUINO_INTERRUPTS true    // Set-up low-level interrupts for most acurate BPM math.
#include "PulseSensorPlayground.h"     // Includes the PulseSensorPlayground Library.
#include <SoftwareSerial.h>
#include <SPI.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

// If using software SPI (the default case):
#define OLED_MOSI  12
#define OLED_CLK   13
#define OLED_DC    9
#define OLED_CS    3
#define OLED_RESET 8
Adafruit_SSD1306 display(OLED_MOSI, OLED_CLK, OLED_DC, OLED_RESET, OLED_CS);

//  Variables
const int PulseWire = A3;       // PulseSensor PURPLE WIRE connected to ANALOG PIN 0
//const int LED13 = 13;          // The on-board Arduino LED, close to PIN 13.
int Threshold = 550;           // Determine which Signal to "count as a beat" and which to ignore.
// Use the "Gettting Started Project" to fine-tune Threshold Value beyond default setting.
// Otherwise leave the default "550" value.
const int BTSerialRX = 10;
const int BTSerialTX = 11;

SoftwareSerial mySerial(BTSerialRX, BTSerialTX);
PulseSensorPlayground pulseSensor;  // Creates an instance of the PulseSensorPlayground object called "pulseSensor"

#define SSD1306_LCDHEIGHT 64
#if (SSD1306_LCDHEIGHT != 64)
#error("Height incorrect, please fix Adafruit_SSD1306.h!");
#endif

long accelX, accelY, accelZ;
float gForceX, gForceY, gForceZ;

long gyroX, gyroY, gyroZ;
float rotX, rotY, rotZ;

float total;
float threshhold = 0.8;
int steps, flag = 0;
int distanceInCM, distanceInM, distanceInKM, distance;
int averageStepLength = 76;
int myBPM = 0;

int pinButton = 7; //the pin where we connect the button
int pinButtonAnotherDevice = 6;

void setup() {

  Serial.begin(9600);          // For Serial Monitor
  mySerial.begin(9600);

  // by default, we'll generate the high voltage from the 3.3v line internally! (neat!)
  display.begin(SSD1306_SWITCHCAPVCC);
  // init done
  Wire.begin();
  setupMPU();

  // Clear the buffer.
  display.clearDisplay();

  // Configure the PulseSensor object, by assigning our variables to it.
  pulseSensor.analogInput(PulseWire);
  //pulseSensor.blinkOnPulse(LED13);       //auto-magically blink Arduino's LED with heartbeat.
  pulseSensor.setThreshold(Threshold);

  // Double-check the "pulseSensor" object was created and "began" seeing a signal.
  if (pulseSensor.begin()) {
    Serial.println("We created a pulseSensor Object !");  //This prints one time at Arduino power-up,  or on Arduino reset.
  }

  pinMode(pinButton, INPUT); //set the button pin as INPUT
  pinMode(pinButtonAnotherDevice, INPUT);
}



void loop() {

  //  int myBPM = pulseSensor.getBeatsPerMinute();  // Calls function on our pulseSensor object that returns BPM as an "int".
  //  // "myBPM" hold this BPM value now.
  //
  //  if (pulseSensor.sawStartOfBeat()) {            // Constantly test to see if "a beat happened".
  //    Serial.println("♥  A HeartBeat Happened ! "); // If test is "true", print a message "a heartbeat happened".
  //    Serial.print("BPM: ");                        // Print phrase "BPM: "
  //    Serial.println(myBPM);                        // Print the value inside of myBPM.
  //    mySerial.print("This is from Bluetooth Serial: ");
  //    mySerial.print(myBPM);
  //    mySerial.println();
  //
  //  }
  //
  //  delay(3000);                    // considered best practice in a simple sketch.


  recordAccelRegisters();
  recordGyroRegisters();
  pulseRateFromSensor();
  printData();

  if (mySerial.available() > 0) {
    char result = mySerial.read();
    Serial.println(" Received: " + result);

    //    if (result == '1') {
    //      sendDataToApp();
    //    } else
    if (result == '2') {
      sendDataToAnotherArduino();
    }
  } else {
    Serial.println("No Data available");
    sendDataToApp();
  }
  //  delay(3000);
}

void pulseRateFromSensor() {
  myBPM = pulseSensor.getBeatsPerMinute();  // Calls function on our pulseSensor object that returns BPM as an "int".
  // "myBPM" hold this BPM value now.

  if (pulseSensor.sawStartOfBeat()) {            // Constantly test to see if "a beat happened".
    Serial.println("♥  A HeartBeat Happened ! "); // If test is "true", print a message "a heartbeat happened".
    Serial.print("BPM: ");                        // Print phrase "BPM: "
    Serial.print(myBPM);                        // Print the value inside of myBPM.
  }
}

void setupMPU() {
  Wire.beginTransmission(0b1101000); //This is the I2C address of the MPU (b1101000/b1101001 for AC0 low/high datasheet sec. 9.2)
  Wire.write(0x6B); //Accessing the register 6B - Power Management (Sec. 4.28)
  Wire.write(0b00000000); //Setting SLEEP register to 0. (Required; see Note on p. 9)
  Wire.endTransmission();
  Wire.beginTransmission(0b1101000); //I2C address of the MPU
  Wire.write(0x1B); //Accessing the register 1B - Gyroscope Configuration (Sec. 4.4)
  Wire.write(0x00000000); //Setting the gyro to full scale +/- 250deg./s
  Wire.endTransmission();
  Wire.beginTransmission(0b1101000); //I2C address of the MPU
  Wire.write(0x1C); //Accessing the register 1C - Acccelerometer Configuration (Sec. 4.5)
  Wire.write(0b00000000); //Setting the accel to +/- 2g
  Wire.endTransmission();
}

void recordAccelRegisters() {
  Wire.beginTransmission(0b1101000); //I2C address of the MPU
  Wire.write(0x3B); //Starting register for Accel Readings
  Wire.endTransmission();
  Wire.requestFrom(0b1101000, 6); //Request Accel Registers (3B - 40)
  while (Wire.available() < 6);
  accelX = Wire.read() << 8 | Wire.read(); //Store first two bytes into accelX
  accelY = Wire.read() << 8 | Wire.read(); //Store middle two bytes into accelY
  accelZ = Wire.read() << 8 | Wire.read(); //Store last two bytes into accelZ
  processAccelData();
}

void processAccelData() {
  gForceX = accelX / 16384.0;
  gForceY = accelY / 16384.0;
  gForceZ = accelZ / 16384.0;
}

void recordGyroRegisters() {
  Wire.beginTransmission(0b1101000); //I2C address of the MPU
  Wire.write(0x43); //Starting register for Gyro Readings
  Wire.endTransmission();
  Wire.requestFrom(0b1101000, 6); //Request Gyro Registers (43 - 48)
  while (Wire.available() < 6);
  gyroX = Wire.read() << 8 | Wire.read(); //Store first two bytes into accelX
  gyroY = Wire.read() << 8 | Wire.read(); //Store middle two bytes into accelY
  gyroZ = Wire.read() << 8 | Wire.read(); //Store last two bytes into accelZ
  processGyroData();
}

void processGyroData() {
  rotX = gyroX / 131.0;
  rotY = gyroY / 131.0;
  rotZ = gyroZ / 131.0;
}

void printData() {
  Serial.println();
  Serial.println("Gyro (deg)");
  Serial.print(" X=");
  Serial.print(rotX);
  Serial.print(" NX=");
  Serial.print(gyroX);
  Serial.print(" Y=");
  Serial.print(rotY);
  Serial.print(" NY=");
  Serial.print(gyroY);
  Serial.print(" Z=");
  Serial.print(rotZ);
  Serial.print(" NZ=");
  Serial.print(gyroZ);
  Serial.println();
  Serial.println("Accel (g)");
  Serial.print(" X=");
  Serial.print(gForceX);
  Serial.print(" NX=");
  Serial.print(accelX);
  Serial.print(" Y=");
  Serial.print(gForceY);
  Serial.print(" NY=");
  Serial.print(accelY);
  Serial.print(" Z=");
  Serial.print(gForceZ);
  Serial.print(" NZ=");
  Serial.print(accelZ);

  calculateSteps();

  display.setTextSize(1);
  display.setCursor(0, 0);
  display.setTextColor(WHITE);
  display.clearDisplay();
  display.print("BPM: ");
  display.print(myBPM);
  display.setCursor(0, 10);
  display.print("Steps: ");
  display.print(steps);
  display.setCursor(0, 20);
  if (distanceInM > 1000) {
    display.print("Distance: ");
    distance = distanceInKM;
    display.print(distance);
    display.print(" km");
  } else {
    display.print("Distance: ");
    distance = distanceInM;
    display.print(distance);
    display.print(" m");
  }
  display.display();
  display.clearDisplay();
}

void calculateSteps() {
  gForceX = float(gForceX);
  gForceY = float(gForceY);
  gForceZ = float(gForceZ);

  total = sqrt((gForceX * gForceX) + (gForceY * gForceY) + (gForceZ * gForceZ));
  total = (total + (total - 1)) / 2;
  Serial.println(total);
  delay(200);

  //cal steps
  if (total > threshhold && flag == 0)
  {
    steps = steps + 1;
    flag = 1;

  }
  else if (total > threshhold && flag == 1)
  {
    //do nothing
  }
  if (total < threshhold  && flag == 1)
  {
    flag = 0;
  }

  Serial.println('\n');
  Serial.print("Steps= ");
  Serial.println(steps);

  distanceInCM = (averageStepLength * steps);
  distanceInKM = (distanceInCM / 100000);
  distanceInM = (distanceInCM / 100);
}

void sendDataToApp() {
  //  mySerial.print("Pulserate: ");

  mySerial.print(";");
  mySerial.print(steps);

  mySerial.print(";");
  mySerial.print(distance);

  mySerial.print(";");
  mySerial.print(myBPM);
  //  mySerial.print(";");

  int stateButton = digitalRead(pinButton); //read the state of the button
  if (stateButton == 1) { //if is pressed
    mySerial.print(";");
    mySerial.print("s");
  }
}

void sendDataToAnotherArduino() {
  String btData = (String)gyroX + "," + (String)gyroY + "," + (String)gyroZ + "|";
  Serial.print("Sending data");
  Serial.print(btData);
  mySerial.print(btData);
}

