package editor;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;


public class TextBuffer extends LinkedListDeque<Text> {
    public int currentPos;
    public int cursorPos;
    private TextNode sentinelfront;
    private TextNode sentinelback;
    private Group root;
    public int x = 5;
    public int y = 0;

    public TextBuffer() {
        sentinelfront = new TextNode();
        sentinelback = new TextNode();
        sentinelfront.next = sentinelback;
        sentinelback.prev = sentinelfront;
        currentPos = 0;
        cursorPos = 0;
    }

    public class TextNode extends ListNode {
        public Text item;
        public TextNode next;
        public TextNode prev;

        public TextNode(Text i, TextNode p, TextNode n) {
            item = i;
            next = n;
            prev = p;
        }

        public TextNode() {
            next = null;
            prev = null;
        }
    }

    public void getRoot(Group root) {
        this.root = root;
    }

    public void addChar(String s) {
        Text newChar = new Text(this.x, this.y, s);
        newChar.setTextOrigin(VPos.TOP);
        this.addLast(newChar);
        TextNode last = new TextNode(newChar, sentinelback.prev, sentinelback);
        sentinelback.prev = last;
        last.prev.next = last;
        currentPos += 1;
        cursorPos += 1;
        updateAddText(newChar);

        root.getChildren().add(newChar);
    }

    public Text deleteChar() {
        if (currentPos == 0) {
            return null;
        } else {
            Text last = this.removeLast();
            root.getChildren().remove(last);
            if (currentPos > 1) {
                Text result = sentinelback.prev.item;
                sentinelback.prev = sentinelback.prev.prev;
                sentinelback.prev.next = sentinelback;
            }
            currentPos -= 1;
            cursorPos = currentPos;
            updateDeleteText(last);
            return last;
        }
    }

    public TextNode getChar(int index) {
        if (index >= currentPos) {
            return null;
        } else {
            TextNode p = sentinelfront.next;
            for (int i = 0; i < index; i++) {
                p = p.next;
            }
            return p;
        }
    }

    public int currentPos() {
        return this.currentPos;
    }

    public TextNode peek() {
        return this.getChar(currentPos() - 1);
    }

    public void updateAddText(Text Chars) {
        double xPos = Chars.getX();
        this.x += Math.round(Chars.getLayoutBounds().getWidth());
    }


    public void updateDeleteText(Text Chars) {
        if (this.x >= 5) {
            this.x -= Math.round(Chars.getLayoutBounds().getWidth());
        }
    }
}
