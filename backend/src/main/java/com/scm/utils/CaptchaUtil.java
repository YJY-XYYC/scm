package com.scm.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 验证码生成工具类
 */
public class CaptchaUtil {
    
    // 验证码字符集
    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    // 验证码长度
    private static final int CODE_LENGTH = 4;
    // 图片宽度
    private static final int IMAGE_WIDTH = 120;
    // 图片高度
    private static final int IMAGE_HEIGHT = 40;
    // 干扰线数量
    private static final int LINE_COUNT = 5;
    
    private static final Random random = new Random();
    
    /**
     * 生成随机验证码字符
     */
    public static String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARS[random.nextInt(CHARS.length)]);
        }
        return code.toString();
    }
    
    /**
     * 根据验证码内容生成图片
     */
    public static BufferedImage generateImage(String code) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        
        // 设置背景色
        g.setColor(getRandomColor(200, 250));
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        
        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 24));
        
        // 画干扰线
        for (int i = 0; i < LINE_COUNT; i++) {
            g.setColor(getRandomColor(160, 200));
            int x1 = random.nextInt(IMAGE_WIDTH);
            int y1 = random.nextInt(IMAGE_HEIGHT);
            int x2 = random.nextInt(IMAGE_WIDTH);
            int y2 = random.nextInt(IMAGE_HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
        
        // 画验证码字符
        for (int i = 0; i < code.length(); i++) {
            g.setColor(getRandomColor(80, 180));
            g.drawString(String.valueOf(code.charAt(i)), 25 * i + 10, 28);
        }
        
        // 画干扰点
        for (int i = 0; i < 100; i++) {
            g.setColor(getRandomColor(120, 200));
            int x = random.nextInt(IMAGE_WIDTH);
            int y = random.nextInt(IMAGE_HEIGHT);
            g.fillOval(x, y, 1, 1);
        }
        
        g.dispose();
        return image;
    }
    
    /**
     * 生成随机颜色
     */
    private static Color getRandomColor(int min, int max) {
        return new Color(min + random.nextInt(max - min), 
                         min + random.nextInt(max - min), 
                         min + random.nextInt(max - min));
    }
    
    /**
     * 验证码比较（忽略大小写）
     */
    public static boolean validateCaptcha(String inputCode, String storedCode) {
        if (inputCode == null || storedCode == null) {
            return false;
        }
        return inputCode.equalsIgnoreCase(storedCode);
    }
}