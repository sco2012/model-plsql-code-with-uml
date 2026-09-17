package vpplugins.packagespecgenerator.adapters;

import vpplugins.packagespecgenerator.codegenerators.PackageSpecGenerator;
import vpplugins.packagespecgenerator.model.enums.AccessModifier;
import vpplugins.packagespecgenerator.model.enums.ModelStereotypes;
import vpplugins.packagespecgenerator.model.objects.OracleDbFunction;
import vpplugins.packagespecgenerator.model.objects.OracleDbPackage;
import vpplugins.packagespecgenerator.model.objects.OracleDbProcedure;
import vpplugins.packagespecgenerator.model.objects.OracleDbRefCursor;
import vpplugins.packagespecgenerator.model.objects.OracleDbSubProgram;
import vpplugins.packagespecgenerator.model.objects.OracleDbSubProgramParameter;
import vpplugins.packagespecgenerator.model.objects.OracleDbVariable;
import vpplugins.packagespecgenerator.util.VpUtils;
import com.vp.plugin.action.VPContext;
import com.vp.plugin.diagram.IShapeUIModel;
import com.vp.plugin.model.IAttribute;
import com.vp.plugin.model.IModelElement;
import com.vp.plugin.model.IOperation;
import com.vp.plugin.model.IParameter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

public class VpOracleDbPackageAdapter implements OracleDbPackageAdapter {
   private VPContext vpContext;
   private String schemaName = "";
   private String specialStereoTypeName = "";
   
   static final Logger log = Logger.getLogger(PackageSpecGenerator.class);


   public VpOracleDbPackageAdapter(VPContext vpContext) {
      this.vpContext = vpContext;
   }

   public OracleDbPackage getOracleDbPackage() {
      IModelElement vClassModel = this.vpContext.getModelElement(); 
      if (this.vpContext.getDiagramElement() instanceof IShapeUIModel) {
         IShapeUIModel ClassShape = (IShapeUIModel)this.vpContext.getDiagramElement();
         if (ClassShape.getParent() != null) {
            this.schemaName = ClassShape.getParent().getModelElement().getName();
         }
      }

      
      @SuppressWarnings("rawtypes")
      Iterator stereotypeElements = vClassModel.stereotypeModelIterator();

      while(stereotypeElements.hasNext()) {
         IModelElement stereotypeElement = (IModelElement)stereotypeElements.next();

         // To qualify as a DBPackage, the stereotype need only contain the string "pack", so OracleDBPackage, OracleDBPackage, DBPack or
         // any other similarly named stereotype name is good enough to differentiate DbPackages
         if (stereotypeElement.getName().toLowerCase().contains("pack")) {
            this.specialStereoTypeName=ModelStereotypes.ORACLE_DB_PACKAGE.getStereotype();
         }
         // Similarly to the above, RefCursor classes can be identified with flexibility in the name of the stereotype, containing
         // either "ref" or "rec", for example "RefC", "OracleDBRec", "DBRec", "RecType" or anything similar
         if (stereotypeElement.getName().toLowerCase().contains("ref") || stereotypeElement.getName().toLowerCase().contains("rec") ) {
            this.specialStereoTypeName=ModelStereotypes.REF_CURSOR.getStereotype();
         }

      }
      
      @SuppressWarnings({ "unchecked", "rawtypes" })
      LinkedList<IAttribute> packageVariables = new LinkedList();
      @SuppressWarnings({ "unchecked", "rawtypes" })
      LinkedList<IOperation> packageSubPrograms = new LinkedList();
      @SuppressWarnings("rawtypes")
      Iterator secondClassElements = vClassModel.childIterator();

      while(secondClassElements.hasNext()) {
         Object currentElement = secondClassElements.next();
         if (currentElement instanceof IAttribute) {
            packageVariables.add((IAttribute)currentElement);
         }

         if (currentElement instanceof IOperation) {
            packageSubPrograms.add((IOperation)currentElement);
         }
      }

      OracleDbPackage result = new OracleDbPackage();
      result.setOwningSchema(this.schemaName);
      result.setName(vClassModel.getName());
      result.setSpecialStereoTypeName(this.specialStereoTypeName);
      result.setDescription(vClassModel.getDescription());
      result.addPackageVariables(this.parsePackageVariables(packageVariables));
      result.addPackageSubPrograms(this.parsePackageSubPrograms(packageSubPrograms, result));
      return result;
   }

   private Map<OracleDbVariable, AccessModifier> parsePackageVariables(LinkedList<IAttribute> packageVariables) {
      @SuppressWarnings({ "unchecked", "rawtypes" })
      Map<OracleDbVariable, AccessModifier> result = new HashMap();
      @SuppressWarnings("rawtypes")
      Iterator variables = packageVariables.iterator();

      while(variables.hasNext()) {
         IAttribute packageVariable = (IAttribute)variables.next();
         OracleDbVariable currentVariable = new OracleDbVariable();
         currentVariable.setName(packageVariable.getName());
         currentVariable.setDataType(packageVariable.getTypeAsText());
         currentVariable.setDefaultValue(packageVariable.getInitialValueAsString());
         currentVariable.setDescription(packageVariable.getDescription());
         result.put(currentVariable, packageVariable.getVisibility().equals(AccessModifier.PUBLIC.getAccessModifier()) ? AccessModifier.PUBLIC : AccessModifier.PRIVATE);
      }

      return result;
   }

   private LinkedList<OracleDbSubProgram> parsePackageSubPrograms(LinkedList<IOperation> packageSubPrograms, OracleDbPackage oracleDbPackage) {
      @SuppressWarnings({ "unchecked", "rawtypes" })
      LinkedList<OracleDbSubProgram> result = new LinkedList();
      @SuppressWarnings("rawtypes")
      Iterator subPrograms = packageSubPrograms.iterator();

      while(true) {
         while(subPrograms.hasNext()) {
            IOperation currentOperation = (IOperation)subPrograms.next();
            if (currentOperation.getReturnType() != null && !currentOperation.getReturnTypeAsText().equals("void")) {
               OracleDbFunction function = this.parseFunctionFromIOperation(currentOperation);
               function.setOwningPackage("\"" + oracleDbPackage.getName() + "\"");
               function.setOwningSchema(oracleDbPackage.getOwningSchema());
               function.setElementLocation(VpUtils.getFullModelPath(currentOperation));
               function.setReturnTypeModifier(currentOperation.getTypeModifier());
               result.add(function);
            } else {
               OracleDbProcedure procedure = this.parseProcedureFromIOperation(currentOperation);
               procedure.setOwningPackage("\"" + oracleDbPackage.getName() + "\"");
               procedure.setOwningSchema(oracleDbPackage.getOwningSchema());
               procedure.setElementLocation(VpUtils.getFullModelPath(currentOperation));
               result.add(procedure);
            }
         }

         return result;
      }
   }

   private OracleDbFunction parseFunctionFromIOperation(IOperation operation) {
      OracleDbFunction result = new OracleDbFunction();
      boolean hasRefCClass = false;

      result.setAccessModifier(operation.getVisibility().equals(AccessModifier.PUBLIC.getAccessModifier()) ? AccessModifier.PUBLIC : AccessModifier.PRIVATE);
      result.setName(operation.getName());
      result.setDescription(operation.getDescription());
      result.addParameters(this.parseSubProgramParameters(operation.toParameterArray()));
      result.setDeprecated(operation.hasStereotype(ModelStereotypes.DEPRECATED.getStereotype()));
      if (operation.getName().indexOf("Get") > -1 && operation.getName().indexOf("Get") < 4) {
         result.setSetterName(operation.getName().replaceFirst("Get", "Set"));
      } else {
         result.setSetterName("Set" + operation.getName());
      }

      IModelElement returnTypeElement = operation.getReturnTypeAsElement();

      // RefCursor classes can be identified with flexibility in the name of the stereotype, containing
      // either "ref" or "rec", for example "RefC", "OracleDBRec", "DBRec", "RecType" or anything similar


      if (returnTypeElement != null) {
         @SuppressWarnings("rawtypes")
         Iterator stereotypeElements = returnTypeElement.stereotypeModelIterator();
         while(stereotypeElements.hasNext()) {
            IModelElement stereotypeElement = (IModelElement)stereotypeElements.next();

            if (stereotypeElement.getName().toLowerCase().contains("ref") || stereotypeElement.getName().toLowerCase().contains("rec") ) {
               hasRefCClass=true;
            }
         }
      }

      // if (returnTypeElement != null && returnTypeElement.hasStereotype(ModelStereotypes.REF_CURSOR.getStereotype())) {
      //    hasRefCClass=true; 
      // }


      // if (returnTypeElement != null && returnTypeElement.hasStereotype(ModelStereotypes.REF_CURSOR.getStereotype())) {
      if (returnTypeElement != null && hasRefCClass) {
         result.setReturnRefCursor(this.parseRefCursorFromIModelElement(returnTypeElement, operation.getTypeModifier()));
      } else {
         result.setReturnDataType(operation.getReturnTypeAsString());
      }

      return result;
   }

   private OracleDbProcedure parseProcedureFromIOperation(IOperation operation) {
      OracleDbProcedure result = new OracleDbProcedure();
      result.setAccessModifier(operation.getVisibility().equals(AccessModifier.PUBLIC.getAccessModifier()) ? AccessModifier.PUBLIC : AccessModifier.PRIVATE);
      result.setName(operation.getName());
      result.setDescription(operation.getDescription());
      result.addParameters(this.parseSubProgramParameters(operation.toParameterArray()));
      result.setDeprecated(operation.hasStereotype(ModelStereotypes.DEPRECATED.getStereotype()));
      return result;
   }

   private OracleDbRefCursor parseRefCursorFromIModelElement(IModelElement refCursorElement, String functionTypeModifier) {
      OracleDbRefCursor result = new OracleDbRefCursor();
      result.setName(refCursorElement.getName());
      result.setDescription(refCursorElement.getDescription());
      @SuppressWarnings("rawtypes")
      Iterator it = refCursorElement.childIterator();
      result.setReturnsMultipleRows(functionTypeModifier != null && functionTypeModifier.equals("[]"));

      while(it.hasNext()) {
         OracleDbRefCursor.OracleDbRefCursorColumn currentColumn = new OracleDbRefCursor.OracleDbRefCursorColumn();
         IAttribute FunctionColumn = (IAttribute)it.next();
         currentColumn.setName(FunctionColumn.getName().toUpperCase());
         currentColumn.setDataType(FunctionColumn.getTypeAsText());
         currentColumn.setDescription(FunctionColumn.getDescription());
         result.addColumn(currentColumn);
      }

      return result;
   }

   private LinkedList<OracleDbSubProgramParameter> parseSubProgramParameters(IParameter[] parameters) {
      @SuppressWarnings({ "unchecked", "rawtypes" })
      LinkedList<OracleDbSubProgramParameter> result = new LinkedList();
      IParameter[] params = parameters;
      int paramCount = parameters.length;

      for(int i = 0; i < paramCount; ++i) {
         IParameter parameter = params[i];
         OracleDbSubProgramParameter currentParameter = new OracleDbSubProgramParameter();
         currentParameter.setName(parameter.getName());
         currentParameter.setDataType(parameter.getTypeAsText());
         currentParameter.setDefaultValue(parameter.getDefaultValueAsString());
         currentParameter.setDescription(parameter.getDescription());
         String dir = parameter.getDirection();
         byte dirByte = -1;
         switch(dir.hashCode()) {
         case 100357129:
            if (dir.equals("inout")) {
               dirByte = 0;
            }
         }

         switch(dirByte) {
         case 0:
            currentParameter.setDirection("in out");
            break;
         default:
            currentParameter.setDirection(parameter.getDirection());
         }

         if (parameter.getTypeModifier().toLowerCase().equals("nocopy")) {
            currentParameter.setNoCopy(true);
         }

         result.add(currentParameter);
      }

      return result;
   }
}
