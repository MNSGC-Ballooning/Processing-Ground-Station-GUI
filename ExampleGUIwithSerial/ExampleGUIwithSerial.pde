// Libraries
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

// Indexes of the incoming data
int gpsAltitudeIndex = 0;
int timeSinceBootUpIndex = 1;
int latitudeIndex = 4;
int longitudeIndex = 5;
int altitudeEstimationIndex = 6;
int intTemperatureIndex = 2;
int extTemperatureIndex = 3;

int lengthGraph1 = 10;
int i = lengthGraph1;

Serial serialPort;

// Output file decleration
PrintWriter output;

JSONObject plotterConfigJSON;

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
Graph LineGraph = new Graph(1000, 70, 370, 190, color (255,255,255));
Graph tempGraph = new Graph(1000, 400, 370, 190, color (255,255,255));

float[][] altitudeGraphValues = new float[2][lengthGraph1];
float[] timeGraphValues = new float[lengthGraph1];
float[] altitudeVals = new float[10000];
float[] altitudeEstimationVals = new float[10000];
float[] timerVals = new float[10000];
float[] extTemperatureVals = new float[10000];
float[] intTemperatureVals = new float[10000];
float[][] tempGraphValues = new float[2][lengthGraph1];
float[] latitudeVals = new float[10000];
float[] longitudeVals = new float[10000];

float[] latitudeGraphVals = new float[lengthGraph1];
float[] longitudeGraphVals = new float[lengthGraph1];

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

boolean serialSelected = false;

void setup() {
  
  surface.setTitle("Ground Station"); // Name of the window
  size(1400,700); // size of the window
  cp5 = new ControlP5(this); // new GUI window
  
  //ControlFont cf1 = new ControlFont(createFont("Arial",28,true),28); // Creating a font
  
  title = cp5.addTextlabel("label")  // Title
                    .setText("MnSGC Ballooning Ground Station")
                    .setPosition(20,20)
                    .setColorValue(0xffffff00)
                    .setFont(createFont("arial bold",30))
                    ;
                    
  time = cp5.addTextarea("Time") // incoming data window
                  .setPosition(20,60)
                  .setSize(400,40)
                  .setFont(createFont("consolas",32))
                  .setLineHeight(20)
                  .setColor(color(255))
                  .setColorBackground(color(0))
                  .setColorForeground(color(0))
                  ;
                  
  GPSFix = cp5.addTextarea("GPSFix") // incoming data window
                  .setPosition(680,350)
                  .setSize(170,40)
                  .setFont(createFont("arial",24))
                  .setLineHeight(20)
                  .setColor(color(255))
                  .setColorBackground(color(0))
                  .setColorForeground(color(0))
                  ;
  
  temperature1 = cp5.addTextarea("Temperature1") // incoming data window
                  .setPosition(680,400)
                  .setSize(170,40)
                  .setFont(createFont("arial",18))
                  .setLineHeight(20)
                  .setColor(color(255))
                  .setColorBackground(color(0))
                  .setColorForeground(color(0))
                  ;
                  
  temperature2 = cp5.addTextarea("Temperature2") // incoming data window
                  .setPosition(680,440)
                  .setSize(170,40)
                  .setFont(createFont("arial",18))
                  .setLineHeight(20)
                  .setColor(color(255))
                  .setColorBackground(color(0))
                  .setColorForeground(color(0))
                  ;
  
  latitudeText = cp5.addTextarea("latitudeText") // incoming data window
                  .setPosition(680,480)
                  .setSize(170,40)
                  .setFont(createFont("arial",18))
                  .setLineHeight(20)
                  .setColor(color(255))
                  .setColorBackground(color(0))
                  .setColorForeground(color(0))
                  ;
                  
  longitudeText = cp5.addTextarea("longitudeText") // incoming data window
                  .setPosition(680,520)
                  .setSize(170,40)
                  .setFont(createFont("arial",18))
                  .setLineHeight(20)
                  .setColor(color(255))
                  .setColorBackground(color(0))
                  .setColorForeground(color(0))
                  ;
                  
  dataWindow = cp5.addTextarea("addTextarea") // incoming data window
                  .setPosition(20,110)
                  .setSize(650,500)
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
                  
  
  setChartSettings();
  
  output = createWriter("data/Ballooning"+computerTime+".csv"); // Output file name. Reasonable options include .txt and .csv
  
  int serialLength = Serial.list().length;
  String[] serialPortsAvail = new String[serialLength];
  serialPortsAvailSelected = serialPortsAvail;
  
  for(int s=0; s < serialLength; s++){
    serialPortsAvail[s] = Serial.list()[s];
  }
  
  // Listing serial objects so the correct one can be selected -- Scrollable list
  
  SerialSelect = cp5.addScrollableList("SelectSerial")
                     .setPosition(680, 110)
                     .setSize(150, 300)
                     .setBarHeight(40)
                     .setItemHeight(30)
                     .addItems(serialPortsAvail)
                     ;
                     
  }
  
void SelectSerial(int selected) {
  
  serialSelected = true;
  serialPort = new Serial(this, serialPortsAvailSelected[selected], 9600);
  delay(3000);
  serialPort.clear();
}
  
void draw() {
  
  background(0); // GUI background color
    
  LineGraph.xDiv=10;  
  LineGraph.xMax=max(timeGraphValues);
  LineGraph.xMin=min(timeGraphValues);
  LineGraph.yMax=max(altitudeGraphValues[0]) + 2000; 
  LineGraph.yMin=min(altitudeGraphValues[0]) - 2000;  
  LineGraph.DrawAxis();
  LineGraph.GraphColor = color(130,255,20); // stroke color
  
  tempGraph.xDiv=10;  
  tempGraph.xMax=max(timeGraphValues);
  tempGraph.xMin=min(timeGraphValues); 
  tempGraph.yMax=max(tempGraphValues[0]) + 0.5*max(tempGraphValues[0]); 
  tempGraph.yMin=min(tempGraphValues[0]) - 0.5*min(tempGraphValues[0]);
  tempGraph.DrawAxis(); //
  tempGraph.GraphColor = color(0,255,20); // stroke color
  
  for(int k=0; k<2; k++){
    tempGraph.LineGraph(timeGraphValues,tempGraphValues[k]);
    LineGraph.LineGraph(timeGraphValues,altitudeGraphValues[k]); // Graph values of X and Y. With real data, this will be most recent data plus last x# of values
  }
  
  if(serialSelected && serialPort.available() > 3){

    serialPort.readBytesUntil('\r',inBuffer);
    incomingData = new String(inBuffer);
     
    int stop = incomingData.indexOf("\0");
    incomingData = incomingData.substring(0,stop);
    
    String[] numbers = split(incomingData, ',');
 
    if(numbers.length>1){
      
      altitude = float(numbers[gpsAltitudeIndex]);
      timer = float(numbers[timeSinceBootUpIndex]);
      temperatureCext = float(numbers[extTemperatureIndex]);
      temperatureCint = float(numbers[intTemperatureIndex]);
      
      latitude = float(numbers[latitudeIndex]);
      longitude = float(numbers[longitudeIndex]);
      
      altitudeEstimation = float(numbers[altitudeEstimationIndex]);
      
    }
    
    timeGraphValues[lengthGraph1-1] = timer;
    altitudeGraphValues[0][lengthGraph1-1] = altitude;
    altitudeGraphValues[1][lengthGraph1-1] = altitudeEstimation;
    
    tempGraphValues[0][lengthGraph1-1] = temperatureCint;
    tempGraphValues[1][lengthGraph1-1] = temperatureCext;

    latitudeGraphVals[lengthGraph1-1] = latitude;
    longitudeGraphVals[lengthGraph1-1] = longitude;
    
    for(int j=0; j < (lengthGraph1-1); j++)
      {
        timerVals[i]= timer;
        altitudeVals[i] = altitude;
        altitudeEstimationVals[i] = altitudeEstimation;
        intTemperatureVals[i] = temperatureCint;
        extTemperatureVals[i] = temperatureCext;
        
        latitudeVals[i] = latitude;
        longitudeVals[i] = longitude;
        
        timeGraphValues[j] = timerVals[i-lengthGraph1+j];
        altitudeGraphValues[0][j] = altitudeVals[i-lengthGraph1+j];
        altitudeGraphValues[1][j] = altitudeEstimationVals[i-lengthGraph1+j];
        
        tempGraphValues[0][j] = intTemperatureVals[i-lengthGraph1+j];
        tempGraphValues[1][j] = extTemperatureVals[i-lengthGraph1+j];
        
        latitudeGraphVals[j] = latitudeVals[i-lengthGraph1+j];
        longitudeGraphVals[j] = longitudeVals[i-lengthGraph1+j];
      }
    
    i = i+1;
    data = data + /*"\n" + str(hour()) + ":" + str(minute()) + ":" + str(second()) + "--> " + */ incomingData;
    output.print(incomingData);
    dataWindow.setText(data);
    
    for (int l=0; l<inBuffer.length; l++) { inBuffer[l] = 0; }
    
    output.flush();
  }
    
  
  timeLabel = str(month()) + "/" + str(day()) + "/" + str(year()) + "  " + str(hour()) + ":" + str(minute()) + ":" + str(second());
  time.setText(timeLabel);
    
  temperature1.setText("Ext Temp(C): " + str(temperatureCext));
  temperature2.setText("Int Temp(C): " + str(temperatureCint));
  latitudeText.setText("Latitude: " + str(latitude));
  longitudeText.setText("Longitude: " + str(longitude));
  GPSFix.setText("GPS: No Fix");
  
}


void setChartSettings() {
    LineGraph.xLabel=" Seconds since bootup ";
    LineGraph.yLabel="Altitude (ft)";
    LineGraph.Title="Altitude vs Time";  

    tempGraph.xLabel=" Seconds since bootup ";
    tempGraph.yLabel="Temperature (C)";
    tempGraph.Title="Temperature vs Time";  
}

public void clear() {
  cp5.get(Textfield.class,"textValue").clear();
}

void controlEvent(ControlEvent theEvent) {
  if(theEvent.isAssignableFrom(Textfield.class)) {
    serialPort.write(theEvent.getStringValue());
  }
}
