package editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import javax.sound.midi.SysexMessage;
import java.io.*;
import java.security.Key;
import java.util.List;


public class Editor extends Application {
    private TextBuffer buffer;
    private Cursor cursorArray;

    void handleDownArrow() {

    }


    private int cursorX;
    private int cursorY;
    public int windowWidth = 500;
    public int windowHeight = 500;
    private final Rectangle textCursor;

    public Editor() {
        textCursor = new Rectangle(0, 0);
        buffer = new TextBuffer();
        cursorArray = new Cursor();
    }

    private class KeyEventHandler implements EventHandler<KeyEvent> {
        int textCenterX;
        int textCenterY;

        private static final int STARTING_TEXT_POSITION_X = 250;
        private static final int STARTING_TEXT_POSITION_Y = 250;
        private static final int STARTING_FONT_SIZE = 20;


        private Text displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");
        private int fontSize = STARTING_FONT_SIZE;

        private String fontName = "Verdana";

        KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
//            textCenterX = windowWidth / 2;
//            textCenterY = windowHeight / 2;
//            displayText = new Text(textCenterX, textCenterY, "");
//            displayText.setTextOrigin(VPos.TOP);
//            displayText.setFont(Font.font(fontName, fontSize));
        }

        private void wordWrap() {
            TextBuffer.TextNode pointer = buffer.getChar(buffer.cursorPos - 1);
            Text empty = new Text(" ");
            while (!pointer.prev.item.getText().equals(empty.getText())) {
                pointer = pointer.prev;
            }
            buffer.y += Math.round(buffer.getChar(buffer.cursorPos - 1).item.getLayoutBounds().getHeight());
            buffer.x = 5;
            pointer.item.setX(buffer.x);
            pointer.item.setY(buffer.y);
            cursorArray.addLast(pointer);
            while (pointer.next.item != null) {
                pointer = pointer.next;
                buffer.x += Math.round(pointer.prev.item.getLayoutBounds().getWidth());
                pointer.item.setX(buffer.x);
                pointer.item.setY(buffer.y);
            }
            pointer = pointer.next;
            buffer.x += Math.round(pointer.prev.item.getLayoutBounds().getWidth());
        }

        private void textSave() {
            List<String> args = getParameters().getRaw();
            if (args.size() < 2) {
                System.out.println("Expected usage: CopyFile <source filename> <destination filename>");
                System.exit(1);
            }
            String inputFilename = args.get(0);
            String outputFilename = args.get(1);

            try {
                File inputFile = new File(inputFilename);
                if (!inputFile.exists()) {
                    System.out.println("Unable to copy because file with name " + inputFilename
                            + " does not exist");
                    return;
                }
                FileReader reader = new FileReader(inputFile);
                BufferedReader bufferedReader = new BufferedReader(reader);
                FileWriter writer = new FileWriter(outputFilename);

                int intRead = -1;
                // Keep reading from the file input read() returns -1, which means the end of the file
                // was reached.
                while ((intRead = bufferedReader.read()) != -1) {
                    char charRead = (char) intRead;
                    String s = Character.toString(charRead);
                    if (s.equals("\r")) {
                        String name = String.valueOf(s);
                        writer.write(name);
                    }
                    writer.write(charRead);
                }
                System.out.println("Successfully copied file " + inputFilename + " to "
                        + outputFilename);
                //bufferedReader.close();
                //writer.close();
            } catch (FileNotFoundException fileNotFoundException) {
                System.out.println("File not found! Exception was: " + fileNotFoundException);
            } catch (IOException ioException) {
                System.out.println("Error when copying; exception was: " + ioException);
            }
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                String characterTyped = keyEvent.getCharacter();
                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                    buffer.addChar(characterTyped);
                    if (buffer.currentPos() == 2) {
                        cursorArray.addLast(buffer.getChar(buffer.currentPos() - 1));
                    }
                    if (buffer.x > (windowWidth - 20)) {
                        wordWrap();
                        keyEvent.consume();
                    }

                } else if (characterTyped.charAt(0) == 8) {
                    Text last = buffer.deleteChar();
                    if (buffer.x < 5) {
                        buffer.y -= Math.round(buffer.getChar(buffer.cursorPos - 1).item.getLayoutBounds().getHeight());
                        buffer.x = (int) last.getX();
                    }
                    keyEvent.consume();
                }

                centerTextAndUpdateBoundingBox(buffer.x, buffer.y);
            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.PLUS || code == KeyCode.EQUALS) {
                    TextBuffer.TextNode pointer = buffer.getChar(0);
                    fontSize += 4;
                    while (pointer.item != null) {
                        int width = (int) pointer.item.getLayoutBounds().getWidth();
                        pointer.item.setFont(Font.font(fontName, fontSize));
                        int newWidth = (int) pointer.item.getLayoutBounds().getWidth();
                        int diffW = newWidth - width;
                        pointer.next.item.setX(pointer.next.item.getX() + diffW);
                        pointer = pointer.next;
                        System.out.println(diffW);
                    }
                    centerTextAndUpdateBoundingBox(buffer.x, buffer.y);
                } else if (code == KeyCode.MINUS) {
                    TextBuffer.TextNode pointer = buffer.getChar(0);
                    fontSize = Math.max(0, fontSize - 4);
                    while (pointer.next.item != null) {
                        int width = (int) pointer.item.getLayoutBounds().getWidth();
                        pointer.item.setFont(Font.font(fontName, fontSize));
                        int diffW = (int) Math.round(pointer.item.getLayoutBounds().getWidth()) - width;
                        pointer.next.item.setX(pointer.next.item.getX() + diffW);
                        pointer = pointer.next;
                        System.out.println(diffW);
                    }
                    centerTextAndUpdateBoundingBox(buffer.x, buffer.y);
                    centerTextAndUpdateBoundingBox(buffer.x, buffer.y);
                } else if (code == KeyCode.ENTER) {
                    buffer.x = 5;
                    buffer.y += Math.round(buffer.getChar(buffer.cursorPos - 1).item.getLayoutBounds().getHeight());
                    buffer.addChar("\r");
                    //TextBuffer.TextNode pointer = buffer.getChar(buffer.cursorPos - 1);
                    keyEvent.consume();

                } else if (code == KeyCode.LEFT) {
                    if (buffer.x >= 5 && buffer.y >= 0) {
                        if (buffer.cursorPos > 0) {
                            TextBuffer.TextNode bufferTemp = buffer.getChar(buffer.cursorPos - 1);
                            System.out.println("Cursor position: " + buffer.cursorPos);
                            System.out.println("Previous character: " + bufferTemp.item.getText());
                            buffer.x -= Math.round(bufferTemp.item.getLayoutBounds().getWidth());
                            centerTextAndUpdateBoundingBox(buffer.x, buffer.y);
                            buffer.cursorPos -= 1;
                            if (buffer.x < 5) {
                                buffer.y -= Math.round(buffer.getChar(buffer.cursorPos - 1).item.getLayoutBounds().getHeight());
                                buffer.x = (int) bufferTemp.item.getX();
                            }
                        }
                    }
                } else if (code == KeyCode.RIGHT) {
                    if (buffer.cursorPos < buffer.size()) {
                        TextBuffer.TextNode bufferTemp = buffer.getChar(buffer.cursorPos);
                        System.out.println("Cursor positionn: " + buffer.cursorPos);
                        buffer.x += Math.round(bufferTemp.item.getLayoutBounds().getWidth());
                        centerTextAndUpdateBoundingBox(buffer.x, buffer.y);
                        buffer.cursorPos += 1;
                        if (buffer.x > windowWidth) {
                            buffer.y += Math.round(buffer.getChar(buffer.cursorPos).item.getLayoutBounds().getHeight());
                            buffer.x = (int) bufferTemp.next.item.getX();
                        }
                    }
                } else if (code == KeyCode.UP) {
                    if (buffer.y > 0) {
                        TextBuffer.TextNode bufferTemp = buffer.getChar(buffer.cursorPos - 1);
                        double lineTemp = bufferTemp.item.getX();
                        double oldY = bufferTemp.item.getY();
                        bufferTemp = bufferTemp.prev;
                        buffer.cursorPos -= 1;
                        TextBuffer.TextNode bestNode = null;
                        double bestDistance = Double.MAX_VALUE;
                        boolean onNextLine = false;
                        System.out.println("Starting at position: " + bufferTemp.item.getX());

                        while (!onNextLine) {
                            bufferTemp = bufferTemp.prev;
                            buffer.cursorPos -= 1;
                            if (bufferTemp.item.getY() != oldY) {
                                onNextLine = true;
                            }

                        }
                        System.out.println("Cursor position: " + buffer.cursorPos);
                        // Invariant - we will be on the next line by now
                        while (bufferTemp.item.getX() != 5) {
                            double distance = Math.abs(bufferTemp.item.getX() + bufferTemp.item.getLayoutBounds().getWidth() - lineTemp);
                            if (distance < bestDistance) {
                                bestNode = bufferTemp;
                                bestDistance = distance;
                                buffer.cursorPos -= 1;
                            }

                            bufferTemp = bufferTemp.prev;
                        }
                        System.out.println("Cursor position after we found best node: " + buffer.cursorPos);
                        System.out.println("Best distance is: " + bestDistance);
                        System.out.println("Best node is: " + bestNode.item.getText());
                        System.out.println("Best node x coord is " + bestNode.item.getX());
                        buffer.y -= Math.round(buffer.getChar(buffer.cursorPos).item.getLayoutBounds().getHeight());
                        buffer.x = (int) (bestNode.item.getX() + bestNode.item.getLayoutBounds().getWidth());
                        buffer.cursorPos += 1;
                        centerTextAndUpdateBoundingBox(buffer.x, buffer.y);
                    }

                } else if (code == KeyCode.DOWN) {
                    if (buffer.y <= buffer.getChar(buffer.cursorPos).item.getY()) {
                        TextBuffer.TextNode bufferTemp = buffer.getChar(buffer.cursorPos - 1);
                        double lineTemp = bufferTemp.item.getX();
                        double oldY = bufferTemp.item.getY();
                        bufferTemp = bufferTemp.next;
                        buffer.cursorPos += 1;
                        TextBuffer.TextNode bestNode = null;
                        double bestDistance = Double.MAX_VALUE;
                        boolean onNextLine = false;
                        System.out.println("Starting at position: " + bufferTemp.item.getX());

                        while (!onNextLine) {
                            bufferTemp = bufferTemp.next;
                            buffer.cursorPos += 1;
                            if (bufferTemp.item.getY() != oldY) {
                                onNextLine = true;
                            }

                        }
                        System.out.println("Cursor position: " + buffer.cursorPos);
                        // Invariant - we will be on the next line by now
                        double newY = bufferTemp.item.getY();
                        while (bufferTemp.item.getY() == newY && bufferTemp.item.getX() < windowWidth) {
                            double distance = Math.abs(bufferTemp.item.getX() + bufferTemp.item.getLayoutBounds().getWidth() - lineTemp);
                            if (distance < bestDistance) {
                                bestNode = bufferTemp;
                                bestDistance = distance;
                                buffer.cursorPos += 1;
                            }

                            bufferTemp = bufferTemp.next;
                        }
                        System.out.println("Cursor position after we found best node: " + buffer.cursorPos);
                        System.out.println("Best distance is: " + bestDistance);
                        System.out.println("Best node is: " + bestNode.item.getText());
                        System.out.println("Best node x coord is " + bestNode.item.getX());
                        buffer.y += Math.round(buffer.getChar(buffer.cursorPos).item.getLayoutBounds().getHeight());
                        buffer.x = (int) (bestNode.item.getX() + bestNode.item.getLayoutBounds().getWidth());
                        buffer.cursorPos -= 1;
                        centerTextAndUpdateBoundingBox(buffer.x, buffer.y);
                    }
                }

                else if (code == KeyCode.P) {
                    System.out.println(buffer.getChar(buffer.cursorPos - 1).item.getX() + ", " +  buffer.getChar(buffer.cursorPos - 1).item.getY());
                }

                else if (code == KeyCode.S) {
                    textSave();
                }
            }
        }






        private void centerTextAndUpdateBoundingBox(int x, int y) {
            double textHeight = buffer.getChar(buffer.currentPos() - 1).item.getLayoutBounds().getHeight();
            double textWidth = 1;

            // Re-position the text.
            textCursor.setHeight(textHeight);
            textCursor.setWidth(textWidth);

            // For rectangles, the position is the upper left hand corner.

            textCursor.setX(x);
            textCursor.setY(y);
            // Many of the JavaFX classes have implemented the toString() function, so that
            // they print nicely by default.
            //System.out.println("Bounding box: " + textCursor);

            // Make sure the text appears in front of the rectangle.
        }
    }

    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
        public Group root;
        MouseClickEventHandler(Group root) {
            this.root = root;
        }

        public void mouseClick(double x, double y) {
            int i = 0;
            while ((cursorArray.get(i).item.getY() - y) < 1 || (cursorArray.get(i).item.getY() - y) == 0) {
                i++;
            }
            TextBuffer.TextNode nodeY = cursorArray.get(i - 1);
            while ((nodeY.next.item.getX() - x) < 1) {
                nodeY = nodeY.next;
            }
            int nodeX = (int) nodeY.item.getX();
            //nodeY.setTextOrigin(VPos.BOTTOM);
            buffer.y = (int) nodeY.item.getY();
            buffer.x = nodeX;
            textCursor.setX(buffer.x);
            textCursor.setY(buffer.y);

        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            double xCoord = mouseEvent.getX();
            double yCoord = mouseEvent.getY();
            mouseClick(xCoord, yCoord);

        }
    }

    private class RectangleBlinkEventHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 0;
        private Color[] boxColors =
                {Color.BLACK, Color.TRANSPARENT};

        RectangleBlinkEventHandler() {
            changeOpacity();
        }

        private void changeOpacity() {
            textCursor.setFill(boxColors[currentColorIndex]);
            currentColorIndex = (currentColorIndex + 1) % boxColors.length;
        }

        @Override
        public void handle(ActionEvent event) {
            changeOpacity();
        }
    }

    public void makeRectangleColorChange() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        RectangleBlinkEventHandler cursorChange = new RectangleBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }


    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        Group textRoot = new Group();
        buffer.getRoot(textRoot);
        Scene scene = new Scene(root, windowWidth, windowHeight, Color.WHITE);
        root.getChildren().add(textRoot);
        ScrollBar scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setPrefHeight(windowHeight);
        scrollBar.setMin(0);
        scrollBar.setMax(windowHeight);
        double usableScreenWidth = windowWidth - scrollBar.getLayoutBounds().getWidth();
        root.getChildren().add(scrollBar);
        scrollBar.setLayoutX(usableScreenWidth);
        scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {
                textRoot.setLayoutY(-newValue.doubleValue());
            }
        });
        List<String> args = getParameters().getRaw();
        if (args.size() < 1) {
            System.out.println("Expected usage: CopyFile <source filename> <destination filename>");
            System.exit(1);
        }
        String inputFilename = args.get(0);

        try {
            File inputFile = new File(inputFilename);
            // Check to make sure that the input file exists!
            if (!inputFile.exists()) {
                System.out.println("Unable to copy because file with name " + inputFilename
                        + " does not exist");
                return;
            }
            FileReader reader = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(reader);
            int intRead = -1;
            while ((intRead = bufferedReader.read()) != -1) {
                char charRead = (char) intRead;
                String s = Character.toString(charRead);
                if (s.equals("\r")) {
                    String name = String.valueOf(s);
                    buffer.addChar(s);
                }
                else {
                    buffer.addChar(s);
                }
            }

            bufferedReader.close();
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File not found! Exception was: " + fileNotFoundException);
        } catch (IOException ioException) {
            System.out.println("Error when copying; exception was: " + ioException);
        }

        EventHandler<KeyEvent> keyEventHandler = new KeyEventHandler(root, windowWidth, windowHeight);
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        scene.setOnMouseClicked(new MouseClickEventHandler(root));
        root.getChildren().add(textCursor);
        makeRectangleColorChange();

        primaryStage.setTitle("Text Editor");

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}



    
