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

public class LinkedListDeque<Item> {
  public ListNode sentinelfront;
  public ListNode sentinelback;
  private int size;

  //-----------------------------------------------
  public class ListNode {
    public Item item;
    public ListNode next;
    public ListNode prev;

    public ListNode(Item i, ListNode p, ListNode n) {
      item = i;
      next = n;
      prev = p;
    }

    public ListNode() {
      next = null;
      prev = null;
    }
  }
  //-----------------------------------------------

  public LinkedListDeque() {
    sentinelfront = new ListNode();
    sentinelback = new ListNode();
    sentinelfront.next = sentinelback;
    sentinelback.prev = sentinelfront;
    size = 0;
  }

  public void addFirst(Item i) {
    ListNode first = new ListNode(i, sentinelfront, sentinelfront.next);
    sentinelfront.next = first;
    first.next.prev = first;
    size += 1;
  }

  public void addLast(Item i) {
    ListNode last = new ListNode(i, sentinelback.prev, sentinelback);
    sentinelback.prev = last;
    last.prev.next = last;
    size += 1;
  }

  public boolean isEmpty() {
    if (size == 0) {
      return true;
    } else {
      return false;
    }
  }

  public int size() {
    return size;
  }

  public void printDeque() {
    ListNode p = sentinelfront.next;
    while(p != sentinelback.prev) {
      System.out.print(p.item.toString() + " ");
      p = p.next;
    }
    System.out.println(p.item.toString());
  }

  public Item removeFirst() {
    if (size == 0) {
      return null;
    } else {
      Item result = sentinelfront.next.item;
      sentinelfront.next = sentinelfront.next.next;
      sentinelfront.next.prev = sentinelfront;
      size -= 1;
      return result;
    }
  }

  public Item removeLast() {
    if (size == 0) {
      return null;
    } else {
      Item result = sentinelback.prev.item;
      sentinelback.prev = sentinelback.prev.prev;
      sentinelback.prev.next = sentinelback;
      size -= 1;
      return result;
    }
  }

  public Item get(int index) {
    if (index >= size) {
      return null;
    } else {
      ListNode p = sentinelfront.next;
      for(int i = 0; i < index; i++) {
        p = p.next;
      }
      return p.item;
    }
  }

  private Item helper(int index, ListNode p) {
    if (index == 0) {
      return p.item;
    } else {
      return helper(index - 1, p.next);
    }
  }

  public Item getRecursive(int index) {
    if (index >= size) {
      return null;
    } else {
      return helper(index, sentinelfront.next);
    }
  }


}
