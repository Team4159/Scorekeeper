import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import de.looksgood.ani.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class scorekeeper2 extends PApplet {

/* score keeping system for 4159 mini robotics competition */




Minim minim;
AudioPlayer player;

PFont bigFont, headFont, smallFont;
PShape flag, assist, flag_alone;
PShader blur;

int matchLength = 135;  //2 minutes 15 s
int gameState = 0;

// note: TEAM 0 is RED. TEAM 1 is BLUE.

int redScore = 0;
int blueScore = 0;
int redStack = 0;
int blueStack = 0;

boolean redAssist = false;
boolean redFlag = false;
boolean blueAssist = false;
boolean blueFlag = false;
boolean redReady = false;
boolean blueReady = false;

int redX = 0;
int blueX = 0;
int flagAloneLoc = 0;

boolean hasPlayedBells = false;
boolean hasPlayedBwong = false;
boolean hasPlayedGo = false;
int countdownState = 0;
int matchTime = 135;
long t_start = 0;
long t_countdown = 0;


public void setup() {

  
  colorMode(RGB);
  background(0);

  flagAloneLoc = width/2;

  blur = loadShader("blur.glsl");

  redX = width/4 - 50;
  blueX = (width/4)*3 + 50;

  flag = loadShape("flag.svg");
  assist = loadShape("assist.svg");
  flag.disableStyle();
  assist.disableStyle();
  flag_alone = loadShape("flag_alone.svg");
  flag_alone.disableStyle();

  //  headFont = createFont("Electronic Highway Sign", 100);
  //  bigFont = createFont("Electronic Highway Sign", 220);
  //  smallFont = createFont("Electronic Highway Sign", 40);

  headFont = createFont("Digital-7 Mono", 130);
  bigFont = createFont("Digital-7 Mono", 280);
  smallFont = createFont("Digital-7 Mono", 40);


  textAlign(CENTER, CENTER);
  printArray(PFont.list());

  minim = new Minim(this);
  Ani.init(this);
}

public void draw() {

  background(0);
  //filter(blur);
  //state machine
  switch(gameState) {
  case 0: // reset/get ready
    textFont(headFont);
    fill(0xffFFE000);
    text("GET READY!", width/2, 60);
    drawReadyLights();
    if (redReady && blueReady) {
      gameState = 1;
      t_countdown = millis();
    }
    break;
  case 1: // countdown to begin
    countdownSequence();
    break;

  case 2: //autonomous

    gameState = 3;
    //write AUTONOMOUS on screen
    //runMatch();
    //textFont(headFont);
    //fill(#FFE000);
    //text("AUTONOMOUS", width/2, 80);

  case 3: // playing game
    drawScores();
    //drawGFX();
    drawClock();
    runMatch();
    break;
  case 4: // game over
    drawScores();

    /*

     textFont(headFont);
     if (redScore > blueScore) {
     fill(#FF0000);
     text("RED WINS!", width/2, 60);
     }
     else if (blueScore > redScore) {
     fill(#0000FF);
     text("BLUE WINS!", width/2, 60);
     }
     else {
     fill(#FFE000);
     text("GAME TIED!", width/2, 60);
     }

     */

    drawClock();
    break;
  }
}


public void keyPressed() {

  switch(key) {

  case 'q': //red power cube in opposing bucket ("high goal")
    score('l', 'r');
    break;

    case 'a': //red power cube in alliance bucket ("low goal")
      score('h', 'r');
      break;

   case 'z':
     redScore -=5;
     break;

   case 'm':
      blueScore -=5;
      break;

    //case 'a': //red assist activated
    //  assist('r');
    //  break;
    //case 's': //red flag activated
    //  flag('r');
    //  break;
    //case 'o': //blue scored high goal
    //  score('h', 'b');
    //  break;

    case 'p': //blue cube in alliance bucket ("low goal")
     score('l', 'b');
     break;

  case 'o': //blue cube in opponent bucket ("high goal")
    score('h', 'b');
    break;

    //case ';': //blue assist activated
    //  assist('b');
    //  break;
    //case 'l': //blue flag activated
    //  flag('b');
    //  break;
  case ' ': //spacebar -- reset game
    reset();
    gameState = 0;
    break;
    //case 'r': //reset flag position
    //  Ani.to(this, 2, "flagAloneLoc", width/2);
    //  redFlag = false;
    //  blueFlag = false;
    //  break;
  case '1': //red team ready
    redReady = true;
    break;
  case '2': //blue team ready
    blueReady = true;
    break;
  case '-': //subtract 10 seconds from match time
    matchLength -= 10;
    break;
  case '=': //add 10 seconds to match time
    matchLength += 10;
    break;
  }
  // println("red stack: "+redStack+" --- "+"blue stack: "+blueStack);
}

public void assist(char team) {
  if (team == 'r') {
    if (redAssist == false) {
      redAssist = true;
      redStack +=5;
      player = minim.loadFile("smb_powerup.wav");
      player.play();
    } else if (redAssist == true) {
      redAssist = false;
      redStack -=5;
    }
  } else if (team == 'b') {
    if (blueAssist == false) {
      blueAssist = true;
      blueStack +=5;
      player = minim.loadFile("smb_powerup.wav");
      player.play();
    } else if (blueAssist == true) {
      blueAssist = false;
      blueStack -= 5;
    }
  } else {
    println("ERROR IN ASSIST FUNCTION");
  }
}

public void flag(char team) {
  if (gameState == 3) {

    if (team == 'r') {
      if (redFlag == false) {
        redFlag = true;
        //redStack +=5;
        player = minim.loadFile("smb_pipe.wav");
        player.play();
        Ani.to(this, 1, "flagAloneLoc", width/2+100, Ani.CUBIC_OUT);
        if (blueFlag == true) {
          blueFlag = false;
          //blueStack -=5;
        }
      }
      // ------- optional, make it easy to reset -------
      //else {
      //  redFlag = false;
      //  redStack -=5;
      //}
      // -----------------------------------------------
    } else if (team == 'b') {
      if (blueFlag == false) {
        blueFlag = true;
        //blueStack +=5;
        player = minim.loadFile("smb_pipe.wav");
        player.play();
        Ani.to(this, 1, "flagAloneLoc", width/2-100, Ani.CUBIC_OUT);
        if (redFlag == true) {
          redFlag = false;
          //redStack -=5;
        }
        //play a blue flag scoring noise
      }
      //else {
      //  blueFlag = false;
      //  blueStack -=5;
      //}
    } else {
      println("ERROR IN PEDESTAL FUNCTION");
    }
  }
}


public void score(char goal, char team) {

  if (gameState == 3) {

    int goalValue = 0;

    if (goal == 'h') {
      goalValue = 10;
    } else if (goal == 'l') {
      goalValue = 5;
    } else {
      println("ERROR IN SCORING FUNCTION");
    }

    if (team == 'r') {
      redStack += goalValue;
      redScore += redStack;
      if (redFlag) redScore +=5;
      //play appropriate scoring animation
      player = minim.loadFile("team1score.wav");
      player.play();
      //redFlag = false;
      redAssist = false;
      redStack = 0;
    } else {
      blueStack += goalValue;
      blueScore += blueStack;
      if (blueFlag) blueScore +=5;
      //play appropriate scoring animation
      player = minim.loadFile("team2score.wav");
      player.play();
      //blueFlag = false;
      blueAssist = false;
      blueStack = 0;
    }
  }
}

public void drawScores() {

  if (matchTime >= 120) {

    textFont(headFont);
    text("AUTONOMOUS", width/2, 60);
  }

  textFont(bigFont);
  fill(255, 0, 0);
  text(redScore, redX, 200);
  fill(0, 0, 255);
  text(blueScore, blueX, 200);
}

public void drawGFX() {
  noStroke();
  shapeMode(CENTER);
  if (redFlag) {
    fill(255, 0, 0);
  } else {
    fill(30);
  }
  shape(flag, redX+90, 100, 160, 120);

  if (redAssist) {
    fill(255, 0, 0);
  } else {
    fill(30);
  }
  shape(assist, redX-90, 100, 160, 120);

  if (blueAssist) {
    fill(0, 0, 255);
  } else {
    fill(30);
  }
  shape(assist, blueX+90, 100, 160, 120);

  if (blueFlag) {
    fill(0, 0, 255);
  } else {
    fill(30);
  }
  shape(flag, blueX-90, 100, 160, 120);

  stroke(0xffFFE000);
  fill(0xffFFE000);
  strokeWeight(2);
  line(width/2, 200, width/2, 400);
  shape(flag_alone, flagAloneLoc, 300, 100, 100);
}


public void drawClock() {
  textFont(bigFont);
  if (matchTime > 20) {
    fill(0xffFFE000);
  } else if ( (matchTime <= 20) && (matchTime > 0) ) {
    fill(0xffFF8500);
  } else if (matchTime == 0) {
    fill(0xffFF0000);
  }
  int minutes = matchTime / 60;
  int seconds = matchTime % 60;
  if (seconds < 10) {
    text("0"+minutes+":"+"0"+seconds, width/2, height-200);
  } else {
    text("0"+minutes+":"+seconds, width/2, height-200);
  }
}

public void drawReadyLights() {
  ellipseMode(CENTER);
  noStroke();
  fill(30);
  ellipse(230, 240, 150, 150);
  ellipse(230, 420, 150, 150);
  ellipse(230, 600, 150, 150);

  ellipse(770, 240, 150, 150);
  ellipse(770, 420, 150, 150);
  ellipse(770, 600, 150, 150);

  ellipse(500, 300, 250, 250);
  ellipse(500, 600, 250, 250);

  if (redReady) fill(0xffFF0000);
  ellipse(100, 200, 50, 50);
  ellipse(100, 280, 50, 50);
  fill(30);
  if (blueReady) fill(0xff0000FF);
  ellipse(900, 200, 50, 50);
  ellipse(900, 280, 50, 50);
}

public void countdownSequence() {
  ellipseMode(CENTER);

  if (millis() >= t_countdown + 1000) {
    //run next in cycle
    countdownState++;
    println(countdownState);
    //play the sound
    if (countdownState <=3) {
      player = minim.loadFile("ding.mp3");
      player.play();
    }

    t_countdown = millis();
  }
  textFont(headFont);
  fill(0xffFFE000);
  text("GET READY!", width/2, 60);
  drawReadyLights();
  fill(0xffFFE000);
  if (countdownState > 0) {
    ellipse(230, 240, 150, 150);
    ellipse(770, 240, 150, 150);
  }
  if (countdownState > 1) {
    ellipse(230, 420, 150, 150);
    ellipse(770, 420, 150, 150);
  }
  if (countdownState > 2) {
    ellipse(230, 600, 150, 150);
    ellipse(770, 600, 150, 150);
  }
  if (countdownState > 3) {
    background(0);

    fill(30);
    ellipse(230, 600, 150, 150);
    ellipse(770, 600, 150, 150);
    ellipse(230, 420, 150, 150);
    ellipse(770, 420, 150, 150);
    ellipse(230, 240, 150, 150);
    ellipse(770, 240, 150, 150);
    ellipse(100, 200, 50, 50);
    ellipse(100, 280, 50, 50);
    ellipse(900, 200, 50, 50);
    ellipse(900, 280, 50, 50);
    fill(0xff13FF00);
    ellipse(500, 300, 250, 250);
    ellipse(500, 600, 250, 250);
    textFont(headFont);
    text("AUTONOMOUS", width/2, 60);
    if (!hasPlayedBells) {
      player = minim.loadFile("charge-1.wav");
      player.play();
      hasPlayedBells = true;
    }
  }
  if (countdownState > 5) {
    gameState = 2;
    t_start = millis();
    hasPlayedBells = false;
    reset();
  }
}


public void runMatch() {
  int elapsed = (int)(millis() - t_start)/1000;
  matchTime = matchLength - elapsed;
  //if ((matchTime == 135)&&(!hasPlayedGo)) {
  //  player = minim.loadFile("three-bells.wav");
  //  player.play();
  //  hasPlayedGo = true;
  //  //
  //}

  if ((matchTime == 120)&&(!hasPlayedBells)) {
    player = minim.loadFile("three-bells.wav");
    player.play();
    hasPlayedBells = true;
  }

  if ((matchTime == 20)&&(!hasPlayedBwong)) {     //modified
    player = minim.loadFile("smb_warning.wav");
    player.play();
    hasPlayedBwong = true;
  }
  if (matchTime == 0) {
    gameState = 4;
    player = minim.loadFile("buzzer.wav");
    player.play();
  }
}

public void reset() {
  redScore = 0;
  blueScore = 0;
  redStack = 0;
  blueStack = 0;
  redAssist = false;
  redFlag = false;
  blueAssist = false;
  blueFlag = false;
  redReady = false;
  blueReady = false;
  hasPlayedBwong = false;
  hasPlayedGo = false;
  hasPlayedBells = false;
  matchLength = 135;
  countdownState = 0;
  t_start = millis();
  flagAloneLoc = width/2;
}
  public void settings() {  size(1000, 750, P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "scorekeeper2" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
