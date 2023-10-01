import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.valueOf;

class BTree<K extends Comparable<K>, V> implements RangeMap<K, V> {
    private static int size = 0;

    private final class Node {
        int keyNum;
        Node parent;
        LinkedList<Entry> entries;
        LinkedList<Node> children;
    }

    // Represent Entry as key and value.
    private final class Entry {
        K k;
        V v;

        /**
         * Entry constructor.
         */
        public Entry(K k, V v) {
            this.k = k;
            this.v = v;
        }
    }

    private final class Result {
        Node ptr;
        int i;
        boolean tag; // Check if the node presents in the tree.

        /**
         * Result constructor.
         */
        public Result(Node ptr, int i, boolean tag) {
            this.ptr = ptr;
            this.i = i;
            this.tag = tag;
        }
    }

    /**
     * Let us assign degree of the BTree 3 as one of the most popular and convenient strategy.
     * m is number of children.
     */
    private final int m = 3;

    private Node root;

    /**
     * BTree constructor.
     */
    public BTree(Node root) {
        this.root = root;
    }

    /**
     * Size of BTree.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Check if BTree is empty. Look at its size.
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Insert item into the tree.
     */
    @Override
    public void add(K k, V v) {
        Entry entry = new Entry(k, v);
        Result result = searchTree(root, entry.k);
        if(!result.tag) {
            addBTree(result.ptr, entry, result.i);
            size++; // Increment the static size variable.
        } else {
            result.ptr.entries.set(result.i, entry); // If node already presents then just change its value.
        }
    }

    /**
     * Check if a key is present.
     */
    @Override
    public boolean contains(K key) {
        Object[] items = checkPresence(root, key);
        return (boolean) items[0];
    }

    /**
     * Lookup a value by the key.
     */
    @Override
    public V lookup(K key) {
        if(!contains(key))
            return null;
        else {
            Result result = searchTree(root, key);
            return result.ptr.entries.get(result.i).v;
        }
    }

    /**
     * Lookup values for a range of keys.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<V> lookupRange(K from, K to) {
        ArrayList<V> list = new ArrayList<>();
        int year = Integer.parseInt(from.toString().substring(0, 4));
        int month = Integer.parseInt(from.toString().substring(5, 7));
        int day = Integer.parseInt(from.toString().substring(8, 10)) - 1;
        boolean breakFlag = false;

        while(!breakFlag) {
            day++;
            if(day == 32) { // Check correctness of days number.
                day = 0;
                month++;
            }
            if(month == 13) { // Check correctness of months number.
                day = 0;
                month = 0;
                year++;
            }

            String dayStr, monthStr;
            dayStr = valueOf(day);
            monthStr = valueOf(month);

            // Set the day and month in the same format as in the input data.
            if(dayStr.length() == 1)
                dayStr = "0" + day;
            if(monthStr.length() == 1)
                monthStr = "0" + month;

            String s = year + "-" + monthStr + "-" + dayStr; // Create the new string in input format to perform lookup.
            if(contains((K) s)) {
                list.add(lookup((K) s));
            }
            if(s.equals(to)) // We reach the final string -> loop is done.
                breakFlag = true;
        }
        return list;
    }

    /**
     * Remove an item from a map.
     */
    @Override
    public Object remove(K k) {
        Result result = searchTree(root, k);
        return result.tag && removeTree(result);
    }

    /**
     * The common algorithm to methods 'searchTree' and 'contains'.
     */
    Object[] checkPresence(Node node, K key) {
        Node p = node, q = null;
        boolean found = false;
        int i = 0;

        while(p != null && !found) {
            i = search(p, key);
            if(i > 0 && isEqualTo(key, p.entries.get(i).k))
                found = true;
            else {
                q = p;
                p = p.children.get(i);
            }
        }

        return new Object[] {found, i, p, q}; // Return array of different types to make the algorithm more general.
    }

    /**
     * The method needed to the following methods performed correctly:
     * "add"
     * "remove"
     * "lookup"
     * <p>
     * The method is similar to "contains" but it returns the Result object.
     */
    @SuppressWarnings("unchecked")
    private Result searchTree(Node node, K key) {
        Object[] items = checkPresence(node, key);

        if((boolean) items[0])
            return new Result((Node) items[2], (int) items[1], true);
        else
            return new Result((Node) items[3], (int) items[1], false);
    }

    /**
     * Search index of node with given key.
     */
    private int search(Node p, K k) {
        int j = 0;
        for(; j < p.keyNum; j++) {
            if(isLessThan(k, p.entries.get(j + 1).k))
                break;
        }
        return j;
    }

    /**
     * If the node which we want to insert is not present in the BTree, this method is performed.
     */
    public void addBTree(Node q, Entry entry, int i) {
        int s;
        Entry x = entry;
        Node node = null, tempNode = null;
        boolean finished = false;

        while(q != null && !finished) {
            addItemElements(q, i, x, node);
            if(q.keyNum < m)
                finished = true;
            else { // The size of elements in single node is bigger than 2.
                s = m / 2 + 1;
                x = q.entries.get(s);
                node = new Node(); // Create new node to split.
                split(q, s, node);
                tempNode = q;
                q = q.parent;
                if(q != null)
                    i = search(q, entry.k);
            }
        }
        if(!finished) {
            root = tempNode;
            newRoot(x, node);
        }
    }

    /**
     * Set new elements for the item.
     */
    public void addItemElements(Node node, int i, Entry entry, Node child) {
        node.children.add(i + 1, child); // Add child by index.
        node.entries.add(i + 1, entry); // Add entry by index.
        if(child != null)
            child.parent = node;
        node.keyNum++; // Increment number of keys.
    }

    /**
     * Create new root.
     */
    public void newRoot(Entry entry, Node child) {
        Node node = new Node();
        node.entries = new LinkedList<>();
        node.children = new LinkedList<>();
        node.parent = null;
        node.entries.add(null);
        node.entries.add(entry); // Add key (data as string) and value (money as double).
        node.keyNum = 1; // Initial number of keys.
        node.children.add(root); // Create the empty child.
        node.children.add(child);

        for(int i = 0; i <= node.keyNum; i++) {
            if(node.children.get(i) != null) {
                node.children.get(i).parent = node;
            }
        }
        root = node; // Finish the root creation.
    }

    /**
     * Split the nodes.
     */
    public void split(Node q, int s, Node p) {
        p.children = new LinkedList<>();
        p.entries = new LinkedList<>();
        p.entries.add(null);
        p.entries.addAll(q.entries.subList(s + 1, m + 1));
        p.children.add(q.children.get(s));
        p.children.addAll(q.children.subList(s + 1, m + 1));
        p.keyNum = m - s;
        q.keyNum = s - 1;

        for(int i = m; i > q.keyNum; i--) {
            q.entries.removeLast();
            q.children.removeLast();
        }
        for(int i = 0; i <= p.keyNum; i++) {
            if(p.children.get(i) != null) {
                p.children.get(i).parent = p;
            }
        }
    }

    public boolean removeTree(Result result) {
        Node q = result.ptr;
        Result minResult = null;
        int i;
        Node node;
        if(q.children.get(result.i) != null) {
            minResult = searchMinKey(q);
        }
        if(minResult != null) {
            node = minResult.ptr;
            i = minResult.i;
        } else {
            node = result.ptr;
            i = result.i;
        }

        // When the node and q refers to the same object exceptions occurs.
        LinkedList<Entry> tempList = new LinkedList<>(node.entries);
        q.entries.remove(result.i);
        q.entries.add(result.i, tempList.get(i));
        return removeItemElements(node, i);
    }

    /**
     * Update node for removing. The method can be recursive.
     */
    private boolean removeItemElements(Node node, int i) {
        int s, tag = 0;
        Node parent, leftChild, rightChild;
        s = m / 2 + 1;
        int order = -1;
        parent = node.parent;

        if(parent == null) {
            tag = 1;
        } else {
            order = findIndexOfParent(node, parent);
            if(node.keyNum >= s) {
                tag = 2; // Delete the entry directly.
            } else {
                if(order < parent.keyNum && parent.children.get(order + 1).keyNum >= s) {
                    tag = 3;
                }
                if(tag == 0 && order > 0 && parent.children.get(order - 1).keyNum >= s) {
                    tag = 4;
                }
                if(tag == 0 && order < parent.keyNum && parent.children.get(order + 1).keyNum == s - 1) {
                    tag = 5;
                }
                if(tag == 0 && order > 0 && parent.children.get(order - 1).keyNum == s - 1) {
                    tag = 6;
                }
            }
        }
        switch(tag) {
            case 0:
                return false; // ERROR: removable object is not present in the BTree.
            case 1:
                removeKeyAndChild(node, i);
                node.keyNum--;
                break;
            case 2:
                removeKeyAndChild(node, i);
                if(node.keyNum == 1 && i == 1)
                    root = node.children.get(0);
                node.keyNum--;
                break;
            case 3:
                leftChild = parent.children.get(order - 1);
                removeKeyAndChild(node, i);
                node.children.add(0, leftChild.children.get(leftChild.keyNum));
                node.entries.add(1, parent.entries.get(order));
                parent.entries.remove(order);
                parent.entries.add(order, leftChild.entries.get(leftChild.keyNum));
                removeKeyAndChild(leftChild, leftChild.keyNum);
                leftChild.keyNum--;
                break;
            case 4:
                rightChild = parent.children.get(order + 1);
                removeKeyAndChild(node, i);
                node.entries.add(parent.entries.get(order + 1));
                node.children.add(rightChild.children.get(0));
                parent.entries.remove(order + 1);
                parent.entries.add(order + 1, rightChild.entries.get(1));
                rightChild.children.remove(0);
                rightChild.entries.remove(1);
                rightChild.keyNum--;
                break;
            case 5:
                rightChild = parent.children.get(order + 1);
                removeKeyAndChild(node, i);
                node.entries.add(parent.entries.get(order + 1));
                node.keyNum = node.keyNum + rightChild.keyNum;
                removeKeyAndChild(parent, order + 1);
                node.children.addAll(rightChild.children);
                node.entries.addAll(rightChild.entries.subList(1, rightChild.entries.size()));
                parent.keyNum--;
                if(parent.keyNum < s - 1) {
                    parent.keyNum = parent.keyNum + 1;
                    node = parent;
                    removeItemElements(node, node.keyNum); // Recursive call.
                }
                break;
            case 6:
                leftChild = parent.children.get(order - 1);
                leftChild.entries.add(parent.entries.get(order));
                leftChild.keyNum++;
                removeKeyAndChild(node, i);
                node.keyNum--;
                removeKeyAndChild(parent, order);
                parent.keyNum--;
                leftChild.children.addAll(node.children);
                node.entries.removeFirst();
                leftChild.entries.addAll(node.entries);
                leftChild.keyNum += node.keyNum;
                if(parent.keyNum < s - 1) {
                    parent.keyNum++;
                    node = parent;
                    removeItemElements(node, node.keyNum); // Recursive call.
                }
                break;
        }
        size--; // If the item is removed, decrement size of BTree;
        return true;
    }

    /**
     * Delete children and entries of an item.
     */
    private void removeKeyAndChild(Node q, int i) {
        if(i > q.entries.size() - 1)
            return;
        q.children.remove(i);
        q.entries.remove(i);
    }

    /**
     * Find parent index by counter which iterates throw node's children.
     */
    public int findIndexOfParent(Node q, Node p) {
        int count = 0;
        while(p.children.get(count) != q)
            count++;
        return count;
    }

    /**
     * Search the minimum key of an item to correctly perform remove operation.
     */
    public Result searchMinKey(Node p) {
        while(p != null && p.children.get(0) != null)
            p = p.children.get(0);
        return new Result(p, 1, true);
    }

    /**
     * Check if key1 is less than key2.
     */
    private boolean isLessThan(K k1, K k2) {
        return k1.compareTo(k2) < 0;
    }

    /**
     * Check if key1 is equal to key2.
     */
    private boolean isEqualTo(K k1, K k2) {
        return k1.compareTo(k2) == 0;
    }
}