
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.List;
import java.util.Random;

/*
 * 作成日: 2005/05/05
 * Copyright (c) 2005 Watanabe. All rights reserved
 */

/**
 * Nats君です。
 * 
 * @author Watanabe
 */
public class Nats implements Serializable{
    
    private static final long serialVersionUID = 1L;

    /** Nats君がとりうる向きの数 */
    private static final int    DIRECTION_NUM = 32;

    /** 生成されたNats君の数 */
    private static int            num;

    /** 乱数発生オブジェクト */
    private static final Random random          = new Random();

    /** 体の色 */
    private Color                color;
    
    /** 体の半径 */
    private int                 radius;

    /** 動きの速さ */
    private double                speed;

    /** 体の向き(弧度法) */
    private double                direction;

    /** 位置のx座標 */
    private double                x;

    /** 位置のy座標 */
    private double                y;

    /**
     * コンストラクタ。 <br>
     * 
     * @param color 体の色
     * @param radius 体の半径
     * @param speed 動きの速さ
     * @param x 位置のx座標
     * @param y 位置のy座標
     */
    public Nats(Color color, int radius, double speed, double x, double y) {
        num++;

        this.color = color;
        this.radius = radius;
        this.speed = speed;
        this.direction = 2 * Math.PI * random.nextInt(DIRECTION_NUM) / DIRECTION_NUM;
        this.x = x;
        this.y = y;
    }

    /**
     * 位置のx座標を戻します。
     * 
     * @return 位置のx座標
     */
    public int getX() {
        return (int) x;
    }

    /**
     * 位置のy座標を戻します。
     * 
     * @return 位置のy座標
     */
    public int getY() {
        return (int) y;
    }

    /**
     * 体の直径を戻します。
     * 
     * @return
     */
    public int getDiameter() {
        return 2 * radius;
    }

    /**
     * 歩きます。 <br>
     * 
     * @param dim 画面のサイズ
     */
    public void walk(Dimension dim, List<Nats> natsList) {
        while (direction < 0.0) {
            direction = direction + 2 * Math.PI;
        }
        
        double preX = x;
        double preY = y;
        
        x = x + speed * Math.cos(direction);
        y = y + speed * Math.sin(direction);

        int width = (int) dim.getWidth();
        int height = (int) dim.getHeight();

        if (x < 0 - radius) {
            x = width + radius;
        }
        else if (width + radius < x) {
            x = 0 - radius;
        }

        if (y < 0 - radius) {
            y = height + radius;
        }
        else if (height + radius < y) {
            y = 0 - radius;
        }

        Nats other = hit(dim, natsList);
        if (other != null) {
            x = preX;
            y = preY;
            turnRight();
            if (random.nextInt(1000000) == 0) {
                // 色の突然変異
                Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                this.color = color;
            }
            // 色情報を交換
            Color newColor = mixedColor(other.color);
            this.color = newColor;
            other.color = newColor;
        }
    }

    Color mixedColor(Color color) {
        return new Color((this.color.getRGB() +  color.getRGB()) / 2);
    }

    public Nats hit(Dimension dim, List<Nats> natsList) {
        for (Nats each : natsList) {
            if (this.equals(each)) {
                continue;
            }
            
            int diffX = this.getX() - each.getX();
            int diffY = this.getY() - each.getY();
            if (Math.sqrt(diffX*diffX + diffY*diffY) < this.radius + each.radius) {
                return each;
            }
        }
        
        return null;
    }

    /**
     * 左を向きます。 <br>
     */
    public void turnLeft(int times) {
        direction = direction - 2 * Math.PI * times / DIRECTION_NUM;
    }

    /**
     * 右を向きます。 <br>
     */
    public void turnRight(int times) {
        direction = direction + 2 * Math.PI * times / DIRECTION_NUM;
    }
    

    /**
     * 左を向きます。 <br>
     */
    public void turnLeft() {
        turnLeft(1);
    }

    /**
     * 右を向きます。 <br>
     */
    public void turnRight() {
        turnRight(1);
    }

    /**
     * Nats君を描きます。 <br>
     * 
     * @param g Graphicsコンテキスト
     */
    public void paint(Graphics g, Dimension dim) {          
        int noseRadius = radius / 5; //鼻の半径
        int whiteEyeRadius = radius / 3; //白目の半径
        int blackEyeRadius = radius / 5; //黒目の半径
        double eyeAngle = 45 * Math.PI / 180; //目の向きの角度

        //体
        g.setColor(color);
        g.fillOval((int) x - radius, (int) y - radius, getDiameter(), getDiameter());

        g.setColor(color.darker());
        g.drawOval((int) x - radius, (int) y - radius, getDiameter(), getDiameter());

        //鼻
        g.setColor(color);
        g.fillOval((int) (x + Math.cos(direction) * radius) - noseRadius, (int) (y + Math.sin(direction) * radius)
            - noseRadius, 2 * noseRadius, 2 * noseRadius);

        //目
        drawEye(g, whiteEyeRadius, blackEyeRadius, eyeAngle);
    }

    /**
     * 目を描きます。 <br>
     * 
     * @param g Graphicsコンテキスト
     * @param radiusW 白目の半径
     * @param radiusB 黒目の半径
     * @param angle 目の向きの角度
     */
    private void drawEye(Graphics g, final int radiusW, int radiusB, double angle) {
        double rAngle = direction + angle; //右目の向きの角度
        double lAngle = direction - angle; //左目の向きの角度
        int rx = (int) (x + Math.cos(rAngle) * radius); //右目の中心のx座標
        int ry = (int) (y + Math.sin(rAngle) * radius); //右目の中心のy座標
        int lx = (int) (x + Math.cos(lAngle) * radius); //左目の中心のx座標
        int ly = (int) (y + Math.sin(lAngle) * radius); //左目の中心のy座標

        //白目
        g.setColor(Color.WHITE);
        g.fillOval(rx - radiusW, ry - radiusW, 2 * radiusW, 2 * radiusW);
        g.fillOval(lx - radiusW, ly - radiusW, 2 * radiusW, 2 * radiusW);
        g.setColor(Color.BLACK);
        g.drawOval(rx - radiusW, ry - radiusW, 2 * radiusW, 2 * radiusW);
        g.drawOval(lx - radiusW, ly - radiusW, 2 * radiusW, 2 * radiusW);

        //黒目
        int seed = radiusB / 2;
        g.fillOval(rx - radiusB + random.nextInt(seed), ry - radiusB + random.nextInt(seed), 2 * radiusB, 2 * radiusB);
        g.fillOval(lx - radiusB + random.nextInt(seed), ly - radiusB + random.nextInt(seed), 2 * radiusB, 2 * radiusB);
    }
}// end Nats class
