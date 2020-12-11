package io.github.npc_strider.oldbeacon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.fabricmc.api.ModInitializer;

public class OldBeacon implements ModInitializer {
	public static final String MOD_ID = "oldbeacon";
	
	public static Boolean IDLE_ANIM;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		// System.out.println("Load"+MOD_ID);

		//Config file stuff.
		Properties p = new Properties();
		Path file = Paths.get("config/oldbeacon.cfg");
		InputStream is = null;
		try {
			is = new FileInputStream(file.toString());
			try {
				p.load(is);
			} catch (IOException ex) {
				System.out.println("Old beacon: cannot read config!");
				ex.printStackTrace();
			}
		} catch (FileNotFoundException ex) {
			List<String> lines = Arrays.asList(
				"# default: true. Beacon has a blue star when it is not emitting a beam.",
				"idle_anim=true"
			);
			try {
				Files.write(file, lines, StandardCharsets.UTF_8);
			} catch (Exception ex_) {
				System.out.println("Old beacon: could not write new config file! Do you have sufficient permissions to write the file?");
				ex_.printStackTrace();
			}
		}
		IDLE_ANIM = p.getProperty("idle_anim").toLowerCase().trim().equals("true") ? true : false;
	};

}