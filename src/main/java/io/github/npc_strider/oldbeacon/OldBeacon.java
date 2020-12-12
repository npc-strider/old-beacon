package io.github.npc_strider.oldbeacon;

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

		//Config file stuff.
		Properties p = new Properties();
		Path file = Paths.get("config/oldbeacon.cfg");
		InputStream is = null;

		IDLE_ANIM = true; //Defaults

		try {
			is = new FileInputStream(file.toString());
			try {
				p.load(is);

				IDLE_ANIM = p.getProperty("idle_anim").toLowerCase().trim().equals("false") ? false : true;
				
			} catch (IOException ex) {
				System.out.println("Old beacon: cannot read config!");
				ex.printStackTrace();
			}
		} catch (FileNotFoundException ex) {
			List<String> lines = Arrays.asList(
				"# default: true. Beacon has a blue star when it is not emitting a beam. Setting this to false results in the star being yellow regardless of whether it's active",
				"idle_anim=true"
			);
			try {
				Files.write(file, lines, StandardCharsets.UTF_8);
			} catch (Exception ex_) {
				System.out.println("Old beacon: could not write new config file! Do you have sufficient permissions to write the file?");
				ex_.printStackTrace();
			}
		}
		//
		
	};

}