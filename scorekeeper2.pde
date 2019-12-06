/* score keeping system for 4159 mini robotics competition */

import ddf.minim.*;
import de.looksgood.ani.*;

Minim minim;
AudioPlayer player;

PFont bigFont, headFont, smallFont;
PShape flag, assist, flag_alone;
PShader blur;

int matchLength;  //2 minutes 15 s
int gameState;

// note: TEAM 0 is RED. TEAM 1 is BLUE.

int redScore;
int blueScore;
int redStack;
int blueStack;

boolean redAssist;
boolean redFlag;
boolean blueAssist;
boolean blueFlag;
boolean redReady;
boolean blueReady;

int redX;
int blueX;
int flagAloneLoc;

boolean hasPlayedBells;
boolean hasPlayedBwong;
boolean hasPlayedGo;
int countdownState;
int matchTime;
long t_start;
long t_countdown;


void setup() {
  reset();

  size(1450, 250);
  colorMode(RGB);
  background(0);

  flagAloneLoc = width/2;

  // blur = loadShader("blur.glsl");

  redX = 250;
  blueX = width - 250;

  flag = loadShape("flag.svg");
  assist = loadShape("assist.svg");
  flag.disableStyle();
  assist.disableStyle();
  flag_alone = loadShape("flag_alone.svg");
  flag_alone.disableStyle();

  //  headFont = createFont("Electronic Highway Sign", 100);
  //  bigFont = createFont("Electronic Highway Sign", 220);
  //  smallFont = createFont("Electronic Highway Sign", 40);
  

  headFont = createFont("EHSMB.TTF", 130);
  bigFont = createFont("EHSMB.TTF", 280);
  smallFont = createFont("EHSMB.TTF", 40);


  textAlign(CENTER, CENTER);
  // printArray(PFont.list());

  minim = new Minim(this);
  Ani.init(this);
}

void draw() {

  background(0);
  //filter(blur);
  //state machine
  switch(gameState) {
  case 0: // reset/get ready
    textFont(headFont);
    fill(#FFE000);
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


void keyPressed() {

  switch(key) {

  case 'q': //red power cube in opposing bucket ("high goal")
    score('l', 'r');
    break;

    case 'a': //red power cube in alliance bucket ("low goal")
      score('h', 'r');
      break;

   case 'z':
     if (redScore > 0) redScore -=5;
     break;

   case 'm':
      if (blueScore > 0) blueScore -=5;
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

  case 'l': //blue cube in opponent bucket ("high goal")
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

void assist(char team) {
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

void flag(char team) {
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


void score(char goal, char team) {

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

void drawScores() {
  textFont(smallFont);
  if (matchTime >= 120) {
    text("AUTONOMOUS", width/2, height - 75);
  } else if (matchTime >= 0) {
    text("TELEOPERATED", width/2, height - 75);
  }

  textFont(bigFont);
  fill(255, 0, 0);
  text(redScore, redX, height / 2);
  fill(0, 0, 255);
  text(blueScore, blueX, height / 2);
}

void drawGFX() {
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

  stroke(#FFE000);
  fill(#FFE000);
  strokeWeight(2);
  line(width/2, 200, width/2, 400);
  shape(flag_alone, flagAloneLoc, 300, 100, 100);
}


void drawClock() {
  textFont(headFont);
  if (matchTime > 20) {
    fill(#FFE000);
  } else if ( (matchTime <= 20) && (matchTime > 0) ) {
    fill(#FF8500);
  } else if (matchTime == 0) {
    fill(#FF0000);
  }
  int minutes = matchTime / 60;
  int seconds = matchTime % 60;
  if (seconds < 10) {
    text("0"+minutes+":"+"0"+seconds, width/2, 75);
  } else {
    text("0"+minutes+":"+seconds, width/2, 75);
  }
}

void drawReadyLights() {
  ellipseMode(CENTER);
  noStroke();
  fill(30);
  if (redReady) fill(#FF0000);
  ellipse(100, height / 2, 175, 175);
  fill(30);
  if (blueReady) fill(#0000FF);
  ellipse(width - 100, height / 2, 175, 175);
}

void countdownSequence() {
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
  fill(#FFE000);
  text("GET READY!", width/2, 60);
  drawReadyLights();
  fill(#FFE000);
  int lightMargin = 100;
  float bottomMargin = 12.5;
  int lightWidth = 100;
  if (countdownState > 0) {
    ellipse(width / 2 - lightWidth - lightMargin, height - lightWidth / 2 - bottomMargin, lightWidth, lightWidth);
  }
  if (countdownState > 1) {
    ellipse(width / 2, height - lightWidth / 2 - bottomMargin, lightWidth, lightWidth);
  }
  if (countdownState > 2) {
    ellipse(width / 2 + lightWidth + lightMargin, height - lightWidth / 2 - bottomMargin, lightWidth, lightWidth);
  }
  if (countdownState > 3) {
    background(0);
    textFont(headFont);
    text("MATCH START!", width/2, height / 2);
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


void runMatch() {
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

void reset() {
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
}
