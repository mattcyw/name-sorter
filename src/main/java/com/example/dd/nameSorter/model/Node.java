package com.example.dd.nameSorter.model;

import lombok.Data;

@Data
class Node<T extends Comparable<T>> {
    T value;
    Node<T> left;
    Node<T> right;
    int height; // Height of the node for balancing purposes

    public Node(T value) {
        this.value = value;
        this.height = 1;
    }
}