package vpplugins.packagespecgenerator.adapters;

import vpplugins.packagespecgenerator.model.objects.OracleDbPackage;
import com.vp.plugin.action.VPContext;

public class CachingVpOracleDbPackageAdapter extends VpOracleDbPackageAdapter {
   OracleDbPackage oracleDbPackage;

   public CachingVpOracleDbPackageAdapter(VPContext vpContext) {
      super(vpContext);
   }

   public OracleDbPackage getOracleDbPackage() {
      return this.oracleDbPackage == null ? (this.oracleDbPackage = super.getOracleDbPackage()) : this.oracleDbPackage;
   }
}
