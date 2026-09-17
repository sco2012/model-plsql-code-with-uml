package vpplugins.packagespecgenerator.util;

import com.vp.plugin.model.IModelElement;
import com.vp.plugin.model.ITaggedValue;
import java.util.Iterator;
import org.apache.log4j.Logger;

public class VpUtils {
   static final Logger log = Logger.getLogger(CodeFormattingUtils.class);

   public static String getFullModelPath(IModelElement model) {
      IModelElement currentModel = model;

      String concatName;
      for(concatName = model.getName(); currentModel.getParent() != null; currentModel = currentModel.getParent()) {
         String aliasName = "";
         boolean aliasFound = false;

         try {
            if (currentModel.getTaggedValues() != null) {
               @SuppressWarnings("rawtypes")
               Iterator taggedValues = currentModel.getTaggedValues().taggedValueIterator();

               while(taggedValues.hasNext()) {
                  ITaggedValue currentTag = (ITaggedValue)taggedValues.next();
                  if (currentTag.getName().toLowerCase().equals("alias") && currentTag.getValueAsString() != null) {
                     aliasName = currentTag.getValueAsString();
                     concatName = aliasName + "." + concatName;
                     aliasFound = true;
                     break;
                  }
               }

               if (!aliasFound) {
                  String removedSpacesName = currentModel.getName().replace(" ", "");
                  concatName = removedSpacesName + "." + concatName;
               }
            } else {
               String removeSpaces = currentModel.getName().replace(" ", "");
               concatName = removeSpaces + "." + concatName;
            }
         } catch (NullPointerException err) {
         }
      }

      return concatName;
   }
}
