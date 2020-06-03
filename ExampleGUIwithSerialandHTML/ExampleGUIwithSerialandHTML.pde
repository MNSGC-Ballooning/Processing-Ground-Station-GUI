// Libraries
// Write to .html file
import java.awt.Frame;
import java.awt.BorderLayout;
import controlP5.*;
import processing.serial.*;

// GUI object
ControlP5 cp5;

// Serial Decleration
String serialPortName = "serialName";
String[] serialPortsAvailSelected = new String[10];
String incomingData;
byte[] inBuffer = new byte[100]; 
String data;
String onlineHTMLdata = "";
String HTMLincomingData = "";
String headerString = "";

// Indexes of the incoming data. That is, list the order of incoming data for plotting and misc. display purposes
int gpsAltitudeIndex = 5;
int timeSinceBootUpIndex = 1;
int latitudeIndex = 3;
int longitudeIndex = 4;
int altitudeEstimationIndex = 6;
int intTemperatureIndex = 7;
int extTemperatureIndex = 8;

int dataLength = 7;

String gpsAltitudeHeader = "GPS Altitude (ft)";
String timeSinceBootUpHeader = "Seconds since bootup";
String latitudeHeader = "Latitude";
String longitudeHeader = "Longitude";
String altitudeEstimationHeader = "Estimated Alt (ft)";
String intTemperatureHeader = "Internal temp (C)";
String extTemperatureHeader = "External temp (C)";

int i = 0; // incremented every time data is received

Serial serialPort;

// Output file decleration
PrintWriter output;
PrintWriter CSVoutput;

JSONObject plotterConfigJSON;

// GUI Section decleration
Textarea dataWindow;
Textlabel title;
Textarea time;
Textarea GPSFix;
Textarea temperature1;
Textarea temperature2;
Textarea longitudeText;
Textarea latitudeText;
Textfield commandWindow;
ScrollableList SerialSelect;

// Global variable decleration 

float[] altitudeVals = new float[10000];
float[] altitudeEstimationVals = new float[10000];
float[] timerVals = new float[10000];
float[] extTemperatureVals = new float[10000];
float[] intTemperatureVals = new float[10000];

float[] latitudeVals = new float[10000];
float[] longitudeVals = new float[10000];

float timer;
float altitude;
float temperatureCext;
float temperatureCint;
float latitude;
float longitude;
float altitudeEstimation;

String computerTime = str(year()) + "-" + str(month()) + "-" + str(day()) + "-" + str(hour()) + "-" + str(minute()) + "-" + str(second());
String timeLabel = str(month()) + "/" + str(day()) + "/" + str(year()) + "  " + str(hour()) + ":" + str(minute()) + ":" + str(second());
String serialPortsAvailable;
StringList header = new StringList();

boolean serialSelected = false;

void setup() {
  
  size(1400,700); // size of the window
  
  setGUIparameters();
  setSerialPort();
  setHTMLheader();
  
}
  
void SelectSerial(int selected) {
  
  serialSelected = true;
  serialPort = new Serial(this, serialPortsAvailSelected[selected], 9600);
  delay(3000);
  serialPort.clear();
  
}
  
void draw() {
  
  background(0); // GUI background color
  
  if(serialSelected && serialPort.available() > 5){

    serialPort.readBytesUntil('\r',inBuffer);
    incomingData = new String(inBuffer);
     
    int stop = incomingData.indexOf("\0");
    incomingData = incomingData.substring(0,stop);
    
    HTMLincomingData = "<td>";
    String[] numbers = split(incomingData, ',');
    //HTMLincomingData = join(numbers,"</td> \n <td>");
    if(numbers.length>1){

      altitude = float(numbers[gpsAltitudeIndex]);
      timer = float(numbers[timeSinceBootUpIndex]);
      temperatureCext = float(numbers[extTemperatureIndex]);
      temperatureCint = float(numbers[intTemperatureIndex]);
      latitude = float(numbers[latitudeIndex]);
      longitude = float(numbers[longitudeIndex]); 
      altitudeEstimation = float(numbers[altitudeEstimationIndex]); 
      
    }
    
    HTMLincomingData = str(latitude) + "</td> \n <td>" + str(longitude) + "</td> \n <td>" + str(altitude) + "</td> \n <td>" 
                      + str(altitudeEstimation) + "</td> \n <td>" + str(temperatureCext) + "</td> \n <td>" + str(temperatureCint) + "</td> \n <td>" + str(timer);

 
        timerVals[i]= timer;
        altitudeVals[i] = altitude;
        altitudeEstimationVals[i] = altitudeEstimation;
        intTemperatureVals[i] = temperatureCint;
        extTemperatureVals[i] = temperatureCext;
        
        latitudeVals[i] = latitude;
        longitudeVals[i] = longitude;
    
    i = i+1;
    
    if(incomingData.length() > 1) {
    onlineHTMLdata = onlineHTMLdata + "<td> " + str(hour()) + ":" + str(minute()) + ":" + nf(second(),2) + " </td> \n <td> " + HTMLincomingData + "</td> \n </tr>";}
    data = data + incomingData;
    //output = createWriter("Website/Ballooning1.php"); // Output file name. Reasonable options include .txt, .csv, and .php
    output = createWriter("C:/Users/16052/Desktop/wordpress/ballooning/Ballooning1.php"); // Output file name. Reasonable options include .txt, .csv, and .php
    output.print("<!DOCTYPE html><html>\n<title>Test</title>\n<body>\n"); //Start of HTML file
    output.print(onlineHTMLdata); // HTML data
    output.print("</body>\n</html>\n"); // end of HTML file
    output.close();
    
    CSVoutput = createWriter("Ballooning" + str(month()) + "-" + str(day()) + "-" + str(year()) + "/Ballooning" + str(hour()) + "_" + str(minute()) + "_" + str(second()) + ".csv"); // Output file name. Reasonable options include .txt, .csv, and .php
    CSVoutput.print(data);
    CSVoutput.close();
    
    dataWindow.setText(data);
    
    for (int l=0; l<inBuffer.length; l++) { inBuffer[l] = 0; } 
    output.flush();
    CSVoutput.flush();
  }
  
  timeLabel = str(month()) + "/" + str(day()) + "/" + str(year()) + "  " + str(hour()) + ":" + str(minute()) + ":" + str(second()); // Time on the top of the GUI
  time.setText(timeLabel); // Reset GUI time
  
  // Write down some important values 
  temperature1.setText("Ext Temp(C): " + str(temperatureCext));
  temperature2.setText("Int Temp(C): " + str(temperatureCint));
  latitudeText.setText("Latitude: " + str(latitude));
  longitudeText.setText("Longitude: " + str(longitude));
  GPSFix.setText("GPS: No Fix");
  
}


public void clear() {
  cp5.get(Textfield.class,"textValue").clear();
}

void controlEvent(ControlEvent theEvent) {
  if(theEvent.isAssignableFrom(Textfield.class)) {
    serialPort.write(theEvent.getStringValue());
  }
}

void setGUIparameters()
{
  
  surface.setTitle("Ground Station"); // Name of the window
  cp5 = new ControlP5(this); // new GUI window
  //ControlFont cf1 = new ControlFont(createFont("Arial",28,true),28); // Creating a font
  
  title = cp5.addTextlabel("label")  // Title
                    .setText("MnSGC Ballooning Ground Station")
                    .setPosition(20,20)
                    .setColorValue(0xffffff00)
                    .setFont(createFont("arial bold",30))
                    ;
                    
  time = cp5.addTextarea("Time") // time dynamic text area
                  .setPosition(20,60)
                  .setSize(400,40)
                  .setFont(createFont("consolas",32))
                  .setLineHeight(20)
                  .setColor(color(255))
                  .setColorBackground(color(0))
                  .setColorForeground(color(0))
                  ;
                  
  GPSFix = cp5.addTextarea("GPSFix") // GPS fix/no fix dynamic text box
                  .setPosition(1180,350)
                  .setSize(190,40)
                  .setFont(createFont("arial",24))
                  .setLineHeight(20)
                  .setColor(color(255))
                  .setColorBackground(color(0))
                  .setColorForeground(color(0))
                  ;
  
  temperature1 = cp5.addTextarea("Temperature1") // temperature dynamic text box
                  .setPosition(1180,400)
                  .setSize(190,40)
                  .setFont(createFont("arial",18))
                  .setLineHeight(20)
                  .setColor(color(255))
                  .setColorBackground(color(0))
                  .setColorForeground(color(0))
                  ;
                  
  temperature2 = cp5.addTextarea("Temperature2") // temperature dynamic text box
                  .setPosition(1180,440)
                  .setSize(170,40)
                  .setFont(createFont("arial",18))
                  .setLineHeight(20)
                  .setColor(color(255))
                  .setColorBackground(color(0))
                  .setColorForeground(color(0))
                  ;
  
  latitudeText = cp5.addTextarea("latitudeText") // latitude dynamic text box
                  .setPosition(1180,480)
                  .setSize(170,40)
                  .setFont(createFont("arial",18))
                  .setLineHeight(20)
                  .setColor(color(255))
                  .setColorBackground(color(0))
                  .setColorForeground(color(0))
                  ;
                  
  longitudeText = cp5.addTextarea("longitudeText") // longitude dynamic text box
                  .setPosition(1180,520)
                  .setSize(170,40)
                  .setFont(createFont("arial",18))
                  .setLineHeight(20)
                  .setColor(color(255))
                  .setColorBackground(color(0))
                  .setColorForeground(color(0))
                  ;
                  
  dataWindow = cp5.addTextarea("addTextarea") // incoming data window
                  .setPosition(20,110)
                  .setSize(1100,500)
                  .setFont(createFont("arial",20))
                  .setLineHeight(32)
                  .setColor(color(255))
                  .setColorBackground(color(255,100))
                  .setColorForeground(color(255,100))
                  ;
                  
  commandWindow =  cp5.addTextfield("commandWindow")
                     .setPosition(20,630)
                     .setSize(500,40)
                     .setFont(createFont("arial",24))
                     .setFocus(true)
                     .setColor(color(255,255,255))
                     ;
                
}

void setSerialPort() // Select the serial port from the list of those available
{
  int serialLength = Serial.list().length;
  String[] serialPortsAvail = new String[serialLength];
  serialPortsAvailSelected = serialPortsAvail;
  
  for(int s=0; s < serialLength; s++){
    serialPortsAvail[s] = Serial.list()[s];
  }
  
  // Listing serial objects so the correct one can be selected -- Scrollable list
  
  SerialSelect = cp5.addScrollableList("SelectSerial")
                     .setPosition(1180, 110)
                     .setSize(150, 300)
                     .setBarHeight(40)
                     .setItemHeight(30)
                     .addItems(serialPortsAvail)
                     ;
                     
  }
  
void setHTMLheader() // Can vary the order of items in the header
{
  header.set(2, gpsAltitudeHeader);
  header.set(6, timeSinceBootUpHeader);
  header.set(0, latitudeHeader);
  header.set(1, longitudeHeader);
  header.set(3, altitudeEstimationHeader);
  header.set(5, intTemperatureHeader);
  header.set(4, extTemperatureHeader);
  
  //extra stuff at top
  String pageImage = "<img src= 'http://www.mnspacegrant.org/wp-content/uploads/2017/10/MnSGC_logo-NEW.jpg' width='100' height = '130'>"; // MNSGC Image
  headerString = "<table><thead><tr><td> Time &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </td>"; //start of table header
  
  for (int k=0; k<dataLength; k++)
  {
    headerString = headerString + "<td>" + header.get(k) + "&nbsp;&nbsp;&nbsp;&nbsp;</td>"; // new cell, add next header item, spaces.
  }
  onlineHTMLdata = pageImage + headerString + "</tr></thead><tbody>"; // close table header and prepare for body
}
  
