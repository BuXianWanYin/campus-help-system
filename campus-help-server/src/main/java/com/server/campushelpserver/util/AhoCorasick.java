package com.server.campushelpserver.util;

import java.util.*;

/**
 * AC自动机算法实现
 */
public class AhoCorasick {
    
    /**
     * AC自动机节点
     */
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        TrieNode fail;
        boolean isEnd;
        String word;
    }
    
    private TrieNode root = new TrieNode();
    
    /**
     * 构建AC自动机
     */
    public void build(List<String> words) {
        // 1. 构建Trie树
        for (String word : words) {
            if (word == null || word.isEmpty()) {
                continue;
            }
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                node = node.children.computeIfAbsent(c, k -> new TrieNode());
            }
            node.isEnd = true;
            node.word = word;
        }
        
        // 2. 构建失败指针（BFS）
        Queue<TrieNode> queue = new LinkedList<>();
        root.fail = root;
        
        for (TrieNode child : root.children.values()) {
            child.fail = root;
            queue.offer(child);
        }
        
        while (!queue.isEmpty()) {
            TrieNode node = queue.poll();
            
            for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
                char c = entry.getKey();
                TrieNode child = entry.getValue();
                queue.offer(child);
                
                TrieNode failNode = node.fail;
                while (failNode != root && !failNode.children.containsKey(c)) {
                    failNode = failNode.fail;
                }
                
                child.fail = failNode.children.getOrDefault(c, root);
            }
        }
    }
    
    /**
     * 检测文本中的敏感词
     */
    public List<String> search(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }
        
        TrieNode node = root;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            // 转小写
            c = Character.toLowerCase(c);
            
            // 跳过空格等特殊字符
            if (!Character.isLetterOrDigit(c) && !isChinese(c)) {
                continue;
            }
            
            while (node != root && !node.children.containsKey(c)) {
                node = node.fail;
            }
            
            node = node.children.getOrDefault(c, root);
            
            // 检查所有匹配的敏感词
            TrieNode temp = node;
            while (temp != root) {
                if (temp.isEnd) {
                    if (!result.contains(temp.word)) {
                        result.add(temp.word);
                    }
                }
                temp = temp.fail;
            }
        }
        
        return result;
    }
    
    /**
     * 判断是否为中文字符
     */
    private boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }
}

