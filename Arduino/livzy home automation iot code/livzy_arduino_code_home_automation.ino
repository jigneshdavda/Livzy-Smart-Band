// these constants describe the pins. They won’t change:
//const int groundpin = 18; // analog input pin 4 — ground
//const int powerpin = 19; // analog input pin 5 — voltage

#include <SoftwareSerial.h>

SoftwareSerial BTserial(10, 11); // RX | TX

int led1 = 4; // Pin numbers to arduino
int led2 = 5;
int led3 = 6;
int led4 = 7;
int valx = 0;
int valy = 0;
int valz = 0;

String stringArray[3];

void setup() {
  // initialize the serial communications:
  Serial.begin(9600);

  // Provide ground and power by using the analog inputs as normal
  // digital pins. This makes it possible to directly connect the
  // breakout board to the Arduino. If you use the normal 5V and
  // GND pins on the Arduino, you can remove these lines.
  //  pinMode(groundpin, OUTPUT);
  //  pinMode(powerpin, OUTPUT);
  pinMode(led1, OUTPUT);
  pinMode(led2, OUTPUT);
  pinMode(led3, OUTPUT);
  pinMode(led4, OUTPUT);
  //  digitalWrite(groundpin, LOW);
  //  digitalWrite(powerpin, HIGH);
  BTserial.begin(9600);
}

void loop() {
  BTserial.write('2');
  // print the sensor values:
  if (BTserial.available() > 0) {

    String data1 = BTserial.readStringUntil(',');
    //    Serial.println("X: " + data1);
    Serial.read();
    String data2 = BTserial.readStringUntil(',');
    //    Serial.println("Y: " + data2);
    Serial.read();
    String data3 = BTserial.readStringUntil('|');
    //    Serial.println("Z: " + data3);


    //    valx = analogRead(xpin);
    //    valy = analogRead(ypin);
    //    valz = analogRead(zpin);

    valx = data1.toInt();
    valy = data2.toInt();
    valz = data3.toInt();

    //    rawx = valx - 7;
    //    rawy = valy - 6;
    //    rawz = valz + 10;
    //    if (rawx < -255)rawx = -255; else if (rawx > 255)rawx = 255;
    //    if (rawy < -255)rawy = -255; else if (rawy > 255)rawy = 255;
    //    if (rawz < -255)rawz = -255; else if (rawz > 255)rawz = 255; // u can use the constrain keyword
    //    mappedRawX = map(rawx, -255, 255, 0, 180);
    //    mappedRawY = map(rawy, -255, 255, 0, 180);
    //    mappedRawZ = map(rawz, -255, 255, 0, 180);
    //    delay(2000);
    //
    //    Serial.print("mappedRawX ="); Serial.println(mappedRawX);
    //    Serial.print("mappedRawY ="); Serial.println(mappedRawY);

    if (valx < 1000 && valy < -8000) {
      Serial.println("BACKWARD");
      digitalWrite(led1, LOW);
      digitalWrite(led4, LOW);
    }
    if (valx < 1000 && valy > 8000) {
      Serial.println("FORWARD");
      digitalWrite(led1, HIGH);
      digitalWrite(led4, HIGH);
    }
    if (valx > 8000 && valy < 1000) {
      Serial.println("RIGHT");
      digitalWrite(led2, LOW);
      digitalWrite(led3, LOW);
    }
    if (valx < -8000 && valy < 1000) {
      Serial.println("LEFT");
      digitalWrite(led2, HIGH);
      digitalWrite(led3, HIGH);
    }

    //    else {
    //      Serial.println("STAY");
    //      digitalWrite(led1, LOW);
    //      digitalWrite(led2, LOW);
    //      digitalWrite(led3, LOW);
    //      digitalWrite(led4, LOW);
    //    }
    //    if (valz <= 290)
    //      digitalWrite(led1, HIGH); // turn the LED on (HIGH is the voltage level)
    //    else
    //      digitalWrite(led1, LOW); // turn the LED off by making the voltage LOW
    //    if (valz >= 380)
    //      digitalWrite(led2, HIGH); // turn the LED on (HIGH is the voltage level)
    //    else
    //      digitalWrite(led2, LOW); // turn the LED off by making the voltage LOW
    //    if (valy <= 290)
    //      digitalWrite(led3, HIGH); // turn the LED on (HIGH is the voltage level)
    //    else
    //      digitalWrite(led3, LOW); // turn the LED off by making the voltage LOW
    //    if (valz >= 380)
    //      digitalWrite(led4, HIGH); // turn the LED on (HIGH is the voltage level)
    //    else
    //      digitalWrite(led4, LOW); // turn the LED off by making the voltage LOW

    //  Serial.print(analogRead(xpin));
    //  // print a tab between values:
    //  Serial.print("\t");
    //  Serial.print(analogRead(ypin));
    //  // print a tab between values:
    //  Serial.print("\t");
    //  Serial.print(analogRead(zpin));
    //  Serial.println();
    //  // delay before next reading:
  }
  delay(100);
}
