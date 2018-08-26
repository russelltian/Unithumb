/* Sweep
 by BARRAGAN <http://barraganstudio.com>
 This example code is in the public domain.

 modified 8 Nov 2013
 by Scott Fitzgerald
 http://www.arduino.cc/en/Tutorial/Sweep
*/

#include <Servo.h>
#include <Wire.h>
#include <SPI.h>
#include <Adafruit_LSM9DS1.h>
#include <Adafruit_Sensor.h>  // not used in this demo but required!
#include <SoftwareSerial.h>
#define LED_PIN 13
// i2c
Adafruit_LSM9DS1 lsm = Adafruit_LSM9DS1();

#define LSM9DS1_SCK A5
#define LSM9DS1_MISO 12
#define LSM9DS1_MOSI A4
#define LSM9DS1_XGCS 6
#define LSM9DS1_MCS 5

Servo left;  // create servo object to control a servo
Servo right;

SoftwareSerial BT05(10, 11); //RX|TX

int sumZ=0;
// twelve servo objects can be created on most boards

int pos = 0;    // variable to store the servo position
int left_pos=0;
int right_pos=180;

void setupSensor()
{
  // 1.) Set the accelerometer range
  lsm.setupAccel(lsm.LSM9DS1_ACCELRANGE_2G);
  //lsm.setupAccel(lsm.LSM9DS1_ACCELRANGE_4G);
  //lsm.setupAccel(lsm.LSM9DS1_ACCELRANGE_8G);
  //lsm.setupAccel(lsm.LSM9DS1_ACCELRANGE_16G);
  
  // 2.) Set the magnetometer sensitivity
  lsm.setupMag(lsm.LSM9DS1_MAGGAIN_4GAUSS);
  //lsm.setupMag(lsm.LSM9DS1_MAGGAIN_8GAUSS);
  //lsm.setupMag(lsm.LSM9DS1_MAGGAIN_12GAUSS);
//  lsm.setupMag(lsm.LSM9DS1_MAGGAIN_16GAUSS);

  // 3.) Setup the gyroscope
  lsm.setupGyro(lsm.LSM9DS1_GYROSCALE_245DPS);
  //lsm.setupGyro(lsm.LSM9DS1_GYROSCALE_500DPS);
  //lsm.setupGyro(lsm.LSM9DS1_GYROSCALE_2000DPS);
}

void setup() {

  Serial.begin(115200);
  BT05.begin(9600);
  left.attach(8);
  right.attach(9);

  left.write(0);
  right.write(180);

   // Try to initialise and warn if we couldn't detect the chip
  if (!lsm.begin())
  {
    Serial.println("Oops ... unable to initialize the LSM9DS1. Check your wiring!");
    while (1);
  }
  Serial.println("Found LSM9DS1 9DOF");

  // helper to just set the default scaling we want, see above!
  setupSensor();
}

void turn(int turn_speed){
  if(turn_speed>0){
    //turn left
      if(right_pos < 180){
        right_pos+=turn_speed;
      }else if(left_pos < 90){
        left_pos+=turn_speed;
      }
  }else{
    //turn right
    if(left_pos > 0){
      left_pos+=turn_speed;
    }else if(right_pos > 90){
      right_pos+=turn_speed;
    }
  }
}

void loop() {
//  for (pos = 0; pos < 90; pos += 3) { // goes from 0 degrees to 180 degrees
//    // in steps of 1 degree
//    left.write(pos);
//    delay(15);               // waits 15ms for the servo to reach the position
//  }
//  delay(3000);
//  for (pos = 90; pos > 0; pos -= 3) { // goes from 180 degrees to 0 degrees
//    left.write(pos);
//    delay(15);              // waits 15ms for the servo to reach the position
//  }
//  delay(3000);
//  for (pos = 180; pos > 90; pos -= 3) { // goes from 180 degrees to 0 degrees
//    right.write(pos);
//    delay(15);                      // waits 15ms for the servo to reach the position
//  }
//  delay(3000);
//  for (pos = 90; pos < 180; pos += 3) { // goes from 180 degrees to 0 degrees
//    right.write(pos);
//    delay(15);                      // waits 15ms for the servo to reach the position
//  }

lsm.read();  /* ask it to read in the data */ 

  /* Get a new sensor event */ 
  sensors_event_t a, m, g, temp;

  lsm.getEvent(&a, &m, &g, &temp); 

  sumZ+=g.acceleration.z;
  Serial.println(sumZ);
  if(sumZ>500){
    turn(-1);
  }else if(sumZ<-500){
    turn(1);
  }
  if (BT05.available()) {
      char c = BT05.read();  
      Serial.print("Received from Bluetooth Device: ");
      Serial.println(c);
      switch(c) { 
        //case 48: /* Number 0 */
        case 'L':
              left_pos=90;
              right_pos=180;
              left.write(left_pos);
              right.write(right_pos);
              delay(1000);
              sumZ=0;
              break;      
        //case 49: /* Number 1 */
        case 'R':
              right_pos=90;
              left_pos=0;
              left.write(left_pos);
              right.write(right_pos);
              delay(1000);
              sumZ=0;
              break;
        default:
              Serial.println("case default");
              break;
      }
  }
  left.write(left_pos);
  right.write(right_pos);
}
