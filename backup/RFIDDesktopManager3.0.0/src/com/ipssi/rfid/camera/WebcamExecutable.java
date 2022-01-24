package com.ipssi.rfid.camera;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import java.awt.event.KeyEvent;

public class WebcamExecutable {

    public static BufferedImage getImage() {
        BufferedImage retval = null;
        Webcam webcam = null;
        try {
            List<Webcam> webCams = Webcam.getWebcams();
            if (webCams != null) {
                for (int i = 0; i < webCams.size(); i++) {
                    webcam = webCams.get(i);
                    String name = webcam.getName();
                    if (!name.equalsIgnoreCase("Integrated")) {
                        break;
                    }
                }
            }
            //webcam = Webcam.getDefault();
            if (webcam != null) {
                webcam.setViewSize(webcam.getViewSizes()[0]);// for size of Image Dialogue
                CamDialouge we = (new WebcamExecutable()).new CamDialouge(webcam);
                
                retval = we.getImage();
                if (we != null) {
                    we.dispose();
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (webcam != null) {
                    webcam.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return retval;
    }

    @SuppressWarnings("serial")
    class CamDialouge extends JDialog {

        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - 200) / 2;
        final int y = (screenSize.height - 200) / 2;
        private AtomicBoolean initialized = new AtomicBoolean(false);
        private WebcamPanel panel = null;
        private JPanel jp = null;
        private JButton button1 = null;
        private JButton button2 = null;
        BufferedImage image = null;
        JDialog jd = null;
        boolean isPaused = false;
        boolean running = false;
        private Webcam mWebcam = null;

        public CamDialouge(Webcam webcam) {
            this.mWebcam = webcam;
        }

        public BufferedImage getImage() throws IOException {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                jd = new JDialog(this, true);
                jd.setTitle("Take Photograph");
                //GridLayout gl = new GridLayout(2,1);
                //jd.setLayout(gl);//(FlowLayout.CENTER));
                jd.setLocation(x, y);
                jd.setResizable(false);

                panel = new WebcamPanel(mWebcam, false);
                panel.setPreferredSize(mWebcam.getViewSize());
                panel.setOpaque(true);
                panel.setBackground(Color.BLACK);

                jp = new JPanel(new FlowLayout(FlowLayout.CENTER));
                button1 = new JButton();
                button1.setText("Capture");
                button1.setFocusable(true); // How do I get focus on button on App launch?
                button1.requestFocus(true); 
                button1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {

                        isPaused = !isPaused;
                        button2.setVisible(isPaused);
                        if (isPaused) {
                            panel.pause();
                            button1.setText("Re Capture");
                        } else {
                            panel.resume();
                            button1.setText("Capture");
                        }
                    }

                    public void keyPressed(java.awt.event.KeyEvent evt) {
                        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                            isPaused = !isPaused;
                            button2.setVisible(isPaused);
                            if (isPaused) {
                                panel.pause();
                                button1.setText("Re Capture");
                            } else {
                                panel.resume();
                                button1.setText("Capture");
                            }
                        }
                    }
                });

                button1.setFocusable(false);
                button2 = new JButton();
                button2.setText("Save");
                button2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        image = mWebcam.getImage();
                        if (image != null) {
                            if (panel != null) {
                                panel.stop();
                            }
                            dispose();
                        }
                    }
                 public void keyPressed(java.awt.event.KeyEvent evt) {
                        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                            image = mWebcam.getImage();
                        if (image != null) {
                            if (panel != null) {
                                panel.stop();
                            }
                            dispose();
                         }
                        }
                    }
                    
                });
                button2.setFocusable(false);
                button2.setVisible(false);
                //button1.setPreferredSize(webcam.getViewSize());
                jd.add(panel);
                jp.add(button1);
                jp.add(button2);
                jd.add(jp, BorderLayout.SOUTH);
                jd.pack();
                if (initialized.compareAndSet(false, true)) {
                    panel.start();
                }
                jd.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        if (panel != null) {
                            panel.stop();
                        }
                        e.getWindow().dispose();
                    }
                });
                jd.show(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return image;
        }
    }
}