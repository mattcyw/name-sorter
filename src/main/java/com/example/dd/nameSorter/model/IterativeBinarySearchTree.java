package com.example.dd.nameSorter.model;

import java.util.List;

/**
 * Iterative Binary Search Tree (BST) implementation for any single type object which is Comparable.
 * Use Loops instead of Recursion to avoid Java's recursive stack depth limits
 * This tree does NOT remove duplicates; duplicates are inserted to the right.
 * Rebalancing is NOT implemented in this version because only sorting is required, but not searching.
 * @param <T>
 */
public class IterativeBinarySearchTree<T extends Comparable<T>> {

    private static final int BALANCE_THRESHOLD = 1;

    private Node<T> root;

    public IterativeBinarySearchTree() {}

    // ==========================================================
    // 1. ITERATIVE INSERTION (Replaces Recursive Insert)
    // ==========================================================
    public void insert(T value) {

        Node<T> newNode = new Node<>(value);
        if (root == null) {
            root = newNode;
            return;
        }

        java.util.Stack<Node<T>> pathStack = new java.util.Stack<>();
        Node<T> current = root;
        Node<T> parent = null; // We need to track the parent to link the new node

        while (current != null) {
            pathStack.push(current);
            parent = current;
            int comparison = value.compareTo(current.value);

            if (comparison < 0) { // Value is smaller, move left
                current = current.left;
            } else { // Value is larger or equal (i.e. Preserve Duplication!), move right
                current = current.right;
            }
        }

        // The loop exited because current is null, meaning parent is the new node's parent.
        // Determine if the new node is the left or right child of the parent.
        if (value.compareTo(parent.value) < 0) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        // Iteratively go back up the path, checking balance (Ascent & Rebalance)
        while (!pathStack.isEmpty()) {

            Node<T> ancestor = pathStack.pop();

            // Find the ancestor's parent on the next iteration (if one exists)
            Node<T> ancestorParent = pathStack.isEmpty() ? null : pathStack.peek();

            // Update height
            updateHeight(ancestor);

            // Check balance
            int balance = getBalance(ancestor);
            Node<T> newSubtreeRoot = ancestor;

            // --- Correct AVL Rebalancing Logic ---

            // Case 1 & 3: Left-Heavy Tree
            if (balance > BALANCE_THRESHOLD) {
                // Case 3: Left-Right (child is right-heavy)
                if (getBalance(ancestor.left) < 0) {
                    ancestor.left = rotateLeft(ancestor.left);
                }
                // Case 1: Left-Left (child is left-heavy or balanced)
                // This rotation is performed for both LL and LR cases.
                newSubtreeRoot = rotateRight(ancestor);
            }
            // Case 2 & 4: Right-Heavy Tree
            else if (balance < -BALANCE_THRESHOLD) {
                // Case 4: Right-Left (child is left-heavy)
                if (getBalance(ancestor.right) > 0) {
                    ancestor.right = rotateRight(ancestor.right);
                }
                // Case 2: Right-Right (child is right-heavy or balanced)
                // This rotation is performed for both RR and RL cases.
                newSubtreeRoot = rotateLeft(ancestor);
            }

            // 4. Update the Parent's Pointer and Check for Optimization
            if (!newSubtreeRoot.equals(ancestor)) {

                // Link the rotated subtree back to the grandparent
                if (ancestorParent == null) {
                    root = newSubtreeRoot;
                } else if (newSubtreeRoot.value.compareTo(ancestorParent.value) < 0) {
                    ancestorParent.left = newSubtreeRoot;
                } else {
                    ancestorParent.right = newSubtreeRoot;
                }

                // AVL OPTIMIZATION: Stop after the first successful rotation.
                break;
            }
        }

    }

    // ==========================================================
    // HEIGHT MANAGEMENT
    // ==========================================================

    /**
     * Gets the height of the node's subtree (or 0 if the node is null).
     */
    private int height(Node<T> N) {
        return (N == null) ? 0 : N.height;
    }

    /**
     * Calculates the Balance Factor (Left Subtree Height - Right Subtree Height).
     * A result > 1 means the tree is left-heavy.
     * A result < -1 means the tree is right-heavy.
     */
    private int getBalance(Node<T> N) {
        return (N == null) ? 0 : height(N.left) - height(N.right);
    }

    /**
     * Recalculates and updates the height of the given node.
     * Height = 1 + Max(Height of Left Child, Height of Right Child).
     */
    private void updateHeight(Node<T> N) {
        if (N != null) {
            N.height = 1 + Math.max(height(N.left), height(N.right));
        }
    }

    // ==========================================================
    // ROTATIONS
    // ==========================================================

    /**
     * Performs a Right Rotation (used for Left-Heavy Imbalances).
     * @param parent The current root of the unbalanced subtree.
     * @return The new root of the subtree after rotation.
     */
    private Node<T> rotateRight(Node<T> parent) {

        // 1. Define the pivot and middle child
        Node<T> pivot = parent.left;
        Node<T> middleChild = pivot.right; // T2 in traditional notation

        // 2. Perform rotation
        pivot.right = parent;
        parent.left = middleChild;

        // 3. Update heights from the bottom up (MUST be done in this order)
        updateHeight(parent);
        updateHeight(pivot);

        return pivot; // New root of the subtree
    }

    /**
     * Performs a Left Rotation (used for Right-Heavy Imbalances).
     * @param parent The current root of the unbalanced subtree.
     * @return The new root of the subtree after rotation.
     */
    private Node<T> rotateLeft(Node<T> parent) {

        // 1. Define the pivot and middle child
        Node<T> pivot = parent.right;
        Node<T> middleChild = pivot.left; // T2 in traditional notation

        // 2. Perform rotation
        pivot.left = parent;
        parent.right = middleChild;

        // 3. Update heights from the bottom up (MUST be done in this order)
        updateHeight(parent);
        updateHeight(pivot);

        return pivot; // New root of the subtree
    }

    // ==========================================================
    // 2. ITERATIVE TRAVERSAL (Uses a Stack to replace the recursion)
    // ==========================================================
    public List<T> traverseInOrder() {

        // 1. Initialize the list that will hold the final sorted result
        List<T> sortedList = new java.util.ArrayList<>();

        if (root == null) {
            return sortedList; // Return an empty list if the tree is empty
        }

        java.util.Stack<Node<T>> stack = new java.util.Stack<>();
        Node<T> current = root;

        while (current != null || !stack.isEmpty()) {

            // 1. Go to the deepest left node, pushing all parents onto the stack
            while (current != null) {
                stack.push(current);
                current = current.left;
            }

            // 2. Current is null, pop the parent (which is the next smallest node)
            current = stack.pop();
            sortedList.add(current.value);

            // 3. Go one step right, then repeat the process
            current = current.right;
        }

        // Return the list containing the elements in ascending sorted order
        return sortedList;
    }

}