package vpplugins.packagespecgenerator.main;

import com.vp.plugin.VPPlugin;
import com.vp.plugin.VPPluginInfo;
import org.apache.log4j.Logger;

public class Main implements VPPlugin {
   static final Logger log = Logger.getLogger(Main.class);

   public void loaded(VPPluginInfo vpPluginInfo) {
      log.info("[VpPackageSpecCodeCreator] plugin loaded");
   }

   public void unloaded() {
      log.info("[VpPackageSpecCodeCreator] plugin unloaded");
   }
}
