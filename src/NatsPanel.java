
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/*
 * 作成日: 2005/05/05
 * Copyright (c) 2005 Watanabe. All rights reserved
 */

/**
 * 
 * 
 * @author Watanabe
 */
public class NatsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Nats君を格納するList */
    private List<Nats> natsList;

    /** Nats君が動き回るパネル */
    private Display display;

    /** Nats君を操作するパネル */
    private ControlPanel controlPanel;

    /** Ntas君を動かすスレッド */
    private volatile Thread thread;

    /** 乱数発生オブジェクト */
    private final Random random = new Random();

    /**
     * Nats君と遊ぶパネルです。 <br>
     */
    public NatsPanel() {
        // Nats君をリストに追加
        natsList = new ArrayList<Nats>();
        display = new Display();
        controlPanel = new ControlPanel();

        setLayout(new BorderLayout());
        add(display, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * threadを開始します。 <br>
     */
    public void start() {
        if (thread == null) {
            thread = new NatsThread();
            thread.start();
            System.out.println("threadを開始しました。");
        }
    }

    /**
     * threadを停止します。 <br>
     * 
     */
    public void stop() {
        thread = null;
        System.out.println("threadを停止しました。");
    }

    /**
     * 画面を消去します。 <br>
     */
    public void delete() {
        if (thread != null) {
            thread = null;
        }
        natsList.clear();
        display.repaint();
        System.out.println("消去しました。");
    }

    /**
     * natsListをファイルに保存します。 <br>
     */
    public void NatsOutput() {

        FileOutputStream stre = null;
        ObjectOutputStream p = null;

        try {
            stre = new FileOutputStream("nats.tmp");
            p = new ObjectOutputStream(stre);
            System.out.println("保存しました。");
            p.reset();
            p.writeObject(natsList); // NatsクラスでSerializableインタフェースを実装しなければならない。
            p.flush();

        } catch (IOException e) {
            throw new RuntimeException("失敗しました。", e);
        } finally {
            try {
                if (p != null)
                    p.close();
                if (stre != null)
                    stre.close();
            } catch (IOException e) {
                throw new RuntimeException("失敗しました。", e);
            }
        }
    }

    /**
     * natsListをファイルから読み込みます。 <br>
     */
    @SuppressWarnings("unchecked")
    public void NatsInput() {

        FileInputStream stre = null; // 宣言はtryの外で。
        ObjectInputStream p = null;

        try {

            System.out.println("読み込みました。");
            natsList.clear();
            stre = new FileInputStream("nats.tmp");
            p = new ObjectInputStream(stre);
            natsList = (List<Nats>) p.readObject();

        } catch (ObjectStreamException e) {
            throw new RuntimeException("オブジェクトストリームクラスの例外です。", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("クラスが見つかりませんでした。", e);
        } catch (IOException e) {
            throw new RuntimeException("失敗しました。", e);
        } finally {
            try {
                if (p != null)
                    p.close(); // 必ずcloseしたいので、finally句に
                if (stre != null)
                    stre.close();
            } catch (IOException e) {
                throw new RuntimeException("失敗しました。", e);
            }
        }
    }

    /**
     * Ntas君を動かすスレッドです。 <br>
     */
    public class NatsThread extends Thread {

        /*
         * (非 Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        @SuppressWarnings("deprecation")
        public void run() {
            Thread thisThread = Thread.currentThread();

            while (thread == thisThread) {
                // Displayを再描画
                display.repaint();

                // Nats君を歩かせる
                synchronized (natsList) {
                    Iterator<Nats> iter = natsList.iterator();
                    while (iter.hasNext()) {
                        Nats nats = (Nats) iter.next();
                        nats.walk(display.size(), natsList);
                    }
                }

                // インターバルをおく
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    } // end class NatsThread

    /**
     * Nats君が動き回るパネルです。 <br>
     */
    public class Display extends JPanel {

        private static final long serialVersionUID = 1L;

        /** ダブルバッファリング用のImageオブジェクト */
        private Image offimage;

        /**
         * コンストラクタです。 <br>
         */
        public Display() {
            addMouseListener(new AddNatsListener());
        }

        /**
         * ダブルバッファリング用のImageオブジェクトを生成します。 <br>
         */
        private void createOffImage() {
            Dimension d = getSize();
            offimage = createImage(d.width, d.height);

        }

        /*
         * (非 Javadoc)
         * 
         * @see java.awt.Component#print(java.awt.Graphics)
         */
        public void update(Graphics g) {
            paint(g);
        }

        /*
         * (非 Javadoc)
         * 
         * @see java.awt.Component#update(java.awt.Graphics)
         */
        public void paint(Graphics g) {
            Dimension d = getSize();
            int width = d.width;
            int height = d.height;
            if (offimage == null || width != offimage.getWidth(this) || height != offimage.getHeight(this)) {
                createOffImage();
            }

            Graphics offGraphics = offimage.getGraphics(); // オフスクリーンイメージに描画するためのグラフィックスコンテキストを作成します。
            offGraphics.clearRect(0, 0, width, height); // バックグラウンドカラーでクリアしている

            // final Comparator<Nats> comparator = new NatsComparator();
            synchronized (natsList) {
                // Collections.sort(natsList, comparator);

                Iterator<Nats> iter = natsList.iterator();
                while (iter.hasNext()) {
                    Nats nats = (Nats) iter.next();
                    nats.paint(offGraphics, d);
                }
            }

            g.drawImage(offimage, 0, 0, this);
            offGraphics.dispose();
        }
    }// end Display class

    /**
     * Nats君を操作するパネルです。 <br>
     */
    public class ControlPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        /**
         * コンストラクタです。 <br>
         */
        public ControlPanel() {
            JButton addButton = createButton("追加", new AddListener(30));
            JButton startButton = createButton("開始", new StartListener());
            JButton stopButton = createButton("停止", new StopListener());
            JButton turnleftButton = createButton("左向け", new TurnLeftListener());
            JButton turnrightButton = createButton("右向け", new TurnRightListener());
            JButton deleteButton = createButton("消す", new DeleteListener());
            JButton outputButton = createButton("保存", new OutputListener());
            JButton inputButton = createButton("読み込む", new InputListener());
            addButton.setBackground(Color.RED);
            startButton.setBackground(Color.PINK);
            stopButton.setBackground(Color.YELLOW);
            turnleftButton.setBackground(Color.GREEN);
            turnrightButton.setBackground(Color.CYAN);
            deleteButton.setBackground(Color.WHITE);
            add(addButton);
            add(startButton);
            add(stopButton);
            add(turnleftButton);
            add(turnrightButton);
            add(deleteButton);
            add(outputButton);
            add(inputButton);
        }

        /**
         * ボタンを生成します。 <br>
         * 
         * @param label
         *            ラベル
         * @param listener
         *            ActionListener
         * @return 生成したボタン
         */
        private JButton createButton(String label, ActionListener listener) {
            JButton button = new JButton(label);
            button.addActionListener(listener);
            return button;
        }

    }// end ControlPanel class

    /**
     * Nats君を左向きに回転させます。 <br>
     */
    public class TurnLeftListener implements ActionListener {

        /*
         * (非 Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
         * ActionEvent)
         */
        public void actionPerformed(ActionEvent arg0) {
            synchronized (natsList) {
                Iterator<Nats> iter = natsList.iterator();
                while (iter.hasNext()) {
                    Nats nats = (Nats) iter.next();
                    nats.turnLeft();
                }
            }
        }
    } // end class MoveListener

    /**
     * Nats君を右向きに回転させます。 <br>
     */
    public class TurnRightListener implements ActionListener {

        public void actionPerformed(ActionEvent arg0) { // アクションが発生すると呼び出される
            synchronized (natsList) {
                Iterator<Nats> iter = natsList.iterator(); // 反復子
                while (iter.hasNext()) { // 要素がある間
                    Nats nats = (Nats) iter.next();
                    nats.turnRight();
                }
            }
        }
    }

    /**
     * Nats君を開始します。 <br>
     */
    public class StartListener implements ActionListener {
        public void actionPerformed(ActionEvent arg0) {
            start();
        }
    }

    /**
     * Nats君を停止します。 <br>
     */
    public class StopListener implements ActionListener {
        public void actionPerformed(ActionEvent arg0) {
            stop();

        }
    }

    /**
     * Nats君を追加します。 <br>
     */
    public class AddListener implements ActionListener {

        private int size;

        public AddListener(int size) {
            this.size = size;
        }

        /*
         * (非 Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
         * ActionEvent)
         */
        public void actionPerformed(ActionEvent arg0) {
            for (int i = 0; i < size; i++) {
                int x = random.nextInt(display.getWidth());
                int y = random.nextInt(display.getHeight());
                addNatsKun(x, y);
            }
        }
    }

    private void addNatsKun(int x, int y) {
        // Color({赤(0～255)}、{緑(0～255)}、{青(0～255)});
        Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        // Nats(Color, {半径(10以上)}, {速さ}, {x座標}, {y座標});
        int radius = random.nextInt(21);
        int speed = 5 + 2 * (int) (1 - (double) radius / 20);
        Nats nats = new Nats(color, 10 + radius, speed, x, y);

        if (nats.hit(getSize(), natsList) == null) {
            natsList.add(nats);
            display.repaint();
        }
    }

    /**
     * Nats君を消去します。 <br>
     */
    public class DeleteListener implements ActionListener {

        /*
         * (非 Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
         * ActionEvent)
         */
        public void actionPerformed(ActionEvent arg0) {
            delete();
        }
    }

    /**
     * Nats君をファイルに保存します。 <br>
     */
    public class OutputListener implements ActionListener {

        /*
         * (非 Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
         * ActionEvent)
         */
        public void actionPerformed(ActionEvent arg0) {
            NatsOutput();
        }

    }

    /**
     * Nats君をファイルから読み込みます。 <br>
     */
    public class InputListener implements ActionListener {

        /*
         * (非 Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
         * ActionEvent)
         */
        public void actionPerformed(ActionEvent arg0) {
            NatsInput();

        }

    }

    /**
     * Nats君を追加します。 <br>
     */
    public class AddNatsListener extends MouseAdapter {
        /*
         * (非 Javadoc)
         * 
         * @see
         * java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        public void mousePressed(MouseEvent event) {
            synchronized (natsList) {
                int x = event.getX();
                int y = event.getY();
                addNatsKun(x, y);
            }
        }
    } // end class AddNatsListener

    /**
     * Nats君をY座標の昇順で並びかえます。
     */
    private class NatsComparator implements Comparator<Nats> {
        @Override
        public int compare(Nats nats1, Nats nats2) {

            return nats1.getY() - nats2.getY();
        }
    }

    /**
     * mainです。 <br>
     */
    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        final NatsPanel natsPanel = new NatsPanel();

        JFrame frame = new JFrame();
        frame.setSize(600, 600);
        frame.getContentPane().add(natsPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ウインドウ操作時の処理
        frame.addWindowListener(new WindowListener() {
            public void windowActivated(WindowEvent event) {

            }

            public void windowClosed(WindowEvent event) {

            }

            public void windowClosing(WindowEvent event) {
                natsPanel.stop();
            }

            public void windowDeactivated(WindowEvent event) {

            }

            public void windowDeiconified(WindowEvent event) {

            }

            public void windowIconified(WindowEvent event) {

            }

            public void windowOpened(WindowEvent event) {
                natsPanel.start();
            }

        });

        // frame.addMouseListener(natsPanel.new AddNatsListener());
        frame.show();
    }
} // end class NatsPanel
