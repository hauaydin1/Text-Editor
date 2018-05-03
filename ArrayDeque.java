package editor;
public class ArrayDeque<Item> {
  private Item[] items;
  private int first;
  private int last;
  private int size;

  private int circlestep(boolean add, int index) {
    if (size == 0) {
      return index;
    }
    if (add == true) {
      if (index == items.length - 1) {
        return 0;
      } else {
        return index + 1;
      }
    } else {
      if (index == 0) {
        return items.length - 1;
      } else {
        return index - 1;
      }
    }
  }

  private void resize(int new_len) {
    int old_len = items.length;
    int old_first = first;
    int old_last = last;
    Item[] new_arr = (Item []) new Object[new_len];
    for (int i = 0; i < new_len; i++) {
      new_arr[i] = null;
    }
    //accounts for reacharound
    //accounts for new_len < old_len. probably.

    // System.arraycopy(items, old_first, new_arr, j, (old_len - old_first));
    // j = j + old_len - old_first;
    // System.arraycopy(items, 0, new_arr, j, (old_last + 1));
    // j = j + old_last;
    // first = new_first;
    // last = j;


    int new_first = (int)(new_len / 4);
    int j = new_first;
    int i = old_first;
    int n = 0;
    while (n < size) {
      new_arr[j] = items[i];
      j += 1;
      i = circlestep(true, i);
      n += 1;
    }
    first = new_first;
    last = j - 1;

    items = new_arr;
  }

  private boolean checknull(int index) {
    return (items[index] == null);
  }

  public ArrayDeque() {
    items = (Item []) new Object[8];
    first = 3;
    last = 3;
    size = 0;
    for (int i = 0; i < items.length; i++) {
      items[i] = null;
    }
  }

  public void addFirst(Item item) {
    if (size == items.length) {
      int new_len = (int)(items.length * 2);
      resize(new_len);
    }
    if (checknull(first) == false) {
      first = circlestep(false, first);
    }
    items[first] = item;
    size += 1;
  }

  public void addLast(Item item) {
    if (size == items.length) {
      int new_len = (int)(items.length * 2);
      resize(new_len);
    }
    if (checknull(last) == false) {
      last = circlestep(true, last);
    }
    items[last] = item;
    size += 1;
  }

  public boolean isEmpty() {
    return (size == 0);
  }

  public int size() {
    return size;
  }

  public void printDeque() {
    int i = first;
    while (i != last) {
      System.out.print(items[i] + " ");
      i = circlestep(true, i);
    }
    System.out.println(items[last]);
  }

  public Item removeFirst() {
    Item result = items[first];
    items[first] = null;
    if (first != last) {
      first = circlestep(true, first);
    }
    size -= 1;
    if (items.length > 8) {
      if ((((float)size) / ((float)items.length)) < 0.25) {
        int new_len = (int)(items.length / 2);
        resize(new_len);
      }
    }
    return result;
  }

  public Item removeLast() {
    Item result = items[last];
    items[last] = null;
    if (first != last) {
      last = circlestep(false, last);
    }
    size -= 1;
    if (items.length > 8) {
      if ((((float)size) / ((float)items.length)) < 0.25) {
        int new_len = (int)(items.length / 2);
        resize(new_len);
      }
    }
    return result;
  }

  public Item get(int index) {
    int i = first + index;
    if (i >= items.length) {
      i = i - items.length;
    }
    return items[i];
  }
}
