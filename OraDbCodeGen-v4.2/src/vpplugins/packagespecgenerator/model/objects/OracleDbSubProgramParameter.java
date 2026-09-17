package vpplugins.packagespecgenerator.model.objects;

public class OracleDbSubProgramParameter extends OracleDbVariable {
   private String direction;
   private boolean isNoCopy;

   public String getDirection() {
      return this.direction != null && (this.direction == null || !this.direction.equals("in")) ? this.direction : "";
   }

   public void setDirection(String direction) {
      this.direction = direction;
   }

   public boolean isNoCopy() {
      return this.isNoCopy;
   }

   public void setNoCopy(boolean noCopy) {
      this.isNoCopy = noCopy;
   }
}
