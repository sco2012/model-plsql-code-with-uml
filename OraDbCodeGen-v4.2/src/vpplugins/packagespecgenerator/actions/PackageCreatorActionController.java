package vpplugins.packagespecgenerator.actions;

import vpplugins.packagespecgenerator.adapters.CachingVpOracleDbPackageAdapter;
import vpplugins.packagespecgenerator.adapters.VpOracleDbPackageAdapter;
import vpplugins.packagespecgenerator.codegenerators.PackageBodyGenerator;
import vpplugins.packagespecgenerator.codegenerators.PackageSpecGenerator;
import vpplugins.packagespecgenerator.ui.builders.OutputWindowBuilder;
import com.vp.plugin.ApplicationManager;
import com.vp.plugin.ViewManager;
import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPContext;
import com.vp.plugin.action.VPContextActionController;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

public class PackageCreatorActionController implements VPContextActionController {
   static final Logger log = Logger.getLogger(PackageCreatorActionController.class);
   public static ViewManager viewManager;
   private static Component parentFrame;
   private JFrame errorMessage = new JFrame();
   private OutputWindowBuilder outputWindowBuilder = new OutputWindowBuilder();

   public void performAction(VPAction vpAction, VPContext vpContext, ActionEvent actionEvent) {
      try {
         log.error("VpPackageSpecCodeCreator invoked");
         ApplicationManager.instance().reloadPluginClasses("VpPackageSpecCodeCreator");
         viewManager = ApplicationManager.instance().getViewManager();
         parentFrame = viewManager.getRootFrame();
         VpOracleDbPackageAdapter adapter = new CachingVpOracleDbPackageAdapter(vpContext);
         this.outputWindowBuilder.setPackageSpecification((new PackageSpecGenerator()).getCode(adapter)).setPackageBody((new PackageBodyGenerator()).getCode(adapter));
         viewManager.showMessageDialog(parentFrame, this.outputWindowBuilder.builder(), "Generated PL/SQL Code", -1);
      } catch (Exception err) {
         JOptionPane.showMessageDialog(this.errorMessage, "Error when building generator.");
         viewManager.showMessage("Please check the output window builder and associated classes \n" + err.toString());
         err.printStackTrace();
      }

   }

   public void update(VPAction vpAction, VPContext vpContext) {
      ApplicationManager.instance().reloadPluginClasses("VpPackageSpecCodeCreator2");
   }
}
