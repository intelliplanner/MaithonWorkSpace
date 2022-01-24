/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.voiceIntegration;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

/**
 * 
 * @author Vi$ky
 */
public class Voice {

	private String filename;

	// static String voicePath = "D:\\testvoice.wav";
	// static InputStream in = null;
	// static AudioStream audios = null;

	public static void playQuestion(String fileName, AudioStream audios) {

		try {
			// in = new FileInputStream(new File(voicePath));
			// audios = new AudioStream(in);
			AudioPlayer.player.start(audios);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stopPlayer(AudioStream audios) {
		AudioPlayer.player.stop(audios);
	}
}
