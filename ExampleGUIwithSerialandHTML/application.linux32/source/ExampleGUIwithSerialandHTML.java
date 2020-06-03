import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.awt.Frame; 
import java.awt.BorderLayout; 
import controlP5.*; 
import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ExampleGUIwithSerialandHTML extends PApplet {

// Libraries
// Write to .html file





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

public void setup() {
  
   // size of the window
  
  setGUIparameters();
  setSerialPort();
  setHTMLheader();
  
}
  
public void SelectSerial(int selected) {
  
  serialSelected = true;
  serialPort = new Serial(this, serialPortsAvailSelected[selected], 9600);
  delay(3000);
  serialPort.clear();
  
}
  
public void draw() {
  
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

      altitude = PApplet.parseFloat(numbers[gpsAltitudeIndex]);
      timer = PApplet.parseFloat(numbers[timeSinceBootUpIndex]);
      temperatureCext = PApplet.parseFloat(numbers[extTemperatureIndex]);
      temperatureCint = PApplet.parseFloat(numbers[intTemperatureIndex]);
      latitude = PApplet.parseFloat(numbers[latitudeIndex]);
      longitude = PApplet.parseFloat(numbers[longitudeIndex]); 
      altitudeEstimation = PApplet.parseFloat(numbers[altitudeEstimationIndex]); 
      
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
    output = createWriter("Ballooning/Ballooning1.php"); // Output file name. Reasonable options include .txt, .csv, and .php
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

public void controlEvent(ControlEvent theEvent) {
  if(theEvent.isAssignableFrom(Textfield.class)) {
    serialPort.write(theEvent.getStringValue());
  }
}

public void setGUIparameters()
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

public void setSerialPort() // Select the serial port from the list of those available
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
  
public void setHTMLheader() // Can vary the order of items in the header
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
  
/*   =================================================================================       
     The Graph class contains functions and variables that have been created to draw 
     graphs. Here is a quick list of functions within the graph class:
          
       Graph(int x, int y, int w, int h,color k)
       DrawAxis()
       Bar([])
       smoothLine([][])
       DotGraph([][])
       LineGraph([][]) 
     
     =================================================================================*/   

    
    class Graph 
    {
      
      boolean Dot=true;            // Draw dots at each data point if true
      boolean RightAxis;            // Draw the next graph using the right axis if true
      boolean ErrorFlag=false;      // If the time array isn't in ascending order, make true  
      boolean ShowMouseLines=true;  // Draw lines and give values of the mouse position
    
      int     xDiv=5,yDiv=5;            // Number of sub divisions
      int     xPos,yPos;            // location of the top left corner of the graph  
      int     Width,Height;         // Width and height of the graph
    

      int   GraphColor;
      int   BackgroundColor=color(0);  
      int   StrokeColor=color(180);     
      
      String  Title="Title";          // Default titles
      String  xLabel="x - Label";
      String  yLabel="y - Label";

      float   yMax=1024, yMin=0;      // Default axis dimensions
      float   xMax=10, xMin=0;
      float   yMaxRight=1024,yMinRight=0;
  
      PFont   Font;                   // Selected font used for text 
      
  //    int Peakcounter=0,nPeakcounter=0;
     
      Graph(int x, int y, int w, int h,int k) {  // The main declaration function
        xPos = x;
        yPos = y;
        Width = w;
        Height = h;
        GraphColor = k;
        
      }
    
     
       public void DrawAxis(){
       
   /*  =========================================================================================
        Main axes Lines, Graph Labels, Graph Background
       ==========================================================================================  */
    
        fill(BackgroundColor); color(255);stroke(StrokeColor);strokeWeight(1);
        int t=60;
        
        //rect(xPos-t*1.6,yPos-t,Width+t*2.5,Height+t*2);            // outline
        textAlign(CENTER);textSize(18);
        float c=textWidth(Title);
        fill(255); color(255);stroke(255);strokeWeight(1);
        rect(xPos+Width/2-c/2,yPos-35,c,0);                         // Heading Rectangle  
        
        fill(255);
        text(Title,xPos+Width/2,yPos-37);                            // Heading Title
        textAlign(CENTER);textSize(14);
        text(xLabel,xPos+Width/2,yPos+Height+t/1.5f);                     // x-axis Label 
        
        rotate(-PI/2);                                               // rotate -90 degrees
        text(yLabel,-yPos-Height/2,xPos-t*1.6f+20);                   // y-axis Label  
        rotate(PI/2);                                                // rotate back
        
        textSize(10); fill(255); stroke(255); smooth();strokeWeight(1);
          //Edges
          line(xPos-3,yPos+Height,xPos-3,yPos);                        // y-axis line 
          line(xPos-3,yPos+Height,xPos+Width+5,yPos+Height);           // x-axis line 
          
           stroke(200);
          if(yMin<0){
                    line(xPos-7,                                       // zero line 
                         yPos+Height-(abs(yMin)/(yMax-yMin))*Height,   // 
                         xPos+Width,
                         yPos+Height-(abs(yMin)/(yMax-yMin))*Height
                         );
          
                    
          }
          
          if(RightAxis){                                       // Right-axis line   
              stroke(255);
              line(xPos+Width+3,yPos+Height,xPos+Width+3,yPos);
            }
            
           /*  =========================================================================================
                Sub-devisions for both axes, left and right
               ==========================================================================================  */
            
            stroke(255);
            
           for(int x=0; x<=xDiv; x++){
       
            /*  =========================================================================================
                  x-axis
                ==========================================================================================  */
             
            line(PApplet.parseFloat(x)/xDiv*Width+xPos-3,yPos+Height,       //  x-axis Sub devisions    
                 PApplet.parseFloat(x)/xDiv*Width+xPos-3,yPos+Height+5);     
                 
            textSize(10);    fill(255);                                  // x-axis Labels
            String xAxis=str(xMin+PApplet.parseFloat(x)/xDiv*(xMax-xMin));  // the only way to get a specific number of decimals 
            String[] xAxisMS=split(xAxis,'.');                 // is to split the float into strings 
            text(xAxisMS[0]+"."+xAxisMS[1].charAt(0),          // ...
                 PApplet.parseFloat(x)/xDiv*Width+xPos-3,yPos+Height+15);   // x-axis Labels
          }
          
          
           /*  =========================================================================================
                 left y-axis
               ==========================================================================================  */
          
          for(int y=0; y<=yDiv; y++){
            line(xPos-3,PApplet.parseFloat(y)/yDiv*Height+yPos,                // ...
                  xPos-7,PApplet.parseFloat(y)/yDiv*Height+yPos);              // y-axis lines 
            
            textAlign(RIGHT);fill(255); // Axis Color
            
            String yAxis=str(yMin+PApplet.parseFloat(y)/yDiv*(yMax-yMin));     // Make y Label a string
            String[] yAxisMS=split(yAxis,'.');                    // Split string
           
            text(yAxisMS[0]+"."+yAxisMS[1].charAt(0),             // ... 
                 xPos-15,PApplet.parseFloat(yDiv-y)/yDiv*Height+yPos+3);       // y-axis Labels 
                        
                        
            /*  =========================================================================================
                 right y-axis
                ==========================================================================================  */
            
            if(RightAxis){
             
              color(GraphColor); stroke(GraphColor);fill(20);
            
              line(xPos+Width+3,PApplet.parseFloat(y)/yDiv*Height+yPos,             // ...
                   xPos+Width+7,PApplet.parseFloat(y)/yDiv*Height+yPos);            // Right Y axis sub devisions
                   
              textAlign(LEFT); fill(255);
            
              String yAxisRight=str(yMinRight+PApplet.parseFloat(y)/                // ...
                                yDiv*(yMaxRight-yMinRight));           // convert axis values into string
              String[] yAxisRightMS=split(yAxisRight,'.');             // 
           
               text(yAxisRightMS[0]+"."+yAxisRightMS[1].charAt(0),     // Right Y axis text
                    xPos+Width+15,PApplet.parseFloat(yDiv-y)/yDiv*Height+yPos+3);   // it's x,y location
            
            }stroke(255);
            
          
          }
          
 
      }
      
      
   /*  =========================================================================================
       Bar graph
       ==========================================================================================  */   
      
      public void Bar(float[] a ,int from, int to) {
        
         
          stroke(GraphColor);
          fill(GraphColor);
          
          if(from<0){                                      // If the From or To value is out of bounds 
           for (int x=0; x<a.length; x++){                 // of the array, adjust them 
               rect(PApplet.parseInt(xPos+x*PApplet.parseFloat(Width)/(a.length)),
                    yPos+Height-2,
                    Width/a.length-2,
                    -a[x]/(yMax-yMin)*Height);
                 }
          }
          
          else {
          for (int x=from; x<to; x++){
            
            rect(PApplet.parseInt(xPos+(x-from)*PApplet.parseFloat(Width)/(to-from)),
                     yPos+Height-2,
                     Width/(to-from)-2,
                     -a[x]/(yMax-yMin)*Height);
                     
    
          }
          }
          
      }
  public void Bar(float[] a ) {
  
              stroke(GraphColor);
          fill(GraphColor);
    
  for (int x=0; x<a.length; x++){                 // of the array, adjust them 
               rect(PApplet.parseInt(xPos+x*PApplet.parseFloat(Width)/(a.length)),
                    yPos+Height-2,
                    Width/a.length-2,
                    -a[x]/(yMax-yMin)*Height);
                 }
          }
  
  
   /*  =========================================================================================
       Dot graph
       ==========================================================================================  */   
       
        public void DotGraph(float[] x ,float[] y) {
          
         for (int i=0; i<x.length; i++){
                    strokeWeight(5);stroke(GraphColor);noFill();smooth();
           ellipse(
                   xPos+(x[i]-x[0])/(x[x.length-1]-x[0])*Width,
                   yPos+Height-(y[i]/(yMax-yMin)*Height)+(yMin)/(yMax-yMin)*Height,
                   2,2
                   );
         }
                             
      }
      
   /*  =========================================================================================
       Streight line graph 
       ==========================================================================================  */
       
      public void LineGraph(float[] x ,float[] y) {
          
         for (int i=0; i<(x.length-1); i++){
                    strokeWeight(2);stroke(GraphColor);noFill();smooth();
           line(xPos+(x[i]-x[0])/(x[x.length-1]-x[0])*Width,
                                            yPos+Height-(y[i]/(yMax-yMin)*Height)+(yMin)/(yMax-yMin)*Height,
                                            xPos+(x[i+1]-x[0])/(x[x.length-1]-x[0])*Width,
                                            yPos+Height-(y[i+1]/(yMax-yMin)*Height)+(yMin)/(yMax-yMin)*Height);
         }
                             
      }
      
      /*  =========================================================================================
             smoothLine
          ==========================================================================================  */
    
      public void smoothLine(float[] x ,float[] y) {
         
        float tempyMax=yMax, tempyMin=yMin;
        
        if(RightAxis){yMax=yMaxRight;yMin=yMinRight;} 
         
        int counter=0;
        int xlocation=0,ylocation=0;
         
//         if(!ErrorFlag |true ){    // sort out later!
          
          beginShape(); strokeWeight(2);stroke(GraphColor);noFill();smooth();
         
            for (int i=0; i<x.length; i++){
              
           /* ===========================================================================
               Check for errors-> Make sure time array doesn't decrease (go back in time) 
              ===========================================================================*/
              if(i<x.length-1){
                if(x[i]>x[i+1]){
                   
                  ErrorFlag=true;
                
                }
              }
         
         /* =================================================================================       
             First and last bits can't be part of the curve, no points before first bit, 
             none after last bit. So a streight line is drawn instead   
            ================================================================================= */ 

              if(i==0 || i==x.length-2)line(xPos+(x[i]-x[0])/(x[x.length-1]-x[0])*Width,
                                            yPos+Height-(y[i]/(yMax-yMin)*Height)+(yMin)/(yMax-yMin)*Height,
                                            xPos+(x[i+1]-x[0])/(x[x.length-1]-x[0])*Width,
                                            yPos+Height-(y[i+1]/(yMax-yMin)*Height)+(yMin)/(yMax-yMin)*Height);
                                            
          /* =================================================================================       
              For the rest of the array a curve (spline curve) can be created making the graph 
              smooth.     
             ================================================================================= */ 
                            
              curveVertex( xPos+(x[i]-x[0])/(x[x.length-1]-x[0])*Width,
                           yPos+Height-(y[i]/(yMax-yMin)*Height)+(yMin)/(yMax-yMin)*Height);
                           
           /* =================================================================================       
              If the Dot option is true, Place a dot at each data point.  
             ================================================================================= */    
           
             if(Dot)ellipse(
                             xPos+(x[i]-x[0])/(x[x.length-1]-x[0])*Width,
                             yPos+Height-(y[i]/(yMax-yMin)*Height)+(yMin)/(yMax-yMin)*Height,
                             2,2
                             );
                             
         /* =================================================================================       
             Highlights points closest to Mouse X position   
            =================================================================================*/ 
                          
              if( abs(mouseX-(xPos+(x[i]-x[0])/(x[x.length-1]-x[0])*Width))<5 ){
                
                 
                  float yLinePosition = yPos+Height-(y[i]/(yMax-yMin)*Height)+(yMin)/(yMax-yMin)*Height;
                  float xLinePosition = xPos+(x[i]-x[0])/(x[x.length-1]-x[0])*Width;
                  strokeWeight(1);stroke(240);
                 // line(xPos,yLinePosition,xPos+Width,yLinePosition);
                  strokeWeight(2);stroke(GraphColor);
                  
                  ellipse(xLinePosition,yLinePosition,4,4);
              }
              
     
              
            }  
       
          endShape(); 
          yMax=tempyMax; yMin=tempyMin;
                float xAxisTitleWidth=textWidth(str(map(xlocation,xPos,xPos+Width,x[0],x[x.length-1])));
          
           
       if((mouseX>xPos&mouseX<(xPos+Width))&(mouseY>yPos&mouseY<(yPos+Height))){   
        if(ShowMouseLines){
              // if(mouseX<xPos)xlocation=xPos;
            if(mouseX>xPos+Width)xlocation=xPos+Width;
            else xlocation=mouseX;
            stroke(200); strokeWeight(0.5f);fill(255);color(50);
            // Rectangle and x position
            line(xlocation,yPos,xlocation,yPos+Height);
            rect(xlocation-xAxisTitleWidth/2-10,yPos+Height-16,xAxisTitleWidth+20,12);
            
            textAlign(CENTER); fill(160);
            text(map(xlocation,xPos,xPos+Width,x[0],x[x.length-1]),xlocation,yPos+Height-6);
            
           // if(mouseY<yPos)ylocation=yPos;
             if(mouseY>yPos+Height)ylocation=yPos+Height;
            else ylocation=mouseY;
          
           // Rectangle and y position
            stroke(200); strokeWeight(0.5f);fill(255);color(50);
            
            line(xPos,ylocation,xPos+Width,ylocation);
             int yAxisTitleWidth=PApplet.parseInt(textWidth(str(map(ylocation,yPos,yPos+Height,y[0],y[y.length-1]))) );
            rect(xPos-15+3,ylocation-6, -60 ,12);
            
            textAlign(RIGHT); fill(GraphColor);//StrokeColor
          //    text(map(ylocation,yPos+Height,yPos,yMin,yMax),xPos+Width+3,yPos+Height+4);
            text(map(ylocation,yPos+Height,yPos,yMin,yMax),xPos -15,ylocation+4);
           if(RightAxis){ 
                          
                           stroke(200); strokeWeight(0.5f);fill(255);color(50);
                           
                           rect(xPos+Width+15-3,ylocation-6, 60 ,12);  
                            textAlign(LEFT); fill(160);
                           text(map(ylocation,yPos+Height,yPos,yMinRight,yMaxRight),xPos+Width+15,ylocation+4);
           }
            noStroke();noFill();
         }
       }
            
   
      }

       
          public void smoothLine(float[] x ,float[] y, float[] z, float[] a ) {
           GraphColor=color(188,53,53);
            smoothLine(x ,y);
           GraphColor=color(193-100,216-100,16);
           smoothLine(z ,a);
   
       }
       
       
       
    }
  public void settings() {  size(1400,700); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ExampleGUIwithSerialandHTML" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
