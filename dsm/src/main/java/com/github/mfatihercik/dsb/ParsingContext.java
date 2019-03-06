package com.github.mfatihercik.dsb;

import java.util.HashMap;
import java.util.Map;

public class ParsingContext {
    private int nodeMapSize = 0;
    private Map<String, Node> mainNodeMap = new HashMap<>();
    private Class<?> resultType;
    private Node rootNode;
    private Node[] nodeMap = null;

    public void initNodeMap(int size) {
        nodeMapSize = size;
        nodeMap = new Node[nodeMapSize];

    }

    public void clear() {
        getMainNodeMap().clear();
        initNodeMap(nodeMapSize);

    }

    public boolean contains(String uniqueName) {
        return getMainNodeMap().containsKey(uniqueName);
    }

    public boolean contains(int index) {
        return nodeMap[index] != null;
    }

    public boolean contains(ParsingElement index) {
        return index.getIndex() > -1 && nodeMap[index.getIndex()] != null;
    }


    public Node get(String uniqueKey) {
        return getMainNodeMap().get(uniqueKey);
    }

    public Node get(int index) {
        return nodeMap[index];
    }

    public Node get(ParsingElement parsingElement) {
        if (parsingElement.getIndex() > -1) {
            return get(parsingElement.getIndex());
        }
        return null;
    }

    public void add(Node node) {
        ParsingElement parsingElement = node.getParsingElement();
        nodeMap[parsingElement.getIndex()] = node;
        getMainNodeMap().put(parsingElement.getUniqueKey(), node);
    }

    public Node remove(ParsingElement parsingElement) {
        nodeMap[parsingElement.getIndex()] = null;
        return getMainNodeMap().remove(parsingElement.getUniqueKey());
    }

    public Map<String, Node> getMainNodeMap() {
        return mainNodeMap;
    }

    public void setMainNodeMap(Map<String, Node> mainNodeMap) {
        this.mainNodeMap = mainNodeMap;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }

    public Class<?> getResultType() {
        return resultType;
    }

    public void setResultType(Class<?> resultType) {
        this.resultType = resultType;
    }

}